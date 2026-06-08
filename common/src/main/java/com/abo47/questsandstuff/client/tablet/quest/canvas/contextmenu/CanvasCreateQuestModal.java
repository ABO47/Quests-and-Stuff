package com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu;


import com.abo47.questsandstuff.client.tablet.controls.StyledTextFields;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.resources.language.I18n;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.addQuestAt;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.button;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.label;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.panel;
import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;

public final class CanvasCreateQuestModal {
    private CanvasCreateQuestModal() {
    }

    public static void render(CanvasViewport canvasViewport, TabletUiState state) {
        if (!state.createQuestModalOpen) {
            return;
        }
        int w = canvasViewport.getSize().width;
        int h = canvasViewport.getSize().height;
        int modalW = 170;
        int modalH = 68;
        int x = Math.max(8, w / 2 - modalW / 2);
        int y = Math.max(8, h / 2 - modalH / 2);

        WidgetGroup modal = panel(x, y, modalW, modalH, withAlpha(ModColors.SURFACE_PANEL, 240), ModColors.INTERACTIVE);
        TextFieldWidget titleField = StyledTextFields.textField(
                8,
                18,
                modalW - 16,
                14,
                () -> state.createQuestTitle,
                Integer.MAX_VALUE,
                value -> state.createQuestTitle = value == null ? "" : value.trim()
        );

        modal.addWidgets(
                label(8, 6, tr("ui.questsandstuff.modal.create_quest_title"), ModColors.TEXT_PRIMARY),
                titleField,
                button(8, 38, 72, 14, tr("ui.questsandstuff.common.create"), ModColors.SURFACE_PANEL_ALT, ModColors.SUCCESS, click -> {
                    String title = state.createQuestTitle == null ? "" : state.createQuestTitle.trim();
                    addQuestAt(canvasViewport.player(), state, state.createQuestLogicalX, state.createQuestLogicalY, title);
                    state.createQuestModalOpen = false;
                    canvasViewport.refresh();
                }),
                button(88, 38, 72, 14, tr("ui.questsandstuff.common.cancel"), ModColors.SURFACE_PANEL_ALT, ModColors.ERROR, click -> {
                    state.createQuestModalOpen = false;
                    canvasViewport.refresh();
                }),
                label(8, 56, tr("ui.questsandstuff.modal.create_quest_hint"), ModColors.TEXT_MUTED)
        );
        canvasViewport.addWidget(modal);
    }

    private static String tr(String key, Object... args) {
        return I18n.get(key, args);
    }
}
