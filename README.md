# Astryxion's Hats (Fabric 1.20.1)

Port of the Forge 1.20.1 mod to Fabric 1.20.1.

## Requirements

- **Java 17** (Fabric 1.20.1 and Loom require Java 17+). Set `JAVA_HOME` to your JDK 17 installation if you have multiple Java versions.
- Gradle is provided via the wrapper (`gradlew.bat`).

## Build

```bat
.\gradlew.bat build
```

The project is still using Forge APIs in source code. The first successful build run will **fail at `compileJava`** with errors about missing Forge classes. After that, we can replace Forge code with Fabric equivalents (registries, events, config, networking, capabilities).

## Project layout

- `src/main/java/` – Java source (decompiled Forge code; to be ported)
- `src/main/resources/` – Fabric mod metadata (`fabric.mod.json`), `pack.mcmeta`, `assets/`, `data/`
- `build.gradle`, `gradle.properties`, `settings.gradle` – Fabric 1.20.1 build setup

## Run (after porting)

- **Client:** `.\gradlew.bat runClient`
- **Server:** `.\gradlew.bat runServer`
