package codebysd.idea.plugin.shadowserve;

import com.intellij.execution.process.ProcessHandler;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.spi.HttpServerProvider;
import org.apache.commons.io.output.NullOutputStream;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Local HTTP server implementation.
 */
public class SSLocalServer extends ProcessHandler {
    private static final int NUM_THREADS = 5;
    private final String mHost;
    private final int mPort;
    private final List<Filter> mFilters;
    private final HttpHandler mHandler;
    private final AtomicBoolean mStarted;
    private HttpServer mServer;
    private ExecutorService mExecutor;

    /**
     * Constructor
     *
     * @param host    Hostname to bind to
     * @param port    Port to listen on.
     * @param filters Request filters.
     * @param handler Request handler.
     */
    public SSLocalServer(String host, int port, List<Filter> filters, HttpHandler handler) {
        mHost = host;
        mPort = port;
        mFilters = filters;
        mHandler = handler;
        mStarted = new AtomicBoolean();
    }

    /**
     * Start Server
     *
     * @throws IOException Startup error
     */
    public void start() throws IOException {
        // start if not started
        if (mStarted.compareAndSet(false, true)) {
            // create HTTP server
            final InetSocketAddress address = new InetSocketAddress(mHost, mPort);
            mServer = HttpServerProvider.provider().createHttpServer(address, 0);

            // init context, set handler and filters
            final HttpContext context = mServer.createContext("/", mHandler);
            context.getFilters().addAll(mFilters);

            // set executor and start
            mExecutor = Executors.newWorkStealingPool(NUM_THREADS);
            mServer.setExecutor(mExecutor);
            mServer.start();
            startNotify();
        }
    }

    /**
     * Get HTTP address of server.
     *
     * @return Server address, or nul if server not running.
     */
    public String getAddress() {
        if (mServer != null) {
            return String.format("http:/%s", mServer.getAddress().toString());
        } else {
            return null;
        }
    }

    /**
     * Stop the server.
     */
    private void stop() {
        // stop if not stopped
        if (mStarted.compareAndSet(true, false)) {
            // stop server
            mServer.stop(0);
            mServer = null;

            // stop executor
            mExecutor.shutdownNow();
            mExecutor = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void destroyProcessImpl() {
        stop();
        notifyProcessTerminated(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void detachProcessImpl() {
        stop();
        notifyProcessDetached();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean detachIsDefault() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public OutputStream getProcessInput() {
        return new NullOutputStream();
    }

}
