# CaveDroid
[![Android CI](https://github.com/fredboy/cavedroid/actions/workflows/android.yml/badge.svg)](https://github.com/fredboy/cavedroid/actions/workflows/android.yml)
[![Ktlint](https://github.com/fredboy/cavedroid/actions/workflows/ktlint.yml/badge.svg)](https://github.com/fredboy/cavedroid/actions/workflows/ktlint.yml)
[![GitHub Tag](https://img.shields.io/github/v/tag/fredboy/cavedroid)](https://github.com/fredboy/cavedroid/tags) <br>
2D Minecraft clone for Android and Desktop. <br>
Written in Java using libGDX framework. <br>
<details>
  <summary>Screenshot</summary>

![Screenshot](https://fredboy.ru/pub/cavedroid/screenshot.png)

</details>

## Binary releases
You can download apk and jar from here: <br>
<https://fredboy.ru/pub/cavedroid/>
## Build instructions
You need to publish [my ksp processor](https://github.com/fredboy/automultibind) to mavenLocal repository first.
### Android
To build for Android use <br>
`./gradlew android:assemble` <br>
### Desktop
To build for Desktop use <br>
`./gradlew desktop:dist` <br>
***
On Windows, use `gradlew.bat` instead of `./gradlew`

