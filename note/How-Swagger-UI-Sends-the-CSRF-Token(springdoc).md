# How Swagger UI Sends the CSRF Token (springdoc)

Sep 26, 2026 · @Wei Li

## Summary

Swagger UI doesn't handle CSRF on its own. springdoc adds the support by rewriting Swagger UI's startup script (`swagger-initializer.js`) when it serves it, and injecting a `requestInterceptor`. The interceptor reads the token from the cookie named by `springdoc.swagger-ui.csrf.cookie-name` and copies it into a request header. The browser also sends the cookie, and Spring Security compares the two values. This is the double-submit cookie pattern.

This was checked against springdoc's source, the `addCSRF` method in `AbstractSwaggerIndexTransformer.java`.

## The springdoc CSRF properties

Nothing below takes effect unless `csrf.enabled` is `true` ([springdoc properties](https://springdoc.org/properties.html)).

| Property | Default | What it does |
| --- | --- | --- |
| `springdoc.swagger-ui.csrf.enabled` | `false` | Turns CSRF support on |
| `springdoc.swagger-ui.csrf.cookie-name` | `XSRF-TOKEN` | Name of the cookie the token is read from |
| `springdoc.swagger-ui.csrf.header-name` | `X-XSRF-TOKEN` | Name of the request header the token is sent in |
| `springdoc.swagger-ui.csrf.use-local-storage` | `false` | Read the token from Local Storage instead of a cookie |
| `springdoc.swagger-ui.csrf.use-session-storage` | `false` | Read the token from Session Storage instead of a cookie |

If both storage options are `false`, springdoc uses the cookie.

## The script springdoc injects

With `csrf.enabled=true` and neither storage option set, springdoc inserts this before `presets:` in the Swagger UI config. The cookie and header names come from the two properties above.

```js
requestInterceptor: (request) => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; XSRF-TOKEN=`);            // <- cookie-name
    const currentURL = new URL(document.URL);
    const requestURL = new URL(request.url, document.location.origin);
    const isSameOrigin = (currentURL.protocol === requestURL.protocol
                          && currentURL.host === requestURL.host);
    if (isSameOrigin && parts.length === 2)
        request.headers['X-XSRF-TOKEN'] = parts.pop().split(';').shift();  // <- header-name
    return request;
},
```

The storage variants (`addCSRFLocalStorage`, `addCSRFSessionStorage`) inject the same interceptor. They read the value from `window.localStorage` or `window.sessionStorage` instead of `document.cookie`.

## What happens on a POST (Try it out, then Execute)

1. Swagger UI runs `requestInterceptor` before every request it sends, including GET, POST, PUT and DELETE. There is no check for the HTTP method.
2. The script reads `document.cookie`, puts ` ;  ` in front of it, and splits on `; <cookie-name>=`. If exactly one match is found, it takes the text up to the next `;` as the token.
3. It only adds the header if the request goes to the same protocol and host as the Swagger UI page. This keeps the token from being sent to other servers listed in your OpenAPI `servers`.
4. It copies the token into the header, for example `X-XSRF-TOKEN: <token>`.
5. The browser also sends the `XSRF-TOKEN` cookie automatically. Spring Security then checks the header value against the cookie.

If the cookie isn't there or isn't readable, the header is skipped without any error and the POST fails with a **403**.

## Where the interceptor runs

The interceptor runs entirely in the browser. springdoc only generates the code on the server.

- **Server side (springdoc):** when the browser requests `/swagger-ui/swagger-initializer.js`, `AbstractSwaggerIndexTransformer` builds the `requestInterceptor` code as a string and inserts it into the file. The cookie and header names from your properties are written directly into that code.
- **Browser side (Swagger UI):** Swagger UI hands `requestInterceptor` to its HTTP layer (swagger-client). Before each `fetch`, swagger-client calls the interceptor with the request object. The interceptor reads `document.cookie`, sets the header, and returns the modified request, which is what actually goes out.

What this means in practice:

- **Only requests sent by Swagger UI get the header.** That covers Try it out / Execute calls and Swagger UI's own fetch of `/v3/api-docs`. Normal page loads, and requests from other scripts or from tools like curl or Postman, don't pass through it.
- **The header on GET requests does no harm.** By default Spring Security doesn't check CSRF on GET, HEAD, OPTIONS or TRACE, so it ignores the header there. It only matters for POST, PUT, PATCH and DELETE.
- **The cookie has to be readable by the page's JavaScript.** The code only sees cookies that `document.cookie` exposes. The cookie must not be HttpOnly, and its path and domain must cover the Swagger UI page.

## Server-side requirements (Spring Security)

- **JavaScript must be able to read the cookie.** Use `CookieCsrfTokenRepository.withHttpOnlyFalse()`. An HttpOnly cookie can't be seen through `document.cookie`.
- **The names have to match.** `cookie-name` and `header-name` must match what the repository uses. `XSRF-TOKEN` and `X-XSRF-TOKEN` are Spring's defaults.
- **Spring Security 6 or later: the cookie may not exist yet.** The token is created lazily, so the cookie may not be set before the first POST. You may need a filter that loads the token on every request.
- **Spring Security 6 or later: the raw cookie value may be rejected.** The default `XorCsrfTokenRequestAttributeHandler` expects a masked token in the header. Since the script sends the raw cookie value, use `CsrfTokenRequestAttributeHandler`, or `csrf.spa()` in Spring Security 7.0 and later.

The two Spring Security 6 points were not tested against a running app.

```java
http.csrf(csrf -> csrf
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
    .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()));
```

**Note for Spring Security 7.0 and later (Spring Boot 4.0):** replace the configuration above with `csrf.spa()`. It sets up a cookie-based token repository and a request handler that accepts the raw token value, which is what the springdoc interceptor sends ([CsrfConfigurer.spa() Javadoc](https://docs.spring.io/spring-security/reference/api/java/org/springframework/security/config/annotation/web/configurers/CsrfConfigurer.html), `@since 7.0`). Keep the default `XSRF-TOKEN` / `X-XSRF-TOKEN` names in springdoc so they match.

```java
http.csrf(csrf -> csrf.spa());
```

```properties
springdoc.swagger-ui.csrf.enabled=true
# springdoc.swagger-ui.csrf.cookie-name=XSRF-TOKEN
# springdoc.swagger-ui.csrf.header-name=X-XSRF-TOKEN
```

## How to verify

- Open DevTools, go to Network, run a POST in Swagger UI, and look for `X-XSRF-TOKEN` in the request headers.
- Open `/swagger-ui/swagger-initializer.js` to see the modified script with the injected `requestInterceptor`.

## Sources

- [springdoc properties](https://springdoc.org/properties.html)
- [AbstractSwaggerIndexTransformer.java](https://github.com/springdoc/springdoc-openapi/blob/main/springdoc-openapi-starter-common/src/main/java/org/springdoc/ui/AbstractSwaggerIndexTransformer.java)
