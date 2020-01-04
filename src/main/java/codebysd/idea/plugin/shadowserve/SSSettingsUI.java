package codebysd.idea.plugin.shadowserve;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;

/**
 * GUI form class for plugin settings UI.
 * GUI design is in "LSSettingsUI.form" file.
 */
public class SSSettingsUI {
    private final SSSettingsStore mSettingsStore;
    private final Project mProject;
    private JTextField mTextLocalPort;
    private JTextField mTextWebURL;
    private JTextField mTextOverlayPath;
    private JPanel mRootPanel;
    private JTextField mTextLocalRoot;
    private JButton mBtnBrowseLocalRoot;
    private JCheckBox mChbCacheResponses;

    /**
     * Constructor
     *
     * @param store   Settings store
     * @param project Project reference.
     */
    public SSSettingsUI(SSSettingsStore store, Project project) {
        mSettingsStore = store;
        mProject = project;

        // handle browse button click
        mBtnBrowseLocalRoot.addActionListener(e -> this.onBtnBrowseClick());
    }

    /**
     * Get UI root component.
     *
     * @return UI root component.
     */
    public JPanel getRootPanel() {
        return mRootPanel;
    }

    /**
     * Load data from store into GUI.
     */
    public void loadData() {
        mTextLocalPort.setText(String.valueOf(mSettingsStore.getLocalPort()));
        mTextWebURL.setText(mSettingsStore.getWebURL().toString());
        mTextOverlayPath.setText(mSettingsStore.getShadowPath());
        mTextLocalRoot.setText(mSettingsStore.getLocalRoot().getCanonicalPath());
        mChbCacheResponses.setSelected(mSettingsStore.isCacheResponses());
    }

    /**
     * Apply GUI data to store.
     *
     * @throws ConfigurationException Data validation error.
     */
    public void applyData() throws ConfigurationException {
        mSettingsStore.setLocalPort(mTextLocalPort.getText());
        mSettingsStore.setWebURL(mTextWebURL.getText());
        mSettingsStore.setShadowPath(mTextOverlayPath.getText());
        mSettingsStore.setLocalRoot(mTextLocalRoot.getText());
        mSettingsStore.setCacheResponses(mChbCacheResponses.isSelected());
    }

    /**
     * Handle file brows click.
     */
    private void onBtnBrowseClick() {
        // file picking options
        FileChooserDescriptor d = new FileChooserDescriptor(
                false,
                true,
                false,
                false,
                false,
                false);

        // Old and new files
        VirtualFile oldFile = mSettingsStore.getLocalRoot();
        VirtualFile newFile = FileChooser.chooseFile(d, mProject, oldFile);

        // Set new file
        if (newFile != null && newFile.exists()) {
            mTextLocalRoot.setText(newFile.getCanonicalPath());
        }
    }
}
