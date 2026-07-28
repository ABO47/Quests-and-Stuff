package com.abo47.questsandstuff.client.tablet.modal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.picker.TiledPickerPanel;
import com.abo47.questsandstuff.client.tablet.icons.ItemStackIconCodec;
import com.abo47.questsandstuff.client.tablet.icons.ScopedItemStackTexture;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

import static com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions.closeAll;
import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;

public final class TabletItemInventoryPickerModal {
    private static final int TILE = 18;

    private TabletItemInventoryPickerModal() {
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        ModalShell.addTitleAndClose(modal, TabletTranslationKeys.text(QuestTranslationKeys.CHOOSE_INVENTORY_ITEM), w, state, refresh);
        int sidePad = 8;
        int headY = 24;
        int headH = 18;
        int gridX = sidePad;
        int gridY = headY + headH + 4;
        int gridW = w - sidePad * 2;
        int gridH = h - gridY - 8;
        TextFieldWidget search = ModalShell.addSearchField(modal, gridX, headY, gridW, headH, state.pickers.itemInventorySearch, 80, value -> {
            state.pickers.itemInventorySearch = SearchFilter.normalizeUserInput(value);
            state.pickers.itemInventoryScroll = 0;
            QuestsAndStuffMod.debugLog("[QnS:UI] inventory item search query='{}'", state.pickers.itemInventorySearch);
            refresh.run();
        }, focused -> state.pickers.itemInventorySearchFocused = focused);

        List<ItemStack> entries = inventoryEntries(player, state.pickers.itemInventorySearch);
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
                TabletTranslationKeys.text(QuestTranslationKeys.NO_INVENTORY_ITEMS),
                ScrollState.bind(
                        () -> state.pickers.itemInventoryScroll,
                        value -> state.pickers.itemInventoryScroll = value,
                        () -> state.pickers.itemInventoryScrollDragging,
                        dragging -> state.pickers.itemInventoryScrollDragging = dragging
                ),
                null,
                refresh,
                (surface, stack, index, x, y, tileW, tileH, layout) -> renderStackTile(surface, stack, x, y, picked -> {
                    QuestDetailsWindow.applyInventoryItemPick(player, state, picked);
                    closeAll(state);
                    refresh.run();
                })
        );
        return search;
    }

    static void renderStackTile(WidgetGroup surface, ItemStack stack, int x, int y, Consumer<ItemStack> onPick) {
        renderStackTile(surface, stack, x, y, onPick, null);
    }

    static void renderStackTile(WidgetGroup surface, ItemStack stack, int x, int y, Consumer<ItemStack> onPick, Consumer<ItemStack> onHover) {
        surface.addWidget(new ImageWidget(x, y, TILE, TILE, SlotWidget.ITEM_SLOT_TEXTURE));
        ItemStack preview = stack.copy();
        preview.setCount(1);
        surface.addWidget(new ImageWidget(x + GRID_1, y + GRID_1, GRID_16, GRID_16, new ScopedItemStackTexture(preview)));
        ButtonWidget hit = new ButtonWidget(x + GRID_1, y + GRID_1, GRID_16, GRID_16, SurfaceFactory.transparentFill(), click -> {
            if (onPick != null) {
                onPick.accept(stack.copy());
            }
        }) {
            @Override
            public void drawInBackground(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
                if (onHover != null && isMouseOverElement(mouseX, mouseY)) {
                    onHover.accept(stack.copy());
                }
            }
        };
        hit.setHoverTexture(GlowShaderHelper.hoverGlow());
        hit.setClickedTexture(SurfaceFactory.fill(withAlpha(TabletColors.INTERACTIVE, 90)));
        hit.setHoverTooltips(tooltip(stack));
        hit.setClientSideWidget();
        surface.addWidget(hit);
    }

    static List<ItemStack> inventoryEntries(Player player, String query) {
        List<ItemStack> entries = new ArrayList<>();
        if (player == null) {
            return entries;
        }
        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEmpty()) {
                continue;
            }
            String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            String name = stack.getHoverName().getString();
            if (SearchFilter.matches(query, id, name) || SearchFilter.matches(query, ItemStackIconCodec.nbtSummary(stack))) {
                entries.add(stack.copy());
            }
        }
        return entries;
    }

    static Component[] tooltip(ItemStack stack) {
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        List<Component> lines = new ArrayList<>();
        lines.addAll(List.of(PickerTooltips.nameAndId(stack.getHoverName().getString(), id)));
        String summary = ItemStackIconCodec.nbtSummary(stack);
        if (!summary.isBlank()) {
            lines.add(Component.literal("NBT: " + summary).withStyle(ChatFormatting.GOLD));
        }
        return lines.toArray(Component[]::new);
    }
}
