package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

import static com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions.closeAll;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.label;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

final class ResourceListPickerModal {
    private static final int ROW_H = 16;

    private ResourceListPickerModal() {
    }

    static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h, Options options) {
        ModalShell.addTitleAndClose(modal, options.title(), w, state, refresh);
        TextFieldWidget search = ModalShell.addSearchField(modal, 8, 24, w - 16, 16, options.search().get(), options.searchMaxLength(), value -> {
            String query = SearchFilter.normalizeUserInput(value);
            options.setSearch().accept(query);
            options.setScroll().accept(0);
            QuestsAndStuffMod.debugLog("[QnS:UI] {} search query='{}'", options.logName(), query);
            refresh.run();
        }, options.setFocused());

        int listX = 8;
        int listY = 46;
        int listW = w - 16;
        int listH = h - listY - 8;
        List<String> entries = options.entries().values(options.search().get());
        PickerListPanel.add(modal, listX, listY, listW, listH, ROW_H, entries, options.emptyText(), options.scroll(),
                3,
                refresh,
                (list, entry, index, rowY, rowW) -> renderRow(list, state, player, refresh, options, entry, rowY, rowW));
        return search;
    }

    private static void renderRow(WidgetGroup list, TabletUiState state, Player player, Runnable refresh, Options options, String entry, int rowY, int rowW) {
        if (!options.icon().isBlank()) {
            list.addWidget(new DisplayIconWidget(8, rowY + 1, 12, 12, options.icon()));
        }
        list.addWidget(label(options.displayX(), rowY + 4, SearchFilter.crop(options.displayName().value(entry), options.displayCrop()), ModColors.TEXT_PRIMARY));
        list.addWidget(label(Math.max(120, rowW - options.idRightOffset()), rowY + 4, SearchFilter.crop(entry, options.idCrop()), ModColors.TEXT_MUTED));
        ButtonWidget hit = flatHitButton(4, rowY, rowW - 8, ROW_H, click -> {
            String detailsTarget = state.questDetailsPickTarget == null ? "" : state.questDetailsPickTarget.trim();
            if (!detailsTarget.isBlank()) {
                options.pickAction().pick(player, state, entry);
            }
            QuestsAndStuffMod.debugLog("[QnS:UI] {} picked value={}", options.logName(), entry);
            closeAll(state);
            refresh.run();
        });
        hit.setHoverTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 54)));
        hit.setHoverTooltips(Component.literal(entry));
        list.addWidget(hit);
    }

    record Options(
            String title,
            String emptyText,
            String logName,
            Supplier<String> search,
            Consumer<String> setSearch,
            IntConsumer setScroll,
            Consumer<Boolean> setFocused,
            ScrollState scroll,
            EntryProvider entries,
            DisplayName displayName,
            PickAction pickAction,
            String icon,
            int displayX,
            int displayCrop,
            int idRightOffset,
            int idCrop,
            int searchMaxLength
    ) {
        Options {
            title = title == null ? "" : title;
            emptyText = emptyText == null ? "" : emptyText;
            logName = logName == null ? "resource" : logName;
            icon = icon == null ? "" : icon;
        }
    }

    @FunctionalInterface
    interface EntryProvider {
        List<String> values(String query);
    }

    @FunctionalInterface
    interface DisplayName {
        String value(String entry);
    }

    @FunctionalInterface
    interface PickAction {
        void pick(Player player, TabletUiState state, String entry);
    }
}
