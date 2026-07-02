package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.panel;
import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;

public final class ModalPreviewLayout {
    public static final int PREVIEW_X = 8;
    public static final int LEFT_W = 150;
    public static final int RIGHT_X = 166;
    public static final int BODY_Y = 22;
    public static final int BODY_BOTTOM_PAD = 26;

    private ModalPreviewLayout() {
    }

    public static Metrics calculate(int modalW, int modalH) {
        return new Metrics(
                LEFT_W,
                RIGHT_X,
                modalW - RIGHT_X - PREVIEW_X,
                BODY_Y,
                modalH - BODY_Y - BODY_BOTTOM_PAD
        );
    }

    public static WidgetGroup previewPanel(Metrics metrics) {
        return panel(PREVIEW_X, metrics.bodyY(), metrics.leftW(), metrics.bodyH(), withAlpha(TabletColors.SURFACE_PANEL_ALT, 120), TabletColors.BORDER_BASE);
    }

    public record Metrics(int leftW, int rightX, int rightW, int bodyY, int bodyH) {
    }
}
