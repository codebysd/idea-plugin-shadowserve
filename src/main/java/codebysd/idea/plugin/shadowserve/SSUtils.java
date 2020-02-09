package codebysd.idea.plugin.shadowserve;

import org.apache.http.client.utils.URIBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Utility functions.
 */
public class SSUtils {

    /**
     * Check if value is null, empty or blank
     *
     * @param val value
     * @return True if nil
     */
    public static boolean isNil(String val) {
        return val == null || val.isEmpty() || val.isBlank();
    }

    /**
     * Join two or more URI path segments, with forward slash as separator.
     *
     * @param parts URI path segments.
     * @return Joined path. Leading and trailing slash is retained.
     */
    public static String pathJoin(String... parts) {
        if (parts == null) {
            return "";
        }
        return Arrays.stream(parts)
                .filter(s -> !SSUtils.isNil(s))
                .collect(Collectors.joining("/"))
                .replaceAll("\\\\", "/")
                .replaceAll("/{2,}", "/");
    }

    /**
     * Edit a URI using an editor.
     *
     * @param uri    Original URI
     * @param editor Editor function.
     * @return Edited URI.
     * @throws IOException URI building exception.
     */
    public static URI editURI(URI uri, Consumer<URIBuilder> editor) throws IOException {
        final URIBuilder builder = new URIBuilder(uri);
        editor.accept(builder);
        try {
            return builder.build();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    /**
     * Copy data from input stream to output stream
     *
     * @param from Source stream
     * @param to   Target stream
     * @throws IOException Read/Write error
     */
    public static void IOCopy(InputStream from, OutputStream to) throws IOException {
        int len;
        byte[] buff = new byte[4096];
        do {
            if (Thread.interrupted()) {
                throw new IOException("Thread interrupted");
            }
            len = from.read(buff);
            if (len > 0) {
                to.write(buff, 0, len);
            }
        } while (len > 0);
    }

    /**
     * Get contents of given input stream as a string
     *
     * @param stream Input stream
     * @return String
     * @throws IOException Read/Write error
     */
    public static String readString(InputStream stream) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IOCopy(stream, bos);
        return bos.toString();
    }

    /**
     * Copy HTTP request headers from source map to destination consumer.
     * Nil keys and values are skipped.
     *
     * @param src  source header map
     * @param dest destination consumer.
     */
    public static void copyRequestHeaders(Map<String, List<String>> src, BiConsumer<String, String> dest) {
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
    public static void copyResponseHeaders(Map<String, List<String>> src, BiConsumer<String, List<String>> dest) {
        src.entrySet().stream()
                .filter(e -> !SSUtils.isNil(e.getKey()))
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .forEach(e -> dest.accept(e.getKey(), e.getValue()));
    }
}
