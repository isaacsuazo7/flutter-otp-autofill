# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository Overview

This is the `otp_autofill` Flutter plugin that provides automatic OTP/SMS code detection and autofill functionality for Android and iOS applications. The plugin supports both SMS User Consent API and SMS Retriever API on Android.

## Commands

### Build and Test
```bash
# Run static analysis
flutter analyze

# Run all tests
flutter test

# Run a specific test file
flutter test test/otp_interactor_test.dart

# Build example app for Android
cd example && flutter build apk

# Build example app for iOS
cd example && flutter build ios

# Run example app
cd example && flutter run
```

### Development Setup
```bash
# Get dependencies
flutter pub get

# Get dependencies for example app
cd example && flutter pub get

# Validate dependencies
flutter pub run dependency_validator
```

## Architecture

### Core Components

1. **Platform Communication**: The plugin uses `MethodChannel` for platform-specific communication:
   - Channel name: `otp_autofill`
   - Main interactor: `lib/src/otp_interactor.dart`

2. **Android Implementation** (`android/src/main/kotlin/`):
   - `OTPPlugin.kt`: Main plugin class handling method calls
   - Supports two APIs:
     - SMS User Consent API (requires user permission)
     - SMS Retriever API (automatic, requires app signature)
   - App signature generation: `AppSignatureHelper.kt`

3. **iOS Implementation** (`ios/Classes/`):
   - Relies on native iOS autofill capabilities
   - Minimal implementation in `SwiftOTPPlugin.swift`

4. **Strategy Pattern**: 
   - Base strategy: `lib/src/base/strategy.dart`
   - Allows custom OTP retrieval methods (push notifications, etc.)

5. **Custom Controller**:
   - `OTPTextEditController` in `lib/src/otp_text_edit_controller.dart`
   - Automatically handles OTP code parsing and insertion

### Key Method Channels

- `getAppSignature`: Get Android app signature for SMS Retriever
- `startListenUserConsent`: Start SMS User Consent API listener
- `startListenRetriever`: Start SMS Retriever API listener  
- `stopListen`: Stop all listeners
- `listenForCode`: Listen for OTP codes with optional sender/strategy

### Error Handling

Custom exceptions in `lib/src/base/exceptions.dart`:
- `OTPException`: Base exception class
- Platform-specific error codes are handled appropriately

## Testing Approach

- Unit tests use `mocktail` for mocking
- Platform channel testing with `MethodChannel` mocks
- Test files mirror source structure in `test/` directory
- Example app in `example/` for integration testing

## Important Notes

1. **Android Requirements**:
   - minSdkVersion: 19
   - Java 17 for builds
   - SMS format for Retriever API must include app signature

2. **iOS Behavior**:
   - Relies on system autofill
   - No active SMS listening implementation

3. **Code Style**:
   - Uses `surf_lint_rules` for consistent formatting
   - Run `flutter analyze` before committing

4. **Version Constraints**:
   - Flutter SDK: >=2.18.0 <4.0.0
   - Dart SDK: >=2.18.0 <4.0.0

5. **State Management**:
   - Plugin maintains listening state internally
   - Always call `stopListen()` when done to prevent memory leaks