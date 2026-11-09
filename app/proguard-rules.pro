# Keep J2ME related classes
-keep class javax.microedition.** { *; }
-keep class com.nokia.mid.** { *; }
-keep class com.siemens.mp.** { *; }
-keep class com.motorola.** { *; }
-keep class com.sonyericsson.** { *; }

# Keep Room database
-keep class com.satse.jarj2me.data.** { *; }

# Keep emulator
-keep class com.satse.jarj2me.emulator.** { *; }

# Keep UI
-keep class com.satse.jarj2me.ui.** { *; }

# Keep Application
-keep class com.satse.jarj2me.J2MEApplication { *; }

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep Parcelable
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Enum
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}