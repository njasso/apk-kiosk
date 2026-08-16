# Conserver les classes WebView / JS bridge si ajoutées ultérieurement.
-keep class uk.na2quiz.kiosk.** { *; }
-keepclassmembers class * { @android.webkit.JavascriptInterface <methods>; }
