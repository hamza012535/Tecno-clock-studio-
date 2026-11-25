# Keep core application components and critical system-level services unobfuscated
-keep class com.example.systemui.** { *; }
-keep class com.example.service.** { *; }
-keep class com.example.receiver.** { *; }
-keep class com.example.ui.lockscreen.** { *; }

# Keep ClockProviderPlugin and ClockController contracts & implementations
-keep interface com.example.systemui.ClockProviderPlugin { *; }
-keep interface com.example.systemui.ClockController { *; }
-keep class * implements com.example.systemui.ClockProviderPlugin { *; }
-keep class * implements com.example.systemui.ClockController { *; }

# Keep Android Services, BroadcastReceivers, and Activities declared in Manifest
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.app.Activity

# Keep DataStore Preferences & Protobuf/Kotlin metadata
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}
-keepattributes Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable
