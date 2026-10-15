# Quote Tap

A small Android home screen widget that brings a new quote and its author to your home screen. Designed to resize across Samsung Galaxy Z Fold cover and inner displays, and compatible with Android 8.0 (API 26) and newer.

## Features

- Resizable quote widget for home screens
- Tap the widget to fetch another quote
- Choose between ZenQuotes and Quotable
- Set automatic refresh to 1, 6, 12, or 24 hours
- Keeps the latest quote on the device when offline
- No account, API key, or analytics SDK

## Build

Open the project in Android Studio, allow Gradle to sync, then select **Build > Build Bundle(s) / APK(s)**. Install the resulting APK, then long-press the home screen and add **Quote Tap** from the Widgets list. The launcher icon opens source and refresh settings.

Requirements: Android Studio with Android SDK 35; JDK 17; internet access for Gradle dependencies and quote retrieval.

## Quote services

The app makes an HTTPS request to the selected public source when the widget is added, tapped, or reaches its refresh interval:

- [ZenQuotes](https://zenquotes.io/)
- [Quotable](https://github.com/lukePeavey/quotable)

Service availability and response limits are controlled by those providers. Android may defer scheduled refreshes to conserve battery. The currently displayed quote remains available locally if a request fails.

## Project structure

- `app/src/main/java/com/dream/quotetap/` — widget, quote sources, settings, and refresh scheduling
- `app/src/main/res/` — widget layout, provider metadata, and app strings

## License

This project is released under the MIT License. See [LICENSE](LICENSE).
