# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /opt/android-studio/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keepparameternames
-renamesourcefileattribute SourceFile
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

#-keepclassmembers class * {
#    java.lang.Class *;
#    java.lang.Enum *;
#}

-keep class com.kolibree.android.sdk.manager.callbacks.** {
    public *;
}
-keep class com.kolibree.android.sdk.calibration.** {
    public *;
}
-keep class com.kolibree.android.sdk.measurements.** {
    public *;
}
-keep class com.kolibree.android.sdk.manager.streaming.** {
    public *;
}

# Nordic DFU library
-keep class no.nordicsemi.android.dfu.** { *; }
