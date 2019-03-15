PROGUARD RULES

-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class com.snakydesign.watchdog.modelse.**$$serializer { *; }
-keepclassmembers class com.snakydesign.watchdog.models.** {
    *** Companion;
}
-keepclasseswithmembers class com.snakydesign.watchdog.models.** {
    kotlinx.serialization.KSerializer serializer(...);
}