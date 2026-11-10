# SEIF CUSTOM Android starter

Open the `android/` folder in Android Studio. Install Android SDK 35 and JDK 17, sync Gradle, then build the `app` module.

## Implemented foundation
- Home/launcher activity that can be selected as the Android default Home app.
- Installed-app list with optional hide/restore **inside this launcher only**.
- Local image/video selection via Android Storage Access Framework, with persisted URI permission.
- Live wallpaper service for muted looping video and animated GIF when supported by the device decoder.
- Wallpaper playback pauses while not visible.
- Adjustable opacity for GIF rendering.
- AES-256-GCM encryption primitive backed by Android Keystore.

## Limitations (not to be mistaken for completed features)
- This starter has not yet been compiled or tested on a physical device in this coding session.
- Animated WebP is not yet connected to the native wallpaper renderer.
- Opacity currently affects GIF rendering; video is rendered directly by MediaPlayer and per-pixel opacity is not guaranteed on all devices.
- VaultCrypto is a primitive only, not a finished vault. Password/recovery UX, encrypted file import/export, biometric gate and failed-attempt notification flow remain to be built.
- Hiding apps only affects this launcher, not Android Settings or other launchers.
- Custom keyboard, full widget factory, full icon/theme studio and Windows companion remain unimplemented.
