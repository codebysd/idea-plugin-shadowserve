package codebysd.idea.plugin.shadowserve;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

/**
 * Allows multiple {@link SSRequestHandler}'s to handle request.
 * If any {@link SSRequestHandler#handle(HttpExchange)} returns true, further stack is ignored.
 */
public class SSRequestHandlerStack implements HttpHandler {
    private final SSRequestHandler[] mHandlers;
    private final SSUILogger mUILogger;

    /**
     * Constructor
     *
     * @param handlers Request handlers.
     */
    public SSRequestHandlerStack(SSUILogger uiLogger, SSRequestHandler... handlers) {
        mUILogger = uiLogger;
        mHandlers = handlers == null ? new SSRequestHandler[0] : handlers;
    }

    /**
     * {@inheritDoc}
     * This implementation calls handlers in stack until one fulfills.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // iterate the stack until a handler returns true
        for (SSRequestHandler handler : mHandlers) {
            try {
                if (handler.handle(exchange)) {
                    return;
                }
            } catch (Exception e) {
                // log errors
                mUILogger.logCrash(e, "Error serving request: %s", exchange.getRequestURI().toString());
                // wrap errors
                throw new IOException(e);
            }
        }

        // default response if not handled
        exchange.sendResponseHeaders(501, 0);
        exchange.getResponseBody().write("No request handler available.".getBytes());
        exchange.close();
    }
}
