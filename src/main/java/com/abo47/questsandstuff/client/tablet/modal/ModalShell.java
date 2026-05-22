package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.controls.StyledTextFields;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import java.util.function.Consumer;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.label;

public final class ModalShell {
    private ModalShell() {
    }

    public static void addTitleAndClose(WidgetGroup modal, String title, int w, TabletUiState state, Runnable refresh) {
        modal.addWidget(label(8, 6, title, ModColors.TEXT_PRIMARY));
        TabletModalPanel.addModalClose(modal, w - 26, 4, 18, state, refresh);
    }

    public static TextFieldWidget addSearchField(
            WidgetGroup modal,
            int x,
            int y,
            int w,
            int h,
            String current,
            int maxLength,
            Consumer<String> responder,
            Consumer<Boolean> focusResponder
    ) {
        TextFieldWidget field = StyledTextFields.search(x, y, w, h, current, maxLength, responder, focusResponder);
        modal.addWidget(field);
        return field;
    }

    public static WidgetGroup bodyPanel(int x, int y, int w, int h) {
        return Surfaces.panel(x, y, w, h, com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha(ModColors.elevatedSurface(), 190), ModColors.subtleBorder());
    }
}
