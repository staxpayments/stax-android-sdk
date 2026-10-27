# The SDK parses its models with Gson by reflection.
-keep class com.fattmerchant.android.** { *; }
-keep class com.fattmerchant.omni.** { *; }

# The card reader libraries in libs/ use reflection and native code.
-keep class com.creditcall.** { *; }
-keep class com.idtechproducts.** { *; }
-keep class com.bbpos.** { *; }

# slf4j-api 1.7 looks up a binding that the SDK does not ship.
-dontwarn org.slf4j.impl.StaticLoggerBinder

# Code paths in the reader libraries that the SDK never calls.
-dontwarn org.apache.http.**
-dontwarn android.bbpos.**
-dontwarn android.app.CustServiceManager
-dontwarn com.bbpos.emvswipe.**
-dontwarn com.bbpos.wisepad.**
