# MISSION_001C_REPORT.md

Legend: VERIFIED = confirmed against the library's own published source or
docs during this investigation. INFERRED = a reasonable conclusion drawn
from related evidence, not directly confirmed. UNKNOWN = not established
either way. REQUIRES DEVICE TEST = cannot be resolved without a real
Gradle build and a real Android device/emulator.

## Dependency coordinates
- VERIFIED: io.github.junkfood02.youtubedl-android:library:0.18.1 and
  :ffmpeg:0.18.1 are the current Maven Central coordinates per the
  library's own README.

## execute() API shape
- CONFLICTING EVIDENCE, RESOLVED BY PREFERRING SOURCE OVER PROSE:
  - README prose example: execute(request, callback, processId), 2-arg
    callback (Float, Long) -> Unit.
  - Library's own real example source (DownloadingExampleActivity.java)
    and an independent third-party API summary: execute(request,
    processId, callback), 3-arg callback of type
    kotlin.jvm.functions.Function3<Float, Long, String, Unit>
    (progress, etaInSeconds, outputLine).
  - This report/implementation follows the source-code version as more
    authoritative than README prose. INFERRED, not compiler-confirmed.
    REQUIRES DEVICE TEST (or at minimum a local Gradle sync + compile) to
    settle definitively.

## destroyProcessById()
- VERIFIED (via README text read earlier in this investigation): a single
  String processId argument stops the associated in-flight process. Not
  affected by the execute() ordering ambiguity above.

## Progress callback type
- INFERRED to be kotlin.jvm.functions.Function3<Float, Long, String, Unit>
  based on direct source-code evidence (see above), which resolves the
  earlier Mission 001-B "SAM conversion" concern: since this is Kotlin's
  own function-type interface (not a custom Java interface), a plain
  Kotlin lambda `{ p, e, l -> }` should satisfy it directly with no
  wrapping needed.

## Native/ABI/manifest requirements
- VERIFIED (library README): abiFilters should include x86, x86_64,
  armeabi-v7a, arm64-v8a for full device coverage; this PoC deliberately
  narrows to arm64-v8a + x86_64 only. android:extractNativeLibs="true"
  is VERIFIED as documented by the library. requestLegacyExternalStorage
  is documented as needed only for API 29 public-storage access, which
  this PoC does not use (app-specific external storage instead) — INFERRED
  not to be needed here, REQUIRES DEVICE TEST to fully confirm.

## SDK levels
- UNKNOWN: the library's actual minSdk floor was not directly confirmed
  against its manifest. minSdk 24 is a conservative default pending local
  Gradle sync.

## Gradle/AGP/Kotlin/Compose versions
- UNKNOWN precise current-latest values: this environment has no access to
  Google's Maven repository or the Gradle plugin portal to check current
  version numbers, so AGP 8.5.2 / Kotlin 1.9.24 / Compose compiler 1.5.14
  / Gradle 8.7 are reasonable-as-of-training defaults, not verified
  current releases. REQUIRES DEVICE TEST (i.e., an actual Gradle sync) to
  confirm or correct.

## FFmpeg decision
- Deferred, as in Mission 001-B: the default test target is expected to
  need no audio/video muxing. Add the :ffmpeg:0.18.1 artifact and call
  FFmpeg.getInstance().init(this) only if a real test proves it necessary.
  UNKNOWN: exact FFmpeg init package/method signature not independently
  re-verified in this pass.

## What this script did NOT do
- Did not run Gradle.
- Did not generate gradlew/gradlew.bat/gradle-wrapper.jar.
- Did not build an APK.
- Did not install or test anything on a device or emulator.
- Did not verify the execute() call compiles.

All of the above remain the developer's next actual steps.
