package codebysd.idea.plugin.shadowserve;

import com.intellij.openapi.vfs.VirtualFile;
import com.sun.net.httpserver.HttpExchange;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

/**
 * Handle requests for the shadowed path, using files under local root directory.
 */
public class SSRequestHandlerShadow implements SSRequestHandler {
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
    public SSRequestHandlerShadow(String shadowPath, VirtualFile localRoot, @Nullable String indexFileName, SSUILogger uiLogger) {
        mShadowPath = shadowPath;
        mLocalRoot = localRoot;
        mIndexFileName = indexFileName;
        mUILogger = uiLogger;
    }

    /**
     * {@inheritDoc}
     * This implementation serves a local file if request path is shadowed.
     */
    @Override
    public boolean handle(HttpExchange exchange) throws Exception {
        // request path
        final String path = exchange.getRequestURI().getPath();

        // skip if not shadowed
        if (!path.startsWith(mShadowPath)) {
            return false;
        }

        // locate file in sub path
        final VirtualFile file = locateFile(path.substring(mShadowPath.length()));

        // not file found
        if (file == null) {
            return false;
        }

        // Log
        mUILogger.logStdOut("Shadowing\t%s\t->\t%s", exchange.getRequestURI().getPath(), file.getCanonicalPath());

        // add file specific headers
        exchange.getResponseHeaders().add("Content-Type", file.getFileType().getName());
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

        // done
        return true;
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
}
