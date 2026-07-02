package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.controls.TabletCycleButton;
import com.abo47.questsandstuff.client.tablet.controls.picker.TiledPickerPanel;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.icons.ScopedItemStackTexture;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.format.DisplayNameFormatter;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.render.Surfaces;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions.closeAll;
import static com.abo47.questsandstuff.client.tablet.modal.ModalSession.TargetSlot.CANVAS_MODEL;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.theme.render.Surfaces.withAlpha;

public final class TabletBlockPickerModal {
    private static final int TILE = 18;
    private static List<BlockChoice> ALL_BLOCKS;
    private static List<BlockChoice> ALL_TAGS;

    private TabletBlockPickerModal() {
    }

    public static void prewarm() {
        if (ALL_BLOCKS != null) {
            return;
        }
        ALL_BLOCKS = BuiltInRegistries.ITEM.stream()
                .filter(item -> item instanceof BlockItem)
                .map(TabletBlockPickerModal::choice)
                .filter(choice -> choice != null)
                .sorted(Comparator.comparing(BlockChoice::value))
                .toList();
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        ModalShell.addTitleAndClose(modal, TabletVocabulary.text(QuestVocabulary.CHOOSE_BLOCK), w, state, refresh);
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

        TextFieldWidget search = ModalShell.addSearchField(modal, searchX, headY, Math.max(24, searchW), headH, state.pickers.blockSearch, 96, value -> {
            state.pickers.blockSearch = SearchFilter.normalizeUserInput(value);
            state.pickers.blockScroll = 0;
            QuestsAndStuffMod.debugLog("[QnS:UI] block search mode={} query='{}'", blockModeName(state), state.pickers.blockSearch);
            refresh.run();
        }, focused -> state.pickers.blockSearchFocused = focused);
        TabletCycleButton.addIconModeButton(
                modal,
                gridX,
                headY,
                modeW,
                headH,
                2,
                () -> state.pickers.blockTagMode ? 1 : 0,
                index -> index == 1 ? "mode_tags" : "mode_items",
                null,
                direction -> {
                    state.pickers.blockTagMode = !state.pickers.blockTagMode;
                    state.pickers.blockScroll = 0;
                    QuestsAndStuffMod.debugLog("[QnS:UI] block picker mode={}", blockModeName(state));
                    refresh.run();
                });

        List<BlockChoice> entries = entries(state.pickers.blockSearch, state.pickers.blockTagMode);
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
                TabletVocabulary.text(QuestVocabulary.NO_BLOCKS),
                ScrollState.bind(
                        () -> state.pickers.blockScroll,
                        value -> state.pickers.blockScroll = value,
                        () -> state.pickers.blockScrollDragging,
                        dragging -> state.pickers.blockScrollDragging = dragging
                ),
                null,
                refresh,
                (surface, entry, index, x, y, tileW, tileH, layout) -> renderTile(surface, player, state, refresh, entry, x, y)
        );
        return search;
    }

    private static void renderTile(WidgetGroup surface, Player player, TabletUiState state, Runnable refresh, BlockChoice entry, int x, int y) {
        surface.addWidget(new ImageWidget(x, y, TILE, TILE, SlotWidget.ITEM_SLOT_TEXTURE));
        if (entry.previews().length == 0) {
            surface.addWidget(new DisplayIconWidget(x + 1, y + 1, 16, 16, "box"));
        } else {
            surface.addWidget(new ImageWidget(x + 1, y + 1, 16, 16, new ScopedItemStackTexture(entry.previews())));
        }
        ButtonWidget hit = flatHitButton(x + 1, y + 1, 16, 16, click -> {
            if (!entry.value().isBlank()) {
                String canvasModelTarget = ModalTargetState.target(state, CANVAS_MODEL, state.modal.modalCanvasModelTarget);
                if (!canvasModelTarget.isBlank()) {
                    if (!TabletModalPanel.runCanvasModelAction(state, canvasModelTarget, entry.value())) {
                        return;
                    }
                } else {
                    QuestDetailsWindow.applyBlockPick(player, state, entry.value());
                }
            }
            QuestsAndStuffMod.debugLog("[QnS:UI] block picked kind={} value={} preview={}", entry.tag() ? "tag" : "block", entry.value(), entry.previewId());
            closeAll(state);
            refresh.run();
        });
        hit.setHoverTooltips(PickerTooltips.nameAndId(entry.displayName(), entry.value()));
        hit.setHoverTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 66)));
        hit.setClickedTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 90)));
        surface.addWidget(hit);
    }

    private static List<BlockChoice> entries(String query, boolean tagMode) {
        String rawQuery = SearchFilter.normalizeUserInput(query);
        return tagMode || rawQuery.startsWith("#") ? tags(rawQuery) : blocks(rawQuery);
    }

    private static List<BlockChoice> blocks(String query) {
        String rawQuery = SearchFilter.normalizeUserInput(query);
        if (ALL_BLOCKS != null) {
            if (rawQuery.isBlank()) {
                return ALL_BLOCKS;
            }
            return ALL_BLOCKS.stream()
                    .filter(choice -> SearchFilter.matches(rawQuery, choice.previewId(), choice.displayName())
                            || SearchFilter.matches(rawQuery, choice.value(), choice.displayName()))
                    .toList();
        }
        return BuiltInRegistries.ITEM.stream()
                .filter(item -> item instanceof BlockItem)
                .map(TabletBlockPickerModal::choice)
                .filter(choice -> choice != null)
                .filter(choice -> rawQuery.isBlank()
                        || SearchFilter.matches(rawQuery, choice.previewId(), choice.displayName())
                        || SearchFilter.matches(rawQuery, choice.value(), choice.displayName()))
                .sorted(Comparator.comparing(BlockChoice::value))
                .toList();
    }

    private static List<BlockChoice> tags(String query) {
        String rawQuery = SearchFilter.normalizeUserInput(query);
        if (rawQuery.startsWith("#")) {
            rawQuery = SearchFilter.normalizeUserInput(rawQuery.substring(1));
        }
        String tagQuery = SearchFilter.normalizeKey(rawQuery);
        String filter = rawQuery;
        List<BlockChoice> source = ALL_TAGS;
        if (source == null) {
            source = BuiltInRegistries.BLOCK.getTagNames()
                    .map(TabletBlockPickerModal::tagChoice)
                    .sorted(Comparator.comparing(BlockChoice::value))
                    .toList();
            ALL_TAGS = source;
        }
        return source.stream()
                .filter(choice -> filter.isBlank()
                        || SearchFilter.matches(filter, choice.value().substring(1), choice.displayName())
                        || SearchFilter.normalizeKey(choice.value()).contains(tagQuery))
                .toList();
    }

    private static BlockChoice choice(Item item) {
        if (!(item instanceof BlockItem blockItem)) {
            return null;
        }
        Block block = blockItem.getBlock();
        if (!isPickable(block)) {
            return null;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
        if (itemId == null || blockId == null) {
            return null;
        }
        return new BlockChoice(blockId.toString(), itemId.toString(), item.getDescription().getString(), new ItemStack[]{new ItemStack(item)}, false);
    }

    private static BlockChoice tagChoice(TagKey<Block> tag) {
        String value = "#" + tag.location();
        ItemStack[] previews = tagPreviews(tag);
        String previewId = previews.length == 0 ? "box" : BuiltInRegistries.ITEM.getKey(previews[0].getItem()).toString();
        return new BlockChoice(value, previewId, DisplayNameFormatter.resourceLeaf(tag.location().toString()), previews, true);
    }

    private static ItemStack[] tagPreviews(TagKey<Block> tag) {
        List<ItemStack> stacks = new ArrayList<>();
        for (var holder : BuiltInRegistries.BLOCK.getTagOrEmpty(tag)) {
            Block block = holder.value();
            Item item = block.asItem();
            if (item != Items.AIR) {
                stacks.add(new ItemStack(item));
            }
        }
        return stacks.toArray(ItemStack[]::new);
    }

    private static boolean isPickable(Block block) {
        return block != Blocks.AIR && block != Blocks.CAVE_AIR && block != Blocks.VOID_AIR;
    }

    private static String blockModeName(TabletUiState state) {
        return state.pickers.blockTagMode || (state.pickers.blockSearch != null && state.pickers.blockSearch.trim().startsWith("#")) ? "tags" : "blocks";
    }

    private record BlockChoice(String value, String previewId, String displayName, ItemStack[] previews, boolean tag) {
    }
}
