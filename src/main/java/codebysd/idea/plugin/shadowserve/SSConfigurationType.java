package codebysd.idea.plugin.shadowserve;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Defines plugin configuration type.
 */
public class SSConfigurationType implements ConfigurationType {

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public String getDisplayName() {
        return SSBundle.message("plugin_name");
    }

    /**
     * {@inheritDoc}
     */
    @Nls
    @Override
    public String getConfigurationTypeDescription() {
        return SSBundle.message("plugin_desc");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Icon getIcon() {
        return SSIcons.PLUGIN_ICON;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public String getId() {
        return SSConstants.Plugin.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{new SSConfigurationFactory(this)};
    }


}
