package com.abo47.questsandstuff.client.tablet.modal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.TabletCycleButton;
import com.abo47.questsandstuff.client.tablet.controls.picker.TiledPickerPanel;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.icons.ScopedItemStackTexture;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.format.DisplayNameFormatter;
import com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

import static com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions.closeAll;
import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.GRID_1;
import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.GRID_16;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.flatHitButton;

public final class TabletItemLockPickerModal {
    private static final int TILE = 18;
    private static List<ItemLockChoice> ALL_ITEMS;
    private static List<ItemLockChoice> ALL_TAGS;

    private TabletItemLockPickerModal() {
    }

    public static void prewarm() {
        if (ALL_ITEMS != null) {
            return;
        }
        ALL_ITEMS = BuiltInRegistries.ITEM.stream()
                .filter(item -> item != Items.AIR)
                .map(TabletItemLockPickerModal::choice)
                .sorted(Comparator.comparing(ItemLockChoice::value))
                .toList();
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        prewarm();
        ModalShell.addTitleAndClose(modal, TabletTranslationKeys.text(QuestTranslationKeys.CHOOSE_ITEM_LOCK), w, state, refresh);
        int sidePad = 8;
        int headY = 24;
        int headH = 18;
        int modeW = headH;
        int gap = 4;
        int gridX = sidePad;
        int gridW = w - sidePad * 2;
        int searchX = gridX + modeW + gap;
        int searchW = gridW - modeW - gap;
        int gridY = headY + headH + 4;
        int gridH = h - gridY - 8;

        TextFieldWidget search = ModalShell.addSearchField(modal, searchX, headY, Math.max(24, searchW), headH, state.pickers.itemLockSearch, 96, value -> {
            state.pickers.itemLockSearch = SearchFilter.normalizeUserInput(value);
            state.pickers.itemLockScroll = 0;
            QuestsAndStuffMod.debugLog("[QnS:UI] item lock search mode={} query='{}'", lockModeName(state), state.pickers.itemLockSearch);
            refresh.run();
        }, focused -> state.pickers.itemLockSearchFocused = focused);
        TabletCycleButton.addIconModeButton(
                modal,
                gridX,
                headY,
                modeW,
                headH,
                2,
                () -> state.pickers.itemLockTagMode ? 1 : 0,
                index -> index == 1 ? "mode_tags" : "mode_items",
                null,
                direction -> {
                    state.pickers.itemLockTagMode = !state.pickers.itemLockTagMode;
                    state.pickers.itemLockScroll = 0;
                    QuestsAndStuffMod.debugLog("[QnS:UI] item lock picker mode={}", lockModeName(state));
                    refresh.run();
                });

        List<ItemLockChoice> entries = entries(state.pickers.itemLockSearch, state.pickers.itemLockTagMode);
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
                TabletTranslationKeys.text(QuestTranslationKeys.NO_ITEM_LOCKS),
                ScrollState.bind(
                        () -> state.pickers.itemLockScroll,
                        value -> state.pickers.itemLockScroll = value,
                        () -> state.pickers.itemLockScrollDragging,
                        dragging -> state.pickers.itemLockScrollDragging = dragging
                ),
                null,
                refresh,
                (surface, entry, index, x, y, tileW, tileH, layout) -> renderTile(surface, player, state, refresh, entry, x, y)
        );
        return search;
    }

    private static void renderTile(WidgetGroup surface, Player player, TabletUiState state, Runnable refresh, ItemLockChoice entry, int x, int y) {
        surface.addWidget(new ImageWidget(x, y, TILE, TILE, SlotWidget.ITEM_SLOT_TEXTURE));
        if (entry.previews().length == 0) {
            surface.addWidget(new DisplayIconWidget(x + GRID_1, y + GRID_1, GRID_16, GRID_16, "box"));
        } else {
            surface.addWidget(new ImageWidget(x + GRID_1, y + GRID_1, GRID_16, GRID_16, new ScopedItemStackTexture(entry.previews())));
        }
        ButtonWidget hit = flatHitButton(x + GRID_1, y + GRID_1, GRID_16, GRID_16, click -> {
            if (!entry.value().isBlank()) {
                QuestDetailsWindow.applyItemLockPick(player, state, entry.value());
            }
            QuestsAndStuffMod.debugLog("[QnS:UI] item lock picked kind={} value={}", entry.tag() ? "tag" : "item", entry.value());
            closeAll(state);
            refresh.run();
        });
        hit.setHoverTooltips(PickerTooltips.nameAndId(entry.displayName(), entry.value()));
        hit.setHoverTexture(GlowShaderHelper.hoverGlow());
        hit.setClickedTexture(SurfaceFactory.fill(withAlpha(TabletColors.LOCKED, 90)));
        hit.setClientSideWidget();
        surface.addWidget(hit);
    }

    private static List<ItemLockChoice> entries(String query, boolean tagMode) {
        String rawQuery = SearchFilter.normalizeUserInput(query);
        return tagMode || rawQuery.startsWith("#") ? tags(rawQuery) : items(rawQuery);
    }

    private static List<ItemLockChoice> items(String query) {
        String rawQuery = SearchFilter.normalizeUserInput(query);
        if (ALL_ITEMS != null) {
            if (rawQuery.isBlank()) {
                return ALL_ITEMS;
            }
            return ALL_ITEMS.stream()
                    .filter(choice -> SearchFilter.matches(rawQuery, choice.previewId(), choice.displayName())
                            || SearchFilter.matches(rawQuery, choice.value(), choice.displayName()))
                    .toList();
        }
        return BuiltInRegistries.ITEM.stream()
                .filter(item -> item != Items.AIR)
                .map(TabletItemLockPickerModal::choice)
                .filter(choice -> rawQuery.isBlank() || SearchFilter.matches(rawQuery, choice.value(), choice.displayName()))
                .sorted(Comparator.comparing(ItemLockChoice::value))
                .toList();
    }

    private static List<ItemLockChoice> tags(String query) {
        String rawQuery = SearchFilter.normalizeUserInput(query);
        if (rawQuery.startsWith("#")) {
            rawQuery = SearchFilter.normalizeUserInput(rawQuery.substring(1));
        }
        String tagQuery = SearchFilter.normalizeKey(rawQuery);
        String filter = rawQuery;
        List<ItemLockChoice> source = ALL_TAGS;
        if (source == null) {
            source = BuiltInRegistries.ITEM.getTagNames()
                    .map(TabletItemLockPickerModal::tagChoice)
                    .sorted(Comparator.comparing(ItemLockChoice::value))
                    .toList();
            ALL_TAGS = source;
        }
        return source.stream()
                .filter(choice -> filter.isBlank()
                        || SearchFilter.matches(filter, choice.value().substring(1), choice.displayName())
                        || SearchFilter.normalizeKey(choice.value()).contains(tagQuery))
                .toList();
    }

    private static ItemLockChoice choice(Item item) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        if (itemId == null) {
            return new ItemLockChoice(item.toString(), item.toString(), item.toString(), new ItemStack[0], false);
        }
        return new ItemLockChoice(itemId.toString(), itemId.toString(), item.getDescription().getString(), new ItemStack[]{new ItemStack(item)}, false);
    }

    private static ItemLockChoice tagChoice(TagKey<Item> tag) {
        String value = "#" + tag.location();
        ItemStack[] previews = tagPreviews(tag);
        String previewId = previews.length == 0 ? "box" : BuiltInRegistries.ITEM.getKey(previews[0].getItem()).toString();
        return new ItemLockChoice(value, previewId, DisplayNameFormatter.resourceLeaf(tag.location().toString()), previews, true);
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

    private static String lockModeName(TabletUiState state) {
        return state.pickers.itemLockTagMode || (state.pickers.itemLockSearch != null && state.pickers.itemLockSearch.trim().startsWith("#")) ? "tags" : "items";
    }

    private record ItemLockChoice(String value, String previewId, String displayName, ItemStack[] previews, boolean tag) {
    }
}
