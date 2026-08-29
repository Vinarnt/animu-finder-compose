/*
 * Dev-only CORS/media proxy for the wasm app.
 *
 * Adds a `GET|POST|HEAD|OPTIONS /proxy?url=<target>` endpoint to webpack-dev-server.
 * The server (running on the user's machine) fetches the target on the app's behalf
 * and returns it with permissive CORS headers. Because the request comes from a
 * residential IP instead of a datacenter, the anime CDNs do not block it the way they
 * block third-party CORS proxies (cors.sh, corsfix, allorigins, ...).
 *
 * HLS manifests are rewritten so every relative URI becomes an absolute
 * `/proxy?url=<absolute-target>` URL. This keeps segment/key resolution correct no
 * matter how the upstream playlist is structured (relative, root-relative or query
 * only), which a naive path-style proxy cannot guarantee.
 */
(function () {
    var CORS = {
        "access-control-allow-origin": "*",
        "access-control-allow-methods": "GET, POST, HEAD, OPTIONS",
        "access-control-allow-headers": "*",
        "access-control-expose-headers":
            "content-type, content-length, content-range, accept-ranges, range",
        "access-control-max-age": "86400",
    };

    function proxify(absTarget) {
        return "/proxy?url=" + encodeURIComponent(absTarget);
    }

    function rewriteManifest(text, baseTarget) {
        var base;
        try {
            base = new URL(baseTarget);
        } catch (e) {
            return text;
        }
        return text
            .split(/\r?\n/)
            .map(function (line) {
                var trimmed = line.trim();
                if (trimmed.charAt(0) === "#") {
                    return line.replace(/(URI\s*=\s*")([^"]+)(")/g, function (_, p1, p2, p3) {
                        var abs;
                        try {
                            abs = new URL(p2, base).toString();
                        } catch (e) {
                            return _;
                        }
                        return p1 + proxify(abs) + p3;
                    });
                }
                if (!trimmed) return line;
                var abs;
                try {
                    abs = new URL(trimmed, base).toString();
                } catch (e) {
                    return line;
                }
                return proxify(abs);
            })
            .join("\n");
    }

    function looksLikeManifest(target, contentType) {
        if (contentType && /mpegurl|m3u8/i.test(contentType)) return true;
        return /\.m3u8([?#]|$)/i.test(target) || /\/m3u8([?#]|$)/i.test(target);
    }

    function sendError(res, status, message) {
        res.writeHead(status, CORS);
        res.end(message || "");
    }

    function log(message) {
        process.stdout.write("[proxy] " + new Date().toISOString() + " " + message + "\n");
    }

    function pump(res, reader) {
        (function read() {
            reader
                .read()
                .then(function (r) {
                    if (r.done) {
                        res.end();
                        return;
                    }
                    res.write(Buffer.from(r.value));
                    read();
                })
                .catch(function () {
                    res.end();
                });
        })();
    }

    function middleware(req, res, next) {
        var url;
        try {
            url = new URL(req.url, "http://localhost");
        } catch (e) {
            return next();
        }
        if (url.pathname !== "/proxy") return next();

        var target = url.searchParams.get("url");
        if (!target || !/^https?:\/\//i.test(target)) {
            log("REQ " + req.method + " INVALID target: " + (target || "(none)"));
            return sendError(res, 400, "missing/invalid url");
        }

        // Wrap the response so every completed request is logged to the dev server
        // stdout (gradle log) with its status, byte count and duration.
        var startedAt = Date.now();
        var resStatus = 0;
        var bytesSent = 0;
        var logged = false;
        var writeHead = res.writeHead.bind(res);
        var write = res.write.bind(res);
        var end = res.end.bind(res);
        res.writeHead = function () {
            resStatus = arguments[0] || resStatus;
            return writeHead.apply(res, arguments);
        };
        res.write = function () {
            if (arguments[0]) bytesSent += Buffer.byteLength(arguments[0]);
            return write.apply(res, arguments);
        };
        res.end = function () {
            if (arguments[0]) bytesSent += Buffer.byteLength(arguments[0]);
            if (!logged) {
                logged = true;
                log(
                    "RES " + req.method + " -> " + target +
                    " | " + (resStatus || 0) +
                    " | " + bytesSent + " B" +
                    " | " + (Date.now() - startedAt) + " ms"
                );
            }
            return end.apply(res, arguments);
        };

        log("REQ " + req.method + " " + target);

        if (req.method === "OPTIONS") {
            res.writeHead(204, CORS);
            res.end();
            return;
        }

        var upstreamHeaders = {};
        [
            "accept",
            "accept-language",
            "user-agent",
            "range",
            "content-type",
            "referer",
            "origin",
            "cookie",
            "x-requested-with",
        ].forEach(function (h) {
            if (req.headers[h] !== undefined) upstreamHeaders[h] = req.headers[h];
        });

        function onReady(body) {
            fetch(target, {
                method: req.method,
                headers: upstreamHeaders,
                body: body,
                redirect: "follow",
            })
                .then(function (resp) {
                    var ctype = resp.headers.get("content-type") || "";
                    if (resp.ok && looksLikeManifest(target, ctype)) {
                        return resp.text().then(function (text) {
                            res.writeHead(resp.status, Object.assign({}, CORS, {
                                "content-type": ctype || "application/vnd.apple.mpegurl",
                                "cache-control": "no-store",
                            }));
                            res.end(rewriteManifest(text, target));
                        });
                    }

                    var headers = Object.assign({}, CORS, {
                        "content-type": ctype || "application/octet-stream",
                        "accept-ranges": "bytes",
                    });
                    var contentLength = resp.headers.get("content-length");
                    if (contentLength) headers["content-length"] = contentLength;
                    var contentRange = resp.headers.get("content-range");
                    if (contentRange) headers["content-range"] = contentRange;
                    res.writeHead(resp.status, headers);

                    if (req.method === "HEAD" || !resp.body) {
                        res.end();
                        return;
                    }
                    pump(res, resp.body.getReader());
                })
                .catch(function (e) {
                    sendError(res, 502, String((e && e.message) || e));
                });
        }

        if (req.method === "POST") {
            var chunks = [];
            req.on("data", function (c) {
                chunks.push(c);
            });
            req.on("end", function () {
                onReady(Buffer.concat(chunks));
            });
            req.on("error", function () {
                sendError(res, 400, "request body error");
            });
        } else {
            onReady(undefined);
        }
    }

    var devServer = (config.devServer = config.devServer || {});
    var original = devServer.setupMiddlewares;
    devServer.setupMiddlewares = function (middlewares, devServer) {
        middlewares.unshift(middleware);
        if (typeof original === "function") return original(middlewares, devServer);
        return middlewares;
    };
})();