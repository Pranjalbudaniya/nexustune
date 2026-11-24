# Room Database consumer rules
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

