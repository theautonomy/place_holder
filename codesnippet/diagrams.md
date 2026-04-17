# Diagrams

Shared diagram slides — import any range with `src: ./shared/diagrams.md#N-M`

---
layout: center
---

```mermaid {scale: 0.75}
flowchart LR
  A[Start] --> B{Decision}
  B -->|Yes| C[Do it]
  B -->|No| D[Skip it]
  C --> E[End]
  D --> E
```

---
layout: center
---

```mermaid {scale: 0.75}
sequenceDiagram
  participant C as Client
  participant S as Server
  participant D as Database

  C->>S: GET /api/users
  S->>D: SELECT * FROM users
  D-->>S: rows[]
  S-->>C: 200 JSON
```

---
layout: center
---

```mermaid {scale: 0.7}
classDiagram
  class Stream {
    +readable: ReadableStream
    +writable: WritableStream
    +pipe()
  }
  class ReadableStream {
    +getReader()
    +pipeTo()
    +pipeThrough()
    +tee()
  }
  class WritableStream {
    +getWriter()
    +abort()
    +close()
  }
  class TransformStream {
    +readable: ReadableStream
    +writable: WritableStream
  }
  Stream <|-- ReadableStream
  Stream <|-- WritableStream
  TransformStream --> ReadableStream
  TransformStream --> WritableStream
```

---
layout: center
---

```mermaid {scale: 0.75}
gitGraph
  commit id: "init"
  branch feature/streams
  checkout feature/streams
  commit id: "add ReadableStream"
  commit id: "add TransformStream"
  checkout main
  branch hotfix
  checkout hotfix
  commit id: "fix typo"
  checkout main
  merge hotfix
  merge feature/streams id: "merge streams"
  commit id: "release v1.0"
```

---
layout: center
---

```mermaid {scale: 0.72}
mindmap
  root((Web Streams))
    ReadableStream
      getReader()
      pipeTo()
      pipeThrough()
      tee()
    WritableStream
      getWriter()
      abort()
      close()
    TransformStream
      TextDecoderStream
      TextEncoderStream
      CompressionStream
    Use Cases
      Fetch body
      File reading
      SSE
      Service Workers
```
