# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Android\sdk/tools/proguard/proguard-android.txt
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

# Picasso
-keepattributes SourceFile,LineNumberTable
-keep class com.parse.*{ *; }
-dontwarn com.parse.**
-dontwarn com.squareup.picasso.**
-keepclasseswithmembernames class * {
    native <methods>;
}

# CSV bean
-dontwarn java.beans.**

# Instabug
-dontwarn org.apache.http.**
-dontwarn android.net.http.AndroidHttpClient
-dontwarn com.google.android.gms.**
-dontwarn com.android.volley.toolbox.**
-dontwarn com.instabug.**
-keep class com.kolibree.android.app.App { *; }

# Android tests
-dontwarn android.test.**

# Jackson
-keep class com.fasterxml.jackson.databind.ObjectMapper {
    public <methods>;
    protected <methods>;
}
-keep class com.fasterxml.jackson.databind.ObjectWriter {
    public ** writeValueAsString(**);
}

-keep class com.fasterxml.jackson.annotation.** { *; }
-dontwarn org.w3c.dom.bootstrap.DOMImplementationRegistry
-dontwarn com.fasterxml.jackson.databind.**

# General
-keepattributes SourceFile,LineNumberTable,*Annotation*,EnclosingMethod,Signature,Exceptions,InnerClasses

# ButterKnife
-keep public class * implements butterknife.Unbinder { public <init>(**, android.view.View); }
-keep class butterknife.*
-keepclasseswithmembernames class * { @butterknife.* <methods>; }
-keepclasseswithmembernames class * { @butterknife.* <fields>; }

# Apache AVRO lib
-keep class org.apache.avro.** { *; }
-dontwarn org.joda.convert.**
-dontwarn org.codehaus.jackson.map.ext.**
-keep class org.w3c.dom.bootstrap.** { *; }
-keep class org.joda.time.** { *; }
-keepattributes *Annotation*,EnclosingMethod,Signature
-keepnames class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.databind.**
-keep class org.codehaus.** { *; }

-keepclassmembers public final enum org.codehaus.jackson.annotate.JsonAutoDetect$Visibility {
  public static final org.codehaus.jackson.annotate.JsonAutoDetect$Visibility *;
}

-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

-keep class org.slf4j.** { *; }
-dontwarn org.slf4j.**

-keep class org.osgi.** { *; }
-dontwarn org.osgi.**

-keep class javax.inject.** { *; }
-dontwarn javax.inject.**

# Joda Time
-dontwarn org.joda.convert.**
-dontwarn org.joda.time.**
-keep class org.joda.time.** { *; }
-keep interface org.joda.time.** { *; }

#dagger
-dontwarn com.google.errorprone.annotations.*

# Nordic DFU library
-keep class no.nordicsemi.android.dfu.** { *; }

# Gson
-dontwarn com.google.gson.Gson$6
-keep class com.google.gson.** { *; }

# Retrofit
-dontwarn org.codehaus.**
-dontwarn okio.**

# Fabric/Crashlytics
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**
