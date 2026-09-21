# AI CHESS ANALYZER

Android chess analysis app using Stockfish.

## What this version does
- Interactive chessboard
- Legal move validation through chesslib
- Stockfish analysis
- Evaluation and mate score
- Search depth selector
- Principal variation
- Best-move arrow
- Undo / redo
- Board flip
- New game
- FEN loading
- ARM64 Stockfish build in CI

## Build the APK

The included GitHub Actions workflow builds the Stockfish ARM64 engine and then builds the Android APK.

1. Create a GitHub repository and upload this project.
2. Push to the `main` branch (or run the workflow manually from Actions).
3. Open **Actions → Build AI CHESS ANALYZER APK**.
4. Download the artifact named `AI-CHESS-ANALYZER-debug`.
5. Inside it is `app-debug.apk`, which can be installed on a compatible Android ARM64 device.

The local environment used to prepare this project does not contain the Android SDK/Gradle toolchain, so an APK was not falsely claimed as locally compiled.

## Stockfish licensing

See `THIRD_PARTY_NOTICES.md`. Stockfish is GPLv3; distribution must comply with the license.
