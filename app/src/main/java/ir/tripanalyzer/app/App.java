package ir.tripanalyzer.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import kotlin.jvm.JvmStatic;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

public final class App extends Application {

    public static final Companion INSTANCE = new Companion(null);
    private static Context ctx;
    private static Activity currentActivity;

    public static final Context getCtx() {
        return INSTANCE.getCtx();
    }

    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @JvmStatic
        public static /* synthetic */ void getCtx$annotations() {
        }

        private Companion() {
        }

        public final Context getCtx() {
            Context context = App.ctx;
            if (context != null) return context;
            Intrinsics.throwUninitializedPropertyAccessException("ctx");
            return null;
        }

        public final Activity getCurrentActivity() {
            return App.currentActivity;
        }
    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }

    public void setCurrentActivity(Activity a) {
        currentActivity = a;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        App app = this;
        ctx = app;
        Fonts.INSTANCE.init(app);
        Notifier.INSTANCE.ensureChannels(app);
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}
            @Override public void onActivityStarted(Activity activity) {}
            @Override public void onActivityResumed(Activity activity) { currentActivity = activity; }
            @Override public void onActivityPaused(Activity activity) {
                if (currentActivity == activity) currentActivity = null;
            }
            @Override public void onActivityStopped(Activity activity) {}
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
            @Override public void onActivityDestroyed(Activity activity) {
                if (currentActivity == activity) currentActivity = null;
            }
        });
    }
}
