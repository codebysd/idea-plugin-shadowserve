package codebysd.idea.plugin.shadowserve;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Filters web requests with results from a cache.
 */
public class SSCacheFilter extends Filter {
    private final SSCache mCache;
    private final SSUILogger mUILogger;

    /**
     * Constructor
     *
     * @param mCache    byte cache.
     * @param mUILogger UI logger.
     */
    public SSCacheFilter(SSCache mCache, SSUILogger mUILogger) {
        this.mCache = mCache;
        this.mUILogger = mUILogger;
    }

    /**
     * {@inheritDoc}
     * Caches web responses and return future responses using cache
     */
    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        // treat URI as key
        final String key = exchange.getRequestURI().toString();

        // get cached data if any
        final byte[] cached = mCache.get(key);

        if (cached == null) {
            // No cache, continue with web request, swapping output streams
            final OutputStream respBody = exchange.getResponseBody();
            final ByteArrayOutputStream captureStream = new ByteArrayOutputStream();

            // pass capture stream as response body
            exchange.setStreams(exchange.getRequestBody(), captureStream);

            // execute exchange
            chain.doFilter(exchange);

            // create web response from exchange headers and captured response body
            final WebResponse res = new WebResponse(exchange.getResponseCode(),
                    exchange.getResponseHeaders(), captureStream.toByteArray());

            // save to cache
            final boolean ok = mCache.put(key, res.toBytes());
            if (!ok) {
                mUILogger.logSystem("WARNING: Cache is full.");
            }

            // flush response body to original output stream
            try {
                respBody.write(res.body);
            } finally {
                respBody.close();
            }
        } else {
            // Cache hit, just send cached data
            mUILogger.logStdOut("Forwarding\t%s\t⟶\tCached", exchange.getRequestURI().getPath());
            WebResponse response = WebResponse.fromBytes(cached);

            // set headers and response body
            SSUtils.copyResponseHeaders(response.headers, exchange.getResponseHeaders()::put);
            try {
                exchange.sendResponseHeaders(response.statusCode, response.body.length);
                exchange.getResponseBody().write(response.body);
            } finally {
                exchange.close();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String description() {
        return "Caches web responses and return future responses using cache";
    }

    /**
     * A serializable object to hold response headers and body.
     */
    private static class WebResponse implements Serializable {
        private final int statusCode;
        private final Map<String, List<String>> headers;
        private final byte[] body;

        /**
         * Constructor
         *
         * @param statusCode Response status code
         * @param headers    Response headers
         * @param body       Response body
         */
        public WebResponse(int statusCode, Headers headers, byte[] body) {
            this.statusCode = statusCode;
            this.headers = new HashMap<>(headers);// Headers class is not serializable
            this.body = body;
        }

        /**
         * De-serialize from byte data.
         *
         * @param data bytes
         * @return {@link WebResponse} instance
         * @throws IOException De-serialization error.
         */
        public static WebResponse fromBytes(byte[] data) throws IOException {
            try {
                final ObjectInputStream objInStr = new ObjectInputStream(new ByteArrayInputStream(data));
                return (WebResponse) objInStr.readObject();
            } catch (ClassNotFoundException e) {
                throw new IOException(e);
            }
        }

        /**
         * Serialize current instance to bytes.
         *
         * @return bytes
         * @throws IOException Serialization error.
         */
        public byte[] toBytes() throws IOException {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final ObjectOutputStream objOutStr = new ObjectOutputStream(bos);
            objOutStr.writeObject(this);
            return bos.toByteArray();
        }
    }
}
