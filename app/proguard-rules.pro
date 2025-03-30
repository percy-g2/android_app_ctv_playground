# Keep source file and line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep annotation information
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions

# BitcoinJ rules
-keep class org.bitcoinj.** { *; }
-keep class org.bitcoin.** { *; }
-dontwarn org.bitcoinj.**
-dontwarn org.bitcoin.**

# BouncyCastle rules
-keep class org.bouncycastle.** { *; }
-keepclassmembers class org.bouncycastle.** {
    public protected private *;
}
-dontwarn org.bouncycastle.**

# Keep all native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep all classes in your app package
-keep class com.androdevlinux.ctvplayground.** { *; }

# Keep Kotlin Coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-keepclassmembers class kotlin.coroutines.** {
    volatile <fields>;
}

# Keep Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep Kotlin data classes
-keepclassmembers class * {
    public static synthetic <methods>;
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep R classes
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Keep Parcelable classes
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable classes
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep SLF4J
-dontwarn org.slf4j.**
-dontwarn javax.annotation.**
-dontwarn javax.inject.**

# Keep Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keepclasseswithmembers class * {
    @androidx.compose.ui.tooling.preview.Preview <methods>;
}

# Keep ViewModels
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Keep Bitcoin-specific classes
-keep class org.bitcoin.NativeSecp256k1 { *; }
-keep class org.bitcoin.Secp256k1Context { *; }

# Keep common crypto algorithms
-keep class javax.crypto.** { *; }
-keep class javax.crypto.spec.** { *; }
-keep class java.security.** { *; }
-keep class java.security.spec.** { *; }

# Keep BouncyCastle provider
-keep class org.spongycastle.** { *; }
-dontwarn org.spongycastle.**
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# Keep all security providers
-keep class * extends java.security.Provider { *; }