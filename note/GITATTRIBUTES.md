# .gitattributes: tell git what each file is

`.gitattributes` is a text file in your repository that tells **git** how to treat paths:
which files are text and which line endings they get, which are binary, how to diff and merge them,
and what to leave out of `git archive`. It's the reason this course's scripts are LF in the repository **and** on your disk,
even though Git for Windows ships with `core.autocrlf=true`.

- Reference: `git help gitattributes` · <https://git-scm.com/docs/gitattributes>
- Generator: <https://richienb.github.io/gitattributes-generator/> builds a starting file from the languages and tools you pick

## Contents

1. [Why bother](#why-bother)
2. [A first example](#a-first-example)
3. [Where git looks, and who wins](#where-git-looks-and-who-wins)
4. [Patterns](#patterns)
5. [Attribute states](#attribute-states)
6. [Line endings: text and eol](#line-endings-text-and-eol)
7. [Binary files](#binary-files)
8. [Diff: better hunk headers and readable binaries](#diff-better-hunk-headers-and-readable-binaries)
9. [Merge drivers](#merge-drivers)
10. [git archive: export-ignore and export-subst](#git-archive-export-ignore-and-export-subst)
11. [Other attributes](#other-attributes)
12. [Macros](#macros)
13. [Inspecting attributes](#inspecting-attributes)
14. [Adding .gitattributes to an existing repository](#adding-gitattributes-to-an-existing-repository)
15. [.gitattributes vs .editorconfig vs core.autocrlf](#gitattributes-vs-editorconfig-vs-coreautocrlf)
16. [Gotchas](#gotchas)
17. [A template for Java + shell projects](#a-template-for-java--shell-projects)

---

## Why bother

Without it, every clone behaves according to **each developer's own git config**, and you get problems like these:

- **CRLF scripts.** Git for Windows defaults to `core.autocrlf=true`, so a script committed with LF is checked out with CRLF.
  It may still run in Git Bash, but in Docker, CI or on a Linux server it fails:
  ```
  /usr/bin/env: 'bash\r': No such file or directory
  ```
- **Diffs where every line changed.** One person commits CRLF, the next commits LF, and the diff shows the whole file as changed.
- **Corrupted binaries.** A `.jar` or `.png` that git wrongly treats as text gets its bytes "fixed".
- **Unreadable diffs.** `Binary files a/notes.gz and b/notes.gz differ`, or hunk headers that name the class instead of the method.

Your own `~/.gitconfig` only fixes these for you. `.gitattributes` is **committed**, so it fixes them **for every clone, on every OS**.

## A first example

This is the `.gitattributes` at the root of this repository:

```gitattributes
# Scripts must have LF endings. A CRLF shebang line fails with "bad interpreter: /usr/bin/env: 'bash\r'".
* text=auto eol=lf
*.png binary
*.gz binary

# Fixtures that must keep their Windows line endings (the lesson is about cleaning them up).
lessons/08-sed-awk/fixtures/*.csv -text
```

Line by line:

| Line | Meaning |
|---|---|
| `* text=auto eol=lf` | Every file: if git detects it as text, store it with LF and check it out with LF, whatever `core.autocrlf` says. |
| `*.png binary`, `*.gz binary` | Never convert, diff or merge these as text (git would usually detect them, but saying so is safer). |
| `lessons/08-sed-awk/fixtures/*.csv -text` | Not text: no conversion at all. These CSVs are **meant** to be CRLF, because lesson 08 strips the `\r`. A later line overrides `*` for these paths. |

**Format:**
- One rule per line: `pattern attr1 attr2 ...`, separated by whitespace.
- **Comments** are lines starting with `#`. There are no inline comments.
- A pattern with spaces is written in double quotes, C-style: `"My File.txt" -text`.
- Attribute names are case-sensitive.

## Where git looks, and who wins

From **lowest** to **highest** precedence:

| File | Committed? | Use it for |
|---|---|---|
| `/etc/gitattributes` (Git for Windows: `<install dir>/etc/gitattributes`) | no | system-wide defaults (rare) |
| `~/.config/git/attributes`, or the file named by `core.attributesFile` | no | your personal defaults, in every repository |
| `.gitattributes` at the repository root | **yes** | the project's rules (this is the one you want) |
| `.gitattributes` in a subdirectory | yes | overrides for that subtree |
| `.git/info/attributes` | no | your private overrides for one repository |

Within a file, **a later line overrides an earlier one**, separately for each attribute. So put the general rules (`*`) first and the exceptions after them.

```
repo/
├── .gitattributes          * text=auto eol=lf
└── windows/
    └── .gitattributes      *.ps1 text eol=crlf   ← wins for windows/*.ps1
```

## Patterns

Patterns follow `.gitignore` rules, with two differences:

| Pattern | Matches |
|---|---|
| `*.sh` | a file name in **any** directory (no `/` in the pattern) |
| `/build.gradle` | only at the level of this `.gitattributes` (leading `/` anchors it) |
| `docs/*.md` | `.md` files directly in `docs/` (a `/` in the middle also anchors) |
| `docs/**` | **everything** under `docs/`, at any depth |
| `**/generated/**` | everything under any `generated/` directory |
| `{a,b}` | ❌ no brace expansion: write two lines |
| `!pattern` | ❌ **negative patterns are forbidden** (use `-attr` or `!attr` on a later line instead) |
| `vendor/` | ❌ **matches nothing useful**: a directory pattern doesn't apply to the files inside it. Write `vendor/**` |

The last one is a classic. Real output from a test repository with `vendor/ linguist-vendored` and `gen/** linguist-generated`:

```console
$ git check-attr -a -- vendor/lib/a.js gen/x/y.java
gen/x/y.java: linguist-generated: set
```

`vendor/lib/a.js` got nothing.

## Attribute states

Each attribute on a path is in one of four states:

| Written as | State | Example |
|---|---|---|
| `attr` | **set** | `*.sh text` |
| `-attr` | **unset** | `*.csv -text` |
| `attr=value` | **set to a value** | `*.sh eol=lf`, `*.java diff=java` |
| *(not mentioned)*, or `!attr` | **unspecified**: git falls back to its config and heuristics | `!eol` cancels an `eol` from an earlier line |

## Line endings: text and eol

This is what most `.gitattributes` files are for. Git converts line endings in two places:
**when you commit** (working tree → repository) and **when it checks out** (repository → working tree).

| Attribute | On commit (`git add`) | On checkout |
|---|---|---|
| `text` | CRLF → LF | LF → `eol`, or `core.eol` / `core.autocrlf` if `eol` is unspecified |
| `text=auto` | like `text`, **if git detects text** (no NUL bytes in the content) | same |
| `text eol=lf` (or just `eol=lf`) | CRLF → LF | LF, on every OS |
| `text eol=crlf` (or just `eol=crlf`) | CRLF → LF | LF → CRLF, on every OS |
| `-text` | nothing | nothing: bytes are kept exactly |
| unspecified | `core.autocrlf` decides (`true`, `input`, or `false`) | `core.autocrlf` decides |

Key points:

- **The repository always stores LF** for text files. `eol` only decides what you get **on disk**.
- **`eol` implies `text`** when `text` is unspecified, but has no effect on a `-text` path.
- **Attributes beat `core.autocrlf`.** That's the whole point: the committed file decides, not each developer's config.

The difference, with real output from Git Bash where `core.autocrlf=true` (the Git for Windows default).
In `git ls-files --eol`, `i/` is the line ending in the index (repository), `w/` in the working tree, `attr/` the attributes that apply:

```console
$ git config core.autocrlf
true

# No .gitattributes: a script committed with LF comes back with CRLF
$ git add run.sh
warning: in the working copy of 'run.sh', LF will be replaced by CRLF the next time Git touches it
$ git ls-files --eol
i/lf    w/crlf  attr/                 	run.sh
$ file run.sh
run.sh: Bourne-Again shell script, ASCII text executable, with CRLF line terminators

# With "* text=auto eol=lf": LF on disk too
$ git ls-files --eol
i/lf    w/lf    attr/text=auto eol=lf 	.gitattributes
i/lf    w/lf    attr/text=auto eol=lf 	run.sh
```

And a CRLF script in a Linux container (real output; in Git Bash, prefix `docker` with `MSYS_NO_PATHCONV=1`):

```console
$ docker run --rm -v "$PWD:/w" -w /w ubuntu:24.04 ./crlf.sh
/usr/bin/env: 'bash\r': No such file or directory
/usr/bin/env: use -[v]S to pass options in shebang lines
```

The same file runs fine in Git Bash, which quietly ignores the `\r`. That's why the bug shows up only in CI or production.

### Which line ending for which file

| Files | Rule | Why |
|---|---|---|
| everything by default | `* text=auto eol=lf` | one line ending everywhere; Java, Maven and IntelliJ are all happy with LF on Windows |
| shell scripts, `mvnw`, `gradlew`, Dockerfiles | LF (covered by `*`, or explicit `*.sh text eol=lf`) | the kernel reads the shebang, and `bash` treats `\r` as part of the command |
| `*.bat`, `*.cmd` | `text eol=crlf` | `cmd.exe` mishandles labels and `goto` in LF batch files |
| `*.ps1` | LF is fine (PowerShell 5.1 and 7 both read it) | add `eol=crlf` only if a tool needs it |
| files whose bytes matter (test fixtures, signed files) | `-text` | keep exactly what was committed |

## Binary files

```gitattributes
*.png binary
*.jar binary
```

`binary` is a built-in **macro** for `-text -diff -merge`: no line-ending conversion, `git diff` says `Binary files ... differ`, and a merge conflict keeps your version instead of inserting `<<<<<<<` markers into the bytes.

Git usually detects binaries itself: with `text=auto`, a file with a NUL byte isn't treated as text. Real output:

```console
$ printf 'abc\0def\n' > blob.dat
$ git ls-files --eol blob.dat
i/-text w/-text attr/text=auto        	blob.dat
```

Detection looks at the first 8000 bytes only, so a file can look like text and still be binary. Mark the binary types your project has: `*.jar`, `*.zip`, `*.gz`, `*.png`, `*.jpg`, `*.ico`, `*.pdf`, `*.class`, keystores (`*.jks`, `*.p12`).

## Diff: better hunk headers and readable binaries

### Hunk headers with `diff=<language>`

Each `@@` line in a diff ends with the "function" the change is in. By default git guesses with a simple rule (any line that starts with a letter);
a language driver knows the real syntax. Real output, same change to a `println` inside `main`:

```console
# without attributes: the class line
@@ -4,6 +4,6 @@ public class App {

# with "*.java diff=java": the method
@@ -4,6 +4,6 @@ public static void main(String[] args) {
```

Built-in drivers (from `git help gitattributes`): `ada`, `bash`, `bibtex`, `cpp`, `csharp`, `css`, `dts`, `elixir`, `fortran`, `fountain`,
`golang`, `html`, `java`, `kotlin`, `markdown`, `matlab`, `objc`, `pascal`, `perl`, `php`, `python`, `ruby`, `rust`, `scheme`, `tex`.

```gitattributes
*.java diff=java
*.kt   diff=kotlin
*.sh   diff=bash
*.md   diff=markdown
```

`git log -L :methodName:File.java` (show the history of one function) uses the same driver.

### Readable diffs of binary files with `textconv`

A diff driver can convert a file to text before diffing. Two parts: the attribute (committed) and the command (in each person's git config, because git won't run commands from a cloned repository):

```gitattributes
*.gz diff=gzip
```

```bash
git config diff.gzip.textconv 'gzip -dc'
```

Real output:

```console
# before
Binary files a/notes.gz and b/notes.gz differ

# after
@@ -1 +1 @@
-hello
+hello world
```

Other useful converters: `unzip -l` for `.jar`/`.zip` (list entries), `exiftool` for images, `pdftotext - -` for PDFs.

### Hiding diffs

`-diff` makes git print `Binary files differ` even for text, which is handy for huge lock files or generated data you never want to read in `git diff`.
(It also hides changes from `git log -p` review, so use it sparingly.)

## Merge drivers

| Attribute | Merge behavior |
|---|---|
| unspecified or `merge` | normal 3-way text merge |
| `-merge` or `merge=binary` | don't merge: keep the current branch's version and report a conflict |
| `merge=union` | keep the lines from **both** sides, no conflict markers. For append-only files like `CHANGELOG.md` or a word list. Order isn't guaranteed, so review the result |
| `merge=<name>` | a custom driver, configured with `merge.<name>.driver` in git config |

A popular custom one is "always keep ours", for files each branch maintains separately:

```gitattributes
src/main/resources/application-local.properties merge=ours
```

```bash
git config merge.ours.driver true    # the command "true" succeeds and leaves our version in place
```

Like `textconv`, the driver itself lives in git config, not in the repository.
Without that config line, `merge=ours` silently does a normal merge (tested: you get the usual `CONFLICT (content)` and `<<<<<<<` markers).
And a merge driver only runs when **both** branches changed the file. If only the other branch changed it, git simply takes their version.

## git archive: export-ignore and export-subst

`git archive` builds a `.tar` or `.zip` of a commit, for source releases (GitHub's "Download ZIP" uses it too). Leave development-only files out:

```gitattributes
.gitattributes   export-ignore
.github/**       export-ignore
docs/internal.md export-ignore
```

Real output: `docs/internal.md` is gone. The empty `docs/` directory entry stays.

```console
$ git archive HEAD | tar -t
docs/
notes.gz
src/
src/main/
src/main/java/
src/main/java/App.java
```

`export-subst` replaces `$Format:...$` placeholders (the `git log --format` syntax) in the archived copy, so a version file can carry the commit:

```gitattributes
VERSION export-subst
```

```
commit: $Format:%H$  date: $Format:%cs$
```

## Other attributes

| Attribute | What it does |
|---|---|
| `working-tree-encoding=UTF-16LE` | store the file as UTF-8 in the repository (so diffs work) but write it as UTF-16 on disk. For Windows tools that insist on UTF-16. Always combine with `text`. |
| `whitespace=...` | which whitespace problems `git diff --check` and `git apply` report: e.g. `*.md whitespace=-trailing-space` (Markdown line breaks), `Makefile whitespace=indent-with-non-tab` |
| `ident` | replaces `$Id$` with `$Id: <blob hash> $` on checkout. A CVS-era habit, rarely useful now. |
| `filter=<name>` | runs `clean` (on commit) and `smudge` (on checkout) commands from `filter.<name>.*` git config. Git LFS is built on it. |
| `filter=lfs diff=lfs merge=lfs -text` | what `git lfs track "*.psd"` writes: the file is stored on the LFS server, the repository only has a pointer |
| `linguist-generated` | **GitHub**: collapse the file in pull request diffs and leave it out of language stats. Good for generated code and lock files. |
| `linguist-vendored`, `linguist-documentation` | **GitHub**: leave out of language stats |
| `linguist-language=Shell` | **GitHub**: override language detection, e.g. for extensionless scripts |

`working-tree-encoding`, real output: the file on disk is UTF-16 (`w/-text`, since it's full of NUL bytes), and the repository copy is plain UTF-8:

```console
$ git ls-files --eol s.ps1
i/lf    w/-text attr/text eol=crlf    	s.ps1
$ git show :s.ps1 | od -c
0000000   W   r   i   t   e  \n
```

## Macros

A macro names a group of attributes. `binary` is the built-in one (`[attr]binary -diff -merge -text`). Define your own **in the root `.gitattributes` only** (macros in subdirectories are ignored):

```gitattributes
[attr]generated linguist-generated -diff
src/main/generated/** generated
*.min.js              generated
```

## Inspecting attributes

```bash
git check-attr -a -- path/to/file      # every attribute that applies to the file
git check-attr eol text -- *.sh        # only these attributes
git ls-files --eol                     # index/worktree line endings and attributes for every tracked file
git ls-files --eol | grep -v 'i/lf'    # find files that are not stored as LF
```

Real output:

```console
$ git check-attr -a -- src/main/java/App.java notes.gz docs/internal.md README.md
src/main/java/App.java: diff: java
notes.gz: diff: gzip
docs/internal.md: export-ignore: set
```

`README.md` prints nothing: no attributes apply. Macros are expanded:

```console
$ git check-attr -a -- logo.png
logo.png: binary: set
logo.png: diff: unset
logo.png: merge: unset
logo.png: text: unset
```

This repository's checklist uses the same command: step 3 of "Verifying changes" in `CLAUDE.md` checks that `git ls-files --eol` shows `i/lf` for every script.

## Adding .gitattributes to an existing repository

Attributes apply when git **touches** a file. Files already committed with CRLF stay that way until you renormalize them:

```bash
# 1. Commit or stash your work first: you want a clean tree.
git status

# 2. Add the rules, then re-add every tracked file with the new rules applied.
git add .gitattributes
git add --renormalize .
git status                # files whose stored line endings change show as modified
git commit -m "Normalize line endings"

# 3. Refresh your working tree so it matches the new rules too.
git rm -r --cached -q . && git reset --hard
```

Real output from a repository with two CRLF files:

```console
$ git ls-files --eol
i/crlf  w/crlf  attr/                 	build.sh
i/crlf  w/crlf  attr/                 	data.csv

$ git add .gitattributes && git add --renormalize . && git status --short
A  .gitattributes
M  build.sh
M  data.csv

$ git ls-files --eol build.sh
i/lf    w/crlf  attr/text=auto eol=lf 	build.sh      # repository fixed, disk not yet

$ git commit -qm "Normalize line endings"
$ git rm -r --cached -q . && git reset -q --hard
$ file build.sh
build.sh: ASCII text                                    # disk fixed
```

Warn your team: everyone has to pull this commit, and open branches will show line-ending changes when they merge.
`git merge -X renormalize` (or `git config merge.renormalize true`) reduces the conflicts.
To keep `git blame` useful, add the commit's hash to a `.git-blame-ignore-revs` file and run `git config blame.ignoreRevsFile .git-blame-ignore-revs`
(GitHub reads that file automatically).

## .gitattributes vs .editorconfig vs core.autocrlf

| | `.gitattributes` | `.editorconfig` | `core.autocrlf` |
|---|---|---|---|
| **Lives in** | the repository | the repository | each developer's git config |
| **Acts when** | files go in and out of git | you type and save in an editor | files go in and out of git, **only if** attributes don't say otherwise |
| **Controls** | line endings, binary, diff, merge, archive, LFS | indentation, encoding, line endings, whitespace | line endings |
| **Guarantees** | every commit, from any tool | only edits made in a supporting editor | nothing for other people |

Use **`.gitattributes` and `.editorconfig` together**: the editor writes LF, and git makes sure the repository and every checkout have LF.
See [EDITORCONFIG.md](EDITORCONFIG.md#editorconfig-vs-gitattributes-vs-formatters).
With a good `.gitattributes`, `core.autocrlf` stops mattering for that repository, so you can leave the Git for Windows default alone.

## Gotchas

- **`dir/` matches nothing inside the directory.** Write `dir/**`. Negative patterns (`!*.txt`) aren't allowed at all.
- **Case sensitivity follows `core.ignorecase`.** On Windows and macOS (where it's `true`), `*.SH` also matches `run.sh`; on Linux it doesn't.
  Real output: `git -c core.ignorecase=false check-attr eol -- run.sh` → `run.sh: eol: unspecified`. Write patterns in the case the files really have.
- **Adding the file doesn't fix committed files.** Renormalize once (see [above](#adding-gitattributes-to-an-existing-repository)).
- **Git Bash hides CRLF bugs.** A CRLF script runs in Git Bash and fails on Linux. Check `git ls-files --eol` and `file script.sh`, not "it works on my machine".
- **Order matters.** A later line wins. `*.csv -text` above `* text=auto` does nothing, because `*` comes later and sets `text=auto` again.
- **Drivers aren't in the repository.** `diff=gzip`, `merge=ours` and `filter=...` need git config on each machine (git won't run commands a cloned repository asks for). Document the setup in the README.
- **`-text` vs `binary`.** `-text` only stops conversion; the file is still diffed and merged as text. Use `binary` for real binaries.
- **`text` forces conversion.** `* text` (without `=auto`) converts binaries too, and corrupts them. Use `* text=auto`.
- **`eol=crlf` on Linux is real.** Files marked `eol=crlf` are checked out with CRLF on every OS, including in Docker builds. Only use it for Windows-only files.
- **UTF-8 BOM isn't a line ending.** `.gitattributes` won't remove a BOM. That's `.editorconfig`'s `charset = utf-8` or a one-time cleanup.
- **The file itself needs LF?** No, git reads `.gitattributes` with CRLF fine. But `* text=auto eol=lf` covers it anyway.

## A template for Java + shell projects

```gitattributes
# Default: git detects text, stores LF, checks out LF on every OS.
* text=auto eol=lf

# Windows-only scripts need CRLF
*.bat  text eol=crlf
*.cmd  text eol=crlf

# Hunk headers that name the method
*.java   diff=java
*.kt     diff=kotlin
*.kts    diff=kotlin
*.sh     diff=bash
*.md     diff=markdown
*.html   diff=html
*.css    diff=css

# Binaries: no conversion, no text diff, no text merge
*.jar   binary
*.war   binary
*.class binary
*.zip   binary
*.gz    binary
*.tgz   binary
*.png   binary
*.jpg   binary
*.gif   binary
*.ico   binary
*.pdf   binary
*.jks   binary
*.p12   binary

# Generated or vendored: collapsed in GitHub PRs, left out of language stats
**/generated/**                linguist-generated
.mvn/wrapper/maven-wrapper.jar linguist-vendored

# Left out of "git archive" / "Download ZIP"
.gitattributes  export-ignore
.gitignore      export-ignore
.editorconfig   export-ignore
.github/**      export-ignore
```

Notes:
- `mvnw` and `gradlew` (shell scripts without an extension) are covered by `* text=auto eol=lf`; `mvnw.cmd` and `gradlew.bat` by the CRLF lines.
  `gradle init` generates a smaller version of the same idea.
- Pair it with the `.editorconfig` template in [EDITORCONFIG.md](EDITORCONFIG.md#a-template-for-java--shell-projects).
  Project 3's `newproj.sh` generates a smaller version of both for new projects.
- For other stacks, the [gitattributes generator](https://richienb.github.io/gitattributes-generator/) builds a file from the languages you pick.
  Review what it gives you: check the order of the lines, and don't use `* text` without `=auto` (see [gotchas](#gotchas)).

---

**See also:** lesson 00 (the CRLF trap), lesson 08 (stripping `\r` from CSVs), lesson 14 (Windows line endings and portability),
[EDITORCONFIG.md](EDITORCONFIG.md), and this repository's own [`.gitattributes`](../.gitattributes).
