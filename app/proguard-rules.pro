# NexusTune R8 Proguard Rules
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep Compose viewmodels and state
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Keep all Enums and their methods (values, valueOf, name) for DataStore and serialization
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    public final java.lang.String name();
}
-keep enum com.nexus.tune.core.model.** { *; }
-keep class com.nexus.tune.core.model.** { *; }

# Keep Entities and DAOs for Room
-keep class * extends androidx.room.RoomDatabase {
    <init>();
}
-keep class **_Impl {
    <init>(...);
}
-keep class com.nexus.tune.core.database.NexusTuneDatabase_Impl {
    <init>();
}
-keep class com.nexus.tune.core.database.dao.** { *; }
-keep interface com.nexus.tune.core.database.dao.** { *; }
-keep class com.nexus.tune.core.database.entity.** { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Database class * { *; }

# NewPipeExtractor reflection and JSON mapping rules
-keep class org.schabi.newpipe.extractor.** { *; }
-keep interface org.schabi.newpipe.extractor.** { *; }
-dontwarn org.schabi.newpipe.extractor.**
-keep class org.mozilla.javascript.** { *; }
-dontwarn org.mozilla.javascript.**
-keep class org.jsoup.** { *; }
-dontwarn org.jsoup.**
-dontwarn javax.script.**
-keep class com.nexus.tune.core.extractor.** { *; }

# OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# Media3 / ExoPlayer rules
-keep class androidx.media3.exoplayer.** { *; }
-keep interface androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.session.** { *; }
-keep interface androidx.media3.session.** { *; }
-keep class androidx.media3.common.** { *; }
-keep interface androidx.media3.common.** { *; }
-keep class androidx.media3.extractor.** { *; }
-keep interface androidx.media3.extractor.** { *; }
-keep class com.nexus.tune.core.player.** { *; }
-dontwarn androidx.media3.**

# Application Components
-keep class com.nexus.tune.NexusTuneApplication { *; }
-keep class com.nexus.tune.MainActivity { *; }
-keep class com.nexus.tune.widget.** { *; }
-keep class com.nexus.tune.core.player.alarm.** { *; }
