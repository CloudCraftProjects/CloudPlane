# CloudPlane

A 1.21.10 Minecraft server fork of [Paper](https://github.com/PaperMC/Paper) and
[Pufferfish](https://github.com/pufferfish-gg/Pufferfish) with some more patches.
This fork is mainly designed to fit the CloudCraft Minecraft network.

## Features

- Split item lore on newlines on protocol level
- Items support adventure's translatable components
- Instantly refresh various things on player locale change
- Add version info to brand in F3 screen
- Add option for changing Pufferfish config location
- Configurable villager gossip limitations
- Various other fixes and minor performance increases

## Download

https://dl.cloudcraftmc.de/cloudplane

## Building

_Building on Windows is untested and not recommended! Please use WSL instead._

Clone this repository using git, _don't_ download the ZIP. After cloning,
apply the patches using `./gradlew applyAllPatches`. This will take some time.

The runnable paperclip jar can be compiled using `./gradlew createMojmapPaperclipJar`.
