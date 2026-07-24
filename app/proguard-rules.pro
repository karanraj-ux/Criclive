# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Match models for serialization or reflection if used
-keep class com.example.model.** { *; }

# Keep workers for WorkManager
-keep class com.example.widget.** { *; }

# Retrofit/OkHttp/Moshi rules usually bundled, but just in case:
-dontwarn okio.**
-dontwarn retrofit2.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
