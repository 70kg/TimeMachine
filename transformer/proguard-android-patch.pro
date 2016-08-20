# Preserve some attributes that may be required for reflection.
-keepattributes *Annotation*,Signature,InnerClasses

# Preserve line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# Preserve constructors and setters of View implementations.
-keepclassmembers !abstract class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# Preserve the CREATOR field of Parcelable implementations.
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

# Preserve annotated Javascript interface methods.
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
