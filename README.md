# SEIF CUSTOM
### PERSONAL DEVICE EXPERIENCE

SEIF CUSTOM is a cross-device personalization project focused on giving users control over the appearance and organization of their Android device and desktop browser/Windows workspace.

> Project status: foundation and requirements phase. Features described here are planned, not yet implemented.

## Product goals

- A highly configurable Android home experience and customization studio.
- A desktop browser extension for a customizable new-tab experience.
- A possible lightweight Windows companion where browser extensions cannot provide the required capabilities.
- A shared visual system for themes, colors, shapes, icons, widgets, and saved designs.
- A private vault for protected photos and supported app-hiding workflows.

## Planned capabilities

### Personalization studio
- Live preview before applying a design.
- Ready-made themes that can be edited, plus a free-form builder.
- Per-element control of colors, gradients, opacity, borders, shadows, corner shapes, spacing, and motion.
- Large color palette for buttons and interface elements.
- Wallpaper-aware app-label contrast, considering the area immediately behind each label.
- Theme save, duplicate, import, export, and sharing.

### Apps and icons
- Organized icon library with accurate app/package matching.
- Search and category filters.
- Multiple icon shapes and custom image import.
- Optional suitable emoji beside app names.
- Per-device capability checks; never report an icon change as successful unless it was actually applied.

### Android experience
- Custom home/launcher experience, app organization, folders, shortcuts, and widgets.
- Custom keyboard concept with readable key sizes and high-contrast text as non-negotiable requirements.
- Supported system appearance controls, with clear disclosure where Android or the device manufacturer blocks changes.
- Optional automatic light/dark schedules.

### Private vault and security
- Encrypted local storage for photos placed in the vault.
- Supported hiding of apps from the SEIF CUSTOM home experience.
- Biometric unlock using Android-supported fingerprint and face authentication, with device capability checks.
- Password and recovery-key fallback.
- Failed-attempt logging, configurable lockout, and security notifications.
- Any attempt-photo feature must follow Android camera permission and foreground-use rules. No covert background photography; local storage by default.

### Desktop
- Browser extension for new-tab layout, wallpaper, shortcuts, widgets, and themes.
- Lightweight Windows companion only for capabilities that cannot be delivered reliably by an extension.
- Compatibility testing for Chromium-based browsers, including older supported environments.

## Platform and security principles

1. Do not promise unrestricted modification of Android system UI. Status bar, notification shade, lock screen, Settings, and manufacturer-specific controls may be restricted.
2. Separate launcher-level customization from system-level customization.
3. Do not require root for core functionality. Any root-dependent enhancement must be optional, isolated, and clearly explained.
4. Use platform biometric APIs. Face authentication security varies by device; use only modalities the OS reports as suitable for the requested security level.
5. Do not store passwords or biometric data in plaintext. Prefer OS-managed authentication and hardware-backed key storage where available.
6. Vault content must be encrypted at rest; hiding a gallery item alone is not secure storage.
7. No hidden camera capture, covert monitoring, or uploading of security photos without explicit consent.
8. Respect image licenses and source terms for wallpaper assets.

## Delivery roadmap

1. Requirements and platform capability matrix.
2. Repository architecture and design system.
3. Interactive design studio and live device preview.
4. Theme, color, shape, and icon engine.
5. Android launcher foundation and widgets.
6. Vault, biometric authentication, recovery, and lockout.
7. Wallpaper library and scheduled appearance.
8. Browser extension.
9. Windows companion feasibility and implementation.
10. Device/browser compatibility, accessibility, security, and performance testing.

## Current implementation status

This repository is being initialized. No production feature should be considered available until it has been implemented and tested on the target platform.
