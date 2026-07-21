<p align="right">                                                                                        
  <a href="README_RU.md">Русская версия</a>                                                                                
</p>

<div align="center">

# Fast Messenger

An unofficial VK messenger for Android, built with Kotlin and Jetpack Compose.

[![Latest release](https://img.shields.io/github/v/release/melod1n/fast-messenger?style=flat-square)](https://github.com/melod1n/fast-messenger/releases/latest)
[![Android 6.0+](https://img.shields.io/badge/Android-6.0%2B-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/about/versions/marshmallow)
[![GPL-3.0](https://img.shields.io/github/license/melod1n/fast-messenger?style=flat-square)](LICENSE)

</div>

> [!IMPORTANT]
> Fast Messenger is an independent, unofficial project. It is not affiliated with, endorsed by, or supported by VK.

> [!WARNING]
> The application is under active development. Some features are still incomplete, and changes to VK APIs may temporarily affect its functionality.

## Screenshots

> [!NOTE]
> Screenshots are on the way. Stay tuned...

## About

Fast Messenger is a modern Android client focused on the core VK messaging experience. The project combines a fully Compose-based interface with a feature-oriented multi-module architecture, local persistence, paginated data loading, and real-time updates through VK Long Poll.

The application is also a personal engineering project used to explore and apply modern Android development practices in a non-trivial product with authentication, conversations, rich message content, media handling, background updates, and complex UI state.

## Features

### Account and authorization

- Two-factor authentication with OTP resend
- CAPTCHA and web CAPTCHA handling
- Service and refresh token support
- Automatic token expiration handling
- Token import and export

### Conversations

- Paginated conversation list
- Manual refresh
- Pinned conversations
- Archived conversations
- Conversation deletion
- New chat creation flow

### Messaging

- Real-time updates through VK Long Poll
- Paginated message history
- Manual refresh
- Sending text messages
- Replies, including swipe-to-reply
- Pinned messages
- Message selection and bulk actions
- Message deletion and forwarding
- Sending, read, edited, and error states
- Read-by information
- Channel message support

### Message content

- Photos
- Videos
- Audio
- Documents
- Links
- Stickers
- Polls
- Gifts
- Wall posts and comments
- Replies and forwarded messages

### Media and people

- In-app photo viewer
- External media opening
- Chat materials
- Friend list with sorting
- Online friends filter
- User profiles

### Application settings

- Application and user-specific settings
- In-app language picker
- Persistent local configuration

## Architecture

Fast Messenger uses a feature-oriented multi-module structure. UI state is managed by ViewModels and exposed through Kotlin `StateFlow`. Business logic is separated into domain use cases, while networking, persistence, settings, models, and reusable UI components live in dedicated core modules.

```text
app
├── feature
│   ├── auth
│   ├── chatmaterials
│   ├── convos
│   ├── createchat
│   ├── friends
│   ├── languagepicker
│   ├── messageshistory
│   ├── photoviewer
│   ├── profile
│   └── settings
├── core
│   ├── common
│   ├── data
│   ├── database
│   ├── datastore
│   ├── domain
│   ├── logger
│   ├── model
│   ├── network
│   ├── presentation
│   └── ui
└── build-logic
```

The project also contains custom Gradle convention plugins, a centralized version catalog, module graph validation, and Compose stability analysis.

## Tech stack

| Area | Technologies |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose, Material 3, Compose Navigation |
| State and concurrency | Coroutines, Flow, StateFlow, ViewModel |
| Architecture | Feature-oriented multi-module architecture, domain use cases |
| Networking | Retrofit, OkHttp, Moshi, EitherNet |
| Real-time updates | VK Long Poll |
| Persistence | Room |
| Dependency injection | Koin |
| Media | Coil |
| Build tooling | Gradle Kotlin DSL, version catalog, convention plugins, KSP |
| Quality and diagnostics | Chucker, custom logging, module graph assertions, Compose stability analyzer |
| CI | GitHub Actions |

## Requirements

- Android 6.0 or newer
- JDK 21
- Android SDK 37
- A recent Android Studio version compatible with the project's Android Gradle Plugin

## Build

Clone the repository:

```bash
git clone https://github.com/melod1n/fast-messenger.git
cd fast-messenger
```

Build a debug APK:

```bash
./gradlew assembleDebug
```

The resulting APK can be found in:

```text
app/build/outputs/apk/debug/
```

Release builds require a signing configuration. For a ready-to-install build, use the APK attached to the latest GitHub release.

## Installation

Download the latest available APK from the [Releases](https://github.com/melod1n/fast-messenger/releases) page.

Because the application is distributed outside Google Play, Android may ask for permission to install applications from your browser or file manager.

## Project status

Fast Messenger is a work in progress. The core conversations and messaging flows are implemented, but feature coverage is not yet (or may never be) equal to the official VK client.

Bug reports and focused pull requests are welcome through [GitHub Issues](https://github.com/melod1n/fast-messenger/issues).

## License

Fast Messenger is distributed under the [GNU General Public License v3.0](LICENSE).
