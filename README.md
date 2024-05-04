# CloudPlane

A 1.20.6 Minecraft server fork of [Pufferfish](https://github.com/pufferfish-gg/Pufferfish) with some more patches.
This fork is mainly designed to fit the CloudCraft Minecraft network.

## Features

- `allowPvP` gamerule
- Split item lore on newlines on protocol level
- Instantly refresh various things on player locale change
- Add version info to brand in F3 screen
- Add option for changing Pufferfish config location
- Various other fixes

## Download

https://nightly.link/CloudCraftProjects/CloudPlane/workflows/build/master/CloudPlane-Artifacts.zip

## Building

_Building on Windows is untested and not recommended!_

Clone this repository, don't download the ZIP. After cloning, apply the patches using `./gradlew applyPatches`.
Then the reobfuscated paperclip jar can be compiled using `./gradlew createReobfPaperclipJar`.
