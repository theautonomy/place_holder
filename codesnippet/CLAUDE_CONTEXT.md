# How Claude Code's Context Window Works

## The Problem: Finite Memory

Every Claude model has a **context window** — a fixed maximum number of tokens it can hold in memory at once. Think of it as a sliding whiteboard: everything Claude can "see" must fit on that whiteboard. Older content that scrolls off is gone.

The hard limit for Claude Sonnet and Opus models is **200,000 tokens** (~150,000 words — roughly a full novel). Claude Code triggers compaction *before* hitting that ceiling to leave room for summary generation and the next response.

A rough sense of scale:

| Content | Approximate tokens |
|---------|-------------------|
| One back-and-forth turn (with code) | 500 – 2,000 |
| A large file read or build log | 2,000 – 10,000 |
| Entire session before compaction | dozens to hundreds of turns |

For a long coding session — many back-and-forth messages, large file reads, build outputs — the window fills up.

---

## What Happens When It Fills Up: Compaction

Claude Code uses **automatic compaction** to keep sessions going indefinitely. When the conversation approaches the context limit, the system:

1. Takes older messages (not the most recent ones)
2. Generates a **summary** of those messages
3. Replaces them with that summary
4. Continues the session with the summary + recent messages fitting in the window

```
BEFORE COMPACTION
┌─────────────────────────────────────────────┐
│  Context Window (e.g. 200k tokens)          │
│                                             │
│  [Turn 1]  user: create hello world app     │
│  [Turn 2]  asst: created main.rs ...        │
│  [Turn 3]  user: add .gitignore             │
│  [Turn 4]  asst: created .gitignore ...     │
│  ...                                        │
│  [Turn 47] user: build the app              │
│  [Turn 48] asst: running cargo build ...    │
│  [Turn 49] user: now add photo grid         │  ← approaching limit
│                                             │
└─────────────────────────────────────────────┘


AFTER COMPACTION
┌─────────────────────────────────────────────┐
│  Context Window (reset, new space freed)    │
│                                             │
│  [SUMMARY] Project is a Tauri 2.x photo     │
│            album app. Frontend: Vue 3 +     │
│            Vite. Backend: Rust with         │
│            list_photos and read_image       │
│            commands. Images load via        │
│            base64 data URLs. Build          │
│            succeeded producing .exe/.msi.   │
│            Current task: add photo grid.    │
│                                             │
│  [Turn 49] user: now add photo grid         │
│  [Turn 50] asst: here is PhotoGrid.vue ...  │
│  [Turn 51] user: ...                        │  ← session continues
│                                             │
└─────────────────────────────────────────────┘
```

---

## What Gets Preserved vs. Lost

| What | Preserved? | How |
|------|-----------|-----|
| Recent messages | Yes | Kept verbatim in context |
| Older messages | Partially | Distilled into summary |
| Fine details (exact code, error text) | May be lost | Summary captures intent, not every line |
| `CLAUDE.md` | Yes | Re-read fresh at session start |
| Memory files (`~/.claude/projects/.../memory/`) | Yes | Re-loaded by the memory system |
| Git history | Yes | Claude can run `git log` any time |

---

## Why CLAUDE.md and Memory Files Matter

Because compaction can blur fine details, Claude Code is designed around two durable sources of truth that survive compaction:

```
┌──────────────────────────────────────────────────────┐
│  New Conversation (or after compaction)              │
│                                                      │
│  Always loaded fresh:                                │
│  ┌─────────────────┐   ┌──────────────────────────┐ │
│  │   CLAUDE.md     │   │  Memory files            │ │
│  │  (repo rules,   │   │  (user prefs, project    │ │
│  │   commands,     │   │   context, feedback)     │ │
│  │   architecture) │   │                          │ │
│  └────────┬────────┘   └────────────┬─────────────┘ │
│           │                         │                │
│           └──────────┬──────────────┘                │
│                      ▼                               │
│          ┌───────────────────────┐                   │
│          │  Compacted Summary    │                   │
│          │  (auto-generated)     │                   │
│          └───────────────────────┘                   │
│                      +                               │
│          ┌───────────────────────┐                   │
│          │  Recent messages      │                   │
│          │  (verbatim)           │                   │
│          └───────────────────────┘                   │
└──────────────────────────────────────────────────────┘
```

**CLAUDE.md** — lives in the repo, committed to git. Contains build commands, architecture overview, conventions. Claude re-reads it at the start of every session, so it's always fresh even after many compactions.

**Memory files** — stored in `~/.claude/projects/<repo>/memory/`. Claude writes these when it learns something worth remembering across sessions: user preferences, project decisions, recurring feedback. These are re-injected into every conversation.

---

## What's Inside the Context Window

Everything Claude can currently "see" falls into two categories:

**Included:**

| Content | Notes |
|---------|-------|
| All messages | Your messages + Claude's responses (or compacted summary if compaction has run) |
| Tool results | Output from every tool call: file reads, bash output, search results, etc. |
| System prompt | CLAUDE.md, memory files, current date, environment info — injected at session start |
| Tool definitions | Schemas for every tool Claude can call (these consume tokens too) |

**Not included (unless explicitly read):**

| Content | How to bring it in |
|---------|-------------------|
| File contents | Claude must call the Read tool first |
| Git history | Claude must run `git log` |
| Previous session details | Only what was saved to CLAUDE.md or memory files survives |

The window is the running transcript of the session plus the system bootstrap — nothing more, nothing less.

```
╔══════════════════════════════════════════════════════════════════╗
║                  CONTEXT WINDOW  (200k tokens)                   ║
║                                                                  ║
║  ┌─────────────────────────────────────────────────────────┐    ║
║  │  SYSTEM PROMPT (injected at session start)              │    ║
║  │  • CLAUDE.md contents                                   │    ║
║  │  • Memory files                                         │    ║
║  │  • Tool definitions (Read, Edit, Bash, ...)             │    ║
║  │  • Current date, OS, environment info                   │    ║
║  └─────────────────────────────────────────────────────────┘    ║
║                                                                  ║
║  ┌─────────────────────────────────────────────────────────┐    ║
║  │  CONVERSATION HISTORY                                   │    ║
║  │                                                         │    ║
║  │  [user]  open a folder to view photos                   │    ║
║  │  [asst]  here is the updated App.vue ...                │    ║
║  │  [tool]  Read › src/App.vue → <file contents>           │    ║
║  │  [tool]  Bash › npm run build → <build output>          │    ║
║  │  [user]  now add a lightbox                             │    ║
║  │  [asst]  created Lightbox.vue ...                       │    ║
║  │   ...                                                   │    ║
║  └─────────────────────────────────────────────────────────┘    ║
║                                                                  ║
╚══════════════════════════════════════════════════════════════════╝

Outside the window — NOT visible unless explicitly fetched:

  📁 Files on disk ──────► only visible after Read tool call
  📜 Git history   ──────► only visible after `git log`
  🕰  Past sessions ──────► only what was saved to CLAUDE.md / memory
```

### Skills and Plugins

Skills (slash commands) and MCP plugins also consume context tokens:

```
╔══════════════════════════════════════════════════════════════════╗
║                  CONTEXT WINDOW                                  ║
║                                                                  ║
║  ┌─────────────────────────────────────────────────────────┐    ║
║  │  SYSTEM PROMPT                                          │    ║
║  │  • CLAUDE.md, memory files, env info                   │    ║
║  │                                                         │    ║
║  │  Tool definitions (schemas loaded at session start):    │    ║
║  │  ├─ Built-in tools  (Read, Edit, Bash, Grep ...)  ◄──┐ │    ║
║  │  ├─ MCP tools       (if server is connected)      ◄──┤ │    ║
║  │  └─ Deferred tools  (name only, schema on demand) ◄──┘ │    ║
║  └─────────────────────────────────────────────────────────┘    ║
║                                                                  ║
║  ┌─────────────────────────────────────────────────────────┐    ║
║  │  CONVERSATION HISTORY                                   │    ║
║  │  • User & assistant messages                            │    ║
║  │  • Tool call results                                    │    ║
║  │  • Skill instructions ◄── injected here when /invoked  │    ║
║  └─────────────────────────────────────────────────────────┘    ║
║                                                                  ║
╚══════════════════════════════════════════════════════════════════╝
```

| Extension type | When tokens are consumed |
|---------------|--------------------------|
| Built-in tools | Always — schemas loaded at session start |
| MCP plugin tools | At session start if the MCP server is connected |
| Deferred tools | On demand — only when `ToolSearch` fetches the schema |
| Skills (slash commands) | Only when invoked — instructions injected into conversation |

**Deferred tools** are an optimization: tools that aren't commonly needed are listed by name only (tiny footprint) and their full schemas are fetched on demand. This keeps the base token cost low when many plugins are installed.

---

## What About Files Open in VS Code?

No — Claude has no awareness of what's open in your editor. The editor and Claude's context are completely separate.

```
┌─────────────────────┐           ┌─────────────────────────────┐
│      VS Code        │           │     Claude Code context     │
│                     │           │                             │
│  📄 App.vue   (open)│           │  ← knows nothing about      │
│  📄 lib.rs    (open)│    ✗      │     these open tabs         │
│  📄 Cargo.toml(open)│           │                             │
│  📄 main.ts   (open)│           │                             │
└─────────────────────┘           └─────────────────────────────┘

A file enters Claude's context only when explicitly fetched:

  Claude calls Read tool ──────────────────────────────────► file contents
  You paste code into chat ────────────────────────────────► file contents
  VS Code extension sends active file/selection (if configured) ► file contents
```

The one exception: the **Claude Code VS Code extension** can be configured to send the active file or a selected code block into the conversation — but that's the extension explicitly passing content in, not VS Code tabs being automatically visible to Claude.

---

## How Does Copilot Compare?

Copilot behaves differently depending on which mode you're using:

**Copilot inline completions (autocomplete)**
Open files *do* matter. Copilot actively uses your open tabs as context when generating completions — it picks relevant snippets from neighboring open files and feeds them into its prompt. Having `App.vue` and `lib.rs` open simultaneously genuinely helps it suggest better code.

**Copilot Chat**
Closer to Claude Code, but with tighter VS Code integration:
- The active file is often included automatically (without you asking)
- Selected text is injected into the chat
- `@workspace` can index the entire repo and retrieve relevant snippets on demand
- `@file` lets you explicitly reference a specific file

```
┌──────────────────────────────────────────────────────────────────┐
│                    Context awareness comparison                   │
├─────────────────────┬────────────────────┬───────────────────────┤
│                     │ Copilot autocomplete│ Copilot Chat          │
├─────────────────────┼────────────────────┼───────────────────────┤
│ Open tabs used      │ Yes (automatically) │ Partial (active file) │
│ Whole repo aware    │ No                  │ @workspace (indexed)  │
│ Conversation history│ No                  │ Yes                   │
│ Runs shell commands │ No                  │ No                    │
│ Edits files         │ No                  │ No                    │
├─────────────────────┴────────────────────┴───────────────────────┤
│                     │        Claude Code                          │
├─────────────────────┼─────────────────────────────────────────────┤
│ Open tabs used      │ No — files fetched on demand via Read tool  │
│ Whole repo aware    │ Yes — reads any file, runs git, greps code  │
│ Conversation history│ Yes (with compaction)                       │
│ Runs shell commands │ Yes                                         │
│ Edits files         │ Yes                                         │
└─────────────────────┴─────────────────────────────────────────────┘
```

The fundamental difference: Copilot is deeply integrated into the editor and knows your editor state automatically. Claude Code is an autonomous agent that fetches what it needs from the filesystem itself.

**Copilot Agent Mode** is the exception — it is the autonomous, agentic version of Copilot and is much closer to Claude Code in capability:
- Runs terminal commands and sees the output
- Edits multiple files across the repo
- Iterates — run, observe errors, fix, repeat
- Supports MCP servers for additional tools

```
┌──────────────────────────────────────────────────────────────────────┐
│              Full comparison (all four modes)                        │
├──────────────────────┬──────────┬──────────┬──────────┬─────────────┤
│                      │ Copilot  │ Copilot  │ Copilot  │ Claude      │
│                      │ inline   │ Chat     │ Agent    │ Code        │
├──────────────────────┼──────────┼──────────┼──────────┼─────────────┤
│ Open tabs (auto)     │ Yes      │ Partial  │ Yes      │ No          │
│ Repo awareness       │ No       │ @workspace│ @workspace│ read/grep  │
│ Conversation history │ No       │ Yes      │ Yes      │ Yes         │
│ Runs shell commands  │ No       │ No       │ Yes      │ Yes         │
│ Edits files          │ No       │ No       │ Yes      │ Yes         │
│ Iterates autonomously│ No       │ No       │ Yes      │ Yes         │
│ MCP support          │ No       │ No       │ Yes      │ Yes         │
│ Underlying model     │ GPT/etc  │ GPT/etc  │ switchable│ Claude only│
│ Lives in             │ VS Code  │ VS Code  │ VS Code  │ Terminal    │
└──────────────────────┴──────────┴──────────┴──────────┴─────────────┘
```

The key remaining difference between Copilot Agent Mode and Claude Code is **editor integration**: Copilot Agent Mode automatically receives your active file and open tabs because it lives inside VS Code. Claude Code has no automatic awareness of editor state — it fetches what it needs from the filesystem on demand.

### Context Management Commands

Copilot's slash commands are task-oriented (`/explain`, `/fix`, `/tests`, `/doc`) rather than context-management commands. It has no equivalents to Claude Code's `/clear` or `/compact`:

| | Claude Code | Copilot |
|--|--|--|
| Clear conversation | `/clear` command | `/new` command or "New Conversation" button in UI |
| Manual compaction | `/compact` command | No equivalent |
| Auto-compaction | Yes — old messages summarized, session continues | No — old messages silently fall off the window |

The lack of compaction is a meaningful limitation for long Copilot sessions: as the context fills up, earlier messages are simply lost with no summary preserved. Claude Code's compaction system lets a session run indefinitely with context continuity.

---

## The Session Lifecycle

```
Start session
     │
     ▼
Load CLAUDE.md + memory files
     │
     ▼
┌────────────────────────────┐
│  Conversation in progress  │◄──────────────────┐
│  (turns accumulate)        │                   │
└────────────────────────────┘                   │
     │                                           │
     │ context fills up                          │
     ▼                                           │
Compaction triggered                             │
  - old turns → summary                          │
  - recent turns kept verbatim                   │
     │                                           │
     └───────────────────────────────────────────┘
          session continues seamlessly
```

The user never needs to trigger compaction — it happens automatically. The session feels continuous, though very old low-level details (exact error output, intermediate code versions) may only survive as a summary.

---

## Resetting the Context Window

There is no `/reset` command in Claude Code. The relevant commands are:

| Command | Effect |
|---------|--------|
| `/clear` | Wipes the entire conversation history — fresh context window |
| `/compact` | Manually triggers compaction — summarizes old messages, frees space, session continues |
| Auto-compaction | Same as `/compact` but triggered automatically when the window approaches the limit |

**Important:** `/clear` only wipes the conversation. It does **not** clear CLAUDE.md or memory files — those are re-injected at the start of the next message. Your durable context survives a `/clear`.

```
/clear                            /compact
  │                                 │
  ▼                                 ▼
Conversation wiped             Old messages → summary
CLAUDE.md re-loaded ✓          Recent messages kept ✓
Memory files re-loaded ✓       CLAUDE.md re-loaded ✓
                               Session continues seamlessly
```

---

## Practical Implications

- **Put important context in CLAUDE.md.** Anything Claude must always know — build commands, architectural constraints, naming conventions — belongs there, not just in chat.
- **Use memory for cross-session preferences.** If you correct Claude's behavior ("don't add comments"), it saves that to memory so it persists after compaction and into future sessions.
- **Long sessions are fine.** Compaction is designed to let sessions run indefinitely. You don't need to start a new conversation to "reset" Claude.
- **Very specific details may blur.** If you need Claude to remember an exact file path or error message from early in a session, either keep it in a file Claude can re-read, or paste it again when it becomes relevant.
