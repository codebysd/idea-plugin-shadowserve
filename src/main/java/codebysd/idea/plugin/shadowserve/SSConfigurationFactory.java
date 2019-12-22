package codebysd.idea.plugin.shadowserve;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationSingletonPolicy;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Factory class to produce {@link SSConfiguration}.
 */
public class SSConfigurationFactory extends ConfigurationFactory {

    /**
     * Constructor
     *
     * @param configurationType Configuration type.
     */
    public SSConfigurationFactory(SSConfigurationType configurationType) {
        super(configurationType);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        // create template configuration
        return new SSConfiguration(project, this, SSBundle.message("plugin_name"));
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public RunConfigurationSingletonPolicy getSingletonPolicy() {
        // Live serve is strictly single instance
        return RunConfigurationSingletonPolicy.SINGLE_INSTANCE_ONLY;
    }
}
