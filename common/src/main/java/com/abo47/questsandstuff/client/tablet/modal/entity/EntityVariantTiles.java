package com.abo47.questsandstuff.client.tablet.modal.entity;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.controls.DragScrollBarWidget;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.controls.picker.TiledPickerPanel;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.entity.variant.EntityVariantCatalog;
import com.abo47.questsandstuff.client.tablet.icons.UiIconAtlas;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.modal.PickerTileText;
import com.abo47.questsandstuff.client.tablet.modal.TabletModalPanel;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.client.tablet.theme.UiThemeManager;
import com.abo47.questsandstuff.client.tablet.theme.WindowChrome;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.Locale;

import static com.abo47.questsandstuff.client.tablet.controls.SearchFilter.crop;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.label;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.panel;
import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;
import static com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions.closeAll;

final class EntityVariantTiles {
    static final int RIGHT_X = 166;
    private static final int PAD = 8;
    private static final int LEFT_W = 150;
    private static final int TILE_COLUMNS = 3;
    private static final int TILE_ROWS = 3;
    private static final int TILE_GAP = 6;
    private static final int TILE_PAD = 8;
    private static final int ENTITY_FRONT_YAW = EntityPreviewRenderer.FRONT_ENTITY_YAW;
    private static final int PREVIEW_SPIN_SPEED = 36;

    private EntityVariantTiles() {
    }

    static void addPreview(WidgetGroup modal, Player player, TabletUiState state, Runnable refresh, EntityVariantPickerModel model, int h) {
        WidgetGroup preview = panel(PAD, 22, LEFT_W, h - 48, withAlpha(ModColors.SURFACE_PANEL_ALT, 120), ModColors.BORDER_BASE);
        preview.addWidget(label(8, 8, crop(EntityPreviewRenderer.entityDisplayName(model.entityId()), 22), ModColors.TEXT_SECONDARY));
        preview.addWidget(label(8, 22, crop(EntityVariantCatalog.labelFor(model.entityId(), model.selected()), 22), ModColors.TEXT_PRIMARY));
        preview.addWidget(new WidgetGroup(10, 42, LEFT_W - 20, Math.max(48, h - 98)) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                EntityPreviewRenderer.renderEntityAsset(
                        graphics,
                        getPositionX(),
                        getPositionY(),
                        getSizeWidth(),
                        getSizeHeight(),
                        EntityPreviewRenderer.entityAsset(model.entityId(), model.selected()),
                        ENTITY_FRONT_YAW,
                        PREVIEW_SPIN_SPEED,
                        partialTicks
                );
            }
        });
        modal.addWidget(preview);
    }

    static void addBackButton(WidgetGroup modal, int x, int y, int w, int h, Runnable action) {
        modal.addWidget(WindowChrome.iconButton(x, y, w, h, "back", UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_DEFAULT), click -> action.run()));
    }

    static void addTiles(WidgetGroup modal, Player player, TabletUiState state, Runnable refresh, EntityVariantPickerModel model, int w, int h) {
        int rightW = w - 174;
        int listH = h - 48;
        TileMetrics metrics = tileMetrics(rightW, listH, model.tiles().size());
        TiledPickerPanel.add(
                modal,
                RIGHT_X,
                22,
                rightW,
                listH,
                metrics.tileW(),
                metrics.tileH(),
                TILE_GAP,
                TILE_PAD,
                TILE_PAD,
                model.tiles(),
                model.emptyText(),
                ScrollState.bind(
                        () -> state.pickers.entityVariantScroll,
                        value -> state.pickers.entityVariantScroll = value,
                        () -> state.pickers.entityVariantScrollDragging,
                        dragging -> state.pickers.entityVariantScrollDragging = dragging
                ),
                null,
                refresh,
                (surface, tileEntry, index, tileX, tileY, tileW, tileH, layout) -> addTile(surface, player, state, refresh, model, tileEntry, tileX, tileY, tileW, tileH)
        );
    }

    private static TileMetrics tileMetrics(int rightW, int listH, int count) {
        int safeCount = Math.max(1, count);
        boolean showScroll = safeCount > TILE_COLUMNS * TILE_ROWS;
        int contentW = Math.max(1, rightW - TILE_PAD * 2 - (showScroll ? DragScrollBarWidget.RESERVED_WIDTH + TILE_GAP : 0));
        int contentH = Math.max(1, listH - TILE_PAD * 2);
        int tileW = Math.max(54, (contentW - TILE_GAP * (TILE_COLUMNS - 1)) / TILE_COLUMNS);
        int tileH = Math.max(60, (contentH - TILE_GAP * (TILE_ROWS - 1)) / TILE_ROWS);
        return new TileMetrics(tileW, tileH);
    }

    private static void addTile(WidgetGroup surface, Player player, TabletUiState state, Runnable refresh, EntityVariantPickerModel model, EntityVariantTile tileEntry, int tileX, int tileY, int tileW, int tileH) {
        if (tileEntry.folder()) {
            addFolderTile(surface, state, refresh, model, tileEntry.folderEntry(), tileX, tileY, tileW, tileH);
            return;
        }
        addVariantTile(surface, player, state, refresh, model, tileEntry.variantEntry(), tileX, tileY, tileW, tileH);
    }

    private static void addFolderTile(WidgetGroup surface, TabletUiState state, Runnable refresh, EntityVariantPickerModel model, EntityVariantCatalog.VariantFolder folder, int tileX, int tileY, int tileW, int tileH) {
        boolean active = folder.key().equals(EntityVariantCatalog.variantFolderFor(model.entityId(), model.selected()));
        surface.addWidget(folderTile(folder, active, tileX, tileY, tileW, tileH));
        ButtonWidget hit = flatHitButton(tileX, tileY, tileW, tileH, click -> {
            state.pickers.entityVariantFolder = folder.key();
            state.pickers.entityVariantSelected = EntityVariantCatalog.defaultVariantForFolder(model.entityId(), folder.key());
            state.pickers.entityVariantSearch = "";
            state.pickers.entityVariantSearchFocused = false;
            state.pickers.entityVariantScroll = 0;
            QuestsAndStuffMod.debugLog("[QnS:UI] entity variant folder opened target={} entity={} folder={}", model.target(), model.entityId(), folder.key());
            refresh.run();
        });
        hit.setHoverTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 64)));
        hit.setClickedTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 90)));
        surface.addWidget(hit);
    }

    private static void addVariantTile(WidgetGroup surface, Player player, TabletUiState state, Runnable refresh, EntityVariantPickerModel model, EntityVariantCatalog.VariantEntry entry, int tileX, int tileY, int tileW, int tileH) {
        boolean active = entry.key().equals(model.selected());
        surface.addWidget(variantTile(model.entityId(), entry, active, tileX, tileY, tileW, tileH, model.activeFolder()));
        ButtonWidget hit = flatHitButton(tileX, tileY, tileW, tileH, click -> {
            state.pickers.entityVariantSelected = entry.key();
            QuestsAndStuffMod.debugLog("[QnS:UI] entity variant selected target={} entity={} variant={}", model.target(), model.entityId(), entry.key());
            if (click.button == 0 && TabletModalPanel.acceptPickerDoubleClick(state, ModalTargets.doubleClickKey("entity_variant", model.target(), entry.key()))) {
                EntityVariantApplyActions.apply(player, state, model.target(), entry.key());
                closeAll(state);
            }
            refresh.run();
        });
        hit.setHoverTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 64)));
        hit.setClickedTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 90)));
        surface.addWidget(hit);
    }

    private static WidgetGroup folderTile(EntityVariantCatalog.VariantFolder folder, boolean active, int x, int y, int tileW, int tileH) {
        WidgetGroup tile = new WidgetGroup(x, y, tileW, tileH);
        if (active) {
            tile.setBackground(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 86)));
        }
        int labelH = 14;
        int iconAreaH = Math.max(24, tileH - labelH - 8);
        int iconSize = Math.max(20, Math.min(44, Math.min(tileW - 18, iconAreaH - 8)));
        int iconX = Math.max(0, (tileW - iconSize) / 2);
        int iconY = Math.max(4, (iconAreaH - iconSize) / 2);
        var folderIcon = UiIconAtlas.iconTexture("folder");
        if (folderIcon != null) {
            tile.addWidget(new ImageWidget(iconX, iconY, iconSize, iconSize, folderIcon));
        } else {
            tile.addWidget(PickerTileText.centeredLabel(0, iconY + iconSize / 2 - 4, tileW, "[dir]", ModColors.TEXT_MUTED));
        }
        tile.addWidget(PickerTileText.centeredLabel(2, tileH - labelH, tileW - 4, folder.label(), active ? ModColors.TEXT_PRIMARY : ModColors.TEXT_SECONDARY));
        return tile;
    }

    private static WidgetGroup variantTile(String entityId, EntityVariantCatalog.VariantEntry entry, boolean active, int x, int y, int tileW, int tileH, String folderKey) {
        WidgetGroup tile = new WidgetGroup(x, y, tileW, tileH);
        if (active) {
            tile.setBackground(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 86)));
        }
        int labelH = 14;
        int previewH = Math.max(28, tileH - labelH - 8);
        tile.addWidget(new WidgetGroup(3, 2, Math.max(16, tileW - 6), previewH) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                EntityPreviewRenderer.renderTileEntityAsset(
                        graphics,
                        getPositionX(),
                        getPositionY(),
                        getSizeWidth(),
                        getSizeHeight(),
                        EntityPreviewRenderer.entityAsset(entityId, entry.key()),
                        ENTITY_FRONT_YAW,
                        0,
                        0,
                        0.0F
                );
            }
        });
        tile.addWidget(PickerTileText.centeredLabel(2, tileH - labelH, tileW - 4, tileLabel(entityId, entry, folderKey), active ? ModColors.TEXT_PRIMARY : ModColors.TEXT_SECONDARY));
        return tile;
    }

    private static String tileLabel(String entityId, EntityVariantCatalog.VariantEntry entry, String folderKey) {
        String label = entry.label();
        if (EntityVariantCatalog.hasVariantFolders(entityId) && folderKey != null && !folderKey.isBlank()) {
            String prefix = EntityVariantCatalog.variantFolderLabel(entityId, folderKey).toLowerCase(Locale.ROOT) + " ";
            if (label.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                label = label.substring(prefix.length());
            }
        }
        String entityName = EntityPreviewRenderer.entityDisplayName(entityId).toLowerCase(Locale.ROOT);
        String lower = label.toLowerCase(Locale.ROOT);
        String suffix = " " + entityName;
        if (!entityName.isBlank() && lower.endsWith(suffix)) {
            return label.substring(0, label.length() - suffix.length());
        }
        if (entityId.contains("villager") && lower.endsWith(" villager")) {
            return label.substring(0, label.length() - " villager".length());
        }
        return label;
    }

    private record TileMetrics(int tileW, int tileH) {
    }
}
