package codebysd.idea.plugin.shadowserve;

import com.sun.net.httpserver.HttpExchange;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

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
     * Get host header for forwarded web request
     *
     * @return host header
     */
    private String getHostHeader() {
        final String host = mWebURI.getHost();
        final int port = mWebURI.getPort();
        if (port == -1) {
            return host;
        } else {
            return String.join(":", host, Integer.toString(port));
        }
    }

    /**
     * Copy HTTP request headers from source map to destination consumer.
     * Nil keys and values are skipped.
     *
     * @param src  source header map
     * @param dest destination consumer.
     */
    private void copyRequestHeaders(Map<String, List<String>> src, BiConsumer<String, String> dest) {
        src.entrySet().stream()
                .filter(e -> !SSUtils.isNil(e.getKey()))
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .forEach(e -> dest.accept(e.getKey(), String.join(",", e.getValue())));
    }

    /**
     * Copy HTTP response headers from source map to destination consumer.
     *
     * @param src  source header map
     * @param dest destination consumer.
     */
    private void copyResponseHeaders(Map<String, List<String>> src, BiConsumer<String, List<String>> dest) {
        src.entrySet().stream()
                .filter(e -> !SSUtils.isNil(e.getKey()))
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .forEach(e -> dest.accept(e.getKey(), e.getValue()));
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
            // Do not follow redirects, return 302 responses as well.
            connection.setInstanceFollowRedirects(false);

            // set request method and headers
            connection.setRequestMethod(exchange.getRequestMethod());
            copyRequestHeaders(exchange.getRequestHeaders(), connection::setRequestProperty);

            // override host
            connection.setRequestProperty("Host", getHostHeader());

            // write request body if any
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                connection.setDoOutput(true);
                SSUtils.IOCopy(exchange.getRequestBody(), connection.getOutputStream());
            }

            // get response code (do request)
            int code = connection.getResponseCode();

            // copy headers
            copyResponseHeaders(connection.getHeaderFields(), exchange.getResponseHeaders()::put);

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
