# DonutSMP TestTools

A client-side Fabric diagnostics starter mod for Minecraft Java 1.21.11.

This is **not** an anti-cheat bypass, cheat client, x-ray, hidden-block ESP, or server-evasion tool.
It records local client measurements for debugging and testing in worlds/servers where you have permission.

## Included

- Local mouse click estimate (rolling one-second window).
- Client-side horizontal movement-speed estimate.
- Distance to the entity currently under the crosshair.
- CSV logging to `logs/donutsmp-testtools/metrics.csv`.
- Press **H** to toggle logging.
- GitHub Actions build artifact and tag-triggered GitHub Release.

No measurements are transmitted to any server or external service.

## Requirements

- Minecraft Java Edition 1.21.11
- Java 21
- Fabric Loader
- Fabric API

## Build locally

With Java 21 and Gradle 8.14.3 installed:

```bash
gradle --no-daemon clean build
```

The JAR is generated in `build/libs/`.

## Upload to GitHub

1. Create an empty GitHub repository.
2. Extract this ZIP and upload the **contents** of the folder to the repository root.
3. Commit to the `main` branch.
4. Open the repository's **Actions** tab. The workflow builds the JAR and uploads it as an artifact.

## Create a GitHub Release

From a local clone:

```bash
git tag v1.0.0
git push origin v1.0.0
```

The workflow builds the mod and attaches the JAR to a GitHub Release.

## Install

Copy the built JAR from `build/libs/` into your Minecraft `mods` folder.
Install Fabric API for Minecraft 1.21.11 as well.

## Measurement notes

- CPS is an estimate from local mouse-button press edges, not server-verified clicks.
- Movement speed is derived from client velocity and is not a server-side measurement.
- Target distance is recorded only when the crosshair points at an entity.
- This project is a starter source package; the GitHub Actions run is the definitive build check for your environment.

## License

MIT
