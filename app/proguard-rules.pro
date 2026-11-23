# NexusTune R8 Proguard Rules
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep Compose viewmodels and state
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Keep Entities for Room
-keep class * extends androidx.room.RoomDatabase

# NewPipeExtractor reflection and JSON mapping rules
-keep class org.schabi.newpipe.extractor.** { *; }
-dontwarn org.schabi.newpipe.extractor.**
-dontwarn org.jsoup.**
-dontwarn okhttp3.**

# Media3 / ExoPlayer rules
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.session.** { *; }

