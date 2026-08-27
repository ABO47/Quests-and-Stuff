package com.abo47.questsandstuff.client.tablet.controls;

import java.util.function.BiConsumer;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.label;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.panel;

public final class FourFieldEditor {
    private FourFieldEditor() {
    }

    public static WidgetGroup build(TabletUiState state, int x, int y, int w, int h,
            String titleKey, String lKey, String rKey, String tKey, String bKey,
            int l, int r, int t, int b, QuadConsumer<Integer, Integer, Integer, Integer> onApply, Runnable onCancel) {
        int pad = 8;
        int buttonW = 82;
        int buttonH = 16;
        int buttonY = h - pad - buttonH;
        int[] vals = {l, r, t, b};
        Runnable apply = () -> onApply.accept(vals[0], vals[1], vals[2], vals[3]);
        int halfW = (w - pad * 3) / 2;
        WidgetGroup popup = panel(x, y, w, h, withAlpha(TabletColors.SURFACE_BASE, 246), TabletColors.BORDER_ACCENT);
        popup.addWidget(label(pad, 6, TabletTranslationKeys.text(titleKey), TabletColors.TEXT_PRIMARY));
        popup.addWidget(label(pad, 22, TabletTranslationKeys.text(lKey), TabletColors.TEXT_SECONDARY));
        popup.addWidget(label(pad + halfW + pad, 22, TabletTranslationKeys.text(rKey), TabletColors.TEXT_SECONDARY));
        TextFieldWidget lf = StyledTextFields.integerField(pad, 34, halfW, 14, l, 0, Integer.MAX_VALUE, 4, v -> {
            try { vals[0] = Integer.parseInt(v); } catch (NumberFormatException ignored) {}
        }, apply, onCancel, apply);
        lf.setClientSideWidget();
        StyledTextFields.applyStandardStyle(lf, TabletColors.SURFACE_PANEL_ALT, TabletColors.BORDER_BASE);
        popup.addWidget(lf);
        TextFieldWidget rf = StyledTextFields.integerField(pad + halfW + pad, 34, halfW, 14, r, 0, Integer.MAX_VALUE, 4, v -> {
            try { vals[1] = Integer.parseInt(v); } catch (NumberFormatException ignored) {}
        }, apply, onCancel, apply);
        rf.setClientSideWidget();
        StyledTextFields.applyStandardStyle(rf, TabletColors.SURFACE_PANEL_ALT, TabletColors.BORDER_BASE);
        popup.addWidget(rf);
        popup.addWidget(label(pad, 52, TabletTranslationKeys.text(tKey), TabletColors.TEXT_SECONDARY));
        popup.addWidget(label(pad + halfW + pad, 52, TabletTranslationKeys.text(bKey), TabletColors.TEXT_SECONDARY));
        TextFieldWidget tf = StyledTextFields.integerField(pad, 64, halfW, 14, t, 0, Integer.MAX_VALUE, 4, v -> {
            try { vals[2] = Integer.parseInt(v); } catch (NumberFormatException ignored) {}
        }, apply, onCancel, apply);
        tf.setClientSideWidget();
        StyledTextFields.applyStandardStyle(tf, TabletColors.SURFACE_PANEL_ALT, TabletColors.BORDER_BASE);
        popup.addWidget(tf);
        TextFieldWidget bf = StyledTextFields.integerField(pad + halfW + pad, 64, halfW, 14, b, 0, Integer.MAX_VALUE, 4, v -> {
            try { vals[3] = Integer.parseInt(v); } catch (NumberFormatException ignored) {}
        }, apply, onCancel, apply);
        bf.setClientSideWidget();
        StyledTextFields.applyStandardStyle(bf, TabletColors.SURFACE_PANEL_ALT, TabletColors.BORDER_BASE);
        popup.addWidget(bf);
        ActionButtons.iconAction(popup, pad, buttonY, buttonW, buttonH, "add", TabletTranslationKeys.text(TabletTranslationKeys.COMMON_SAVE), TabletColors.SUCCESS, null, click -> apply.run());
        ActionButtons.iconAction(popup, w - pad - buttonW, buttonY, buttonW, buttonH, "close", TabletTranslationKeys.text(TabletTranslationKeys.COMMON_CANCEL), TabletColors.ERROR, null, click -> onCancel.run());
        lf.setFocus(true);
        return popup;
    }

    @FunctionalInterface
    public interface QuadConsumer<A, B, C, D> {
        void accept(A a, B b, C c, D d);
    }
}
