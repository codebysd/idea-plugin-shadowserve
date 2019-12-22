package codebysd.idea.plugin.shadowserve;

import com.sun.net.httpserver.HttpExchange;

/**
 * Optionally handles HTTP requests.
 */
@FunctionalInterface
public interface SSRequestHandler {
    /**
     * Handle HTTP request and return true. Or, return false to let request go to next handler in sequence.
     *
     * @param exchange HTTP exchange.
     * @return True if handled.
     * @throws Exception Request processing error.
     */
    boolean handle(HttpExchange exchange) throws Exception;
}
