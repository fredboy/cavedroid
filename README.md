![CaveDroid](assets/gamelogo.png)

[![Android CI](https://github.com/fredboy/cavedroid/actions/workflows/android.yml/badge.svg)](https://github.com/fredboy/cavedroid/actions/workflows/android.yml)
[![Ktlint](https://github.com/fredboy/cavedroid/actions/workflows/ktlint.yml/badge.svg)](https://github.com/fredboy/cavedroid/actions/workflows/ktlint.yml)
[![GitHub Tag](https://img.shields.io/github/v/tag/fredboy/cavedroid)](https://github.com/fredboy/cavedroid/tags)

CaveDroid is a **2D Minecraft-inspired game** for Android, Desktop (Windows, Linux, macOS), and potentially iOS.
Explore, mine, and build in a looped world.

<details>
  <summary>Screenshots</summary>

![Screenshot 1](https://fredboy.ru/pub/cavedroid/screenshots/screenshot_01.png)
![Screenshot 2](https://fredboy.ru/pub/cavedroid/screenshots/screenshot_02.png)
![Screenshot 3](https://fredboy.ru/pub/cavedroid/screenshots/screenshot_03.png)
![Screenshot 4](https://fredboy.ru/pub/cavedroid/screenshots/screenshot_04.png)
![Screenshot 5](https://fredboy.ru/pub/cavedroid/screenshots/screenshot_05.png)
![Screenshot 6](https://fredboy.ru/pub/cavedroid/screenshots/screenshot_06.png)

</details>

---

## Features

- 2D world, looped horizontally
- Craft, mine, and explore
- Procedurally generated world
- Cross-platform: Android, Desktop (Windows/Linux/macOS), iOS (untested)
- Single-player mode (multiplayer not available yet)

---

## Controls

| Action | Touch / Mobile | Keyboard / Mouse |
|--------|----------------|-----------------|
| Move left/right | Drag **joystick** on left half | **A / D** |
| Jump | Tap left side or press **jump button** | **Space** (jump mid-air in Creative = fly) |
| Move cursor / aim | Drag on right side | Move **mouse** |
| Break block | Hold right side | **LMB** while aiming at block |
| Place block (background layer) | Hold right side while aiming empty cell | **RMB** while aiming empty space |
| Activate / Use / Place active block | Tap right side | **RMB click** |
| Attack mob | Tap while aiming at mob | **LMB click** |
| Open inventory | Chest button | **E** |
| Inventory: pick up / move | Drag-n-drop, tap | Click to pick up, Right-click for half stack or place single item |
| Inventory: move single item (touch) | Hold item with one finger + tap target cell with another | N/A |

---

## Binary Releases

You can download APK and JAR builds from [the releases page](https://github.com/fredboy/cavedroid/releases).

---

## Build Instructions

### Android

```bash
./gradlew android:assembleDebug
```

### Desktop

```bash
./gradlew desktop:dist
```

On Windows, use `gradlew.bat` instead of `./gradlew`, though it will fail because of symlinks used to reference assets
directory, so some tweaks are required.

## Setting up the keystore for signing

To build an android release and enable the `desktop:generateSignedJar` task for release builds,
you need a `keystore.properties` file in the root of the project.

Create a file named `keystore.properties` with the following properties:

```properties
# Path to your Java keystore file
releaseKeystorePath=/path/to/your/keystore.jks

# Keystore password
releaseKeystorePassword=yourKeystorePassword

# Alias of the key to use
releaseKeyAlias=yourKeyAlias

# Password for the key
releaseKeyPassword=yourKeyPassword
```

---

## License

### Code
CaveDroid is licensed under the **MIT License**. See [LICENSE](LICENSE) for details.

### Assets

- **Textures**: Pixel Perfection by XSSheep, licensed under [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/)
- **On-screen joystick**: CC-0 from [OpenGameArt.org](https://opengameart.org/content/mmorpg-virtual-joysticks)
- **Font**: LanaPixel by eishiya, licensed under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/)
- **Scripts**: Various scripts from Stack Overflow are distributed under their applicable licenses
- **Attributions**: Licensed assets have an `attribution.txt` file in their directories with applicable attributions.

---

## Contributing

Contributions are welcome! Please open issues or pull requests for suggestions, bug fixes, or improvements.
