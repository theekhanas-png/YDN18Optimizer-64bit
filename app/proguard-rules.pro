# Add project specific ProGuard rules here.
-keep class com.yodhan18.ydn18optimizer.** { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
