# UI Icon SVG Sources

Put editable SVG icons in this folder. Each file name becomes the exported PNG name:

```text
add.svg -> src/main/resources/assets/questsandstuff/textures/gui/icons/add.png
```

Run the exporter with:

```powershell
.\gradlew.bat generateUiIconPngs
```

The exporter writes 256x256 PNGs by default and replaces `currentColor` with white so `UiIconAtlas` can tint icons at runtime. The UI still draws them at normal button sizes, but the larger source texture keeps diagonal and curved Lucide strokes cleaner in Minecraft's renderer.

You can override those defaults:

```powershell
.\gradlew.bat generateUiIconPngs -PuiIconSize=256 -PuiIconColor=#ffffff
```

Connection chevrons and other non-icon UI textures live in `art/source-textures/`.
