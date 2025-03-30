# Basic Android optimization
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable

# Keep minimal source file attributes for stack traces
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod

# Remove Android logging
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# BitcoinJ - keep only what's needed
-keep class org.bitcoinj.core.** { *; }
-keep class org.bitcoinj.script.** { *; }
-keep class org.bitcoinj.crypto.** { *; }
-keep class org.bitcoinj.base.** { *; }
-dontwarn org.bitcoinj.**

# BouncyCastle - minimal keep rules
-keep class org.bouncycastle.jce.provider.BouncyCastleProvider
-keep class org.bouncycastle.jce.provider.symmetric.** { *; }
-keep class org.bouncycastle.jcajce.provider.digest.** { *; }
-keep class org.bouncycastle.jcajce.provider.symmetric.** { *; }
-dontwarn org.bouncycastle.**

# SpongyCastle - minimal keep rules
-keep class org.spongycastle.jce.provider.BouncyCastleProvider
-keep class org.spongycastle.jce.provider.symmetric.** { *; }
-keep class org.spongycastle.jcajce.provider.digest.** { *; }
-keep class org.spongycastle.jcajce.provider.symmetric.** { *; }
-dontwarn org.spongycastle.**

# Keep your app's core classes
-keep class com.androdevlinux.ctvplayground.models.** { *; }
-keep class com.androdevlinux.ctvplayground.vault.** { *; }
-keep class com.androdevlinux.ctvplayground.utils.** { *; }

# Kotlin specific
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-keep class kotlinx.coroutines.** { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Compose - minimal rules
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-dontwarn androidx.compose.**

# ViewModels
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>();
}

# Crypto
-keep class javax.crypto.Cipher
-keep class javax.crypto.spec.SecretKeySpec
-keep class java.security.MessageDigest
-keep class java.security.SecureRandom

# Remove debugging info
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Optimization flags
-allowaccessmodification
-repackageclasses ''
-mergeinterfacesaggressively

# Remove unused code
-dontwarn javax.**
-dontwarn java.awt.**
-dontwarn java.beans.**
-dontwarn org.w3c.**
-dontwarn org.apache.**
-dontwarn android.support.**
-dontwarn com.google.android.material.**
-dontwarn org.slf4j.**

# Keep necessary attributes
-keepattributes RuntimeVisible*Annotations
-keepattributes AnnotationDefault

# Shrinking optimization
-shrinkunusedprotofields
-optimizationpasses 5

# Additional size optimizations
-keepclasseswithmembernames class * {
    native <methods>;
}

# Remove debug logs in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}