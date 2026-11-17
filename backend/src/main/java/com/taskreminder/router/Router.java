package com.taskreminder.router;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.taskreminder.util.ResponseUtil;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight HTTP router that supports path parameters like /api/tasks/{id}.
 *
 * Usage:
 *   Router router = new Router();
 *   router.get("/api/tasks",       TaskController::list);
 *   router.get("/api/tasks/{id}",  TaskController::getById);
 *   router.post("/api/tasks",      TaskController::create);
 *
 *   server.createContext("/api", router);
 */
public class Router implements HttpHandler {

    public interface RouteHandler {
        void handle(HttpExchange exchange, Map<String, String> pathParams) throws IOException;
    }

    private record Route(String method, Pattern pattern, List<String> paramNames, RouteHandler handler) {}

    private final List<Route> routes = new ArrayList<>();

    // ─── Registration ───────────────────────────────────────────────

    public void get(String path, RouteHandler handler)    { add("GET",    path, handler); }
    public void post(String path, RouteHandler handler)   { add("POST",   path, handler); }
    public void put(String path, RouteHandler handler)    { add("PUT",    path, handler); }
    public void patch(String path, RouteHandler handler)  { add("PATCH",  path, handler); }
    public void delete(String path, RouteHandler handler) { add("DELETE", path, handler); }

    private void add(String method, String pathTemplate, RouteHandler handler) {
        List<String> paramNames = new ArrayList<>();
        // Replace {param} placeholders with regex capture groups
        Matcher m = Pattern.compile("\\{([^/]+)}").matcher(pathTemplate);
        StringBuffer sb = new StringBuffer("^");
        while (m.find()) {
            paramNames.add(m.group(1));
            m.appendReplacement(sb, "([^/]+)");
        }
        m.appendTail(sb);
        sb.append("$");
        routes.add(new Route(method, Pattern.compile(sb.toString()), paramNames, handler));
    }

    // ─── Dispatching ───────────────────────────────────────────────

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        String path   = exchange.getRequestURI().getPath();

        // Handle CORS preflight
        if ("OPTIONS".equals(method)) {
            ResponseUtil.sendOptions(exchange);
            return;
        }

        for (Route route : routes) {
            if (!route.method().equals(method)) continue;
            Matcher m = route.pattern().matcher(path);
            if (!m.matches()) continue;

            Map<String, String> pathParams = new HashMap<>();
            for (int i = 0; i < route.paramNames().size(); i++) {
                pathParams.put(route.paramNames().get(i), m.group(i + 1));
            }

            route.handler().handle(exchange, pathParams);
            return;
        }

        ResponseUtil.sendError(exchange, 404, "Route not found: " + method + " " + path);
    }
}
