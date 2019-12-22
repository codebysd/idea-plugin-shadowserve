package codebysd.idea.plugin.shadowserve;

import com.intellij.openapi.project.Project;

import java.util.Objects;

/**
 * Plugin constants
 */
public interface SSConstants {

    /**
     * Plugin identifiers
     */
    interface Plugin {
        /**
         * Unique plugin ID. Must never change in new releases.
         */
        String ID = "ShadowServe";
    }

    /**
     * Setting properties
     */
    interface Settings {
        /**
         * Prefix plugin ID with setting keys, to de-conflict with any other project setting
         *
         * @param k key name
         * @return Plugin setting key
         */
        static String key(String k) {
            return Plugin.ID + "_" + k;
        }

        /**
         * Local port key
         */
        String LOCAL_PORT = key("LocalPort");

        /**
         * Web URL key
         */
        String WEB_URL = key("WebUrl");

        /**
         * Shadow path key
         */
        String SHADOW_PATH = key("ShadowPath");

        /**
         * Local root key
         */
        String LOCAL_ROOT = key("LocalRoot");

        /**
         * Cache switch key
         */
        String CACHE_RESPONSES = key("CacheResponses");
    }

    /**
     * Setting defaults
     */
    interface Defaults {
        /**
         * Default host.
         */
        String LOCAL_HOST = "127.0.0.1";

        /**
         * Default local port value
         */
        int LOCAL_PORT = 8181;

        /**
         * Default web URL value
         */
        String WEB_URL = "http://localhost:8080/";

        /**
         * Default shadow path value
         */
        String SHADOW_PATH = "/";

        /**
         * Default cache switch value
         */
        boolean CACHE_RESPONSES = false;

        /**
         * Default index file for serving directory.
         */
        String INDEX_FILE = "index.html";

        /**
         * Default local root directory value.
         *
         * @param project Project
         * @return Project base directory.
         */
        static String LOCAL_ROOT(Project project) {
            return Objects.requireNonNull(project.getBasePath());
        }
    }

}
