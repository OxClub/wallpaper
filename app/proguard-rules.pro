# Retrofit / Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.example.wallpaperapp.data.model.** { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**

# Firebase
-keep class com.google.firebase.** { *; }
