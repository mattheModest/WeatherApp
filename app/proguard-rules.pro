# WeatherApp ProGuard Rules

# --- Kotlin ---
-keepclassmembers class **$WhenMappings { <fields>; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata { *; }

# --- Hilt / Dagger ---
-keepclassmembers,allowobfuscation class * {
    @javax.inject.* *;
    @dagger.* *;
    <init>();
}
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# --- Room ---
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-dontwarn androidx.room.paging.**

# --- Retrofit + Gson ---
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory { *; }
-keep class * implements com.google.gson.JsonSerializer { *; }
-keep class * implements com.google.gson.JsonDeserializer { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# --- Play Billing ---
-keep class com.android.billingclient.** { *; }

# --- WorkManager ---
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# --- Glance (App Widget) ---
-keep class androidx.glance.** { *; }

# --- Timber ---
-dontwarn org.jetbrains.annotations.**

# --- App model classes (Retrofit response bodies, Room entities) ---
-keep class com.weatherapp.data.db.entity.** { *; }
-keep class com.weatherapp.data.weather.model.** { *; }
-keep class com.weatherapp.model.** { *; }
