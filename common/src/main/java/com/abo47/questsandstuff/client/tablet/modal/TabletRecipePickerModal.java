package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.controls.TabletCycleButton;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

public final class TabletRecipePickerModal {
    private TabletRecipePickerModal() {
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        ModalShell.addTitleAndClose(modal, TabletVocabulary.text(QuestVocabulary.CHOOSE_RECIPE), w, state, refresh);
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

        TextFieldWidget search = ModalShell.addSearchField(modal, searchX, headY, Math.max(24, searchW), headH, state.recipeSearch, 96, value -> {
            String query = RecipePickerModeController.setSearch(state, value);
            RecipePickerMode mode = RecipePickerModeController.mode(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] recipe search mode={} query='{}'", mode.logName(query), query);
            refresh.run();
        }, focused -> state.recipeSearchFocused = focused);

        TabletCycleButton.addIconModeButton(
                modal,
                gridX,
                headY,
                modeW,
                headH,
                RecipePickerModeController.cycleSize(),
                () -> RecipePickerModeController.cycleIndex(state),
                RecipePickerModeController::iconAt,
                RecipePickerModeController.tooltip(state),
                direction -> {
                    RecipePickerMode nextMode = RecipePickerModeController.cycle(state, direction);
                    QuestsAndStuffMod.debugLog("[QnS:UI] recipe picker mode={} direction={}", nextMode.logName(state.recipeSearch), direction < 0 ? "backward" : "forward");
                    refresh.run();
                });
        RecipePickerApplyActions.addRecipeViewerKeyHandler(modal, state, player, refresh);
        RecipePickerGridRenderer.add(modal, state, player, refresh, gridX, gridY, gridW, gridH);
        return search;
    }
}
