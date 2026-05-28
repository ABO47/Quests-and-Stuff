package com.abo47.questsandstuff.client.tablet.modal;


import com.abo47.questsandstuff.client.canvas.blueprint.CanvasBlueprintMiniRenderer;
import com.abo47.questsandstuff.client.canvas.blueprint.CanvasBlueprintStore;
import com.abo47.questsandstuff.client.hud.QuestHudLayout;
import com.abo47.questsandstuff.client.tablet.assets.AssetLibrary;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.controls.StyledTextFields;
import com.abo47.questsandstuff.client.tablet.controls.ToggleSwitchWidget;
import com.abo47.questsandstuff.client.tablet.icons.UiIconAtlas;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.client.tablet.theme.UiThemeManager;
import com.abo47.questsandstuff.client.tablet.theme.WindowChrome;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.List;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.addWindowsContextRow;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.assetDimensions;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.assetThumbnailTexture;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.button;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.chapterBackgroundTexture;
import static com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions.closeAll;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.confirmDeleteClick;
import static com.abo47.questsandstuff.client.tablet.controls.SearchFilter.crop;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.deleteAssetFile;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.label;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.panel;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.pendingDeleteLabel;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.renameAssetFile;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.searchAssetEntries;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

public final class TabletAssetPickerModal {
    private TabletAssetPickerModal() {
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        boolean soundPicker = (state.modalQuestCompletionSoundTarget != null && !state.modalQuestCompletionSoundTarget.isBlank()
                || !state.modalQuestCompletionSoundTargets.isEmpty())
                && state.assetBrowseDir != null && state.assetBrowseDir.startsWith("sounds");
        boolean blueprintPicker = isBlueprintPicker(state);
        boolean hudPicker = isHudBackgroundPicker(state);
        boolean bottomPreviewControls = isQuestBackgroundPicker(state) || hudPicker;
        String title = blueprintPicker
                ? "ui.questsandstuff.modal.blueprints"
                : soundPicker ? "ui.questsandstuff.modal.custom_sounds" : "ui.questsandstuff.modal.assets_library";
        ModalShell.addTitleAndClose(modal, TabletModalPanel.tr(title), w, state, refresh);
        String dir = state.assetBrowseDir == null ? "" : state.assetBrowseDir;
        List<AssetLibrary.AssetEntry> assets = searchAssetEntries(dir, SearchFilter.normalizeUserInput(state.assetSearch));
        if (!blueprintPicker) {
            assets = assets.stream()
                    .filter(entry -> !"blueprints".equals(entry.relativePath()) && !entry.relativePath().startsWith("blueprints/"))
                    .toList();
        }

        int leftW = 150;
        int rightX = 166;
        int rightW = w - 174;
        int previewH = h - 48;
        WidgetGroup preview = panel(8, 22, leftW, previewH, withAlpha(ModColors.SURFACE_PANEL_ALT, 120), ModColors.BORDER_BASE);
        String selected = state.assetSelected == null ? "" : state.assetSelected;
        preview.addWidget(label(8, 8, crop(dir.isBlank() ? "/" : "/" + dir, 22), ModColors.TEXT_SECONDARY));
        preview.addWidget(label(8, 20, selected.isBlank()
                ? TabletModalPanel.tr(blueprintPicker ? "ui.questsandstuff.blueprints.none_selected" : soundPicker ? "ui.questsandstuff.sound.none_selected" : "ui.questsandstuff.asset.none_selected")
                : crop(selected, 22), ModColors.TEXT_SECONDARY));
        AssetLibrary.AssetDimensions dims = soundPicker || blueprintPicker || selected.isBlank() ? null : assetDimensions(selected);
        if (soundPicker) {
            String previewSound = selected.startsWith("sounds/") ? selected : "";
            if (!previewSound.isBlank()) {
                int volumeY = Math.max(46, previewH - 24);
                int playY = 34;
                int playH = Math.max(34, volumeY - playY - 8);
                preview.addWidget(new SoundPreviewPlayerWidget(8, playY, leftW - 16, playH, previewSound, () -> state.soundVolumeDraft));
                SoundVolumeControls.add(preview, state, player, refresh, 8, volumeY, leftW - 16, previewSound);
            }
        } else if (blueprintPicker) {
            CanvasBlueprint blueprint = CanvasBlueprintStore.read(selected);
            preview.addWidget(label(8, 32, blueprint.isEmpty()
                    ? TabletModalPanel.tr("ui.questsandstuff.common.none_short")
                    : TabletModalPanel.tr("ui.questsandstuff.blueprints.item_count", blueprint.contentCount()), ModColors.TEXT_MUTED));
            if (!blueprint.isEmpty()) {
                preview.addWidget(CanvasBlueprintMiniRenderer.previewWidget(8, 48, leftW - 16, Math.max(24, previewH - 58), blueprint));
            }
        } else {
            preview.addWidget(label(8, 32, dims == null ? TabletModalPanel.tr("ui.questsandstuff.common.none_short") : dims.width() + "x" + dims.height(), ModColors.TEXT_MUTED));
            addQuestBackgroundOptions(preview, state, refresh, leftW, previewH);
            addHudBackgroundOptions(preview, state, refresh, leftW, previewH);
        }
        if (!selected.isBlank() && dims != null) {
            boolean grayscale = isQuestBackgroundPicker(state) && state.modalQuestBackgroundGrayscale;
            IGuiTexture texture = chapterBackgroundTexture(selected, grayscale);
            if (texture != null) {
                int areaW = leftW - 16;
                int areaH = Math.max(1, h - (bottomPreviewControls ? 132 : 100));
                float scale = Math.min(1f, Math.min((float) areaW / Math.max(1, dims.width()), (float) areaH / Math.max(1, dims.height())));
                int drawW = Math.max(1, Math.round(dims.width() * scale));
                int drawH = Math.max(1, Math.round(dims.height() * scale));
                preview.addWidget(new ImageWidget(8 + Math.max(0, (areaW - drawW) / 2), 48, drawW, drawH, texture));
            }
        }
        modal.addWidget(preview);

        int controlsY = 2;
        int controlsH = 16;
        int backY = 1;
        int backSize = 18;
        int backX = rightX + rightW - backSize - 22;
        boolean canGoBack = !dir.isBlank();
        int searchW = canGoBack ? Math.max(40, backX - rightX - 3) : Math.max(40, rightW - 22);
        TextFieldWidget search = ModalShell.addSearchField(modal, rightX, controlsY, searchW, controlsH, state.assetSearch, 80, value -> {
            state.assetSearch = SearchFilter.normalizeUserInput(value);
            state.assetGridScroll = 0;
            refresh.run();
        }, focused -> state.assetSearchFocused = focused);

        if (canGoBack) {
            modal.addWidget(WindowChrome.iconButton(backX, backY, backSize, backSize, "back", UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_DEFAULT), click -> {
                state.assetBrowseDir = dir.contains("/") ? dir.substring(0, dir.lastIndexOf('/')) : "";
                state.assetGridScroll = 0;
                state.assetContextOpen = false;
                refresh.run();
            }));
        }

        int listY = 22;
        int listH = h - 48;
        TileMetrics tileMetrics = tileMetrics(rightW, listH, assets.size());
        TiledPickerPanel.add(
                modal,
                rightX,
                listY,
                rightW,
                listH,
                tileMetrics.tileW(),
                tileMetrics.tileH(),
                tileMetrics.gap(),
                tileMetrics.pad(),
                tileMetrics.pad(),
                assets,
                "No assets",
                ScrollState.bind(
                        () -> state.assetGridScroll,
                        value -> state.assetGridScroll = value,
                        () -> state.assetGridScrollDragging,
                        dragging -> state.assetGridScrollDragging = dragging
                ),
                () -> {
                    state.assetContextOpen = false;
                    state.assetRenameOpen = false;
                },
                refresh,
                (surface, entry, index, x, y, cellW, cellH, layout) -> {
            String relative = entry.relativePath();
            WidgetGroup tile = new WidgetGroup(x, y, cellW, cellH);
            if (relative.equals(selected)) {
                tile.setBackground(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 54)));
            }
            int labelH = 14;
            int iconAreaH = Math.max(24, cellH - labelH - 8);
            int iconSize = Math.max(24, Math.min(96, Math.min(cellW - 24, iconAreaH - 12)));
            int iconX = Math.max(0, (cellW - iconSize) / 2);
            int iconY = Math.max(4, (iconAreaH - iconSize) / 2);
            if (entry.directory()) {
                var folderIcon = UiIconAtlas.iconTexture("folder");
                if (folderIcon != null) {
                    tile.addWidget(new ImageWidget(iconX, iconY, iconSize, iconSize, folderIcon));
                } else {
                    tile.addWidget(centeredLabel(0, iconY + iconSize / 2 - 4, cellW, "[dir]", ModColors.TEXT_MUTED));
                }
            } else {
                IGuiTexture thumb = assetThumbnailTexture(relative);
                if (thumb != null) {
                    int thumbW = Math.max(12, cellW - 14);
                    int thumbH = Math.max(16, iconAreaH - 4);
                    tile.addWidget(new ImageWidget((cellW - thumbW) / 2, 4, thumbW, thumbH, thumb));
                } else if (relative.startsWith("sounds/")) {
                    var soundIcon = UiIconAtlas.iconTexture("audio-lines");
                    if (soundIcon != null) {
                        tile.addWidget(new ImageWidget(iconX, iconY, iconSize, iconSize, soundIcon));
                    }
                } else if (relative.startsWith("blueprints/")) {
                    var blueprintIcon = UiIconAtlas.iconTexture("scroll");
                    if (blueprintIcon != null) {
                        tile.addWidget(new ImageWidget(iconX, iconY, iconSize, iconSize, blueprintIcon));
                    }
                }
            }
            tile.addWidget(centeredLabel(2, cellH - labelH, cellW - 4, entry.name(), ModColors.TEXT_SECONDARY));
            surface.addWidget(tile);
            ButtonWidget hit = flatHitButton(x, y, cellW, cellH, click -> {
                if (click.button == 1) {
                    state.assetContextOpen = true;
                    state.assetContextFile = relative;
                    state.assetContextX = x;
                    state.assetContextY = y;
                    refresh.run();
                    return;
                }
                boolean doubleClick = click.button == 0 && TabletModalPanel.acceptPickerDoubleClick(state, ModalTargets.doubleClickKey("asset", relative));
                if (entry.directory()) {
                    state.assetBrowseDir = relative;
                    state.assetGridScroll = 0;
                    state.assetContextOpen = false;
                } else {
                    state.assetSelected = relative;
                    state.assetContextFile = relative;
                    if (doubleClick) {
                        TabletModalPanel.runAssetBackgroundAction(player, state, relative);
                        closeAll(state);
                    }
                }
                refresh.run();
            });
            hit.setHoverTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 38)));
            surface.addWidget(hit);
                });

        if (state.assetContextOpen && !state.assetContextFile.isBlank()) {
            addContext(modal, state, player, refresh, rightX, rightW, h, assets);
        }
        return search;
    }

    private static TileMetrics tileMetrics(int panelW, int panelH, int entryCount) {
        int pad = 10;
        int gap = 10;
        int count = Math.max(1, entryCount);
        int contentW = Math.max(1, panelW - pad * 2);
        int contentH = Math.max(1, panelH - pad * 2);
        int bestCols = 1;
        int bestRows = count;
        int bestScore = Integer.MIN_VALUE;
        int maxCols = Math.max(1, Math.min(count, (contentW + gap) / (72 + gap)));
        for (int cols = 1; cols <= maxCols; cols++) {
            int rows = (count + cols - 1) / cols;
            int tileW = (contentW - gap * (cols - 1)) / cols;
            int tileH = (contentH - gap * (rows - 1)) / rows;
            if (tileW < 48 || tileH < 48) {
                continue;
            }
            int emptySlots = cols * rows - count;
            int balance = Math.min(tileW, tileH);
            int area = tileW * tileH / 100;
            int aspectPenalty = Math.abs(tileW - tileH) / 4;
            int score = balance * 10 + area - aspectPenalty - emptySlots * 12;
            if (score > bestScore) {
                bestScore = score;
                bestCols = cols;
                bestRows = rows;
            }
        }
        int tileW = Math.max(48, (contentW - gap * (bestCols - 1)) / bestCols);
        int tileH = Math.max(48, (contentH - gap * (bestRows - 1)) / bestRows);
        return new TileMetrics(tileW, tileH, gap, pad);
    }

    private static WidgetGroup centeredLabel(int x, int y, int w, String text, int color) {
        String safeText = text == null ? "" : text;
        return new WidgetGroup(x, y, w, 10) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                var font = Minecraft.getInstance().font;
                String fitted = fitText(safeText, Math.max(1, getSizeWidth()));
                int drawX = getPositionX() + Math.max(0, (getSizeWidth() - font.width(fitted)) / 2);
                graphics.drawString(font, fitted, drawX, getPositionY(), color, true);
            }
        };
    }

    private static String fitText(String text, int width) {
        var font = Minecraft.getInstance().font;
        if (font.width(text) <= width) {
            return text;
        }
        String suffix = "..";
        return font.plainSubstrByWidth(text, Math.max(1, width - font.width(suffix))) + suffix;
    }

    private record TileMetrics(int tileW, int tileH, int gap, int pad) {
    }

    private static void addQuestBackgroundOptions(WidgetGroup preview, TabletUiState state, Runnable refresh, int leftW, int previewH) {
        if (!isQuestBackgroundPicker(state)) {
            return;
        }
        String target = state.modalQuestBackgroundTargets.isEmpty() ? state.modalQuestBackgroundTarget.trim() : "batch";
        int rowY = Math.max(48, previewH - 24);
        preview.addWidget(label(8, rowY + 3, TabletModalPanel.tr(QuestVocabulary.QUEST_BACKGROUND_GRAYSCALE), ModColors.TEXT_SECONDARY));
        preview.addWidget(new ToggleSwitchWidget(
                "quest_background_grayscale:" + target,
                Math.max(8, leftW - 8 - ToggleSwitchWidget.DEFAULT_WIDTH),
                rowY,
                ToggleSwitchWidget.DEFAULT_WIDTH,
                ToggleSwitchWidget.DEFAULT_HEIGHT,
                () -> state.modalQuestBackgroundGrayscale,
                enabled -> state.modalQuestBackgroundGrayscale = enabled,
                refresh,
                new Component[]{
                        Component.translatable(QuestVocabulary.QUEST_BACKGROUND_GRAYSCALE_TOOLTIP)
                }
        ));
    }

    private static boolean isQuestBackgroundPicker(TabletUiState state) {
        return state.modalQuestBackgroundTarget != null && !state.modalQuestBackgroundTarget.trim().isBlank()
                || !state.modalQuestBackgroundTargets.isEmpty();
    }

    private static boolean isBlueprintPicker(TabletUiState state) {
        return state.modalBlueprintTarget != null && !state.modalBlueprintTarget.trim().isBlank();
    }

    private static void addHudBackgroundOptions(WidgetGroup preview, TabletUiState state, Runnable refresh, int leftW, int previewH) {
        QuestHudLayout.Element element = hudElement(state);
        if (element == null) {
            return;
        }
        int rowY = Math.max(58, previewH - 56);
        preview.addWidget(label(8, rowY, TabletModalPanel.tr("ui.questsandstuff.hud.opacity"), ModColors.TEXT_SECONDARY));
        PercentSliderControls.add(
                preview,
                8,
                rowY + 12,
                leftW - 16,
                QuestHudLayout.opacityPercent(element),
                next -> {
                    QuestHudLayout.setOpacityPercent(element, next);
                    state.modalHudBackgroundOpacityDraft = QuestHudLayout.opacityPercent(element);
                    refresh.run();
                },
                refresh,
                () -> state.modalHudBackgroundOpacityDragging,
                dragging -> state.modalHudBackgroundOpacityDragging = dragging,
                new Component[]{Component.translatable("ui.questsandstuff.hud.opacity")}
        );
        preview.addWidget(button(8, rowY + 32, leftW - 16, 14, TabletModalPanel.tr("ui.questsandstuff.hud.remove_background"), ModColors.SURFACE_PANEL_ALT, ModColors.WARNING, click -> {
            QuestHudLayout.setBackground(element, "");
            state.assetSelected = "";
            refresh.run();
        }));
    }

    private static boolean isHudBackgroundPicker(TabletUiState state) {
        return hudElement(state) != null;
    }

    private static QuestHudLayout.Element hudElement(TabletUiState state) {
        String target = state.modalHudBackgroundTarget == null ? "" : state.modalHudBackgroundTarget.trim();
        if ("completion".equalsIgnoreCase(target)) {
            return QuestHudLayout.Element.COMPLETION;
        }
        if ("pinned".equalsIgnoreCase(target)) {
            return QuestHudLayout.Element.PINNED;
        }
        return null;
    }

    private static void addContext(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int rightX, int rightW, int h, List<AssetLibrary.AssetEntry> assets) {
        int ctxW = Math.min(132, rightW - 8);
        int ctxH = state.assetRenameOpen ? 84 : 46;
        int ctxX = rightX + Math.max(4, Math.min(state.assetContextX, rightW - ctxW - 4));
        int ctxY = 22 + Math.max(4, Math.min(state.assetContextY, h - 48 - ctxH - 4));
        WidgetGroup ctx = panel(ctxX, ctxY, ctxW, ctxH, withAlpha(ModColors.SURFACE_BASE, 236), ModColors.BORDER_ACCENT);
        boolean isDir = assets.stream().anyMatch(asset -> asset.relativePath().equals(state.assetContextFile) && asset.directory());
        addWindowsContextRow(ctx, 4, ctxW - 8, TabletModalPanel.tr("ui.questsandstuff.common.use"), "background", click -> {
            if (isDir) {
                state.assetBrowseDir = state.assetContextFile;
            } else {
                state.assetSelected = state.assetContextFile;
                TabletModalPanel.runAssetBackgroundAction(player, state, state.assetContextFile);
                closeAll(state);
            }
            state.assetContextOpen = false;
            refresh.run();
        });
        String deleteKey = "asset:delete:" + state.assetContextFile;
        addWindowsContextRow(ctx, 18, ctxW - 8, pendingDeleteLabel(state, deleteKey, TabletModalPanel.tr("ui.questsandstuff.menu.delete")), "delete", click -> {
            if (!confirmDeleteClick(state, deleteKey)) {
                refresh.run();
                return;
            }
            if (!isDir) {
                deleteAssetFile(state.assetContextFile);
            }
            state.assetContextOpen = false;
            refresh.run();
        });
        addWindowsContextRow(ctx, 32, ctxW - 8, TabletModalPanel.tr("ui.questsandstuff.menu.rename"), "rename", click -> {
            state.assetRenameOpen = true;
            state.assetRenameDraft = TabletModalPanel.fileNameFromRelativePath(state.assetContextFile);
            refresh.run();
        });
        if (state.assetRenameOpen) {
            final TextFieldWidget[] renameRef = new TextFieldWidget[1];
            TextFieldWidget rename = StyledTextFields.commitField(
                    6,
                    50,
                    ctxW - 12,
                    12,
                    null,
                    value -> state.assetRenameDraft = value == null ? "" : value.trim(),
                    () -> {
                        commitAssetRename(state, renameRef[0]);
                        refresh.run();
                    },
                    () -> {
                        state.assetRenameOpen = false;
                        refresh.run();
                    },
                    () -> {
                    }
            );
            renameRef[0] = rename;
            rename.setClientSideWidget();
            rename.setCurrentString(state.assetRenameDraft);
            StyledTextFields.applyStandardStyle(rename, ModColors.SURFACE_BASE, ModColors.BORDER_BASE);
            ctx.addWidget(rename);
            ctx.addWidget(button(6, 66, ctxW - 12, 12, TabletModalPanel.tr("ui.questsandstuff.common.done"), ModColors.SURFACE_PANEL_ALT, ModColors.SUCCESS, click -> {
                commitAssetRename(state, rename);
                refresh.run();
            }));
        }
        modal.addWidget(ctx);
    }

    private static void commitAssetRename(TabletUiState state, TextFieldWidget rename) {
        String oldRelative = state.assetContextFile;
        String parent = TabletModalPanel.parentRelativePath(oldRelative);
        String name = rename == null || rename.getCurrentString() == null ? state.assetRenameDraft : rename.getCurrentString().trim();
        renameAssetFile(oldRelative, name);
        state.assetContextFile = parent.isBlank() ? name : parent + "/" + name;
        state.assetRenameOpen = false;
        state.assetContextOpen = false;
    }
}
