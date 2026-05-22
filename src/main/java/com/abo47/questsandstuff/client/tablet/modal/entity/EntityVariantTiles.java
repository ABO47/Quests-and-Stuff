package com.abo47.questsandstuff.client.tablet.modal.entity;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.entity.variant.EntityVariantCatalog;
import com.abo47.questsandstuff.client.tablet.icons.UiIconAtlas;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.modal.TabletModalPanel;
import com.abo47.questsandstuff.client.tablet.modal.TiledPickerPanel;
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
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions.closeAll;

final class EntityVariantTiles {
    static final int RIGHT_X = 166;
    private static final int PAD = 8;
    private static final int LEFT_W = 150;
    private static final int TILE_W = 62;
    private static final int TILE_H = 54;
    private static final int TILE_GAP = 6;
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
        TiledPickerPanel.add(
                modal,
                RIGHT_X,
                22,
                rightW,
                h - 48,
                TILE_W,
                TILE_H,
                TILE_GAP,
                8,
                8,
                model.tiles(),
                model.emptyText(),
                ScrollState.bind(
                        () -> state.entityVariantScroll,
                        value -> state.entityVariantScroll = value,
                        () -> state.entityVariantScrollDragging,
                        dragging -> state.entityVariantScrollDragging = dragging
                ),
                null,
                refresh,
                (surface, tileEntry, index, tileX, tileY, tileW, tileH, layout) -> addTile(surface, player, state, refresh, model, tileEntry, tileX, tileY)
        );
    }

    private static void addTile(WidgetGroup surface, Player player, TabletUiState state, Runnable refresh, EntityVariantPickerModel model, EntityVariantTile tileEntry, int tileX, int tileY) {
        if (tileEntry.folder()) {
            addFolderTile(surface, state, refresh, model, tileEntry.folderEntry(), tileX, tileY);
            return;
        }
        addVariantTile(surface, player, state, refresh, model, tileEntry.variantEntry(), tileX, tileY);
    }

    private static void addFolderTile(WidgetGroup surface, TabletUiState state, Runnable refresh, EntityVariantPickerModel model, EntityVariantCatalog.VariantFolder folder, int tileX, int tileY) {
        boolean active = folder.key().equals(EntityVariantCatalog.variantFolderFor(model.entityId(), model.selected()));
        surface.addWidget(folderTile(folder, active, tileX, tileY));
        ButtonWidget hit = flatHitButton(tileX, tileY, TILE_W, TILE_H, click -> {
            state.entityVariantFolder = folder.key();
            state.entityVariantSelected = EntityVariantCatalog.defaultVariantForFolder(model.entityId(), folder.key());
            state.entityVariantSearch = "";
            state.entityVariantSearchFocused = false;
            state.entityVariantScroll = 0;
            QuestsAndStuffMod.debugLog("[QnS:UI] entity variant folder opened target={} entity={} folder={}", model.target(), model.entityId(), folder.key());
            refresh.run();
        });
        hit.setHoverTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 64)));
        hit.setClickedTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 90)));
        surface.addWidget(hit);
    }

    private static void addVariantTile(WidgetGroup surface, Player player, TabletUiState state, Runnable refresh, EntityVariantPickerModel model, EntityVariantCatalog.VariantEntry entry, int tileX, int tileY) {
        boolean active = entry.key().equals(model.selected());
        surface.addWidget(variantTile(model.entityId(), entry, active, tileX, tileY, model.activeFolder()));
        ButtonWidget hit = flatHitButton(tileX, tileY, TILE_W, TILE_H, click -> {
            state.entityVariantSelected = entry.key();
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

    private static WidgetGroup folderTile(EntityVariantCatalog.VariantFolder folder, boolean active, int x, int y) {
        WidgetGroup tile = panel(x, y, TILE_W, TILE_H, active ? withAlpha(ModColors.INTERACTIVE, 86) : withAlpha(ModColors.SURFACE_BASE, 46), active ? ModColors.BORDER_ACCENT : ModColors.BORDER_BASE);
        var folderIcon = UiIconAtlas.iconTexture("folder");
        if (folderIcon != null) {
            tile.addWidget(new ImageWidget((TILE_W - 24) / 2, 8, 24, 24, folderIcon));
        } else {
            tile.addWidget(label(5, 12, "[dir]", ModColors.TEXT_MUTED));
        }
        tile.addWidget(label(5, 40, crop(folder.label(), 10), active ? ModColors.TEXT_PRIMARY : ModColors.TEXT_SECONDARY));
        return tile;
    }

    private static WidgetGroup variantTile(String entityId, EntityVariantCatalog.VariantEntry entry, boolean active, int x, int y, String folderKey) {
        WidgetGroup tile = panel(x, y, TILE_W, TILE_H, active ? withAlpha(ModColors.INTERACTIVE, 86) : withAlpha(ModColors.SURFACE_BASE, 46), active ? ModColors.BORDER_ACCENT : ModColors.BORDER_BASE);
        tile.addWidget(new WidgetGroup(5, 4, TILE_W - 10, 32) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                EntityPreviewRenderer.renderEntityAsset(
                        graphics,
                        getPositionX(),
                        getPositionY(),
                        getSizeWidth(),
                        getSizeHeight(),
                        EntityPreviewRenderer.entityAsset(entityId, entry.key()),
                        ENTITY_FRONT_YAW,
                        0,
                        0.0F
                );
            }
        });
        tile.addWidget(label(5, 40, crop(tileLabel(entityId, entry, folderKey), 10), active ? ModColors.TEXT_PRIMARY : ModColors.TEXT_SECONDARY));
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
}
