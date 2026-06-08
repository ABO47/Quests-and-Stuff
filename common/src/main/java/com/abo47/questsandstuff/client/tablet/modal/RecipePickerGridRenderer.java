package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.controls.picker.TiledPickerPanel;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconProvider;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.icons.ItemStackIconCodec;
import com.abo47.questsandstuff.client.tablet.icons.ScopedItemStackTexture;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;

final class RecipePickerGridRenderer {
    private static final int TILE = 18;

    private RecipePickerGridRenderer() {
    }

    static void add(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int gridX, int gridY, int gridW, int gridH) {
        RecipePickerMode mode = RecipePickerModeController.mode(state);
        switch (RecipePickerModeController.contentKind(mode)) {
            case INVENTORY -> addInventoryGrid(modal, state, player, refresh, gridX, gridY, gridW, gridH);
            case FLUIDS -> addFluidGrid(modal, state, player, refresh, gridX, gridY, gridW, gridH);
            case RECIPES -> addRecipeGrid(modal, state, player, refresh, gridX, gridY, gridW, gridH, mode);
        }
    }

    private static void addInventoryGrid(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int gridX, int gridY, int gridW, int gridH) {
        List<ItemStack> entries = TabletItemInventoryPickerModal.inventoryEntries(player, state.pickers.recipeSearch);
        TiledPickerPanel.add(
                modal,
                gridX,
                gridY,
                gridW,
                gridH,
                TILE,
                TILE,
                0,
                6,
                6,
                entries,
                TabletVocabulary.text(QuestVocabulary.NO_INVENTORY_ITEMS),
                scrollState(state),
                null,
                refresh,
                (surface, stack, index, x, y, tileW, tileH, layout) -> TabletItemInventoryPickerModal.renderStackTile(surface, stack, x, y, picked -> applyInventoryRecipePick(player, state, picked, refresh), hovered -> RecipePickerApplyActions.trackRecipeHover(state, ItemStackIconCodec.iconFromStack(hovered)))
        );
    }

    private static void addFluidGrid(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int gridX, int gridY, int gridW, int gridH) {
        List<String> entries = DisplayIconProvider.searchableFluidEntries(state.pickers.recipeSearch);
        TiledPickerPanel.add(
                modal,
                gridX,
                gridY,
                gridW,
                gridH,
                TILE,
                TILE,
                0,
                6,
                6,
                entries,
                TabletVocabulary.text(QuestVocabulary.NO_FLUIDS),
                scrollState(state),
                null,
                refresh,
                (surface, entry, index, x, y, tileW, tileH, layout) -> renderFluidTile(surface, player, state, refresh, entry, x, y)
        );
    }

    private static void addRecipeGrid(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int gridX, int gridY, int gridW, int gridH, RecipePickerMode mode) {
        List<RecipeChoiceIndex.RecipeChoice> entries = RecipeChoiceIndex.recipes(state.pickers.recipeSearch, mode.showingTags(state.pickers.recipeSearch));
        TiledPickerPanel.add(
                modal,
                gridX,
                gridY,
                gridW,
                gridH,
                TILE,
                TILE,
                0,
                6,
                6,
                entries,
                TabletVocabulary.text(QuestVocabulary.NO_RECIPES),
                scrollState(state),
                null,
                refresh,
                (surface, entry, index, x, y, tileW, tileH, layout) -> renderTile(surface, player, state, refresh, entry, x, y)
        );
    }

    private static ScrollState scrollState(TabletUiState state) {
        return ScrollState.bind(
                () -> state.pickers.recipeScroll,
                value -> state.pickers.recipeScroll = value,
                () -> state.pickers.recipeScrollDragging,
                dragging -> state.pickers.recipeScrollDragging = dragging
        );
    }

    private static void renderTile(WidgetGroup surface, Player player, TabletUiState state, Runnable refresh, RecipeChoiceIndex.RecipeChoice entry, int x, int y) {
        surface.addWidget(new ImageWidget(x, y, TILE, TILE, SlotWidget.ITEM_SLOT_TEXTURE));
        if (entry.previews().length == 0) {
            surface.addWidget(new DisplayIconWidget(x + 1, y + 1, 16, 16, entry.tag() ? "name_tag" : "recipe"));
        } else {
            surface.addWidget(new ImageWidget(x + 1, y + 1, 16, 16, new ScopedItemStackTexture(entry.previews())));
        }
        ButtonWidget hit = new ButtonWidget(x + 1, y + 1, 16, 16, Surfaces.transparentFill(), click -> {
            if (!entry.value().isBlank()) {
                RecipePickerApplyActions.applyRecipePick(player, state, entry.value(), refresh);
            }
            QuestsAndStuffMod.debugLog("[QnS:UI] recipe picked kind={} value={} recipes={}", entry.tag() ? "tag" : "output", entry.value(), entry.recipeIds());
        }) {
            @Override
            public void drawInBackground(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
                if (isMouseOverElement(mouseX, mouseY)) {
                    RecipePickerApplyActions.trackRecipeHover(state, entry.value());
                }
            }
        };
        hit.setHoverTooltips(entry.tooltip());
        hit.setHoverTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 66)));
        hit.setClickedTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 90)));
        hit.setClientSideWidget();
        surface.addWidget(hit);
    }

    private static void renderFluidTile(WidgetGroup surface, Player player, TabletUiState state, Runnable refresh, String entry, int x, int y) {
        surface.addWidget(new ImageWidget(x, y, TILE, TILE, SlotWidget.ITEM_SLOT_TEXTURE));
        surface.addWidget(new DisplayIconWidget(x + 1, y + 1, 16, 16, entry));
        ButtonWidget hit = new ButtonWidget(x + 1, y + 1, 16, 16, Surfaces.transparentFill(), click -> {
            if (entry != null && !entry.isBlank()) {
                RecipePickerApplyActions.applyRecipePick(player, state, entry, refresh);
            }
            QuestsAndStuffMod.debugLog("[QnS:UI] recipe picked kind=fluid value={}", entry);
        }) {
            @Override
            public void drawInBackground(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
                if (isMouseOverElement(mouseX, mouseY)) {
                    RecipePickerApplyActions.trackRecipeHover(state, entry);
                }
            }
        };
        hit.setHoverTooltips(TabletModalPanel.iconTooltip(entry));
        hit.setHoverTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 66)));
        hit.setClickedTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 90)));
        hit.setClientSideWidget();
        surface.addWidget(hit);
    }

    private static void applyInventoryRecipePick(Player player, TabletUiState state, ItemStack stack, Runnable refresh) {
        String pick = ItemStackIconCodec.iconFromStack(stack);
        if (!pick.isBlank()) {
            RecipePickerApplyActions.applyRecipePick(player, state, pick, refresh);
        }
        QuestsAndStuffMod.debugLog("[QnS:UI] recipe picked kind=inventory value={} hasNbt={}", pick, stack != null && stack.hasTag());
    }
}
