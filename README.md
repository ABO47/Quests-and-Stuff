# Quests and Stuff

[![Version](https://img.shields.io/badge/version-0.7.1--alpha-blue)](https://github.com/ABO47/Quests-and-Stuff/blob/main/changelogs/0.7.1-alpha.md)
[![Minecraft](https://img.shields.io/badge/minecraft-1.20.1-green)](https://www.minecraft.net/en-us/article/minecraft--java-edition-1-20-1)
[![Forge](https://img.shields.io/badge/forge-47.4.10-orange)](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html)
[![Fabric](https://img.shields.io/badge/fabric_api-0.92.2-yellow)](https://modrinth.com/mod/fabric-api/version/0.92.2+1.20.1)
[![License](https://img.shields.io/badge/license-MIT-brightgreen)](LICENSE)

Quests and Stuff is a questing mod for Minecraft 1.20.1 that runs on Forge and Fabric. Its core concept is a **visual canvas**: you lay out quests as nodes, connect them into lines and branches, and then play through them from the same in-game tablet you built them on. There are no scripts and no external tools; everything is created and managed inside the game. I originally made it because existing questing mods either lacked features I wanted or used licenses that would lock my future modpack to specific platforms (I am lying, I will probably never finish that modpack).

## Screenshots

### Main Canvas

![Main canvas outside edit mode](media/screenshots/8.png)

### Quest Details

![Quest details outside edit mode](media/screenshots/7.png)

## Apps

The tablet opens with one keybind (R by default) and holds four apps.

### Quest Editor

The core app for creating and completing quests.
- Visual canvas editor with chapters, quests, prerequisites, and Exclusive Choice branching
- 16 task types (kill, gather, craft, use, interact, visit, stat, XP, advancement, manual check, and more) and 5 reward types (items, XP, loot tables, commands, selectable rewards)
- Canvas blueprints: save layouts and share them with others as copy-paste codes
- Decorative canvas nodes for text, images, items, blocks, and entity previews
- Custom quest icons, backgrounds, connection styles, and completion sounds
- Repeatable, lockable, and hidden quests and chapters
- Minimap for navigating large questlines
- Undo, redo, copy, paste, quick connect, and rename shortcuts
- Pinned quest HUD for tracking progress without opening the tablet, with its own layout editor and per-quest completion backgrounds
- Keybinds for nearly everything, all rebindable in the vanilla controls screen

### Teams

Group up with other players to share quest progress.
- Join through invite codes
- Quest progress is shared across all team members
- Chunk claims belong to the whole team, not one player

### Chunk Claimer

Claim and protect your territory.
- Claim chunks through an interactive map
- Protects against other players breaking, placing, and interacting, plus explosions, mob griefing, fire, and PvP
- Optional chunk force-loading with its own cap, separate from the claim cap

### Settings

Customize your experience in one place.
- Theme picker with pre-defined themes
- Canvas display options: minimap, full screen mode, effect icons, mini notifications
- Completion HUD settings: toggle, sound, and display duration
- Animation toggles per UI area
- Chunk claim protections and caps

## Integrations

- Optional JEI, EMI, and REI support for recipe keybinds and canvas recipe cards

## Dependencies

- Forge: Forge 47.4.10+ and LDLib 1.0.50+
- Fabric: Fabric Loader 0.15.11+, Fabric API 0.92.2+1.20.1, and LDLib 1.0.50+
- Optional: JEI, EMI, or REI (any one of them), see Integrations

## Notes

- My mods and texture packs are officially published only on Modrinth. Since this mod is licensed under the MIT License, you may also see reuploads elsewhere, so please download only from sources you trust and be careful with random files.
- An AI coding agent was used during development. Just putting it out there for transparency. If that bothers you, that is completely fine: use it, avoid it, ignore it, or simply do what you want with it. It is up to you.

## License

<details>
<summary>MIT License</summary>

```
MIT License

Copyright (c) 2025 ABO47

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

</details>

## Third Party Licenses

<details>
<summary>Lucide Icons License</summary>

```
Copyright (c) 2026 Lucide Icons and Contributors

Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted, provided that the above copyright notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
```

</details>
