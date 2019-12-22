package codebysd.idea.plugin.shadowserve;

import com.intellij.CommonBundle;
import com.intellij.reference.SoftReference;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.util.ResourceBundle;

/**
 * Resource bundle accessor class for "strings/SSBundle.properties".
 */
public final class SSBundle {
    @NonNls
    private static final String BUNDLE = "strings.SSBundle";
    private static Reference<ResourceBundle> sResBundle;

    /**
     * Private constructor.
     */
    private SSBundle() {
    }

    /**
     * Get resource bundle.
     *
     * @return Resource bundle.
     */
    private static ResourceBundle getBundle() {
        // try cached reference
        ResourceBundle bundle = SoftReference.dereference(sResBundle);
        if (bundle == null) {
            // load bundle
            bundle = ResourceBundle.getBundle(BUNDLE);
            sResBundle = new SoftReference<>(bundle);
        }
        return bundle;
    }

    /**
     * Get string message from resource bundle, for given key and template arguments.
     *
     * @param key  Message key
     * @param args template arguments.
     * @return String message
     */
    public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, @NotNull Object... args) {
        return CommonBundle.message(getBundle(), key, args);
    }

    /**
     * Get string message from resource bundle, for given key.
     *
     * @param key Message key
     * @return String message
     */
    public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key) {
        return CommonBundle.message(getBundle(), key);
    }
}
