# SEIF CUSTOM Android

Open the `android/` folder in Android Studio. Install Android SDK 35 and JDK 17, sync Gradle, then build the `app` module.

## Implemented foundation
- Home/launcher activity selectable as the Android default Home app.
- App center with live search by app label/package, All/Favorites/Hidden filters, favorite toggles, launch action, and hide/restore inside this launcher.
- Local image/video selection through Android's document picker.
- Live wallpaper service for muted looping video, GIF, static images and Android-decoder-supported animated image formats.
- Wallpaper playback pauses while not visible.
- Optional Arabic/English local keyboard service. It does not store or transmit typed text; user must enable/select it in Android keyboard settings.
- Resizable home-screen clock/date widget.
- AES-256-GCM Android Keystore encryption primitive.

## SEIF Gesture Navigation\n- Optional accessibility service: short upward swipe from the bottom opens Recents; a longer upward swipe invokes Home; inward swipes from enabled left/right edges invoke Back.\n- Holding the bottom indicator for 3 seconds attempts to launch the configured voice assistant.\n- User must explicitly enable the service in Android Accessibility settings. It declares `canRetrieveWindowContent=false` and does not inspect screen text.\n- Gesture zones may conflict with OEM/system edge gestures. Disable the service from Accessibility settings if needed. Physical-device testing is still required.\n\n## Privacy limits
The Hidden tab only removes selected apps from the SEIF CUSTOM launcher's visible app list. It does not hide them from Android Settings, other launchers, or Digital Wellbeing/Screen Time usage statistics. A normal third-party launcher cannot erase or falsify system usage records. Android Private Space, if available on the device, is configured and managed by Android itself.

## Build and downloads
- Use Android Studio, Android SDK 35 and JDK 17; sync Gradle and build the `app` module.
- GitHub Actions builds a debug APK artifact on Android source changes.
- The manual/tag-triggered Android Release workflow publishes `SEIF-CUSTOM-Android.apk` and `SEIF-CUSTOM-Source.zip` to GitHub Releases.
- Download page: https://seifjaballah77-eng.github.io/SEIF-CUSTOM/extension/download.html

## Not complete / requires testing
- The latest Android changes still require a successful CI build and physical-device testing.
- WebP animation depends on the Android ImageDecoder implementation/version.
- The keyboard is a functional starter layout, not yet a full-featured prediction/dictionary keyboard. No typed text is stored or sent.
- VaultCrypto is only a cryptographic primitive, not a finished vault. Password/recovery UX, encrypted file import/export, biometric gate and failed-attempt notification flow remain to be built.
- The full widget factory, full icon/theme studio, curated wallpaper library, advanced launcher folders/gestures and Windows companion remain unimplemented.
