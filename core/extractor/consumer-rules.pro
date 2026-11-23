# NewPipeExtractor R8 rules
-keep class org.schabi.newpipe.extractor.** { *; }
-keep interface org.schabi.newpipe.extractor.** { *; }
-keep class org.mozilla.javascript.** { *; }
-dontwarn org.mozilla.javascript.**
-keep class org.jsoup.** { *; }
-keepclassmembers class * {
    @com.fasterxml.jackson.annotation.* *;
}

