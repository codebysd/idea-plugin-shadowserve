package codebysd.idea.plugin.shadowserve;

import com.intellij.openapi.vfs.VirtualFile;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import org.jetbrains.annotations.Nullable;

import javax.activation.MimetypesFileTypeMap;
import java.io.IOException;
import java.io.InputStream;

/**
 * Filters requests for the shadowed path, using files under local root directory.
 */
public class SSShadowFilter extends Filter {
    private final MimetypesFileTypeMap mFileTypeMap;
    private final String mShadowPath;
    private final VirtualFile mLocalRoot;
    private final String mIndexFileName;
    private final SSUILogger mUILogger;

    /**
     * Constructor
     *
     * @param shadowPath    Web resources path to shadow, i.e. serve from local files.
     * @param localRoot     Local files root directory.
     * @param indexFileName Optional, name of the index file if path resolves to a directory.
     * @param uiLogger      UI logger.
     */
    public SSShadowFilter(String shadowPath, VirtualFile localRoot, @Nullable String indexFileName, SSUILogger uiLogger) {
        mShadowPath = shadowPath;
        mLocalRoot = localRoot;
        mIndexFileName = indexFileName;
        mUILogger = uiLogger;
        mFileTypeMap = new MimetypesFileTypeMap();

        // add additional mime types for web resources
        try {
            final InputStream str = getClass().getResourceAsStream("/META-INF/mime.types");
            mFileTypeMap.addMimeTypes(SSUtils.readString(str));
        } catch (Exception e) {
            mUILogger.logCrash(e, "Unable to load mime.types resource");
        }
    }

    /**
     * Locate a local file for th given path.
     *
     * @param path path inside local root directory
     * @return Local file if resolved, or null.
     */
    private VirtualFile locateFile(String path) {
        // find relative file
        VirtualFile file;
        if (SSUtils.isNil(path) || "/".equals(path) || ".".equals(path)) {
            file = mLocalRoot;
        } else {
            file = mLocalRoot.findFileByRelativePath(path);
        }

        // file must exist
        if (file == null || !file.exists()) {
            return null;
        }

        // if resolved to a directory and index file name is specified, find index file in it
        if (file.isDirectory() && !SSUtils.isNil(mIndexFileName)) {
            file = file.findChild(mIndexFileName);
        }

        // file still not resolved
        if (file == null || !file.exists() || file.isDirectory()) {
            return null;
        }

        // resolved file
        return file;
    }

    /**
     * {@inheritDoc}
     * Serves local files for URL path if found
     */
    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        // request path
        final String path = exchange.getRequestURI().getPath();

        // skip if not shadowed
        if (!path.startsWith(mShadowPath)) {
            chain.doFilter(exchange);
            return;
        }

        // sub path
        final String subPath = path.substring(mShadowPath.length());

        // locate file in sub path
        final VirtualFile file = locateFile(subPath);

        // file not found
        if (file == null) {
            chain.doFilter(exchange);
            return;
        }

        // Log
        mUILogger.logStdOut("Shadowing\t%s\t⟶\t%s", exchange.getRequestURI().getPath(), file.getCanonicalPath());

        // add file specific headers
        exchange.getResponseHeaders().add("Content-Type", mFileTypeMap.getContentType(file.getName()));
        exchange.getResponseHeaders().add("Content-Length", Long.toString(file.getLength()));

        // signal browsers not to cache shadowed resources
        exchange.getResponseHeaders().add("Cache-Control", "no-cache");

        // write file content
        exchange.sendResponseHeaders(200, 0);
        try (InputStream is = file.getInputStream()) {
            SSUtils.IOCopy(is, exchange.getResponseBody());
        } finally {
            exchange.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String description() {
        return "Serves local files for URL path if found";
    }
}
