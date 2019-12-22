package codebysd.idea.plugin.shadowserve;

import com.intellij.build.BuildTextConsoleView;
import com.intellij.build.events.impl.FailureImpl;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.openapi.project.Project;

/**
 * Default implementation of {@link SSUILogger}.
 */
public class SSUILoggerDefault implements SSUILogger {
    private final BuildTextConsoleView mConsole;

    /**
     * Constructor
     *
     * @param project Project reference.
     */
    public SSUILoggerDefault(Project project) {
        mConsole = new BuildTextConsoleView(project, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecutionConsole getExecutionConsole() {
        return mConsole;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logStartup(String msg, Object... args) {
        mConsole.append(formatSafe(msg, args) + System.lineSeparator(), false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logStdOut(String msg, Object... args) {
        mConsole.append(formatSafe(msg, args) + System.lineSeparator(), true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logCrash(Exception e, String msg, Object... args) {
        mConsole.append(new FailureImpl(formatSafe(msg, args), e));
    }
}
