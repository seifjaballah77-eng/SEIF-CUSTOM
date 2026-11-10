# SEIF CUSTOM
### PERSONAL DEVICE EXPERIENCE

SEIF CUSTOM is a user-controlled personalization project for Android and desktop browsing. **Suggest, never force:** the user decides whether to change appearance, move or hide items, enable motion, or use optional automation.

## Browser New Tab
- Arabic RTL layout, compact translucent controls, clock/date and web search.
- Add/remove up to 20 custom shortcuts.
- Manual light/dark toggle and adjustable panel transparency.
- Choose a local still/animated image (GIF/WebP) or MP4/WebM/OGG video as the New Tab background.
- Adjust wallpaper opacity separately from glass-panel transparency.
- Video loops muted by default; motion can be paused manually.
- Wallpaper files and preferences stay in local browser storage. Settings can be imported/exported; the media file itself is not included in settings backup.
- No automatic power-saving changes and no automatic light/dark switching.

## Install the browser extension
1. Download the repository ZIP and extract it.
2. Open the browser extensions page and enable Developer mode.
3. Choose **Load unpacked** and select the `extension` folder.
4. Open a new tab.

## Desktop phone simulator
Open `extension/simulator.html` from the New Tab phone icon. It provides an interactive phone-shaped design preview with Home, Apps and Widgets pages; accent/background colors; card and wallpaper opacity; icon shapes; preview scale; image/video wallpapers; and editable app links. App icons open the configured official website or download page in a new tab. They do not install or run Android APKs in the simulator. Preferences and the preview wallpaper are stored locally in this browser.

## Android native starter
The `android/` folder is an Android Studio project with a selectable Home/launcher activity, optional in-launcher app hiding/restoring, local media selection, live wallpaper playback for video and GIF, static image rendering, and a local AES-GCM encryption primitive. See [android/README.md](android/README.md) for build instructions and limitations.

## Full requirements and honest status
- [Master plan](docs/MASTER-PLAN.md)
- [Testing plan](docs/TESTING.md)

The Android vault interface, biometric/recovery flow, custom keyboard, native widgets, full theme/icon studio, 120-wallpaper curated library, and Windows companion are not complete yet. Android build/device testing is pending; the repository does not claim a verified APK until CI succeeds.
