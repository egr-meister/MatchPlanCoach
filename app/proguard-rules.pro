# ---------------------------------------------------------------------------
# MatchPlan Coach ProGuard / R8 rules
# Keep rules required for kotlinx.serialization, Retrofit and OkHttp so the
# release (minified) build does not crash with ClassNotFound / serialization
# errors.
# ---------------------------------------------------------------------------

# --- Kotlin / Coroutines ---
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# --- kotlinx.serialization ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
# Keep the generated serializers for every @Serializable class.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    *** Companion;
}
-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}
# Keep all our data models and their fields.
-keep,includedescriptorclasses class com.matchplan.coach.data.model.** { *; }
-keep,includedescriptorclasses class com.matchplan.coach.data.remote.dto.** { *; }
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** *;
}

# --- Retrofit ---
-keepattributes Signature, Exceptions, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-dontwarn retrofit2.**
-keep class com.matchplan.coach.data.remote.FootballDataApiService { *; }

# --- OkHttp ---
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
