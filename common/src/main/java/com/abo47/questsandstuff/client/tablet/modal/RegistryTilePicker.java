package com.abo47.questsandstuff.client.tablet.modal;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

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
import com.abo47.questsandstuff.client.tablet.controls.TabletCycleButton;
import com.abo47.questsandstuff.client.tablet.controls.picker.TiledPickerPanel;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.icons.ScopedItemStackTexture;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;

import static com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions.closeAll;
import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.GRID_1;
import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.GRID_16;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.flatHitButton;

public final class RegistryTilePicker {
    private static final int TILE = 18;
    private static final Map<String, List<Choice>> CACHE = new ConcurrentHashMap<>();

    private RegistryTilePicker() {
    }

    public static void prewarm(String key, Supplier<List<Choice>> builder) {
        cached(key, builder);
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h, Config config) {
        ModalShell.addTitleAndClose(modal, config.title(), w, state, refresh);
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
        StateAccess access = config.access();

        TextFieldWidget search = ModalShell.addSearchField(modal, searchX, headY, Math.max(24, searchW), headH, access.search().get(), 96, value -> {
            access.searchSet().accept(SearchFilter.normalizeUserInput(value));
            access.scrollSet().accept(0);
            QuestsAndStuffMod.debugLog("[QnS:UI] {} search mode={} query='{}'", config.logName(), modeName(access, config), access.search().get());
            refresh.run();
        }, focused -> access.focusSet().accept(focused));
        TabletCycleButton.addIconModeButton(
                modal,
                gridX,
                headY,
                modeW,
                headH,
                2,
                () -> access.tagMode().getAsBoolean() ? 1 : 0,
                index -> index == 1 ? "mode_tags" : "mode_items",
                null,
                direction -> {
                    access.tagToggle().run();
                    access.scrollSet().accept(0);
                    QuestsAndStuffMod.debugLog("[QnS:UI] {} picker mode={}", config.logName(), modeName(access, config));
                    refresh.run();
                });

        List<Choice> entries = entries(access.search().get(), access.tagMode().getAsBoolean(), config);
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
                config.emptyText(),
                ScrollState.bind(
                        () -> access.scroll().getAsInt(),
                        value -> access.scrollSet().accept(value),
                        () -> access.scrollDragging().getAsBoolean(),
                        access.scrollDraggingSet()::accept
                ),
                null,
                (surface, entry, index, x, y, tileW, tileH, layout) -> renderTile(surface, player, state, refresh, entry, x, y, config)
        );
        return search;
    }

    private static List<Choice> entries(String query, boolean tagMode, Config config) {
        String rawQuery = SearchFilter.normalizeUserInput(query);
        return tagMode || rawQuery.startsWith("#") ? tags(rawQuery, config) : items(rawQuery, config);
    }

    private static List<Choice> items(String query, Config config) {
        String rawQuery = SearchFilter.normalizeUserInput(query);
        List<Choice> source = cached(config.itemsKey(), config.itemsBuilder());
        if (rawQuery.isBlank()) {
            return source;
        }
        return source.stream()
                .filter(choice -> SearchFilter.matches(rawQuery, choice.previewId(), choice.displayName())
                        || SearchFilter.matches(rawQuery, choice.value(), choice.displayName()))
                .toList();
    }

    private static List<Choice> tags(String query, Config config) {
        String rawQuery = SearchFilter.normalizeUserInput(query);
        if (rawQuery.startsWith("#")) {
            rawQuery = SearchFilter.normalizeUserInput(rawQuery.substring(1));
        }
        String tagQuery = SearchFilter.normalizeKey(rawQuery);
        String filter = rawQuery;
        List<Choice> source = cached(config.tagsKey(), config.tagsBuilder());
        return source.stream()
                .filter(choice -> filter.isBlank()
                        || SearchFilter.matches(filter, choice.value().substring(1), choice.displayName())
                        || SearchFilter.normalizeKey(choice.value()).contains(tagQuery))
                .toList();
    }

    private static List<Choice> cached(String key, Supplier<List<Choice>> builder) {
        List<Choice> cached = CACHE.get(key);
        if (cached == null) {
            cached = builder.get().stream().sorted((left, right) -> left.value().compareTo(right.value())).toList();
            CACHE.put(key, cached);
        }
        return cached;
    }

    private static void renderTile(WidgetGroup surface, Player player, TabletUiState state, Runnable refresh, Choice entry, int x, int y, Config config) {
        surface.addWidget(new ImageWidget(x, y, TILE, TILE, SlotWidget.ITEM_SLOT_TEXTURE));
        if (entry.previews().length == 0) {
            surface.addWidget(new DisplayIconWidget(x + GRID_1, y + GRID_1, GRID_16, GRID_16, "box"));
        } else {
            surface.addWidget(new ImageWidget(x + GRID_1, y + GRID_1, GRID_16, GRID_16, new ScopedItemStackTexture(entry.previews())));
        }
        ButtonWidget hit = flatHitButton(x + GRID_1, y + GRID_1, GRID_16, GRID_16, click -> {
            boolean applied = config.pick().pick(player, state, entry);
            QuestsAndStuffMod.debugLog("[QnS:UI] {} picked kind={} value={}", config.logName(), entry.tag() ? "tag" : config.itemWord(), entry.value());
            if (applied) {
                closeAll(state);
                refresh.run();
            }
        });
        GlowShaderHelper.glowHit(hit, PickerTooltips.nameAndId(entry.displayName(), entry.value()));
        hit.setClickedTexture(SurfaceFactory.fill(withAlpha(config.clickedColor(), 90)));
        hit.setClientSideWidget();
        surface.addWidget(hit);
    }

    private static String modeName(StateAccess access, Config config) {
        String search = access.search().get();
        return access.tagMode().getAsBoolean() || (search != null && search.trim().startsWith("#")) ? "tags" : config.itemWord() + "s";
    }

    public record Choice(String value, String previewId, String displayName, ItemStack[] previews, boolean tag) {
    }

    public interface PickAction {
        boolean pick(Player player, TabletUiState state, Choice entry);
    }

    public record StateAccess(
            Supplier<String> search,
            Consumer<String> searchSet,
            IntSupplier scroll,
            IntConsumer scrollSet,
            BooleanSupplier scrollDragging,
            Consumer<Boolean> scrollDraggingSet,
            Consumer<Boolean> focusSet,
            BooleanSupplier tagMode,
            Runnable tagToggle) {
    }

    public record Config(
            String title,
            String emptyText,
            String logName,
            String itemWord,
            int clickedColor,
            StateAccess access,
            String itemsKey,
            Supplier<List<Choice>> itemsBuilder,
            String tagsKey,
            Supplier<List<Choice>> tagsBuilder,
            PickAction pick) {
    }
}
