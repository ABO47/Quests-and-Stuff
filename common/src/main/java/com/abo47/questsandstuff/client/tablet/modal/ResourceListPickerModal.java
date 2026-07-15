package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.SearchScrollState;
import com.abo47.questsandstuff.client.tablet.controls.picker.PickerListPanel;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

import java.util.List;

import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.ROW_H_16;
import static com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions.closeAll;
import static com.abo47.questsandstuff.client.tablet.modal.ModalSession.TargetSlot.QUEST_DETAILS_PICK;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.label;

final class ResourceListPickerModal {
    private static final int ROW_H = ROW_H_16;

    private ResourceListPickerModal() {
    }

    static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h, Options options) {
        SearchScrollState picker = ModalPickerStates.forType(state, options.type());
        ModalShell.addTitleAndClose(modal, options.title(), w, state, refresh);
        TextFieldWidget search = ModalShell.addSearchField(modal, 8, 24, w - 16, 16, picker.search(), options.searchMaxLength(), value -> {
            picker.setSearch(value);
            picker.setScrollValue(0);
            String query = picker.search();
            QuestsAndStuffMod.debugLog("[QnS:UI] {} search query='{}'", options.logName(), query);
            refresh.run();
        }, picker::setFocused);

        int listX = 8;
        int listY = 46;
        int listW = w - 16;
        int listH = h - listY - 8;
        List<String> entries = options.entries().values(picker.search());
        PickerListPanel.add(modal, listX, listY, listW, listH, ROW_H, entries, options.emptyText(), picker.scroll(),
                3,
                refresh,
                (list, entry, index, rowY, rowW) -> renderRow(list, state, player, refresh, options, entry, rowY, rowW));
        return search;
    }

    private static void renderRow(WidgetGroup list, TabletUiState state, Player player, Runnable refresh, Options options, String entry, int rowY, int rowW) {
        if (!options.icon().isBlank()) {
            list.addWidget(new DisplayIconWidget(8, rowY + 1, 12, 12, options.icon()));
        }
        list.addWidget(label(options.displayX(), rowY + 4, SearchFilter.crop(options.displayName().value(entry), options.displayCrop()), TabletColors.TEXT_PRIMARY));
        list.addWidget(label(Math.max(120, rowW - options.idRightOffset()), rowY + 4, SearchFilter.crop(entry, options.idCrop()), TabletColors.TEXT_MUTED));
        ButtonWidget hit = flatHitButton(4, rowY, rowW - 8, ROW_H, click -> {
            if (!ModalTargetState.parsedTarget(state, QUEST_DETAILS_PICK, state.questDetails.questDetailsPickTarget).kind().isBlank()) {
                options.pickAction().pick(player, state, entry);
            }
            QuestsAndStuffMod.debugLog("[QnS:UI] {} picked value={}", options.logName(), entry);
            closeAll(state);
            refresh.run();
        });
        hit.setHoverTexture(GlowShaderHelper.hoverGlow());
        hit.setHoverTooltips(PickerTooltips.nameAndId(options.displayName().value(entry), entry));
        list.addWidget(hit);
    }

    record Options(
            ModalWindowManager.ModalType type,
            String title,
            String emptyText,
            String logName,
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
