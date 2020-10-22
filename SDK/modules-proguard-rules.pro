# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#################################################a################################################
### Warning ! Don't add any general rule here, always target the most specific content you can ###
##################################################################################################

# Keep public Kolibree classes and interfaces
-keep, includedescriptorclasses public class com.kolibree.** {
    public <fields>;
    protected <fields>;
    public <methods>;
    protected <methods>;
 }

 -keep public interface com.kolibree.**  {*;}

# Keep DI
-keep class javax.inject.** { *; }

# Keep all fields and constructors with @Inject
-keepclassmembers,allowobfuscation class * {
  @javax.inject.Inject <fields>;
  @javax.inject.Inject <init>(...);
}

# Keep all Dagger graph construction methods and classes
-keep class * {
  @dagger.Module *;
  @dagger.SubComponent *;
  @dagger.SubComponent.Builder *;
  @dagger.Binds <methods>;
  @dagger.BindsOptionalOf <methods>;
  @dagger.Provides <methods>;
  @dagger.multibindings.* <methods>;
  @dagger.android.ContributesAndroidInjector <methods>;
  @com.kolibree.android.app.dagger.scopes.ActivityScope *;
  @com.kolibree.android.app.dagger.scopes.FragmentScope *;
}

# Keep Dagger generated factories, injectors and builders so all the modules can work with each
# other once obfuscated
-keep class * implements dagger.internal.Factory { *; }
-keep class * implements dagger.MembersInjector { *; }

# Keep all Parcelables and creators classes
-keep class * {
  @kotlinx.android.parcel.Parcelize *;
}
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

# Keep Kotlin reflection
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.jvm.internal.** { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

# Keep all public JobServices, Activities and Fragments names
-keep public class * extends androidx.appcompat.app.AppCompatActivity { *; }
-keep public class * extends androidx.fragment.app.Fragment { *; }
-keep public class * extends android.app.job.JobService { *; }

# Keep Unity related stuff
-keep class bitter.jnibridge.* { *; }
-keep class com.unity3d.** { *; }
-keep class org.fmod.* { *; }

# Keep databinding-related stuff
-keep public class com.kolibree.**.BR { public *; }
-keep public class com.kolibree.**.BR$* { public *; }
-keepclassmembers class com.kolibree.**.BR$* {
  public static <fields>;
}
-keepclassmembers class com.kolibree.**.BR$* {
  public static <fields>;
}

# Strong configuration to avoid any issue / warning with Retrofit or JSON parsing
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-dontwarn sun.misc.Unsafe
-dontwarn com.octo.android.robospice.retrofit.RetrofitJackson**
-dontwarn retrofit.appengine.UrlFetchClient
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}

# Fabric/Crashlytics
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

-keep class com.google.gson.** { *; }
-keep class com.google.inject.** { *; }
-keep class org.apache.** { *; }
-keep class retrofit.** { *; }
-keep class org.joda.** { *; }
-dontwarn org.apache.http.**
-dontwarn android.net.http.AndroidHttpClient
-dontwarn retrofit.**
-dontwarn sun.misc.**

