# SEIF CUSTOM Android starter

Open the `android/` folder in Android Studio. Install Android SDK 35 and JDK 17, sync Gradle, then build the `app` module.

## Implemented foundation
- Home/launcher activity selectable as the Android default Home app.
- Installed-app list with optional hide/restore **inside this launcher only**.
- Local image/video selection through Android's document picker.
- Live wallpaper service for muted looping video, GIF, static images and Android-decoder-supported animated image formats.
- Wallpaper playback pauses while not visible.
- Adjustable opacity for GIF and image rendering; video pixel opacity is device/renderer dependent.
- Optional Arabic/English local keyboard service. It does not store or transmit typed text; user must enable/select it in Android keyboard settings.
- Resizable home-screen clock/date widget.
- AES-256-GCM Android Keystore encryption primitive.

## Build
Use Android Studio, Android SDK 35 and JDK 17; sync Gradle and build the `app` module. GitHub Actions also attempts a debug build on Android source changes.

## Limitations
- This project has not yet completed a successful CI build or physical-device test.
- WebP animation depends on the Android ImageDecoder implementation/version and requires device testing.
- Opacity applies to GIF and image rendering. Video is rendered directly by MediaPlayer and per-pixel opacity is not guaranteed.
- The keyboard is a functional starter layout, not yet a full-featured prediction/dictionary keyboard. No typed text is stored or sent.
- VaultCrypto is only a cryptographic primitive, not a finished vault. Password/recovery UX, encrypted file import/export, biometric gate and failed-attempt notification flow remain to be built.
- Hiding apps only affects this launcher, not Android Settings or other launchers.
- Full widget factory, full icon/theme studio, curated wallpaper library and Windows companion remain unimplemented.
