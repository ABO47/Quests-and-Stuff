# UI Texture SVG Sources

Put editable SVG sources for non-toolbar UI textures in this folder.

```text
chevron.svg -> src/main/resources/assets/questsandstuff/textures/gui/chevron.png
```

Run the exporter with:

```powershell
.\gradlew.bat generateUiTexturePngs
```

The exporter writes 256x256 PNGs by default and replaces `currentColor` with white so the renderer can tint the texture.
