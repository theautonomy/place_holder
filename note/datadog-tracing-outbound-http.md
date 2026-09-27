# Tracing Outbound HTTP Calls in Datadog

2026-09-27 · Wei Li

When service `abc` calls `https://sample.com`, the call shows up in Datadog APM as a **client span**. The span is created by `abc`'s tracer and holds the URL, the status code and the duration. Search for it in Trace Explorer by its URL or host attributes.

## Find the outgoing call spans

```text
service:abc @span.kind:client @http.url:*sample.com*
```

Other attributes you can use, depending on the tracing library:

| Query | Notes |
| --- | --- |
| `@out.host:sample.com` | Target host (common in Datadog tracers) |
| `@peer.hostname:sample.com` | Newer peer-tag name |
| `@peer.service:sample.com` | Datadog often names the uninstrumented dependency after its host |
| `operation_name:http.request` | Typical name for client spans |

**Escaping:** a URL contains `:` and `/`, which must be escaped, and quoting the value turns wildcards off. The easiest fix is to leave out the scheme and use `*sample.com*`. For a stricter match, escape the characters: `@http.url:https\:\/\/sample.com*`.

**Older tracer setups:** these may give client spans their own service name, such as `service:abc-http-client` or `service:okhttp`. If `service:abc` finds nothing, search for `@http.url:*sample.com*` on its own and look at the `service` facet.

## Narrow it down

Add a filter to the base query to find the calls you care about:

| Query | Finds |
| --- | --- |
| `service:abc @http.url:*sample.com* status:error` | Failed calls |
| `service:abc @http.url:*sample.com* @http.status_code:>=500` | Server errors from sample.com |
| `service:abc @http.url:*sample.com* @duration:>1s` | Slow calls |

`status:error` is the span's error flag, and `@http.status_code` is the HTTP response code. A 404 is often not marked as an error.

## Open the trace

Click a span to see the whole request in the flame graph or waterfall. It shows `abc`'s entry span, then the client span to sample.com. The client span's duration is how long sample.com took to respond, as `abc` saw it.

- **sample.com is not traced by Datadog** (for example, a third-party API): the trace stops at `abc`'s client span. You still get the timing, status code and errors. In the service map, sample.com appears as an inferred dependency.
- **sample.com is traced by Datadog** and receives the trace headers: its spans join the same trace. You can then run a trace query across both services.

```text
a: service:abc
b: service:<sample-service> status:error
Traces matching: a => b
```

`a => b` matches traces where `a` is the direct parent of `b`. Use `a -> b` to match `b` anywhere downstream of `a`.

## Visualize it

In the Timeseries view, filter on `service:abc @http.url:*sample.com*`. Plot the span count or the p95 of `@duration`, and group by `@http.status_code` or `@http.route` to see trends.

If these attributes aren't facets yet, use **Live Search**, which can filter on any attribute for the last 15 minutes. Or create facets for `@http.url` and `@out.host`, and make `@duration` a measure for range queries.

## Tracing an inbound endpoint

When other services call `abc`'s endpoint `/sample/entry`, each request shows up as a **server span**, which is also `abc`'s service entry span. You search for it by route or resource name rather than by URL.

### Find the requests

```text
service:abc @http.route:"/sample/entry"
```

| Query | Notes |
| --- | --- |
| `resource_name:"GET /sample/entry"` | Resource is usually `METHOD route` |
| `resource_name:*sample/entry*` | Any HTTP method |
| `@span.kind:server` | Only the inbound span, not internal child spans |
| `@http.url:*\/sample\/entry*` | When the route isn't set; this also matches query strings |

In Trace Explorer, select **Service entry spans** so each request appears once. `/` must be escaped, so quote the value or write `\/sample\/entry`. If the route has path parameters, match the template, such as `"/sample/entry/{id}"`.

### Narrow it down

| Query | Finds |
| --- | --- |
| `service:abc @http.route:"/sample/entry" status:error` | Failed requests |
| `service:abc @http.route:"/sample/entry" @http.status_code:>=500` | Server errors |
| `service:abc @http.route:"/sample/entry" @duration:>1s` | Slow requests |

### Find out who calls it

**Callers that are traced by Datadog** join the same trace, and the caller's span is the parent of `abc`'s entry span:

```text
a: -service:abc
b: service:abc @http.route:"/sample/entry"
Traces matching: a => b
```

Use `a -> b` to include callers further upstream. To list the calling services, show query `a` as a Top list or Table grouped by `service`. The endpoint's resource page (APM service `abc`, then Resources) also lists its upstream services.

**Callers that aren't traced** (external clients, or no trace headers) leave the trace starting at `abc`. Choose **Root spans** instead of Service entry spans so that only these requests match, then group by one of these attributes:

| Attribute | Tells you |
| --- | --- |
| `@network.client.ip` / `@http.client_ip` | Caller IP |
| `@http.useragent` | Client library or app |
| `@usr.id` | Authenticated user, if set |

### Open the trace and visualize

Click a span. The flame graph shows the caller above `abc`'s entry span when the caller is traced, and everything `abc` does underneath: database, cache and outbound HTTP calls. **Focus** the entry span to isolate it, or use the **Map** tab to see the caller → `abc` → downstream chain.

In the Timeseries view, filter on `service:abc @http.route:"/sample/entry"`. Plot the count, error rate or p95 of `@duration`, and group by `@http.status_code`, `version` or caller IP.

## `@http.url` vs `@http.route`

The difference is concrete versus template. `@http.url` is the **actual URL** of one request. `@http.route` is the **route pattern** from the web framework that handled it.

For a request to `GET https://abc.example.com/sample/entry/42?debug=true`:

| | `@http.url` | `@http.route` |
| --- | --- | --- |
| Value | `https://abc.example.com/sample/entry/42?debug=true` | `/sample/entry/{id}` |
| Contains | Scheme, host, port, path with real values, often the query string | Path template only |
| Set on | Server **and** client spans | Server spans only, when the framework integration knows the route |
| Cardinality | High: one value per ID or query | Low: one value per endpoint |
| Facet / group by | Poor, because values fragment | Good, because each endpoint is one bucket |
| Typical use | Outbound calls (`@http.url:*sample.com*`), a specific request | Inbound endpoints (`@http.route:"/sample/entry"`) |

What this means in practice:

- **Outbound calls** (abc → sample.com) have no route, because the client doesn't know the remote server's routing. Use `@http.url`, `@out.host` or `@peer.service`.
- **Inbound endpoints** should use `@http.route`, so `/sample/entry/42` and `/sample/entry/43` count as one endpoint. `resource_name` is usually built from it: `GET /sample/entry/{id}`.
- **When `@http.route` is missing**, fall back to `@http.url:*\/sample\/entry*`. This can happen with custom handlers, some frameworks or older tracers. Wildcards are needed because the URL includes the host and query string.
- **Query strings in `@http.url`** are often obfuscated or removed by the tracer, for example tokens replaced with `?`. So don't rely on searching for query parameter values.

The template style depends on the framework: `{id}` (Spring), `:id` (Express), `<id>` (Flask). Check the facet panel for the values your spans actually carry.

## `=>` vs `->` between services

Both lines mean "traces where service-a calls service-b." The arrow sets how directly: `=>` requires service-a to be the immediate parent, while `->` allows anything in between.

```text
service:service-a => service:service-b
service:service-a -> service:service-b
```

| Syntax | Matches traces where… |
| --- | --- |
| `service:service-a => service:service-b` | A service-a span is the **direct parent** of a service-b span, meaning service-a calls service-b itself. |
| `service:service-a -> service:service-b` | A service-a span is **anywhere upstream** of a service-b span. This includes direct calls and chains like a → x → y → b. |

So `=>` is a subset of `->`. Some examples:

| Call path in the trace | `=>` | `->` |
| --- | --- | --- |
| a → b | yes | yes |
| a → gateway → b | no | yes |
| b → a | no | no (direction matters) |
| a and b in separate traces | no | no |

**Entering it:** in Trace Explorer, you usually define each side as a span query (`a: service:service-a`, `b: service:service-b`) and type `a => b` in **Traces matching**. The one-line form is shorthand for that. If the search bar doesn't accept the inline form, use the labeled form.

**Why `=>` can miss direct calls:** the direct parent of service-b's server span is service-a's **client span**. That client span doesn't always carry `service:service-a`:

- Older tracers may tag it as `service:service-a-http-client` or `service:okhttp`.
- A proxy, gateway or mesh sidecar that's also traced (Envoy, nginx) sits in between.

In those cases `a => b` finds nothing even though a calls b. Use `->`, or widen `a` to something like `service:service-a*`.

**Neither arrow checks errors or latency.** Put those conditions in the span query itself:

```text
a: service:service-a
b: service:service-b status:error
Traces matching: a -> b
```

That finds traces where service-a is upstream of a failing service-b span.

**Trace headers must reach service-b** for any of this to work. If the context isn't propagated, a and b land in separate traces and neither operator matches.

### When service-b is external (not in Datadog)

If service-b isn't instrumented with Datadog, it sends no spans. Neither `=>` nor `->` can match, because no span has `service:service-b`. The only record of the call is **service-a's client span**, so you search for that single span. You don't need a trace query.

```text
service:service-a @span.kind:client @out.host:service-b.example.com
```

Depending on the tracer, the host is in one of these attributes:

| Attribute | Example |
| --- | --- |
| `@out.host` | `@out.host:api.service-b.com` |
| `@peer.hostname` | `@peer.hostname:api.service-b.com` |
| `@peer.service` | `@peer.service:service-b` (if set, or defaulted from the host) |
| `@http.url` | `@http.url:*service-b.com*` |

**Inferred services:** Datadog builds an inferred service from these peer tags. service-b then shows up in the service map and Software Catalog as a dependency of service-a, with request, error and latency metrics. But it's an entity built from service-a's spans, not a service with spans of its own. In Trace Explorer, filter on the peer tag, not on `service:`.

| You get | You don't get |
| --- | --- |
| Call count, latency and status codes, as service-a saw them | What happened inside service-b |
| Errors: timeouts, connection refused, 5xx (`@error.type`, `@error.message`) | Network time vs. service-b's processing time |
| The upstream path to the call | Anything downstream of service-b |

The call is the last hop in the trace, so trace queries still show how requests reach it:

```text
a: service:frontend
b: service:service-a @out.host:service-b.example.com status:error
Traces matching: a -> b
```

This finds frontend requests that ended in a failed call to the external service.

**If service-b is traced by a different system** (its own APM, or a vendor's tracing), the trace can only continue if both sides use the same trace headers, such as W3C `traceparent`. Even then, service-b's spans live in that other system, not in Datadog.

## Sources

- [Trace Explorer query syntax](https://docs.datadoghq.com/tracing/trace_explorer/query_syntax/)
- [Trace queries](https://docs.datadoghq.com/tracing/trace_explorer/trace_queries/)
- [Span tags & attributes](https://docs.datadoghq.com/tracing/trace_explorer/span_tags_attributes/)
- [Trace view](https://docs.datadoghq.com/tracing/trace_explorer/trace_view/)

Exact attribute names vary by tracing library and version. Check the facet panel for the names your spans carry.
