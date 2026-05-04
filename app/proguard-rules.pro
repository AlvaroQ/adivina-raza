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

# Hide the original source file name in stack traces
-renamesourcefileattribute SourceFile

# Add this global rule
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Legacy warnings
-dontwarn sun.misc.**

# Domain classes — keep fields for Firebase/Gson deserialization, allow method obfuscation
-keepclassmembers class com.alvaroquintana.domain.* { <fields>; }
-keep class com.alvaroquintana.domain.* { <init>(...); }

# Javax annotations
-dontwarn javax.annotation.**

# Test frameworks (not in release but suppresses build noise)
-dontnote junit.framework.**
-dontnote junit.runner.**
-dontwarn android.test.**
-dontwarn android.support.test.**
-dontwarn org.junit.**
-dontwarn org.hamcrest.**
-dontwarn com.squareup.javawriter.JavaWriter
-dontwarn org.mockito.**

# Crashlytics
-keepattributes SourceFile,LineNumberTable        # Keep file names and line numbers.
-keep public class * extends java.lang.Exception  # Optional: Keep custom exceptions.

# Kotlin serialization
-keepattributes RuntimeVisibleAnnotations
-keepclassmembers class kotlinx.serialization.** { *; }
-keep class * implements kotlinx.serialization.KSerializer { *; }
-keepclassmembers @kotlinx.serialization.Serializable class * {
	static ** Companion;
}
-keepclassmembers class **$$serializer { *; }

# Navigation type-safe args
-keep class * extends androidx.navigation.NavArgs { *; }
-keepnames @kotlinx.serialization.Serializable class *

# Type-safe Compose navigation routes
-keepnames class com.alvaroquintana.adivinaperro.ui.navigation.**
-keep class com.alvaroquintana.adivinaperro.ui.navigation.**$$serializer { *; }

# Room (transitive via WorkManager from Firebase/Play Services)
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Database class * { *; }
-keep @androidx.room.Dao class * { *; }

# WorkManager (transitive from Firebase/Play Services)
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.ListenableWorker { *; }

