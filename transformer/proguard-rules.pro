# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/drakeet/Applications/sdk/tools/proguard/proguard-android.txt
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

-keep class me.drakeet.transformer.** { *;}
-keep class me.drakeet.timemachine.** { *;}
-keepattributes SourceFile, LineNumberTable
-keepattributes *Annotation*
-keepattributes Signature

-keep class com.google.android.agera.** { *;}
-keep interface com.google.android.agera.** { *;}

# Keep the support library
-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }
-keep public class android.support.v7.internal.view.menu.** { *; }
-keep public class * extends android.support.v4.view.ActionProvider {
    public <init>(android.content.Context);
}
-keep class android.support.v8.renderscript.** { *; }

# For using GSON @Expose annotation

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keep class **.R$* {*;}
-ignorewarnings

-verbose
-keepclasseswithmembernames class * {
    native <methods>;
}

# Remove logging calls
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

-keep class sun.misc.Unsafe { *; }
-dontwarn java.lang.invoke.*

-keepclassmembers enum * {
    **[] $VALUES;
    public *;
}