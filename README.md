# CaveDroid

[![Android CI](https://github.com/fredboy/cavedroid/actions/workflows/android.yml/badge.svg)](https://github.com/fredboy/cavedroid/actions/workflows/android.yml)
[![Ktlint](https://github.com/fredboy/cavedroid/actions/workflows/ktlint.yml/badge.svg)](https://github.com/fredboy/cavedroid/actions/workflows/ktlint.yml)
[![GitHub Tag](https://img.shields.io/github/v/tag/fredboy/cavedroid)](https://github.com/fredboy/cavedroid/tags)

CaveDroid is a **2D Minecraft-inspired game** for Android, Desktop (Windows, Linux, macOS), and potentially iOS.
Explore, mine, and build in a looped world.

<details>
  <summary>Screenshot</summary>

![Screenshot](https://fredboy.ru/pub/cavedroid/screenshot.png)

</details>

---

## Features

- 2D world, looped horizontally
- Craft, mine, and explore
- Procedurally generated world
- Cross-platform: Android, Desktop (Windows/Linux/macOS), iOS (untested)
- Single-player mode (multiplayer not available yet)

---

## Binary Releases

You can download APK and JAR builds from [the releases page](https://github.com/fredboy/cavedroid/releases).

---

## Build Instructions

### Android

```bash
./gradlew android:assemble
```

### Desktop

```bash
./gradlew desktop:dist
```

On Windows, use `gradlew.bat` instead of `./gradlew`

---

## License

### Code
CaveDroid is licensed under the **MIT License**. See [LICENSE](LICENSE) for details.

### Assets

- **Textures**: Pixel Perfection by XSSheep, licensed under [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/)
- **On-screen joystick**: CC-0 from [OpenGameArt.org](https://opengameart.org/content/mmorpg-virtual-joysticks)
- **Font**: Minecraft Font by JDGraphics, Public Domain ([Fontspace](https://www.fontspace.com/minecraft-font-f28180))
- **Scripts**: Various scripts from Stack Overflow are distributed under their applicable licenses

---

## Contributing

Contributions are welcome! Please open issues or pull requests for suggestions, bug fixes, or improvements.
