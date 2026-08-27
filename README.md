# Sky yt-dlp — Android PoC (Mission 001-C)

## Purpose
Smallest possible native Android app proving that youtubedl-android (a real
yt-dlp execution engine, not a simulation) can run inside a standalone
Android app: URL in -> real network request -> real download -> local file
on disk, with live progress and cancellation.

This is a PoC, not the final Sky yt-dlp app.

## Prerequisites
- Android Studio (recent stable) OR command-line Android SDK + JDK 17.
- A real Android device with USB debugging enabled (recommended) or an
  emulator image with Google APIs, arm64-v8a or x86_64.
- Internet access on the build machine (first Gradle sync pulls AGP,
  Kotlin, Compose, and the youtubedl-android artifact from Maven Central).
- Internet access on the test device.

## Generate the Gradle wrapper
This installer writes gradle/wrapper/gradle-wrapper.properties but cannot
write gradlew / gradlew.bat / gradle-wrapper.jar (the jar is binary).
Options:
- Open the project in Android Studio; it offers to generate/repair the
  wrapper automatically.
- Or, if you have any system Gradle install:
  gradle wrapper --gradle-version 8.7

## Build
./gradlew assembleDebug

## APK location after build
app/build/outputs/apk/debug/app-debug.apk

## Install on a connected device
adb install -r app/build/outputs/apk/debug/app-debug.apk

## Run the PoC
1. Launch "Sky yt-dlp PoC" on the device.
2. If you see a red init-failure message, stop there and capture the exact
   error text before doing anything else.
3. Use the pre-filled Vimeo URL, or paste a different non-YouTube/
   Instagram/TikTok/X URL yt-dlp supports.
4. Tap Download. Progress/ETA/last yt-dlp output line should update live.
5. Tap Cancel mid-download to confirm it actually stops.
6. On success, confirm a real file exists under
   Android/data/com.skyytdlp.poc/files/ on the device.

## Known limitations
- Default test URL may be stale (14-year-old Vimeo ID from the library's
  own README example).
- No FFmpeg — formats needing audio/video muxing won't fully post-process.
- No retry, no history, no background/foreground service.
- The execute() call argument order was corrected in Mission 001-C based
  on the library's real example source rather than its README prose, but
  is still not independently compiled/confirmed. See the comment block at
  the top of DownloadViewModel.kt if the build fails there.
- YouTube untested on purpose — test it separately once this base PoC is
  confirmed working.
