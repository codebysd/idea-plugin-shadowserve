package codebysd.idea.plugin.shadowserve;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;

/**
 * Handles web requests to resource server.
 */
public class SSWebHandler implements HttpHandler {
    private final URI mWebURI;
    private final SSUILogger mUILogger;

    /**
     * Constructor
     *
     * @param webURI   Resource server URI
     * @param uiLogger UI logger.
     */
    public SSWebHandler(URI webURI, SSUILogger uiLogger) {
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
     * {@inheritDoc}
     * <p>
     * This implementation proxies requests to resource server.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // modify request URI
        final URI uri = SSUtils.editURI(exchange.getRequestURI(), ub -> {
            ub.setScheme(mWebURI.getScheme());
            ub.setHost(mWebURI.getHost());
            ub.setPort(mWebURI.getPort());
            ub.setPath(SSUtils.pathJoin(mWebURI.getPath(), exchange.getRequestURI().getPath()));
        });

        // log
        mUILogger.logStdOut("Forwarding\t%s\t⟶\t%s", exchange.getRequestURI().getPath(), uri.toString());

        // Create an HTTP connection
        final HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();

        try {
            // Do not follow redirects, return 302 responses as well.
            connection.setInstanceFollowRedirects(false);

            // set request method and headers
            connection.setRequestMethod(exchange.getRequestMethod());
            SSUtils.copyRequestHeaders(exchange.getRequestHeaders(), connection::setRequestProperty);

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
            SSUtils.copyResponseHeaders(connection.getHeaderFields(), exchange.getResponseHeaders()::put);

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
    }
}
