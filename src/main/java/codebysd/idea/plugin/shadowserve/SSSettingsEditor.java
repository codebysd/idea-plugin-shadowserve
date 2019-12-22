package codebysd.idea.plugin.shadowserve;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Handles UI and editing of plugin settings.
 */
public class SSSettingsEditor extends SettingsEditor<SSConfiguration> {
    private final SSSettingsUI mSettingsForm;

    /**
     * Constructor.
     *
     * @param project Project reference
     * @param store   Settings store.
     */
    public SSSettingsEditor(Project project, SSSettingsStore store) {
        mSettingsForm = new SSSettingsUI(store, project);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void resetEditorFrom(@NotNull SSConfiguration conf) {
        mSettingsForm.loadData();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void applyEditorTo(@NotNull SSConfiguration conf) throws ConfigurationException {
        mSettingsForm.applyData();
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    protected JComponent createEditor() {
        // return settings UI panel
        return mSettingsForm.getRootPanel();
    }
}
