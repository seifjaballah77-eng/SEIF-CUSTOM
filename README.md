# SEIF CUSTOM
### PERSONAL DEVICE EXPERIENCE

SEIF CUSTOM is a user-controlled personalization project for Android and desktop browsing. **Suggest, never force:** the user decides whether to change appearance, move or hide items, enable motion, or use optional automation.

## Android app
The `android/` folder contains the native Android Studio project. The launcher app center now includes:
- Installed app discovery with live name/package search.
- Separate All, Favorites and Hidden views.
- Favorite/unfavorite controls and app launch from the list.
- Optional hide/restore **inside the SEIF CUSTOM launcher only**.
- Local image/video wallpaper selection and preview before applying.
- Starter Arabic/English keyboard, clock/date widget and AES-GCM encryption primitive.

**Important privacy boundary:** hiding an app in this launcher does not hide it from Android Settings, other launchers, or Digital Wellbeing/Screen Time usage statistics. A normal third-party app cannot erase or falsify Android's system usage records. Android Private Space, where supported, is a separate OS-managed feature and must be configured by the device owner.

## Download center
Open [SEIF CUSTOM Download Center](https://seifjaballah77-eng.github.io/SEIF-CUSTOM/extension/download.html).
- Android APK is published to GitHub Releases after a successful Android release workflow.
- Full source ZIP is available from the download center.
- Browser extension files are included in the source ZIP; load the `extension/` directory using Developer mode.
- Debug APK artifacts are attached to successful Android build workflow runs.

The APK release workflow is `.github/workflows/android-release.yml`. It can be started manually from GitHub Actions or by pushing a `v*` tag. The first APK download will only work after that workflow successfully publishes its first release.

## Browser New Tab
- Arabic RTL layout, compact translucent controls, clock/date and web search.
- Add/remove up to 20 custom shortcuts.
- Manual light/dark toggle and adjustable panel transparency.
- Choose a local still/animated image (GIF/WebP) or MP4/WebM/OGG video as the New Tab background.
- Adjust wallpaper opacity separately from glass-panel transparency.
- Video loops muted by default; motion can be paused manually.
- Wallpaper files and preferences stay in local browser storage. Settings can be imported/exported; the media file itself is not included in settings backup.
- No automatic power-saving changes and no automatic light/dark switching.

## Desktop phone simulator
Open `extension/simulator.html` from the New Tab phone icon. It provides an interactive phone-shaped design preview with Home, Apps and Widgets pages; accent/background colors; card and wallpaper opacity; icon shapes; preview scale; image/video wallpapers; and editable app links. App icons open the configured official website or download page in a new tab. They do not install or run Android APKs in the simulator. Preferences and the preview wallpaper are stored locally in this browser.

## Full requirements and honest status
- [Master plan](docs/MASTER-PLAN.md)
- [Testing plan](docs/TESTING.md)
- [Android build notes](android/README.md)

The Android vault interface, biometric/recovery flow, custom keyboard studio, native widget factory, full theme/icon studio, curated 120-wallpaper library, advanced app folders/gestures, and Windows companion are not complete yet. The launcher app drawer enhancement is committed, but the current APK must pass CI and device testing before it is considered ready.
