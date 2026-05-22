package com.abo47.questsandstuff.client.tablet.modal;


import com.abo47.questsandstuff.client.tablet.assets.AssetLibrary;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.controls.StyledTextFields;
import com.abo47.questsandstuff.client.tablet.icons.UiIconAtlas;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.client.tablet.theme.UiThemeManager;
import com.abo47.questsandstuff.client.tablet.theme.WindowChrome;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

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
        ModalShell.addTitleAndClose(modal, TabletModalPanel.tr("ui.questsandstuff.modal.assets_library"), w, state, refresh);
        String dir = state.assetBrowseDir == null ? "" : state.assetBrowseDir;
        List<AssetLibrary.AssetEntry> assets = searchAssetEntries(dir, SearchFilter.normalizeUserInput(state.assetSearch));

        int leftW = 150;
        int rightX = 166;
        int rightW = w - 174;
        WidgetGroup preview = panel(8, 22, leftW, h - 48, withAlpha(ModColors.SURFACE_PANEL_ALT, 120), ModColors.BORDER_BASE);
        String selected = state.assetSelected == null ? "" : state.assetSelected;
        preview.addWidget(label(8, 8, crop(dir.isBlank() ? "/" : "/" + dir, 22), ModColors.TEXT_SECONDARY));
        preview.addWidget(label(8, 20, selected.isBlank() ? TabletModalPanel.tr("ui.questsandstuff.asset.none_selected") : crop(selected, 22), ModColors.TEXT_SECONDARY));
        AssetLibrary.AssetDimensions dims = selected.isBlank() ? null : assetDimensions(selected);
        preview.addWidget(label(8, 32, dims == null ? TabletModalPanel.tr("ui.questsandstuff.common.none_short") : dims.width() + "x" + dims.height(), ModColors.TEXT_MUTED));
        if (!selected.isBlank() && dims != null) {
            IGuiTexture texture = chapterBackgroundTexture(selected);
            if (texture != null) {
                int areaW = leftW - 16;
                int areaH = h - 100;
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
            state.assetSearch = SearchFilter.normalize(value);
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
        int tileW = 62;
        int tileH = 54;
        int gap = 6;
        TiledPickerPanel.add(
                modal,
                rightX,
                listY,
                rightW,
                listH,
                tileW,
                tileH,
                gap,
                8,
                8,
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
            WidgetGroup tile = panel(x, y, tileW, tileH, relative.equals(selected) ? withAlpha(ModColors.INTERACTIVE, 86) : withAlpha(ModColors.SURFACE_BASE, 46), ModColors.BORDER_BASE);
            if (entry.directory()) {
                var folderIcon = UiIconAtlas.iconTexture("folder");
                if (folderIcon != null) {
                    tile.addWidget(new ImageWidget((tileW - 24) / 2, 8, 24, 24, folderIcon));
                } else {
                    tile.addWidget(label(5, 8, "[dir]", ModColors.TEXT_MUTED));
                }
            } else {
                IGuiTexture thumb = assetThumbnailTexture(relative);
                if (thumb != null) {
                    tile.addWidget(new ImageWidget(5, 4, tileW - 10, 32, thumb));
                } else if (relative.startsWith("sounds/")) {
                    var soundIcon = UiIconAtlas.iconTexture("audio-lines");
                    if (soundIcon != null) {
                        tile.addWidget(new ImageWidget((tileW - 24) / 2, 8, 24, 24, soundIcon));
                    }
                }
            }
            tile.addWidget(label(5, 40, crop(entry.name(), 10), ModColors.TEXT_SECONDARY));
            surface.addWidget(tile);
            ButtonWidget hit = flatHitButton(x, y, tileW, tileH, click -> {
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
            hit.setHoverTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 64)));
            surface.addWidget(hit);
                });

        if (state.assetContextOpen && !state.assetContextFile.isBlank()) {
            addContext(modal, state, player, refresh, rightX, rightW, h, assets);
        }
        return search;
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
