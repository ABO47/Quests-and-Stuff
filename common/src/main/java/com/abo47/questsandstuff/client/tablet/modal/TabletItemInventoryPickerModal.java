package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.icons.ScopedItemStackTexture;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions.closeAll;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

public final class TabletItemInventoryPickerModal {
    private static final int TILE = 18;

    private TabletItemInventoryPickerModal() {
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        ModalShell.addTitleAndClose(modal, QuestVocabulary.text(QuestVocabulary.CHOOSE_INVENTORY_ITEM), w, state, refresh);
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
                QuestVocabulary.text(QuestVocabulary.NO_INVENTORY_ITEMS),
                ScrollState.bind(
                        () -> state.itemInventoryScroll,
                        value -> state.itemInventoryScroll = value,
                        () -> state.itemInventoryScrollDragging,
                        dragging -> state.itemInventoryScrollDragging = dragging
                ),
                null,
                refresh,
                (surface, stack, index, x, y, tileW, tileH, layout) -> renderStackTile(surface, state, player, refresh, stack, x, y)
        );
        return search;
    }

    private static void renderStackTile(WidgetGroup surface, TabletUiState state, Player player, Runnable refresh, ItemStack stack, int x, int y) {
        surface.addWidget(new ImageWidget(x, y, TILE, TILE, SlotWidget.ITEM_SLOT_TEXTURE));
        ItemStack preview = stack.copy();
        preview.setCount(1);
        surface.addWidget(new ImageWidget(x + 1, y + 1, 16, 16, new ScopedItemStackTexture(preview)));
        ButtonWidget hit = flatHitButton(x + 1, y + 1, 16, 16, click -> {
            QuestDetailsWindow.applyInventoryItemPick(player, state, stack);
            closeAll(state);
            refresh.run();
        });
        hit.setHoverTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 66)));
        hit.setClickedTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 90)));
        hit.setHoverTooltips(tooltip(stack));
        surface.addWidget(hit);
    }

    private static List<ItemStack> inventoryEntries(Player player, String query) {
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
            if (SearchFilter.matches(query, id, name) || SearchFilter.matches(query, nbtSummary(stack))) {
                entries.add(stack.copy());
            }
        }
        return entries;
    }

    private static Component[] tooltip(ItemStack stack) {
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        List<Component> lines = new ArrayList<>();
        lines.addAll(List.of(PickerTooltips.nameAndId(stack.getHoverName().getString(), id)));
        String summary = nbtSummary(stack);
        if (!summary.isBlank()) {
            lines.add(Component.literal("NBT: " + summary).withStyle(ChatFormatting.GOLD));
        }
        return lines.toArray(Component[]::new);
    }

    private static String nbtSummary(ItemStack stack) {
        if (stack == null || !stack.hasTag()) {
            return "";
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || tag.isEmpty()) {
            return "";
        }
        List<String> keys = new ArrayList<>(tag.getAllKeys());
        if (keys.isEmpty()) {
            return "";
        }
        int limit = Math.min(3, keys.size());
        String summary = String.join(", ", keys.subList(0, limit));
        if (keys.size() > limit) {
            summary += ", ...";
        }
        return summary;
    }
}
