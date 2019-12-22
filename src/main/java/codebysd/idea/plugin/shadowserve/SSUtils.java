package codebysd.idea.plugin.shadowserve;

import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
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
     * @throws URISyntaxException URI building exception.
     */
    public static URI editURI(URI uri, Consumer<URIBuilder> editor) throws URISyntaxException {
        final URIBuilder builder = new URIBuilder(uri);
        editor.accept(builder);
        return builder.build();
    }

    /**
     * Copy data from input stream to output stream
     *
     * @param from Source stream
     * @param to   Target stream
     * @throws IOException          Read/Write error
     * @throws InterruptedException If thread interrupted during copy
     */
    public static void IOCopy(InputStream from, OutputStream to) throws IOException, InterruptedException {
        int len;
        byte[] buff = new byte[4096];
        do {
            if (Thread.interrupted()) {
                throw new InterruptedException("Thread interrupted");
            }
            len = from.read(buff);
            if (len > 0) {
                to.write(buff, 0, len);
            }
        } while (len > 0);
    }
}
