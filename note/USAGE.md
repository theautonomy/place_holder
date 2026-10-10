# git-who: usage guide

`git-who.sh` answers "who changed what, and when?" for any git repository:

- **which files** changed in a time window, how often, **who made the latest change**, and **everyone** who changed them
- **who made the latest change** to specific files, plus all their authors and the commit message
- **per-person activity**: commits, files touched, lines added and removed

It reads the repository and changes nothing.

## Contents

1. [Install](#install)
2. [Quick start](#quick-start)
3. [Commands](#commands): `files`, `authors`, `last`
4. [Options](#options)
5. [Dates and time windows](#dates-and-time-windows)
6. [Output formats](#output-formats)
7. [Recipes](#recipes)
8. [How things are counted](#how-things-are-counted)
9. [Exit status](#exit-status)
10. [Troubleshooting](#troubleshooting)

---

## Install

It's a single, self-contained bash script. Copy it anywhere on your `PATH`:

```bash
mkdir -p ~/bin
cp projects/07-git-activity/solution/git-who.sh ~/bin/git-who
chmod +x ~/bin/git-who            # then run it as: git-who files
```

**Requirements:** bash 4+, git, awk, `column` (all included in Git Bash on Windows and in every Linux distribution).
On macOS, install a newer bash first (`brew install bash`), because the built-in one is 3.2.

**Try it on a sample repository** with a known history (four authors, a deleted file, a binary file, a merge, one person with two email addresses):

```bash
bash projects/07-git-activity/fixtures/make-repo.sh /tmp/shop
cd /tmp/shop
```

All examples below were run in that repository with `TZ=UTC`. On your machine, the commit hashes are the same.

## Quick start

```bash
git-who -s 2026-10-01 -u 2026-10-10 files      # what changed in that window, and who changed it
git-who last pom.xml                            # who changed pom.xml last (and who else has)
git-who -s "2 weeks ago" authors                # who has been active lately
git-who -h                                      # built-in help
```

Run it inside the repository, or point it elsewhere with `-C`:

```bash
git-who -C ~/dev/shop-service -s yesterday files
```

---

## Commands

### `files`: what changed, and who changed it

One row per file changed in the window, **most recently changed first**:

```
$ git-who -s 2026-10-01 -u 2026-10-10 files
FILE                                                  COMMITS  LAST CHANGE       BY           COMMIT   AUTHORS
src/main/java/com/acme/Discount.java                  1        2026-10-05 09:00  Dave Brown   2a16c79  Dave Brown(1)
src/main/java/com/acme/LegacyImporter.java (deleted)  1        2026-10-04 12:00  Carol White  1c3a902  Carol White(1)
src/main/java/com/acme/OrderService.java              2        2026-10-04 12:00  Carol White  1c3a902  Carol White(2)
assets/logo.png                                       1        2026-10-03 08:00  Bob Jones    b039e43  Bob Jones(1)
README.md                                             1        2026-10-02 15:30  Alice Smith  d8e4076  Alice Smith(1)
docs/Setup Guide.md                                   1        2026-10-02 15:30  Alice Smith  d8e4076  Alice Smith(1)
```

| Column | Meaning |
|---|---|
| `FILE` | path from the repository root. `(deleted)` means it no longer exists at the revision being looked at (`-r`, default `HEAD`) |
| `COMMITS` | commits that changed the file, **within the window** |
| `LAST CHANGE`, `BY`, `COMMIT` | the most recent of those commits: its date, its author, its short hash (`git show 1c3a902` for details) |
| `AUTHORS` | **everyone** who changed the file within the window, with their commit count, most recent first (so `BY` is always first) |

> **Every column is limited to the window.** In the example, `OrderService.java` shows only Carol, although Bob created it on Sept 20 and changed it again on Oct 12,
> because both of Bob's commits fall outside Oct 1–10. Without `-s`/`-u` you get the whole history:
>
> ```
> $ git-who files
> FILE                                                  COMMITS  LAST CHANGE       BY           COMMIT   AUTHORS
> src/main/java/com/acme/OrderService.java              4        2026-10-12 16:45  Bob Jones    991c14f  Bob Jones(2), Carol White(2)
> …
> ```
>
> To see the full history of a few specific files, use [`last`](#last-file-who-made-the-latest-change).

### `authors`: who has been active

One row per author, **most commits first**:

```
$ git-who -s 2026-10-01 -u 2026-10-10 authors
AUTHOR       COMMITS  FILES  ADDED  REMOVED  FIRST             LATEST
Carol White  2        2      +4     -4       2026-10-01 11:00  2026-10-04 12:00
Alice Smith  1        2      +6     -0       2026-10-02 15:30  2026-10-02 15:30
Bob Jones    1        1      +0     -0       2026-10-03 08:00  2026-10-03 08:00
Dave Brown   1        1      +2     -0       2026-10-05 09:00  2026-10-05 09:00
```

| Column | Meaning |
|---|---|
| `COMMITS` | commits by this author in the window |
| `FILES` | distinct files they changed |
| `ADDED` / `REMOVED` | lines added / removed. Binary files (images, jars) count as 0, which is why Bob's logo shows `+0` |
| `FIRST` / `LATEST` | their earliest and latest commit in the window |

### `last FILE...`: who made the latest change

For each file you name: the latest change, **every author** who has changed it, and the commit message:

```
$ git-who last README.md src/main/java/com/acme/OrderService.java nope.txt
FILE                                      LAST CHANGE       BY            COMMIT   AUTHORS                       SUBJECT
README.md                                 2026-10-02 15:30  Alice Smith   d8e4076  Alice Smith(2)                docs: setup guide
src/main/java/com/acme/OrderService.java  2026-10-12 16:45  Bob Jones     991c14f  Bob Jones(2), Carol White(2)  fix: better message
nope.txt                                  -                 (no commits)  -        -                             -
```

- **Paths are relative to where you run it**, like in git (`last pom.xml` from the root, `last main/App.java` from `src/`).
- **Renames are followed:** if `Order.java` used to be `OrderOld.java`, the old commits count too.
- **"As of" a date:** `-u` answers "who had last changed this *before* the incident?":

  ```
  $ git-who -u 2026-10-04 last src/main/java/com/acme/OrderService.java
  FILE                                      LAST CHANGE       BY           COMMIT   AUTHORS                       SUBJECT
  src/main/java/com/acme/OrderService.java  2026-10-04 12:00  Carol White  1c3a902  Carol White(2), Bob Jones(1)  refactor: drop legacy importer
  ```

- With `-s` as well, both the latest change and the authors list are limited to the window.

---

## Options

Options go **before** the command (`git-who -s 2026-10-01 files`, not `git-who files -s …`).

| Option | Meaning | Example |
|---|---|---|
| `-C DIR` | the repository to read (default: the current directory) | `-C ~/dev/shop-service` |
| `-s SINCE` | start of the time window (see [dates](#dates-and-time-windows)) | `-s 2026-10-01`, `-s "2 weeks ago"` |
| `-u UNTIL` | end of the time window | `-u 2026-10-10`, `-u "2026-10-10 18:00"` |
| `-p PATH` | only these paths. **Repeatable.** A directory covers everything under it | `-p src/main/java/com/acme/payment -p pom.xml` |
| `-a AUTHOR` | only commits whose author name or email matches. Case-insensitive **extended regex** | `-a carol`, `-a 'alice\|bob'`, `-a '@acme\.com$'` |
| `-r REV` | look at another branch, tag or commit (default: `HEAD`) | `-r origin/main`, `-r v1.4.0` |
| `-n N` | show at most N rows | `-n 20` |
| `-o FORMAT` | `text` (default), `csv` or `md` (see [formats](#output-formats)) | `-o md` |
| `-h` | help | |

Examples:

```
$ git-who -p docs -p pom.xml files
FILE                 COMMITS  LAST CHANGE       BY           COMMIT   AUTHORS
docs/Setup Guide.md  1        2026-10-02 15:30  Alice Smith  d8e4076  Alice Smith(1)
pom.xml              1        2026-09-01 10:00  Alice Smith  3a02232  Alice Smith(1)

$ git-who -a carol files
FILE                                                  COMMITS  LAST CHANGE       BY           COMMIT   AUTHORS
src/main/java/com/acme/LegacyImporter.java (deleted)  1        2026-10-04 12:00  Carol White  1c3a902  Carol White(1)
src/main/java/com/acme/OrderService.java              2        2026-10-04 12:00  Carol White  1c3a902  Carol White(2)
```

`-a` matches the **canonical** identity from `.mailmap` (see [counting](#how-things-are-counted)).
Inside single quotes, write `'alice|bob'` (the `\|` in the table above is only Markdown escaping).

---

## Dates and time windows

| You write | Means |
|---|---|
| `2026-10-01` | with `-s`: Oct 1st **00:00:00**. With `-u`: Oct 1st **23:59:59** (the whole day is included) |
| `"2026-10-01 14:00"` or `2026-10-01T14:00` | that exact time (`:ss` seconds optional) |
| `yesterday`, `today` | relative to now |
| `"3 days ago"`, `"2 weeks ago"`, `"1 month ago"` | relative to now (minute, hour, day, week, month, year; singular or plural) |

- **Both ends are inclusive.** `-s 2026-10-01 -u 2026-10-01` is exactly one day.
- **Times are in your local time zone.** Set `TZ` for another one: `TZ=UTC git-who -s 2026-10-01 files`.
- **Anything else is rejected** with exit status 2. That's on purpose: plain `git log --since=lastweek` silently ignores a date it can't read and shows **everything**,
  which looks like a real answer.
- **The window filters on the *commit* date**, while `LAST CHANGE` shows the *author* date (when the change was written).
  They're equal for normal commits. After a rebase or cherry-pick, a change written last month can appear in this week's window. See [troubleshooting](#troubleshooting).

---

## Output formats

**`-o text`** (default): aligned columns for the terminal. Pipe through `less -S` when lines are long.

**`-o md`**: a Markdown table for tickets, pull requests or wikis:

```
$ git-who -s 2026-10-01 -u 2026-10-10 -n 2 -o md files
| FILE | COMMITS | LAST CHANGE | BY | COMMIT | AUTHORS |
|---|---|---|---|---|---|
| src/main/java/com/acme/Discount.java | 1 | 2026-10-05 09:00 | Dave Brown | 2a16c79 | Dave Brown(1) |
| src/main/java/com/acme/LegacyImporter.java (deleted) | 1 | 2026-10-04 12:00 | Carol White | 1c3a902 | Carol White(1) |
```

**`-o csv`**: for spreadsheets and scripts. Standard CSV quoting, so a cell containing a comma stays one cell:

```
$ git-who -n 2 -o csv files
FILE,COMMITS,LAST CHANGE,BY,COMMIT,AUTHORS
src/main/java/com/acme/OrderService.java,4,2026-10-12 16:45,Bob Jones,991c14f,"Bob Jones(2), Carol White(2)"
src/main/java/com/acme/Discount.java,1,2026-10-05 09:00,Dave Brown,2a16c79,Dave Brown(1)
```

Data goes to **stdout** and messages to **stderr**, so `git-who … > report.csv` contains only the report.

---

## Recipes

```bash
# Who touched the payment code last week?
git-who -s "1 week ago" -p src/main/java/com/acme/payment files

# Who should I ask about this file? (everyone who changed it, most recent first)
git-who last src/main/resources/application.yml

# Who changed the config before the outage on Oct 4th, 14:30?
git-who -u "2026-10-04 14:30" last src/main/resources/application.yml

# What did the team do this sprint? (as a Markdown table for the retro)
git-who -s 2026-09-29 -u 2026-10-10 -o md authors > sprint-activity.md

# The 20 busiest files of the last month (sorted by commits, using the CSV)
git-who -s "1 month ago" -o csv files | tail -n +2 | sort -t, -k2,2nr | head -20

# What did two people change, on the release branch?
git-who -r release/1.4 -a 'alice|bob' -s 2026-10-01 files

# Everything in a repo you didn't clone into the current directory
git-who -C ~/dev/shop-service -s yesterday files

# Open it in Excel / LibreOffice
git-who -s "3 months ago" -o csv files > activity.csv
```

---

## How things are counted

- **Merge commits are skipped.** Their changes are already counted in the commits they merged, so counting them again would credit the person who clicked "merge".
  In the sample repo, Dave's `Discount.java` is credited to Dave, not to Alice, who merged it.
- **One person, several identities.** If the repository has a [`.mailmap`](https://git-scm.com/docs/gitmailmap) file, its canonical names are used.
  In the sample repo, `alice <alice.smith@users.noreply.github.com>` (a commit made from GitHub's web UI) is reported as `Alice Smith`.
  Add a `.mailmap` to your repository to merge identities:

  ```
  Alice Smith <alice@acme.com> alice <alice.smith@users.noreply.github.com>
  ```

- **`(deleted)`** compares against the revision being looked at (`-r`, default `HEAD`), not against the end of the window.
- **Renames:** `last` follows renames (`git log --follow`). `files` and `authors` don't, so a renamed file appears under both names.
- **Lines:** `ADDED`/`REMOVED` come from `git log --numstat`. A changed line counts as one removed plus one added. Binary files count 0.
- **Sorting:** `files` by latest change (newest first). `authors` by commits (most first), then name. `AUTHORS` lists by most recent change.

---

## Exit status

Useful in scripts (`if git-who … ; then`):

| Status | Meaning |
|---|---|
| `0` | a report was printed |
| `1` | nothing found (no commits in the window, or none of the `last` files has commits), not a git repository, or an unknown `-r` revision |
| `2` | usage error: unknown option or command, bad `-n`/`-o`, or a date it doesn't understand |

```
$ git-who -s 2027-01-01 files
git-who: no commits found since 2027-01-01 00:00:00

$ git-who -s lastweek files
git-who: unsupported date: 'lastweek' (use 2026-10-01, "2026-10-01 14:00", yesterday or "N days ago")
Try 'git-who -h'.
```

---

## Troubleshooting

**"no commits found", but I know there were changes**
- Check the window: is `-u` before `-s`? Is the time zone right (`TZ=UTC`)?
- Check the branch: without `-r`, only commits reachable from your current `HEAD` count. Use `-r origin/main` for the shared branch (after `git fetch`).
- `-a` uses the canonical (mailmapped) name and email. Try a shorter pattern: `-a carol`.

**A commit from "last month" shows up in this week's window**
It was rebased or cherry-picked this week. The window uses the commit date and `LAST CHANGE` shows the author date. Check with
`git log -1 --format='author %ad%ncommit  %cd' <hash>`.

**The same person appears twice**
They committed with two names or emails. Add a `.mailmap` line (see [counting](#how-things-are-counted)).

**"not a git repository" or a surprising result in a non-repo directory**
git searches the parent directories for a repository. If your **home directory** is itself a git repository (`ls ~/.git`),
every folder under it counts as "inside a repo". Check with `git rev-parse --show-toplevel`.

**File names with non-ASCII characters**
They're printed as-is (UTF-8). File names containing tabs or newlines aren't supported.

**Slow on a very large repository**
Narrow it down with `-s` and `-p`. The tool reads the history once, so most of the time is git's own. On Windows, process start-up is slow,
so `last` with many files (one `git log` per file) is the slowest command.

**"files takes no arguments (use -p to pick paths)"**
Options must come **before** the command: `git-who -n 5 files`, not `git-who files -n 5`. To limit `files` to some paths, use `-p`.
