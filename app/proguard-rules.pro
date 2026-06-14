# Kotlinx serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.telen.noteskeeper.**$$serializer { *; }
-keepclassmembers class com.telen.noteskeeper.** {
    *** Companion;
}
-keepclasseswithmembers class com.telen.noteskeeper.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Ktor / OkHttp
-dontwarn org.slf4j.**
-dontwarn okhttp3.**
-dontwarn okio.**
