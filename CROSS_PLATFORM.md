# ScamShield AI: iOS & Windows Porting Guide
## Powered by Kotlin Multiplatform (KMP) & Compose Multiplatform (CMP)

Since **ScamShield AI** is built using modern **Jetpack Compose (Material 3)**, **Kotlin StateFlows**, and **Room**, it is fully ready to be migrated to a cross-platform codebase. 

With **Compose Multiplatform** (developed by JetBrains), you can compile the exact same UI and backend code to run natively on **iOS** (using native UIKit/metal rendering) and **Windows/Desktop** (using Skia-backed hardware accelerated graphics in a native JVM-packaged application).

This guide walks you through restructuring the current project, configuring shared dependencies (Ktor, Multiplatform Room, Gemini), and launching iOS and Windows binaries.

---

## 1. Multiplatform Project Structure

In a Kotlin Multiplatform project, you split your codebase into shared (Common) and platform-specific modules. Here is how your directory layout will look:

```text
ScamShieldAI/
├── composeApp/                 # Shared application module
│   ├── build.gradle.kts        # KMP & Compose configurations
│   └── src/
│       ├── commonMain/         # 95%+ of your code lives here!
│       │   ├── kotlin/
│       │   │   ├── com.example/
│       │   │   │   ├── data/           # Common Database, Repos, Models
│       │   │   │   └── ui/             # ALL Compose Screens & Theme
│       │   │   │       ├── theme/
│       │   │   │       ├── dashboard/
│       │   │   │       ├── chat/
│       │   │   │       ├── verify/
│       │   │   │       ├── academy/
│       │   │   │       └── report/
│       │   └── resources/      # Shared assets (images, vectors, fonts)
│       │
│       ├── androidMain/        # Android specific code
│       │   ├── kotlin/
│       │   │   └── com.example/
│       │   │       └── MainActivity.kt
│       │   └── AndroidManifest.xml
│       │
│       ├── iosMain/            # iOS specific code & UI wrapper
│       │   └── kotlin/
│       │       └── MainViewController.kt
│       │
│       └── desktopMain/        # Windows/macOS/Linux Desktop code
│           └── kotlin/
│               └── main.kt     # Main entry point for Windows executable
│
├── iosApp/                     # Xcode Project for iOS
│   ├── iosApp/
│   │   ├── App.swift
│   │   └── ContentView.swift
│   └── iosApp.xcodeproj
│
├── settings.gradle.kts
└── build.gradle.kts            # Project-level configuration
```

---

## 2. Multiplatform `build.gradle.kts` configuration

Replace your `composeApp/build.gradle.kts` with the following configuration. It targets **Android, iOS, and Desktop (JVM/Windows)**, pulls multiplatform dependencies, and configures packaging:

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.google.devtools.ksp)
}

kotlin {
    // 1. Android Target
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    
    // 2. iOS Targets (Generates Swift/Objective-C frameworks)
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    // 3. Desktop Target (Windows / macOS / Linux)
    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                
                // KotlinX Serialization
                implementation(libs.kotlinx.serialization.json)
                
                // Navigation
                implementation(libs.navigation.compose)
                
                // Ktor for Multiplatform Networking & Gemini REST Client
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                
                // Multiplatform Room Database
                implementation(libs.room.runtime)
                implementation(libs.sqlite.bundled)
            }
        }
        
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.ktor.client.okhttp) // Android network engine
            }
        }
        
        val iosMain by getting {
            dependencies {
                implementation(libs.ktor.client.darwin) // iOS network engine
            }
        }
        
        val desktopMain by getting {
            dependencies {
                implementation(compose.preview)
                implementation(libs.ktor.client.cio)    // Windows network engine
            }
        }
    }
}

android {
    namespace = "com.example"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.aistudio.scamshield"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }
}

// Windows/Desktop Packaging config
compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe)
            packageVersion = "1.0.0"
            packageName = "ScamShieldAI"
            
            windows {
                iconFile.set(project.file("desktop_icon.ico"))
                menuGroup = "ScamShield Security"
            }
        }
    }
}
```

---

## 3. Shared Database (Room KMP)

To share the scam reports database seamlessly across Android, iOS, and Windows, configure Room using **Expect/Actual** database builders:

### In `commonMain/kotlin/com/example/data/AppDatabase.kt`:
```kotlin
package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [ScamReport::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scamReportDao(): ScamReportDao
}

// Declare expected platform builder
expect class DatabaseBuilder {
    fun build(): AppDatabase
}
```

### In `androidMain/.../DatabaseBuilder.kt` (Android sqlite driver):
```kotlin
package com.example.data

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.AndroidSqliteDriver

actual class DatabaseBuilder(private val context: Context) {
    actual fun build(): AppDatabase {
        val dbFile = context.getDatabasePath("scam_shield.db")
        return Room.databaseBuilder<AppDatabase>(
            context = context,
            name = dbFile.absolutePath
        )
        .setDriver(AndroidSqliteDriver())
        .build()
    }
}
```

### In `iosMain/.../DatabaseBuilder.kt` (iOS sqlite driver):
```kotlin
package com.example.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import platform.Foundation.NSHomeDirectory

actual class DatabaseBuilder {
    actual fun build(): AppDatabase {
        val dbPath = NSHomeDirectory() + "/Documents/scam_shield.db"
        return Room.databaseBuilder<AppDatabase>(
            name = dbPath,
            factory = { AppDatabase::class.instantiateImpl() } // Generated by Room KSP
        )
        .setDriver(BundledSQLiteDriver())
        .build()
    }
}
```

### In `desktopMain/.../DatabaseBuilder.kt` (Windows JVM sqlite driver):
```kotlin
package com.example.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

actual class DatabaseBuilder {
    actual fun build(): AppDatabase {
        val dbFile = File(System.getProperty("user.home"), ".scamshield/scam_shield.db")
        dbFile.parentFile.mkdirs()
        return Room.databaseBuilder<AppDatabase>(
            name = dbFile.absolutePath
        )
        .setDriver(BundledSQLiteDriver())
        .build()
    }
}
```

---

## 4. Platform Entry Points

### Shared `App()` composable (In `commonMain/.../App.kt`)
This is where your main Compose UI flow lives. It is called by all platforms:
```kotlin
import androidx.compose.runtime.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.ScamViewModel

@Composable
fun App(viewModel: ScamViewModel) {
    MyApplicationTheme {
        MainScreenContainer(viewModel) // Contains your Navigation & BottomBar
    }
}
```

### Windows Entry Point (In `desktopMain/.../main.kt`)
This runs natively on Windows. It launches a standard Win32/64 container:
```kotlin
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.data.DatabaseBuilder
import com.example.ui.ScamViewModel

fun main() = application {
    // Initialize common Room Database
    val db = DatabaseBuilder().build()
    val viewModel = ScamViewModel(db)

    Window(
        onCloseRequest = ::exitApplication,
        title = "ScamShield AI - Cyber Academy & Risk Advisor",
    ) {
        App(viewModel)
    }
}
```

### iOS Entry Point (In `iosMain/.../MainViewController.kt`)
This exports the Compose view controller to UIKit/SwiftUI:
```kotlin
import androidx.compose.ui.window.ComposeUIViewController
import com.example.data.DatabaseBuilder
import com.example.ui.ScamViewModel
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    val db = DatabaseBuilder().build()
    val viewModel = ScamViewModel(db)
    
    return ComposeUIViewController {
        App(viewModel)
    }
}
```

### Xcode Swift App Wrapper (`iosApp/iosApp/ContentView.swift`)
This standard Swift class imports your Kotlin framework and shows the shared UI:
```swift
import SwiftUI
import ComposeApp // Your Kotlin KMP shared framework name

struct ContentView: View {
    var body: some View {
        ComposeView().ignoresSafeArea(.all, edges: .bottom)
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

---

## 5. Building & Distributing

Once structured, you can generate your cross-platform binaries using basic command line instructions:

### Build for Windows (.exe / .msi installer)
Run on a Windows machine:
```bash
./gradlew :composeApp:packageDistributionForCurrentOS
```
This outputs a lightweight, self-contained Windows Installer (`.msi`) or executable (`.exe`) in `composeApp/build/compose/binaries/main/`.

### Build for iOS (Xcode Archive / App Store)
Open the `iosApp/` folder in Xcode on a macOS computer, select your Target Device, and hit **Archive** or **Run**. Xcode will automatically compile your Kotlin code down to native assembly (`Arm64`) and blend it into the App binary.
