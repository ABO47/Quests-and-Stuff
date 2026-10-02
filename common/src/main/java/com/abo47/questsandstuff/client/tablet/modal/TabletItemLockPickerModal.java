package com.abo47.questsandstuff.client.tablet.modal;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.format.DisplayNameFormatter;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

public final class TabletItemLockPickerModal {
    private TabletItemLockPickerModal() {
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        return RegistryTilePicker.rebuild(modal, state, player, refresh, w, h, new RegistryTilePicker.Config(
                TabletTranslationKeys.text(QuestTranslationKeys.CHOOSE_ITEM_LOCK),
                TabletTranslationKeys.text(QuestTranslationKeys.NO_ITEM_LOCKS),
                "item lock",
                "item",
                TabletColors.LOCKED,
                new RegistryTilePicker.StateAccess(
                        () -> state.pickers.itemLockSearch,
                        value -> state.pickers.itemLockSearch = value,
                        () -> state.pickers.itemLockScroll,
                        value -> state.pickers.itemLockScroll = value,
                        () -> state.pickers.itemLockScrollDragging,
                        dragging -> state.pickers.itemLockScrollDragging = dragging,
                        focused -> state.pickers.itemLockSearchFocused = focused,
                        () -> state.pickers.itemLockTagMode,
                        () -> state.pickers.itemLockTagMode = !state.pickers.itemLockTagMode),
                "item_lock_items",
                TabletItemLockPickerModal::itemChoices,
                "item_lock_tags",
                TabletItemLockPickerModal::tagChoices,
                (pickPlayer, pickState, entry) -> {
                    if (!entry.value().isBlank()) {
                        QuestDetailsWindow.applyItemLockPick(pickPlayer, pickState, entry.value());
                    }
                    return true;
                }));
    }

    private static List<RegistryTilePicker.Choice> itemChoices() {
        return BuiltInRegistries.ITEM.stream()
                .filter(item -> item != Items.AIR)
                .map(TabletItemLockPickerModal::choice)
                .toList();
    }

    private static List<RegistryTilePicker.Choice> tagChoices() {
        return BuiltInRegistries.ITEM.getTagNames()
                .map(TabletItemLockPickerModal::tagChoice)
                .toList();
    }

    private static RegistryTilePicker.Choice choice(Item item) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        if (itemId == null) {
            return new RegistryTilePicker.Choice(item.toString(), item.toString(), item.toString(), new ItemStack[0], false);
        }
        return new RegistryTilePicker.Choice(itemId.toString(), itemId.toString(), item.getDescription().getString(), new ItemStack[]{new ItemStack(item)}, false);
    }

    private static RegistryTilePicker.Choice tagChoice(TagKey<Item> tag) {
        String value = "#" + tag.location();
        ItemStack[] previews = tagPreviews(tag);
        String previewId = previews.length == 0 ? "box" : BuiltInRegistries.ITEM.getKey(previews[0].getItem()).toString();
        return new RegistryTilePicker.Choice(value, previewId, DisplayNameFormatter.resourceLeaf(tag.location().toString()), previews, true);
    }

    private static ItemStack[] tagPreviews(TagKey<Item> tag) {
        List<ItemStack> stacks = new ArrayList<>();
        for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
            Item item = holder.value();
            if (item != Items.AIR) {
                stacks.add(new ItemStack(item));
            }
        }
        return stacks.toArray(ItemStack[]::new);
    }
}
