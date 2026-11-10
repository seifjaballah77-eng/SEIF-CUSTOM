# SEIF CUSTOM
### PERSONAL DEVICE EXPERIENCE

SEIF CUSTOM is a user-controlled personalization project for Android and desktop browsing. The rule is simple: **suggest, never force**. The user decides whether to change appearance, move or hide items, enable motion, or use optional automation.

## First working web milestone
The repository now contains a first browser New Tab implementation in `extension/`:
- Minimal Arabic RTL layout, compact translucent controls, clock/date and web search.
- Add/remove up to 20 custom shortcuts.
- Manual light/dark toggle and adjustable panel transparency.
- Choose a local image or MP4/WebM/OGG video as the New Tab background.
- Video loops muted by default; motion can be paused manually.
- Wallpaper files and preferences stay in local browser storage. Settings can be imported/exported (wallpaper file itself is not included in settings backup).
- No automatic power-saving changes and no automatic light/dark switching.

## Run it
1. Open the repository's `extension/` folder.
2. In a Chromium-based browser, open its extensions page and enable Developer mode.
3. Choose **Load unpacked** and select the `extension` folder.
4. Open a new tab.
5. For a GitHub Pages preview, enable Pages for the `main` branch root, then visit the repository's Pages URL; the root page redirects to `extension/newtab.html`.

## Complete project requirements
See [docs/MASTER-PLAN.md](docs/MASTER-PLAN.md) for the full Android, desktop, icon studio, wallpaper library, widgets, custom keyboard, encrypted vault, biometrics, recovery, optional security actions, and compatibility principles. See [docs/TESTING.md](docs/TESTING.md) for manual checks and known limits.

## Important implementation boundaries
- The browser extension customizes the New Tab page only. It cannot restyle protected browser pages or the Windows shell.
- Android system surfaces such as the notification shade, lock screen, and Settings are restricted. The future Android app will distinguish its own launcher UI from system UI.
- The Android launcher, encrypted vault, biometric flows, keyboard, native widgets, and Windows companion are **planned, not implemented** in this milestone.
- Do not treat a feature as complete until it has been tested on the target browser/device.
