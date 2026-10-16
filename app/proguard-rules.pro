# RAIDO proguard — keep analysis + accessibility + zone bridge
-keep class ir.tripanalyzer.app.** { *; }
-keepclassmembers class ir.tripanalyzer.app.** { *; }
-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-dontwarn org.jetbrains.annotations.**
-dontwarn kotlin.**
