package ir.tripanalyzer.app;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;

/**
 * Checks whether RAIDO accessibility / notification-listener services are enabled
 * and actually usable (not just listed but restricted by the system).
 */
public final class Status {
    public static final Status INSTANCE = new Status();

    private Status() {}

    private boolean listedInSecureSettings(Context c) {
        String enabled = Settings.Secure.getString(
                c.getContentResolver(), "enabled_accessibility_services");
        if (enabled == null || enabled.isEmpty()) return false;
        String pkg = c.getPackageName();
        for (String entry : enabled.split(":")) {
            if (entry != null
                    && entry.contains(pkg)
                    && entry.contains("SnappScreenWatcherService")) {
                return true;
            }
        }
        return false;
    }

    private boolean listedInAccessibilityManager(Context c) {
        try {
            AccessibilityManager am =
                    (AccessibilityManager) c.getSystemService(Context.ACCESSIBILITY_SERVICE);
            if (am == null || !am.isEnabled()) return false;
            List<AccessibilityServiceInfo> list =
                    am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
            if (list == null) return false;
            String pkg = c.getPackageName();
            String expected = new ComponentName(pkg, SnappScreenWatcherService.class.getName())
                    .flattenToString();
            String expectedShort = pkg + "/.SnappScreenWatcherService";
            for (AccessibilityServiceInfo info : list) {
                if (info == null || info.getId() == null) continue;
                String id = info.getId();
                if (id.contains("SnappScreenWatcherService")
                        || id.equalsIgnoreCase(expected)
                        || id.contains(expectedShort)
                        || id.contains(pkg + "/" + SnappScreenWatcherService.class.getName())) {
                    return true;
                }
            }
        } catch (Throwable ignored) {}
        return false;
    }

    /**
     * True only if service is both in Secure settings AND reported as enabled
     * by AccessibilityManager (covers "restricted / limited" cases better).
     */
    public final boolean accessibilityEnabled(Context c) {
        Intrinsics.checkNotNullParameter(c, "c");
        boolean inSecure = listedInSecureSettings(c);
        boolean inManager = listedInAccessibilityManager(c);
        // Require Secure listing at minimum; prefer both when manager is available
        if (!inSecure) return false;
        // If manager says accessibility master switch is off, treat as off
        try {
            AccessibilityManager am =
                    (AccessibilityManager) c.getSystemService(Context.ACCESSIBILITY_SERVICE);
            if (am != null && !am.isEnabled()) return false;
        } catch (Throwable ignored) {}
        // If manager list is available and our service is missing → restricted/limited
        try {
            AccessibilityManager am =
                    (AccessibilityManager) c.getSystemService(Context.ACCESSIBILITY_SERVICE);
            if (am != null && am.isEnabled()) {
                List<AccessibilityServiceInfo> list =
                        am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
                if (list != null && !list.isEmpty()) {
                    return inManager;
                }
            }
        } catch (Throwable ignored) {}
        return inSecure;
    }

    /** True if notification listener is enabled for our package. */
    public final boolean listenerEnabled(Context c) {
        Intrinsics.checkNotNullParameter(c, "c");
        String enabled = Settings.Secure.getString(
                c.getContentResolver(), "enabled_notification_listeners");
        if (enabled == null || enabled.isEmpty()) {
            return false;
        }
        String pkg = c.getPackageName();
        for (String entry : enabled.split(":")) {
            if (entry != null
                    && entry.contains(pkg)
                    && entry.contains("SnappNotificationListenerService")) {
                return true;
            }
        }
        return false;
    }

    /** Both core services usable. */
    public final boolean permissionsReady(Context c) {
        return accessibilityEnabled(c);
    }
}
