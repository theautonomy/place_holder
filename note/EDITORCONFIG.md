# EditorConfig: one formatting setup for every editor

`.editorconfig` is a small text file in your repository that tells **every editor and IDE** how to format files:
tabs or spaces, indent size, line endings, character set, trailing whitespace and final newlines.
It's the reason this course's scripts keep their LF line endings and 2-space indentation, whether you open them in VS Code, IntelliJ, Vim or Notepad++.

- Website and full specification: <https://editorconfig.org> · <https://spec.editorconfig.org>

## Contents

1. [Why bother](#why-bother)
2. [A first example](#a-first-example)
3. [How editors find and apply it](#how-editors-find-and-apply-it)
4. [Section patterns (globs)](#section-patterns-globs)
5. [All standard properties](#all-standard-properties)
6. [Tool-specific properties](#tool-specific-properties)
7. [Editor support](#editor-support)
8. [.editorconfig vs .gitattributes vs formatters](#editorconfig-vs-gitattributes-vs-formatters)
9. [Gotchas](#gotchas)
10. [A template for Java + shell projects](#a-template-for-java--shell-projects)
11. [Checking it in CI](#checking-it-in-ci)

---

## Why bother

Without it, every developer's editor uses its own defaults, and the repository fills up with problems like these:

- **Mixed tabs and spaces.** Code looks aligned in one editor and ragged in another, or in the GitHub diff view.
- **CRLF line endings.** A shell script saved on Windows fails with `/usr/bin/env: 'bash\r': No such file or directory` (lesson 00).
- **Noise in diffs.** Whole files show as changed because someone's editor stripped trailing spaces or added a final newline.
- **Wrong character set.** `€` and `é` turn into `â‚¬` and `Ã©`.

`.editorconfig` fixes these **at the source, as you type and save**, for everyone, with nothing to install in most IDEs. It's the cheapest
consistency win a team can get.

## A first example

This is the `.editorconfig` at the root of this repository:

```ini
root = true                       # stop searching parent directories: this is the top

[*]                               # every file
charset = utf-8                   # save as UTF-8
end_of_line = lf                  # Unix line endings, even on Windows
insert_final_newline = true       # always end with a newline
trim_trailing_whitespace = true   # remove spaces at the end of lines
indent_style = space              # indent with spaces...
indent_size = 2                   # ...2 of them

[*.md]                            # Markdown files: override one setting
trim_trailing_whitespace = false  # two trailing spaces are a line break in Markdown
```

(The `#` comments are added here for explanation. In a real file, a comment must be on its own line; see [gotchas](#gotchas).)

**Format:** an INI-style file named exactly `.editorconfig`.
- **Sections** start with `[glob]` and apply to the files that match the pattern.
- **Properties** are `key = value` lines below a section.
- **`root = true`** is the only property allowed above the first section.
- **Comments** are lines starting with `#` or `;`.
- **Case:** keys and values are case-insensitive.

## How editors find and apply it

When you open `src/main/java/App.java`, the editor:

1. Looks for `.editorconfig` in `src/main/java/`, then `src/main/`, then `src/`, then the repository root, and so on up the tree,
   **until it reaches a file with `root = true`** (or the file system root).
2. Applies the files **from the top down**: the root file first, then each file closer to `App.java`.
   **Closer files override** farther ones.
3. Within one file, **later sections override earlier ones** for the same property.

So you can have one root `.editorconfig` for the whole repository, plus small overrides in subdirectories:

```
repo/
├── .editorconfig          root = true, [*] indent_size = 4
└── frontend/
    └── .editorconfig      [*.{js,ts,json}] indent_size = 2   ← wins for files under frontend/
```

Put `root = true` in the repository's top file. Otherwise an `.editorconfig` in your home directory or a parent folder leaks into the project.

## Section patterns (globs)

| Pattern | Matches |
|---|---|
| `*` | any characters except `/` |
| `**` | any characters including `/` (any depth) |
| `?` | exactly one character (not `/`) |
| `[abc]`, `[a-z]` | one character from the set or range |
| `[!abc]` | one character **not** in the set |
| `{js,ts,json}` | any of the comma-separated alternatives |
| `{1..5}` | any integer from 1 to 5 |
| `\` | escapes the next character: `[\[*\]]` |

**Where a pattern applies:**

- A pattern **without `/`** matches the file name in **any** directory: `[*.java]`, `[Makefile]`.
- A pattern **with `/`** is relative to the directory of the `.editorconfig` file: `[src/**.java]`, `[/build.gradle]` (the leading `/` anchors it to that directory).

**Examples:**

```ini
[*]                        # everything
[*.{java,kt,gradle}]       # several extensions
[*.{yml,yaml}]
[{Makefile,*.mk}]          # exact names and an extension together
[**/test/**.java]          # Java files under any test/ directory
[docs/**]                  # everything under docs/ (next to this .editorconfig)
[*.min.{js,css}]           # minified files...
indent_style = unset       # ...hands off (see "unset" below)
```

## All standard properties

These are defined by the EditorConfig specification and work in every supporting editor.

| Property | Values | What it does |
|---|---|---|
| `root` | `true` | **Preamble only** (above all sections). Stop searching parent directories for more `.editorconfig` files. |
| `indent_style` | `space`, `tab` | Indent with spaces or with tab characters. |
| `indent_size` | a number, or `tab` | Columns per indentation level. `tab` means "use `tab_width`". |
| `tab_width` | a number | How wide a tab character is displayed. Defaults to `indent_size` when that's a number, so you rarely need it. |
| `end_of_line` | `lf`, `crlf`, `cr` | The line ending written when saving. `lf` = Linux/macOS/scripts, `crlf` = Windows-only files (`.bat`, `.cmd`), `cr` = classic Mac OS (obsolete). |
| `charset` | `utf-8`, `utf-8-bom`, `latin1`, `utf-16be`, `utf-16le` | The file's encoding. Use `utf-8` almost always. **Avoid `utf-8-bom`** for scripts and Java: a BOM before `#!` breaks the shebang. |
| `trim_trailing_whitespace` | `true`, `false` | Remove spaces and tabs at the end of lines when saving. |
| `insert_final_newline` | `true`, `false` | `true`: make sure the file ends with a newline when saving. `false`: per the spec, make sure it does **not** (many editors just leave the file alone). Use `true`, because Unix tools expect it and `wc -l` and `read` miss an unterminated last line. |
| `spelling_language` | e.g. `en`, `en-US`, `de` | Language for spell checking (added in spec 0.16, so few editors support it yet). |

### Special value: `unset`

Any property can be set to `unset` to **remove** a setting inherited from an earlier section or a parent file.
The editor then falls back to its own default:

```ini
[*]
indent_style = space
indent_size = 4

[*.min.js]
indent_style = unset      # don't touch generated files
indent_size = unset
```

### Widely supported, not in the core spec

| Property | Values | Notes |
|---|---|---|
| `max_line_length` | a number, or `off` | Where the editor shows a ruler and wraps or hard-wraps text. Supported by IntelliJ, Vim/Neovim, Emacs and Prettier (as `printWidth`). The VS Code extension ignores it, so use VS Code's `editor.rulers` setting instead. |

## Tool-specific properties

Unknown properties are ignored by editors that don't understand them, so tools add their own. Some useful ones:

| Tool | Properties | Example |
|---|---|---|
| **IntelliJ IDEA** | `ij_<language>_<option>`, i.e. the full code style (export one with *Settings → Code Style → ⚙ → Export → EditorConfig File*) | `ij_java_class_count_to_use_import_on_demand = 999` (never `import x.*`) |
| **Visual Studio / .NET** | `dotnet_*`, `csharp_*` analyzer and style rules | `dotnet_diagnostic.CA1822.severity = warning` |
| **shfmt** (shell formatter) | `shell_variant`, `binary_next_line`, `switch_case_indent`, `space_redirects`, `function_next_line` | `switch_case_indent = true` |
| **Prettier** | reads `indent_style`, `indent_size`/`tab_width`, `end_of_line`, `max_line_length` from `.editorconfig` | (no extra keys; `.prettierrc` wins if both set something) |
| **ktlint** (Kotlin) | `ktlint_*` settings and rule switches | `ktlint_code_style = intellij_idea` |

For this course, **shfmt** is the interesting one. It formats shell scripts and takes its settings from `.editorconfig`:

```ini
[*.sh]
indent_style = space
indent_size = 2
switch_case_indent = true    # indent the patterns inside case ... esac
space_redirects = false      # write >file, not > file
binary_next_line = true      # && and || start the continued line
```

```bash
shfmt -d .    # show what would change
shfmt -w .    # rewrite files
```

## Editor support

| Editor / tool | Support |
|---|---|
| **IntelliJ IDEA**, Android Studio, other JetBrains IDEs | Built in (enabled by default). Also reads `ij_*` properties |
| **Visual Studio** | Built in |
| **VS Code** | Install the **EditorConfig for VS Code** extension (`editorconfig.editorconfig`). This repo recommends it in `.vscode/extensions.json` |
| **Neovim 0.9+** | Built in, on by default |
| **Vim 9.1** (and late 9.0 patches) | Shipped as an optional package: add `packadd! editorconfig` to your `vimrc`. Older versions need the editorconfig-vim plugin |
| **Emacs** | Built in from Emacs 30, earlier versions need the `editorconfig` package |
| **Eclipse**, **Sublime Text**, **Notepad++** | Plugin |
| **GitHub** web view | Uses `indent_size`/`tab_width` for how wide tabs look in code and diffs |
| **Prettier**, **shfmt**, **ktlint**, **editorconfig-checker** | Read it for formatting or checking (see above) |

Full list: <https://editorconfig.org/#pre-installed> and <https://editorconfig.org/#download>.

## .editorconfig vs .gitattributes vs formatters

They overlap, and you usually want all three:

| | `.editorconfig` | `.gitattributes` | A formatter (Prettier, shfmt, Spotless) |
|---|---|---|---|
| **Acts when** | you type and save in an editor | files go in and out of git | you run it, or a pre-commit hook or CI does |
| **Controls** | indentation, encoding, line endings, whitespace | line endings in the repository and on checkout, binary files, diff and merge drivers | everything about code style: braces, wrapping, spacing, import order |
| **Catches** | new edits only | every commit, from any tool (including `git add` of a file you never opened) | whole files |
| **Doesn't** | fix existing files you don't touch, or files written by scripts | care about indentation | run unless you run it |

The combination this repository uses:

```ini
# .editorconfig: editors write LF
[*]
end_of_line = lf
```

```gitattributes
# .gitattributes: git stores LF and checks out LF, even with core.autocrlf=true on Windows
* text=auto eol=lf
*.bat text eol=crlf
*.png binary
```

`.editorconfig` alone isn't enough for line endings. A file created by a script, or edited outside your IDE, can still get CRLF,
and git with `core.autocrlf=true` converts on checkout. `.gitattributes` is the safety net.

## Gotchas

- **No inline comments.** `indent_size = 2  # two spaces` is invalid in current versions of the spec, and some parsers take `2  # two spaces` as the value.
  Put comments on their own line. (The annotated example above is for reading, not copying.)
- **It only affects new edits.** Opening a file with tabs doesn't convert it to spaces, and setting `end_of_line = lf` doesn't rewrite existing CRLF files.
  Reformat once in the IDE (*Reformat Code*, or VS Code's *Change End of Line Sequence*), or run `dos2unix` / `sed -i 's/\r$//'` / a formatter over the repository.
- **Markdown trailing spaces.** Two trailing spaces mean a line break in Markdown, so set `trim_trailing_whitespace = false` for `[*.md]`.
- **Makefiles need tabs.** If `[*]` says spaces, add `[{Makefile,*.mk}]` with `indent_style = tab`, or `make` fails with "missing separator".
- **YAML must not use tabs.** Keep `indent_style = space` for `[*.{yml,yaml}]`.
- **`utf-8-bom` breaks scripts.** A BOM before `#!/usr/bin/env bash` means the kernel no longer sees the shebang. It also shows up as a stray character in some Java tools.
- **Forgetting `root = true`.** Settings from an `.editorconfig` in a parent folder (or your home directory) silently apply to the project.
- **`[*.{js}]` with one item** doesn't work in some parsers. Braces need at least two alternatives: write `[*.js]`.
- **`indent_size` doesn't reformat code structure.** It sets the indent unit. Where braces go and how lines wrap is a formatter's job.
- **VS Code without the extension ignores the file completely**, silently. If settings "don't work", check the extension first.

## A template for Java + shell projects

A good starting point for the kind of repositories in this course (Maven/Gradle Java plus shell scripts):

```ini
# EditorConfig: https://editorconfig.org
root = true

[*]
charset = utf-8
end_of_line = lf
insert_final_newline = true
trim_trailing_whitespace = true
indent_style = space
indent_size = 4

# Java and build files
[*.{java,kt,kts,gradle}]
indent_size = 4
max_line_length = 120

[pom.xml]
indent_size = 4

# Shell (shfmt reads the last three)
[{*.sh,*.bash,bin/*}]
indent_size = 2
switch_case_indent = true
space_redirects = false
binary_next_line = true

# Config and data
[*.{yml,yaml,json,toml}]
indent_size = 2

[*.properties]
# Java 9+ ResourceBundles read .properties as UTF-8, but Properties.load(InputStream)
# still reads ISO-8859-1. If your code uses the latter, use charset = latin1 here.
charset = utf-8

# Documentation
[*.md]
trim_trailing_whitespace = false
indent_size = 2

# Files with fixed formats
[{Makefile,*.mk}]
indent_style = tab

[*.{bat,cmd}]
end_of_line = crlf

# Generated: hands off
[{*.min.js,*.min.css,**/generated/**}]
indent_style = unset
indent_size = unset
trim_trailing_whitespace = unset
insert_final_newline = unset
```

Pair it with the matching `.gitattributes` (above). Project 3's `newproj.sh` generates a smaller version of both for new projects.

## Checking it in CI

Editors apply `.editorconfig` only to the files people edit. To check **every** file, run
[editorconfig-checker](https://github.com/editorconfig-checker/editorconfig-checker) in CI or a pre-commit hook:

```bash
# No install needed with Docker (Git Bash: prefix with MSYS_NO_PATHCONV=1)
docker run --rm -v "$PWD:/check" mstruebing/editorconfig-checker

# Or as a binary
ec                      # checks the current directory against its .editorconfig
```

It reports wrong line endings, tabs in space-indented files, trailing whitespace and missing final newlines, with file and line numbers,
and exits non-zero, so a pipeline can fail on it. Exclude generated or vendored files in a `.editorconfig-checker.json`.

---

**See also:** lesson 00 (the CRLF trap), lesson 14 (Windows line endings and portability), this repository's own
[`.editorconfig`](../.editorconfig) and [`.gitattributes`](../.gitattributes).
