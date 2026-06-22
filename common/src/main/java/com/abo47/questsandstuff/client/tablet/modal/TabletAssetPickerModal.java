package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;

import com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint.CanvasBlueprintMiniRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint.CanvasBlueprintStore;
import com.abo47.questsandstuff.client.quest.hud.QuestHudLayout;
import com.abo47.questsandstuff.client.tablet.assets.AssetLibrary;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextActions;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuAnimation;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.controls.PercentSliderControls;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.controls.StyledTextFields;
import com.abo47.questsandstuff.client.tablet.controls.ToggleSwitchWidget;
import com.abo47.questsandstuff.client.tablet.controls.picker.TiledPickerPanel;
import com.abo47.questsandstuff.client.tablet.icons.UiIconAtlas;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.client.tablet.theme.UiThemeManager;
import com.abo47.questsandstuff.client.tablet.theme.WindowChrome;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static com.abo47.questsandstuff.client.tablet.modal.ModalSession.TargetSetSlot.QUEST_BACKGROUND;
import static com.abo47.questsandstuff.client.tablet.modal.ModalSession.TargetSetSlot.QUEST_COMPLETION_SOUND;
import static com.abo47.questsandstuff.client.tablet.modal.ModalSession.TargetSlot.BLUEPRINT;
import static com.abo47.questsandstuff.client.tablet.modal.ModalSession.TargetSlot.HUD_BACKGROUND;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.assetDimensions;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.assetThumbnailTexture;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.button;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.chapterBackgroundTexture;
import static com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions.closeAll;
import static com.abo47.questsandstuff.client.tablet.controls.SearchFilter.crop;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.deleteAssetFile;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.label;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.renameAssetFile;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.searchAssetEntries;
import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;

public final class TabletAssetPickerModal {
    private static final int HEADER_BUTTON_SIZE = 18;
    private static final int HEADER_GAP = 3;
    private static final int HEADER_BUTTON_Y = 1;
    private static final int HEADER_CLOSE_ANCHOR_RIGHT_PAD = 26;
    private static final int HEADER_CLOSE_RENDER_X_OFFSET = 1;

    private TabletAssetPickerModal() {
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        boolean soundPicker = !ModalTargetState.target(state, ModalSession.TargetSlot.QUEST_COMPLETION_SOUND, state.modal.modalQuestCompletionSoundTarget).isBlank()
                || !ModalTargetState.targetSet(state, QUEST_COMPLETION_SOUND, state.modal.modalQuestCompletionSoundTargets).isEmpty();
        boolean blueprintPicker = isBlueprintPicker(state);
        boolean hudPicker = isHudBackgroundPicker(state);
        boolean bottomPreviewControls = isQuestBackgroundPicker(state) || hudPicker;
        boolean imagePicker = !state.modal.modalEcBackgroundTarget.isBlank()
                || !state.modal.modalCanvasBackgroundTarget.isBlank()
                || !state.modal.modalQuestCompletionHudBackgroundTarget.isBlank()
                || !state.modal.modalCanvasImageTarget.isBlank()
                || !state.questDetails.questDetailsAssetPickTarget.isBlank()
                || !state.modal.modalChapterTarget.isBlank()
                || isQuestBackgroundPicker(state)
                || isHudBackgroundPicker(state);
        String title = blueprintPicker
                ? "ui.questsandstuff.modal.blueprints"
                : soundPicker ? "ui.questsandstuff.modal.custom_sounds" : "ui.questsandstuff.modal.assets_library";
        ModalShell.addTitleAndClose(modal, TabletModalPanel.tr(title), w, state, refresh);
        if (blueprintPicker) {
            addBlueprintHeaderActions(modal, state, refresh, w);
        }
        String currentMode = soundPicker ? "sound" : blueprintPicker ? "blueprint" : "image";
        if (state.pickers.assetPickerSessionFresh) {
            String saved = switch (currentMode) {
                case "sound" -> state.pickers.assetBrowseDirSound;
                case "blueprint" -> state.pickers.assetBrowseDirBlueprint;
                default -> state.pickers.assetBrowseDirImage;
            };
            if (!saved.isEmpty()) {
                state.pickers.assetBrowseDir = saved;
            }
            state.pickers.assetPickerSessionFresh = false;
        }
        state.pickers.assetPickerMode = currentMode;
        String dir = state.pickers.assetBrowseDir == null ? "" : state.pickers.assetBrowseDir;
        List<AssetLibrary.AssetEntry> assets = searchAssetEntries(dir, SearchFilter.normalizeUserInput(state.pickers.assetSearch));
        if (soundPicker) {
            assets = filterByKind(assets, AssetLibrary.AssetKind.SOUND);
        } else if (blueprintPicker) {
            assets = filterByKind(assets, AssetLibrary.AssetKind.BLUEPRINT);
        } else if (imagePicker) {
            assets = filterByKind(assets, AssetLibrary.AssetKind.IMAGE, AssetLibrary.AssetKind.GIF);
        }

        ModalLibraryLayout.Metrics libraryLayout = ModalLibraryLayout.calculate(w, h);
        int leftW = libraryLayout.leftW();
        int rightX = libraryLayout.rightX();
        int rightW = libraryLayout.rightW();
        int previewH = libraryLayout.bodyH();
        WidgetGroup preview = ModalLibraryLayout.previewPanel(libraryLayout);
        String selected = state.pickers.assetSelected == null ? "" : state.pickers.assetSelected;
        preview.addWidget(label(8, 8, crop(dir.isBlank() ? "/" : "/" + dir, 22), ModColors.TEXT_SECONDARY));
        preview.addWidget(label(8, 20, selected.isBlank()
                ? TabletModalPanel.tr(blueprintPicker ? "ui.questsandstuff.blueprints.none_selected" : soundPicker ? "ui.questsandstuff.sound.none_selected" : "ui.questsandstuff.asset.none_selected")
                : crop(selected, 22), ModColors.TEXT_SECONDARY));
        AssetLibrary.AssetKind selectedKind = selected.isBlank() ? AssetLibrary.AssetKind.UNKNOWN : AssetLibrary.assetKind(selected);
        AssetLibrary.AssetDimensions dims = selectedKind.hasImageThumbnail() ? assetDimensions(selected) : null;
        if (soundPicker || selectedKind == AssetLibrary.AssetKind.SOUND) {
            String previewSound = selectedKind == AssetLibrary.AssetKind.SOUND ? selected : "";
            if (!previewSound.isBlank()) {
                int volumeY = Math.max(46, previewH - 24);
                int playY = 34;
                int playH = Math.max(34, volumeY - playY - 8);
                preview.addWidget(new SoundPreviewPlayerWidget(8, playY, leftW - 16, playH, previewSound, () -> state.pickers.soundVolumeDraft));
                SoundVolumeControls.add(preview, state, player, refresh, 8, volumeY, leftW - 16, previewSound);
            }
        } else if (blueprintPicker || selectedKind == AssetLibrary.AssetKind.BLUEPRINT) {
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
            boolean grayscale = isQuestBackgroundPicker(state) && state.modal.modalQuestBackgroundGrayscale;
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
        int backY = HEADER_BUTTON_Y;
        int backSize = HEADER_BUTTON_SIZE;
        int firstHeaderButtonX = blueprintPicker ? headerChainButtonX(w, 2) : headerCloseRenderX(w);
        int backX = firstHeaderButtonX - HEADER_GAP - backSize;
        boolean canGoBack = !dir.isBlank();
        int searchW = canGoBack
                ? Math.max(40, backX - rightX - HEADER_GAP)
                : Math.max(40, firstHeaderButtonX - rightX - HEADER_GAP);
        TextFieldWidget search = ModalShell.addSearchField(modal, rightX, controlsY, searchW, controlsH, state.pickers.assetSearch, 80, value -> {
            state.pickers.assetSearch = SearchFilter.normalizeUserInput(value);
            state.pickers.assetGridScroll = 0;
            refresh.run();
        }, focused -> state.pickers.assetSearchFocused = focused);

        if (canGoBack) {
            modal.addWidget(WindowChrome.iconButton(backX, backY, backSize, backSize, "back", UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_DEFAULT), click -> {
                state.pickers.assetBrowseDir = dir.contains("/") ? dir.substring(0, dir.lastIndexOf('/')) : "";
                state.pickers.saveBrowseDirForMode();
                state.pickers.assetGridScroll = 0;
                state.pickers.assetContextOpen = false;
                refresh.run();
            }));
        }

        int listY = libraryLayout.bodyY();
        int listH = libraryLayout.bodyH();
        PickerTileMetrics.Metrics tileMetrics = PickerTileMetrics.calculate(rightW, listH, assets.size());
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
                        () -> state.pickers.assetGridScroll,
                        value -> state.pickers.assetGridScroll = value,
                        () -> state.pickers.assetGridScrollDragging,
                        dragging -> state.pickers.assetGridScrollDragging = dragging
                ),
                () -> {
                    state.pickers.assetContextOpen = false;
                    state.pickers.assetRenameOpen = false;
                },
                refresh,
                (surface, entry, index, x, y, cellW, cellH, layout) -> {
            String relative = entry.relativePath();
            boolean renaming = state.pickers.assetRenameOpen && relative.equals(state.pickers.assetContextFile) && !entry.directory();
            WidgetGroup tile = new WidgetGroup(x, y, cellW, cellH);
            if (relative.equals(selected)) {
                tile.setBackground(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 54)));
            }
            int labelH = 14;
            int iconAreaH = Math.max(24, cellH - labelH - 8);
            int iconSize = Math.max(24, Math.min(96, Math.min(cellW - 24, iconAreaH - 12)));
            int iconX = Math.max(0, (cellW - iconSize) / 2);
            int iconY = Math.max(4, (iconAreaH - iconSize) / 2);
            if (entry.kind() == AssetLibrary.AssetKind.DIRECTORY) {
                var folderIcon = UiIconAtlas.iconTexture("folder");
                if (folderIcon != null) {
                    tile.addWidget(new ImageWidget(iconX, iconY, iconSize, iconSize, folderIcon));
                } else {
                    tile.addWidget(PickerTileText.centeredLabel(0, iconY + iconSize / 2 - 4, cellW, "[dir]", ModColors.TEXT_MUTED));
                }
            } else if (entry.kind() == AssetLibrary.AssetKind.BLUEPRINT) {
                CanvasBlueprint blueprint = CanvasBlueprintStore.read(relative);
                if (!blueprint.isEmpty()) {
                    tile.addWidget(CanvasBlueprintMiniRenderer.previewWidget(4, 4, Math.max(12, cellW - 8), Math.max(16, iconAreaH - 4), blueprint));
                } else {
                    var blueprintIcon = UiIconAtlas.iconTexture("scroll");
                    if (blueprintIcon != null) {
                        tile.addWidget(new ImageWidget(iconX, iconY, iconSize, iconSize, blueprintIcon));
                    }
                }
            } else if (entry.kind().hasImageThumbnail()) {
                IGuiTexture thumb = assetThumbnailTexture(relative);
                if (thumb != null) {
                    int thumbW = Math.max(12, cellW - 14);
                    int thumbH = Math.max(16, iconAreaH - 4);
                    tile.addWidget(new ImageWidget((cellW - thumbW) / 2, 4, thumbW, thumbH, thumb));
                }
            } else if (entry.kind() == AssetLibrary.AssetKind.SOUND) {
                var soundIcon = UiIconAtlas.iconTexture("audio-lines");
                if (soundIcon != null) {
                    tile.addWidget(new ImageWidget(iconX, iconY, iconSize, iconSize, soundIcon));
                }
            } else {
                var fileIcon = UiIconAtlas.iconTexture("file");
                if (fileIcon != null) {
                    tile.addWidget(new ImageWidget(iconX, iconY, iconSize, iconSize, fileIcon));
                }
            }
            if (renaming) {
                tile.addWidget(assetInlineRenameField(state, refresh, relative, 2, cellH - labelH - 1, cellW - 4));
            } else {
                tile.addWidget(PickerTileText.centeredLabel(2, cellH - labelH, cellW - 4, entry.name(), ModColors.TEXT_SECONDARY));
            }
            surface.addWidget(tile);
            if (renaming) {
                return;
            }
            ButtonWidget hit = flatHitButton(x, y, cellW, cellH, click -> {
                if (click.button == 1) {
                    state.pickers.assetContextOpen = true;
                    state.pickers.assetContextFile = relative;
                    anchorAssetContextAtPointer(state, w, h);
                    state.pickers.assetRenameOpen = false;
                    ContextMenuState.clearDeleteConfirm(state);
                    ContextMenuAnimation.start(state, ContextMenuAnimation.DEFAULT_KEY);
                    refresh.run();
                    return;
                }
                boolean doubleClick = click.button == 0 && TabletModalPanel.acceptPickerDoubleClick(state, ModalTargets.doubleClickKey("asset", relative));
                if (entry.directory()) {
                    state.pickers.assetBrowseDir = relative;
                    state.pickers.saveBrowseDirForMode();
                    state.pickers.assetGridScroll = 0;
                    state.pickers.assetContextOpen = false;
                } else {
                    state.pickers.assetSelected = relative;
                    state.pickers.assetContextFile = relative;
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

        if (state.pickers.assetContextOpen && !state.pickers.assetContextFile.isBlank()) {
            addAssetContextDismissLayer(modal, state, refresh, w, h);
            addContext(modal, state, player, refresh, w, h, rightW, assets);
        }
        if (state.modal.blueprintCodeOpen) {
            TabletBlueprintCodeModal.add(modal, state, refresh, w, h);
        }
        return search;
    }

    public static boolean handleKeyPressed(TabletUiState state, Runnable refresh, int keyCode) {
        if (!ModalStateQueries.isOpen(state, ModalWindowManager.ModalType.ASSET_PICKER) || keyCode != GLFW.GLFW_KEY_F3 || state.pickers.assetSelected == null || state.pickers.assetSelected.isBlank()) {
            return false;
        }
        beginInlineRename(state, state.pickers.assetSelected);
        refresh.run();
        return true;
    }

    private static void addAssetContextDismissLayer(WidgetGroup modal, TabletUiState state, Runnable refresh, int w, int h) {
        int rootW = TabletStateQueries.rootWidth(state);
        int rootH = TabletStateQueries.rootHeight(state);
        int modalX = ModalContextMenuPlacement.modalX(state, w);
        int modalY = ModalContextMenuPlacement.modalY(state, h);
        ButtonWidget dismiss = flatHitButton(-modalX, -modalY, rootW, rootH, click -> {
            state.pickers.assetContextOpen = false;
            state.pickers.assetRenameOpen = false;
            ContextMenuState.clearDeleteConfirm(state);
            refresh.run();
        });
        modal.addWidget(dismiss);
    }

    private static TextFieldWidget assetInlineRenameField(TabletUiState state, Runnable refresh, String relative, int x, int y, int w) {
        final TextFieldWidget[] renameRef = new TextFieldWidget[1];
        TextFieldWidget rename = StyledTextFields.commitField(
                x,
                y,
                w,
                12,
                null,
                value -> state.pickers.assetRenameDraft = value == null ? "" : value.trim(),
                () -> {
                    commitAssetRename(state, renameRef[0]);
                    refresh.run();
                },
                () -> {
                    state.pickers.assetRenameOpen = false;
                    refresh.run();
                },
                () -> {
                    commitAssetRename(state, renameRef[0]);
                    refresh.run();
                }
        );
        renameRef[0] = rename;
        rename.setClientSideWidget();
        rename.setMaxStringLength(80);
        rename.setCurrentString(state.pickers.assetRenameDraft.isBlank() ? TabletModalPanel.fileNameFromRelativePath(relative) : state.pickers.assetRenameDraft);
        StyledTextFields.applyStandardStyle(rename, ModColors.SURFACE_BASE, ModColors.BORDER_ACCENT);
        rename.setFocus(true);
        return rename;
    }

    private static void anchorAssetContextAtPointer(TabletUiState state, int modalW, int modalH) {
        state.pickers.assetContextX = ModalContextMenuPlacement.localPointerX(state, modalW);
        state.pickers.assetContextY = ModalContextMenuPlacement.localPointerY(state, modalH);
    }

    private static void addBlueprintHeaderActions(WidgetGroup modal, TabletUiState state, Runnable refresh, int w) {
        int importX = headerChainButtonX(w, 1);
        int exportX = headerChainButtonX(w, 2);
        modal.addWidget(WindowChrome.iconButton(exportX, HEADER_BUTTON_Y, HEADER_BUTTON_SIZE, HEADER_BUTTON_SIZE, "file-up", UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_INTERACTIVE), click -> {
            TabletBlueprintCodeModal.openExport(state, state.pickers.assetSelected);
            refresh.run();
        }));
        modal.addWidget(WindowChrome.iconButton(importX, HEADER_BUTTON_Y, HEADER_BUTTON_SIZE, HEADER_BUTTON_SIZE, "file-down", UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_SUCCESS), click -> {
            TabletBlueprintCodeModal.openImport(state);
            refresh.run();
        }));
    }

    private static int headerChainButtonX(int modalW, int slotLeftOfClose) {
        return headerCloseRenderX(modalW) - slotLeftOfClose * (HEADER_BUTTON_SIZE + HEADER_GAP);
    }

    private static int headerCloseRenderX(int modalW) {
        return modalW - HEADER_CLOSE_ANCHOR_RIGHT_PAD + HEADER_CLOSE_RENDER_X_OFFSET;
    }

    private static void addQuestBackgroundOptions(WidgetGroup preview, TabletUiState state, Runnable refresh, int leftW, int previewH) {
        if (!isQuestBackgroundPicker(state)) {
            return;
        }
        String target = ModalTargetState.targetSet(state, QUEST_BACKGROUND, state.modal.modalQuestBackgroundTargets).isEmpty()
                ? ModalTargetState.target(state, ModalSession.TargetSlot.QUEST_BACKGROUND, state.modal.modalQuestBackgroundTarget)
                : "batch";
        int rowY = Math.max(48, previewH - 24);
        preview.addWidget(label(8, rowY + 3, TabletModalPanel.tr(QuestVocabulary.QUEST_BACKGROUND_GRAYSCALE), ModColors.TEXT_SECONDARY));
        preview.addWidget(new ToggleSwitchWidget(
                "quest_background_grayscale:" + target,
                Math.max(8, leftW - 8 - ToggleSwitchWidget.DEFAULT_WIDTH),
                rowY,
                ToggleSwitchWidget.DEFAULT_WIDTH,
                ToggleSwitchWidget.DEFAULT_HEIGHT,
                () -> state.modal.modalQuestBackgroundGrayscale,
                enabled -> state.modal.modalQuestBackgroundGrayscale = enabled,
                refresh,
                new Component[]{
                        Component.translatable(QuestVocabulary.QUEST_BACKGROUND_GRAYSCALE_TOOLTIP)
                }
        ));
    }

    private static boolean isQuestBackgroundPicker(TabletUiState state) {
        return !ModalTargetState.target(state, ModalSession.TargetSlot.QUEST_BACKGROUND, state.modal.modalQuestBackgroundTarget).isBlank()
                || !ModalTargetState.targetSet(state, QUEST_BACKGROUND, state.modal.modalQuestBackgroundTargets).isEmpty();
    }

    private static boolean isBlueprintPicker(TabletUiState state) {
        return !ModalTargetState.target(state, BLUEPRINT, state.modal.modalBlueprintTarget).isBlank();
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
                    state.modal.modalHudBackgroundOpacityDraft = QuestHudLayout.opacityPercent(element);
                    refresh.run();
                },
                refresh,
                () -> state.modal.modalHudBackgroundOpacityDragging,
                dragging -> state.modal.modalHudBackgroundOpacityDragging = dragging,
                new Component[]{Component.translatable("ui.questsandstuff.hud.opacity")}
        );
        preview.addWidget(button(8, rowY + 32, leftW - 16, 14, TabletModalPanel.tr("ui.questsandstuff.hud.remove_background"), ModColors.SURFACE_PANEL_ALT, ModColors.WARNING, click -> {
            QuestHudLayout.setBackground(element, "");
            state.pickers.assetSelected = "";
            refresh.run();
        }));
    }

    private static boolean isHudBackgroundPicker(TabletUiState state) {
        return hudElement(state) != null;
    }

    private static List<AssetLibrary.AssetEntry> filterByKind(List<AssetLibrary.AssetEntry> entries, AssetLibrary.AssetKind first, AssetLibrary.AssetKind second) {
        return entries.stream()
                .filter(e -> e.directory() || e.kind() == first || e.kind() == second)
                .toList();
    }

    private static List<AssetLibrary.AssetEntry> filterByKind(List<AssetLibrary.AssetEntry> entries, AssetLibrary.AssetKind kind) {
        return entries.stream()
                .filter(e -> e.directory() || e.kind() == kind)
                .toList();
    }

    private static QuestHudLayout.Element hudElement(TabletUiState state) {
        String target = ModalTargetState.target(state, HUD_BACKGROUND, state.modal.modalHudBackgroundTarget);
        if ("completion".equalsIgnoreCase(target)) {
            return QuestHudLayout.Element.COMPLETION;
        }
        if ("pinned".equalsIgnoreCase(target)) {
            return QuestHudLayout.Element.PINNED;
        }
        return null;
    }

    private static void addContext(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int modalW, int modalH, int rightW, List<AssetLibrary.AssetEntry> assets) {
        AssetLibrary.AssetEntry contextEntry = assets.stream()
                .filter(asset -> asset.relativePath().equals(state.pickers.assetContextFile))
                .findFirst()
                .orElse(null);
        boolean isDir = contextEntry != null && contextEntry.directory();
        List<ContextAction> actions = assetContextActions(state, player, contextEntry, isDir);
        int ctxW = Math.min(150, Math.max(96, rightW - 8));
        int rowCount = ContextMenuPanel.rowActionCount(actions);
        int visibleRows = ContextMenuPanel.safeVisibleRows(rowCount, rowCount);
        int menuH = ContextMenuPanel.heightFor(actions, visibleRows);
        ModalContextMenuPlacement.Placement placement = ModalContextMenuPlacement.fitToRootFromModal(state, state.pickers.assetContextX, state.pickers.assetContextY, ctxW, menuH, modalW, modalH);
        int ctxX = placement.x();
        int ctxY = placement.y();
        state.pickers.assetContextMenuX = ctxX;
        state.pickers.assetContextMenuY = ctxY;
        state.pickers.assetContextMenuW = ctxW;
        state.pickers.assetContextMenuH = menuH;
        modal.addWidget(ContextMenuPanel.build(ctxX, ctxY, ctxW, actions, 0, visibleRows, ModColors.BORDER_ACCENT, state, action -> {
            if (action.closeAfterClick()) {
                state.pickers.assetContextOpen = false;
                if (!state.pickers.assetRenameOpen) {
                    state.pickers.assetRenameDraft = "";
                }
                ContextMenuState.clearDeleteConfirm(state);
            }
            refresh.run();
        }, modalW, modalH));
    }

    private static List<ContextAction> assetContextActions(TabletUiState state, Player player, AssetLibrary.AssetEntry contextEntry, boolean isDir) {
        List<ContextAction> actions = new ArrayList<>();
        actions.add(ContextActions.action(TabletModalPanel.tr("ui.questsandstuff.common.use"), isDir ? "open" : "background", ModColors.INTERACTIVE, () -> {
            if (isDir) {
                state.pickers.assetBrowseDir = state.pickers.assetContextFile;
                state.pickers.saveBrowseDirForMode();
                state.pickers.assetGridScroll = 0;
            } else {
                state.pickers.assetSelected = state.pickers.assetContextFile;
                TabletModalPanel.runAssetBackgroundAction(player, state, state.pickers.assetContextFile);
                closeAll(state);
            }
        }));
        if (contextEntry != null && contextEntry.kind() == AssetLibrary.AssetKind.BLUEPRINT) {
            actions.add(ContextActions.action(TabletModalPanel.tr("ui.questsandstuff.blueprints.export"), "file-up", ModColors.INTERACTIVE, () ->
                    TabletBlueprintCodeModal.openExport(state, state.pickers.assetContextFile)));
        }
        if (!isDir) {
            actions.add(ContextActions.rename(TabletModalPanel.tr("ui.questsandstuff.menu.rename"), () -> beginInlineRename(state, state.pickers.assetContextFile)));
        }
        if (!isDir) {
            String deleteKey = "asset:delete:" + state.pickers.assetContextFile;
            actions.add(ContextActions.warningDelete(state, deleteKey, TabletModalPanel.tr("ui.questsandstuff.menu.delete"), () -> {
                deleteAssetFile(state.pickers.assetContextFile);
                state.pickers.assetContextOpen = false;
                state.pickers.assetRenameOpen = false;
            }));
        }
        return actions;
    }

    private static void beginInlineRename(TabletUiState state, String relative) {
        state.pickers.assetContextOpen = false;
        state.pickers.assetContextFile = relative == null ? "" : relative;
        state.pickers.assetRenameOpen = !state.pickers.assetContextFile.isBlank();
        state.pickers.assetRenameDraft = TabletModalPanel.fileNameFromRelativePath(state.pickers.assetContextFile);
        state.pickers.assetSearchFocused = false;
        ContextMenuState.clearDeleteConfirm(state);
    }

    private static void commitAssetRename(TabletUiState state, TextFieldWidget rename) {
        String oldRelative = state.pickers.assetContextFile;
        String parent = TabletModalPanel.parentRelativePath(oldRelative);
        String name = rename == null || rename.getCurrentString() == null ? state.pickers.assetRenameDraft : rename.getCurrentString().trim();
        if (oldRelative == null || oldRelative.isBlank() || name.isBlank()) {
            state.pickers.assetRenameOpen = false;
            state.pickers.assetContextOpen = false;
            return;
        }
        String nextRelative = renamedAssetPath(oldRelative, parent, name);
        renameAssetFile(oldRelative, name);
        state.pickers.assetContextFile = nextRelative;
        state.pickers.assetSelected = nextRelative;
        state.pickers.assetRenameOpen = false;
        state.pickers.assetContextOpen = false;
    }

    private static String renamedAssetPath(String oldRelative, String parent, String name) {
        String nextName = name;
        if (!nextName.contains(".")) {
            String oldName = TabletModalPanel.fileNameFromRelativePath(oldRelative);
            int dot = oldName.lastIndexOf('.');
            if (dot > 0) {
                nextName = nextName + oldName.substring(dot);
            }
        }
        return parent.isBlank() ? nextName : parent + "/" + nextName;
    }
}
