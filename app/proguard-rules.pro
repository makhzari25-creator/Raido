# RAIDO release R8 — keep all app logic (no behavior change)
-keep class ir.tripanalyzer.app.** { *; }
-keep interface ir.tripanalyzer.app.** { *; }
-keep enum ir.tripanalyzer.app.** { *; }
-keep public class * extends android.app.Application
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.accessibilityservice.AccessibilityService
-keep public class * extends android.service.notification.NotificationListenerService
-keepclassmembers class * {
    public void *(android.view.View);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class androidx.** { *; }
-dontwarn androidx.**
-dontwarn com.google.android.material.**
-dontwarn kotlin.**
-dontwarn kotlinx.**
-keep class kotlin.Metadata { *; }
-keep class ir.tripanalyzer.app.BuildConfig { *; }
-keep class org.json.** { *; }
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod
-renamesourcefileattribute SourceFile
