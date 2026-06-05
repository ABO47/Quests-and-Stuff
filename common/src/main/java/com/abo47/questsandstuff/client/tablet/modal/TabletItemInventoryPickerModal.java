package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
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
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions.closeAll;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

public final class TabletItemInventoryPickerModal {
    private static final int TILE = 18;

    private TabletItemInventoryPickerModal() {
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        ModalShell.addTitleAndClose(modal, TabletVocabulary.text(QuestVocabulary.CHOOSE_INVENTORY_ITEM), w, state, refresh);
        int sidePad = 8;
        int headY = 24;
        int headH = 18;
        int gridX = sidePad;
        int gridY = headY + headH + 4;
        int gridW = w - sidePad * 2;
        int gridH = h - gridY - 8;
        TextFieldWidget search = ModalShell.addSearchField(modal, gridX, headY, gridW, headH, state.itemInventorySearch, 80, value -> {
            state.itemInventorySearch = SearchFilter.normalizeUserInput(value);
            state.itemInventoryScroll = 0;
            QuestsAndStuffMod.debugLog("[QnS:UI] inventory item search query='{}'", state.itemInventorySearch);
            refresh.run();
        }, focused -> state.itemInventorySearchFocused = focused);

        List<ItemStack> entries = inventoryEntries(player, state.itemInventorySearch);
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
                ScrollState.bind(
                        () -> state.itemInventoryScroll,
                        value -> state.itemInventoryScroll = value,
                        () -> state.itemInventoryScrollDragging,
                        dragging -> state.itemInventoryScrollDragging = dragging
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
        surface.addWidget(new ImageWidget(x + 1, y + 1, 16, 16, new ScopedItemStackTexture(preview)));
        ButtonWidget hit = new ButtonWidget(x + 1, y + 1, 16, 16, Surfaces.fill(0x00000000), click -> {
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
        hit.setHoverTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 66)));
        hit.setClickedTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 90)));
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
