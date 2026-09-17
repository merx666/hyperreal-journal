# Hilt
-keep,allowobfuscation,allowshrinking @dagger.hilt.android.AndroidEntryPoint class *
-keep,allowobfuscation,allowshrinking @dagger.hilt.android.HiltAndroidApp class *

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Domain Models & Gson Reflection
-keep class info.hyperreal.journal.domain.model.** { *; }
-keepclassmembers class info.hyperreal.journal.domain.model.** { <fields>; }
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Compose
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
