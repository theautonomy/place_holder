#!/usr/bin/env bash
# git-who.sh: who changed what in a git repository, and when.
#
#   git-who.sh [options] files            every file changed in the window: commits, authors, latest change
#   git-who.sh [options] authors          every author in the window: commits, files, lines added/removed
#   git-who.sh [options] last FILE...     who made the latest change to each FILE (and when, and why)
#
#   -C DIR        the repository (default: current directory)
#   -s SINCE      start of the window: 2026-10-01, "2026-10-01 14:00", yesterday, "2 weeks ago"
#   -u UNTIL      end of the window (same formats)
#   -p PATH       only these paths (repeatable; a directory means everything under it)
#   -a AUTHOR     only commits whose author matches (name or email, extended regex: "alice|bob")
#   -r REV        branch / tag / commit to look at (default: HEAD)
#   -n N          at most N rows
#   -o FORMAT     text (default), csv or md
#
# Merge commits are skipped (their changes are counted in the commits they merged).
# .mailmap is honored, so one person with two emails is one author.
# Exit status: 0 ok, 1 nothing found / not a repository, 2 usage.
set -euo pipefail

# Self-contained on purpose (no lib/common.sh), so this one file can be copied anywhere,
# e.g. into ~/bin, and just work. These are the few helpers it needs.
PROG="$(basename "$0")"
log() { printf '%s: %s\n' "$PROG" "$*" >&2; }
die() { printf '%s: error: %s\n' "$PROG" "$*" >&2; exit 1; }
need_cmd() {
  local cmd
  for cmd in "$@"; do
    command -v "$cmd" >/dev/null 2>&1 || die "required command not found: $cmd"
  done
}

usage() { sed -n '2,21s/^# \{0,1\}//p' "${BASH_SOURCE[0]}"; }
usage_error() {
  printf '%s: %s\nTry '\''%s -h'\''.\n' "$PROG" "$*" "$PROG" >&2
  exit 2
}

repo=. since="" until="" author="" rev=HEAD limit="" format=text
paths=()
while getopts ':C:s:u:p:a:r:n:o:h' opt; do
  case "$opt" in
    C) repo="$OPTARG" ;;
    s) since="$OPTARG" ;;
    u) until="$OPTARG" ;;
    p) paths+=("$OPTARG") ;; # repeatable: collect into an array (lesson 10)
    a) author="$OPTARG" ;;
    r) rev="$OPTARG" ;;
    n) limit="$OPTARG" ;;
    o) format="$OPTARG" ;;
    h) usage; exit 0 ;;
    :) usage_error "option -$OPTARG needs a value" ;;
    \?) usage_error "unknown option: -$OPTARG" ;;
  esac
done
shift $((OPTIND - 1))

[[ $# -ge 1 ]] || usage_error "expected a command: files, authors or last"
command="$1"
shift
case "$command" in
  files | authors) [[ $# -eq 0 ]] || usage_error "$command takes no arguments (use -p to pick paths)" ;;
  last) [[ $# -ge 1 ]] || usage_error "last needs at least one FILE" ;;
  *) usage_error "unknown command: $command" ;;
esac
case "$format" in text | csv | md) ;; *) usage_error "-o must be text, csv or md" ;; esac
[[ -z "$limit" || "$limit" =~ ^[1-9][0-9]*$ ]] || usage_error "-n must be a positive number"

# git silently IGNORES dates it can't parse ("--since=lastweek" means "no limit"),
# so validate them here: a too-wide report would look right and be wrong.
date_re='^([0-9]{4}-[0-9]{2}-[0-9]{2}([ T][0-9]{2}:[0-9]{2}(:[0-9]{2})?)?|yesterday|today|[0-9]+ (minute|hour|day|week|month|year)s? ago)$'
for d in "$since" "$until"; do
  [[ -z "$d" || "$d" =~ $date_re ]] || usage_error "unsupported date: '$d' (use 2026-10-01, \"2026-10-01 14:00\", yesterday or \"N days ago\")"
done

# Gotcha: git reads a date WITHOUT a time as "that day at the CURRENT time of day", so
# --since=2026-10-01 run at 14:30 skips the morning of the 1st. Pin date-only values to
# the start (since) and the end (until) of the day, which is what people mean.
[[ "$since" =~ ^[0-9]{4}-[0-9]{2}-[0-9]{2}$ ]] && since+=" 00:00:00"
[[ "$until" =~ ^[0-9]{4}-[0-9]{2}-[0-9]{2}$ ]] && until+=" 23:59:59"

need_cmd git
git -C "$repo" rev-parse --git-dir >/dev/null 2>&1 || die "not a git repository: $repo"
git -C "$repo" rev-parse -q --verify "$rev^{commit}" >/dev/null || die "unknown revision: $rev"

# g ARGS...  git in the repository. core.quotePath=false keeps non-ASCII file names readable
# instead of "\303\251"-escaped. (Names with tabs or newlines would still break the parsing.)
g() { git -C "$repo" -c core.quotePath=false "$@"; }

# Common git log options, built as an array so values with spaces stay intact.
log_args=(--no-merges --date='format:%Y-%m-%d %H:%M')
[[ -n "$since" ]] && log_args+=(--since="$since")
[[ -n "$until" ]] && log_args+=(--until="$until")
# --extended-regexp: "alice|bob" means alice OR bob (in git's default basic regex, | is a literal).
[[ -n "$author" ]] && log_args+=(--author="$author" --regexp-ignore-case --extended-regexp)

# --- output: every command produces TSV with a header row; render turns it into the format ---
render() {
  case "$format" in
    text) column -t -s $'\t' ;;
    csv)
      # RFC 4180: quote fields that contain a comma, quote or newline; double inner quotes
      awk -F '\t' -v OFS=, '{
        $1 = $1 # assigning a field rebuilds the line with OFS; without it, unchanged lines keep their tabs
        for (i = 1; i <= NF; i++) if ($i ~ /[",]/) { gsub(/"/, "\"\"", $i); $i = "\"" $i "\"" }
        print
      }'
      ;;
    md)
      awk -F '\t' '{
        gsub(/\|/, "\\|")                   # a | inside a cell would end it
        row = "|"; for (i = 1; i <= NF; i++) row = row " " $i " |"; print row
        if (NR == 1) { sep = "|"; for (i = 1; i <= NF; i++) sep = sep "---|"; print sep }
      }'
      ;;
  esac
}

# limit_rows  keeps the header + the first N rows (-n)
limit_rows() {
  if [[ -n "$limit" ]]; then head -n "$((limit + 1))"; else cat; fi
}

# history  one git log pass: a header line per commit (starts with \x1e, fields split by \x1f),
# then --numstat lines "ADDED<TAB>REMOVED<TAB>PATH" ("-" for binary files). Newest first.
history() {
  g log "${log_args[@]}" --format='%x1e%h%x1f%aN%x1f%aE%x1f%ad%x1f%s' --numstat "$rev" -- "${paths[@]+"${paths[@]}"}"
}

files_report() {
  # The first input is the list of files that exist at REV (for the "deleted" marker), the second is the history.
  # FNR == NR is true only while reading the first input: the classic awk two-file idiom.
  awk -F '\t' '
    FNR == NR { exists[$0] = 1; next }
    substr($0, 1, 1) == "\036" {                 # commit header
      split(substr($0, 2), c, "\037")
      hash = c[1]; name = c[2]; date = c[4]
      next
    }
    NF == 3 {                                     # numstat line
      f = $3
      if (!(f in commits)) {                      # newest first: the first time we see a file is its latest change
        order[++n] = f
        last[f] = date "\t" name "\t" hash
      }
      commits[f]++
      if (!((f, name) in by)) who[f] = who[f] (who[f] == "" ? "" : ", ") name
      by[f, name]++
    }
    END {
      if (!n) exit 1
      print "FILE\tCOMMITS\tLAST CHANGE\tBY\tCOMMIT\tAUTHORS"
      for (i = 1; i <= n; i++) {
        f = order[i]
        # "Alice Smith(2), Bob Jones(1)": add the count to each name
        k = split(who[f], names, ", "); list = ""
        for (j = 1; j <= k; j++) list = list (j > 1 ? ", " : "") names[j] "(" by[f, names[j]] ")"
        printf "%s%s\t%d\t%s\t%s\n", f, (f in exists ? "" : " (deleted)"), commits[f], last[f], list
      }
    }
  ' <(g ls-tree -r --full-tree --name-only "$rev") <(history) # --full-tree: root-relative paths, like git log's, even from a subdirectory
}

authors_report() {
  awk -F '\t' '
    substr($0, 1, 1) == "\036" {
      split(substr($0, 2), c, "\037")
      name = c[2]; date = c[4]
      if (!(name in commits)) { order[++n] = name; latest[name] = date }   # newest first
      commits[name]++
      first[name] = date                                                  # overwritten until the oldest
      next
    }
    NF == 3 {
      if (!((name, $3) in touched)) { touched[name, $3] = 1; files[name]++ }
      if ($1 != "-") { added[name] += $1; removed[name] += $2 }           # "-" = binary
    }
    END {
      if (!n) exit 1
      print "AUTHOR\tCOMMITS\tFILES\tADDED\tREMOVED\tFIRST\tLATEST"
      for (i = 1; i <= n; i++) {
        a = order[i]
        printf "%s\t%d\t%d\t+%d\t-%d\t%s\t%s\n", a, commits[a], files[a], added[a], removed[a], first[a], latest[a]
      }
    }
  ' <(history) | {
    # most commits first; keep the header on top (read it, print it, sort the rest)
    IFS= read -r header && printf '%s\n' "$header" && sort -t $'\t' -k2,2nr -k1,1
  }
}

last_report() {
  local file line found=0
  printf 'FILE\tLAST CHANGE\tBY\tCOMMIT\tAUTHORS\tSUBJECT\n'
  for file in "$@"; do
    # One git log per file, newest first (--follow tracks renames; it works for one file only).
    # Line 1 is the latest change; all lines together give every author, with a count,
    # listed in order of their most recent change: "Bob Jones(2), Carol White(2)".
    line="$(g log --follow "${log_args[@]}" --format='%ad%x09%aN%x09%h%x09%s' "$rev" -- "$file" |
      awk -F '\t' '
        NR == 1 { latest = $1 "\t" $2 "\t" $3; subject = $4 }
        !($2 in n) { order[++k] = $2 }
        { n[$2]++ }
        END {
          if (!NR) exit
          for (i = 1; i <= k; i++) authors = authors (i > 1 ? ", " : "") order[i] "(" n[order[i]] ")"
          print latest "\t" authors "\t" subject
        }')"
    if [[ -n "$line" ]]; then
      printf '%s\t%s\n' "$file" "$line"
      found=$((found + 1))
    else
      printf '%s\t-\t(no commits)\t-\t-\t-\n' "$file"
    fi
  done
  ((found > 0))
}

# Run the report into a variable first, so "nothing found" can be detected before rendering.
case "$command" in
  files) out="$(files_report)" || true ;;
  authors) out="$(authors_report)" || true ;;
  last) out="$(last_report "$@")" || { printf '%s\n' "$out" | render; exit 1; } ;;
esac

if [[ "$(wc -l <<<"$out")" -le 1 ]]; then
  log "no commits found${since:+ since $since}${until:+ until $until}${author:+ by $author}"
  exit 1
fi
printf '%s\n' "$out" | limit_rows | render
