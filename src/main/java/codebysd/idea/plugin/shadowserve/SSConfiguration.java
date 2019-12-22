package codebysd.idea.plugin.shadowserve;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.sun.net.httpserver.HttpHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Run configuration for the plugin.
 */
public class SSConfiguration extends RunConfigurationBase<SSConfiguration> {
    private final SSSettingsStore mSettingsStore;

    /**
     * Constructor
     *
     * @param project Project reference
     * @param factory Configuration Factory
     * @param name    Configuration name.
     */
    public SSConfiguration(Project project, SSConfigurationFactory factory, String name) {
        super(project, factory, name);
        mSettingsStore = new SSSettingsStore(project);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new SSSettingsEditor(getProject(), mSettingsStore);
    }

    /**
     * Create HTTP handler for the local server.
     *
     * @param uiLogger UI logger instance.
     * @return HTTP handler.
     */
    private HttpHandler createRequestHandler(SSUILogger uiLogger) {
        // shadow handler
        final SSRequestHandlerShadow handlerShadow = new SSRequestHandlerShadow(
                mSettingsStore.getShadowPath(),
                mSettingsStore.getLocalRoot(),
                SSConstants.Defaults.INDEX_FILE, uiLogger);

        // web handler
        final SSRequestHandlerWeb handlerWeb = new SSRequestHandlerWeb(mSettingsStore.getWebURL(), uiLogger);

        // handler stack
        return new SSRequestHandlerStack(uiLogger, handlerShadow, handlerWeb);
    }

    /**
     * Create the local server.
     *
     * @param handler HTTP handler
     * @return Local server.
     */
    private SSLocalServer createLocalServer(HttpHandler handler) {
        return new SSLocalServer(SSConstants.Defaults.LOCAL_HOST, mSettingsStore.getLocalPort(), handler);
    }

    /**
     * Create UI logger.
     *
     * @return UI logger.
     */
    private SSUILogger createUILogger() {
        return new SSUILoggerDefault(getProject());
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {

        // return run profile state
        return (exec, runner) -> {

            // create a UI logger
            final SSUILogger uiLogger = createUILogger();

            // request handler
            final HttpHandler httpHandler = createRequestHandler(uiLogger);

            // local server
            final SSLocalServer localServer = createLocalServer(httpHandler);

            try {
                // start local server
                localServer.start();

                // return execution result
                uiLogger.logStartup("Local Server started @ %s", localServer.getAddress());
                uiLogger.logStartup("Shadow root:\t%s", mSettingsStore.getLocalRoot().getCanonicalPath());
                uiLogger.logStartup("Shadow Path:\t%s", mSettingsStore.getShadowPath());
                uiLogger.logStartup("Shadow Over:\t%s", mSettingsStore.getWebURL().toString());
                return new DefaultExecutionResult(uiLogger.getExecutionConsole(), localServer);
            } catch (IOException e) {
                // Log and throw error
                uiLogger.logCrash(e, "Error running shadow server");
                throw new ExecutionException("Failed to start Local Server", e);
            }
        };
    }

}
