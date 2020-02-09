package codebysd.idea.plugin.shadowserve;

import com.intellij.execution.ui.ExecutionConsole;

import java.util.IllegalFormatException;

/**
 * Provides an {@link ExecutionConsole} for the configuration log display.
 * Also, provide methods to print logs to UI.
 */
public interface SSUILogger {
    /**
     * A safe version of {@link String#format(String, Object...)}.
     * In case of formatting error, the un-formatted message is returned.
     *
     * @param msg  Message
     * @param args Format arguments.
     * @return Formatted string, or, message if cannot be formatted.
     */
    default String formatSafe(String msg, Object... args) {
        try {
            return String.format(msg, args);
        } catch (IllegalFormatException e) {
            return msg;
        }
    }

    /**
     * Get UI console.
     *
     * @return UI console.
     */
    ExecutionConsole getExecutionConsole();

    /**
     * Log a system level message.
     *
     * @param msg  message
     * @param args format arguments.
     */
    void logSystem(String msg, Object... args);

    /**
     * Log a standard output message.
     *
     * @param msg  message
     * @param args format arguments.
     */
    void logStdOut(String msg, Object... args);

    /**
     * Log an error.
     *
     * @param e    exception.
     * @param msg  error message
     * @param args format arguments.
     */
    void logCrash(Exception e, String msg, Object... args);
}
