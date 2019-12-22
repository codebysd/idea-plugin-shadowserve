package codebysd.idea.plugin.shadowserve;

import com.sun.net.httpserver.HttpExchange;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;

/**
 * Handles web requests to resource server.
 */
public class SSRequestHandlerWeb implements SSRequestHandler {
    private final URI mWebURI;
    private final SSUILogger mUILogger;

    /**
     * Constructor
     *
     * @param webURI   Resource server URI
     * @param uiLogger UI logger.
     */
    public SSRequestHandlerWeb(URI webURI, SSUILogger uiLogger) {
        mWebURI = webURI;
        mUILogger = uiLogger;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation proxies requests to resource server.
     */
    @Override
    public boolean handle(HttpExchange exchange) throws Exception {
        // modify request URI
        final URI uri = SSUtils.editURI(exchange.getRequestURI(), ub -> {
            ub.setScheme(mWebURI.getScheme());
            ub.setHost(mWebURI.getHost());
            ub.setPort(mWebURI.getPort());
            ub.setPath(SSUtils.pathJoin(mWebURI.getPath(), exchange.getRequestURI().getPath()));
        });

        // log
        mUILogger.logStdOut("Forwarding\t%s\t->\t%s", exchange.getRequestURI().getPath(), uri.toString());

        // Create an HTTP connection
        final HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();

        try {
            // set request method and headers
            connection.setRequestMethod(exchange.getRequestMethod());
            exchange.getRequestHeaders().forEach((k, vs) -> connection.setRequestProperty(k, String.join(",", vs)));

            // override host
            connection.setRequestProperty("host", mWebURI.getHost());

            // write request body if any
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                connection.setDoOutput(true);
                SSUtils.IOCopy(exchange.getRequestBody(), connection.getOutputStream());
            }

            // get response code (do request)
            int code = connection.getResponseCode();

            // copy headers
            connection.getHeaderFields().forEach((k, vs) -> {
                // raw connection headers may have null keys
                if (k != null && vs != null && !vs.isEmpty()) {
                    exchange.getResponseHeaders().put(k, vs);
                }
            });

            // write response body
            exchange.sendResponseHeaders(code, connection.getContentLengthLong());

            // get response body
            InputStream resStr = connection.getErrorStream();
            if (resStr == null) {
                resStr = connection.getInputStream();
            }

            // copy response data if any
            if (resStr != null) {
                SSUtils.IOCopy(resStr, exchange.getResponseBody());
            }
        } finally {
            // close resources
            exchange.close();
            connection.disconnect();
        }

        return true;
    }
}
