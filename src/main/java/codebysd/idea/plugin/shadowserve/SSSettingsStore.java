package codebysd.idea.plugin.shadowserve;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Stores and retrieves plugin settings.
 */
public class SSSettingsStore {
    private final Project mProject;
    private final PropertiesComponent mComponent;

    /**
     * Constructor
     *
     * @param project Project reference.
     */
    public SSSettingsStore(Project project) {
        mProject = project;

        // get properties component to persist settings in IDE project data
        mComponent = PropertiesComponent.getInstance(project);
    }

    /**
     * Get local port value
     *
     * @return port value
     */
    public int getLocalPort() {
        return mComponent.getInt(SSConstants.Settings.LOCAL_PORT, SSConstants.Defaults.LOCAL_PORT);
    }

    /**
     * Set local port value
     *
     * @param port port value
     * @throws ConfigurationException Validation or save error
     */
    public void setLocalPort(String port) throws ConfigurationException {
        if (SSUtils.isNil(port)) {
            throw new ConfigurationException("Local port is required.");
        }
        int value;
        try {
            value = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Local port must be a number.");
        }

        mComponent.setValue(SSConstants.Settings.LOCAL_PORT, value, SSConstants.Defaults.LOCAL_PORT);
    }

    /**
     * Get web URL value
     *
     * @return Web URL value
     */
    public URI getWebURL() {
        return URI.create(mComponent.getValue(SSConstants.Settings.WEB_URL, SSConstants.Defaults.WEB_URL));
    }

    /**
     * Set web URL value
     *
     * @param url web URL value
     * @throws ConfigurationException Validation or save error
     */
    public void setWebURL(String url) throws ConfigurationException {
        if (SSUtils.isNil(url)) {
            throw new ConfigurationException("Web URL is required.");
        }
        URI value;
        try {
            value = new URI(url);
            value = value.resolve(SSUtils.pathJoin(value.getPath(), "/"));
        } catch (URISyntaxException e) {
            throw new ConfigurationException("Web URL must be a valid URI.");
        }

        if (SSUtils.isNil(value.getScheme())) {
            throw new ConfigurationException("Web URL must have valid protocol scheme (http or https)");
        }

        if (SSUtils.isNil(value.getHost())) {
            throw new ConfigurationException("Web URL must have valid host name.");
        }

        String scheme = value.getScheme().toLowerCase();
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            throw new ConfigurationException("Web URL must have valid protocol scheme (http or https)");
        }

        mComponent.setValue(SSConstants.Settings.WEB_URL, value.toString());
    }

    /**
     * Get shadow path value
     *
     * @return Shadow path value
     */
    public String getShadowPath() {
        return mComponent.getValue(SSConstants.Settings.SHADOW_PATH, SSConstants.Defaults.SHADOW_PATH);
    }

    /**
     * Set shadow path value
     *
     * @param path path value
     */
    public void setShadowPath(String path) {
        if (SSUtils.isNil(path)) {
            path = "/";
        }
        path = SSUtils.pathJoin("/", path, "/");
        mComponent.setValue(SSConstants.Settings.SHADOW_PATH, path);
    }

    /**
     * Get local root value
     *
     * @return local root value
     */
    public VirtualFile getLocalRoot() {
        String root = mComponent.getValue(SSConstants.Settings.LOCAL_ROOT, SSConstants.Defaults.LOCAL_ROOT(mProject));
        return LocalFileSystem.getInstance().findFileByIoFile(new File(root));
    }

    /**
     * Set local root value
     *
     * @param root local root value
     * @throws ConfigurationException Validation or save error
     */
    public void setLocalRoot(String root) throws ConfigurationException {
        if (SSUtils.isNil(root)) {
            throw new ConfigurationException("Local root directory path is required.");
        }
        Path value;
        try {
            value = Paths.get(root).normalize();
        } catch (InvalidPathException e) {
            throw new ConfigurationException("Local root directory path is invalid.");
        }

        if (!Files.exists(value) || !Files.isDirectory(value)) {
            throw new ConfigurationException("Local root directory does not exist or is not a directory.");
        }

        mComponent.setValue(SSConstants.Settings.LOCAL_ROOT, value.toString());
    }

    /**
     * Get if cache is enabled
     *
     * @return true if enabled
     */
    public boolean isCacheResponses() {
        return mComponent.getBoolean(SSConstants.Settings.CACHE_RESPONSES, SSConstants.Defaults.CACHE_RESPONSES);
    }

    /**
     * Set cache enable state
     *
     * @param cache True to enable
     */
    public void setCacheResponses(boolean cache) {
        mComponent.setValue(SSConstants.Settings.CACHE_RESPONSES, cache);
    }
}
