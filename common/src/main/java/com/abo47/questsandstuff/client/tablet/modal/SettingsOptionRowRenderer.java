package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.StyledTextFields;
import com.abo47.questsandstuff.client.tablet.controls.ToggleSwitchWidget;
import com.abo47.questsandstuff.client.tablet.icons.IconAtlas;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.label;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.panel;

final class SettingsOptionRowRenderer {
    static final int ROW_H = 26;

    private static final int ROW_INSET = 4;
    private static final int SWITCH_GAP = 8;

    private SettingsOptionRowRenderer() {
    }

    static void render(WidgetGroup list, SettingsOptionDescriptor option, int rowY, int rowW, Runnable refresh) {
        if (option.isAction()) {
            renderActionOptionRow(list, option, rowY, rowW);
            return;
        }
        if (option.number()) {
            renderNumberOptionRow(list, option, rowY, rowW, refresh);
            return;
        }
        boolean enabled = option.enabled();
        int rowX = ROW_INSET;
        int rowH = ROW_H - ROW_INSET;
        int cardW = Math.max(1, rowW - ROW_INSET * 2);
        int fill = enabled ? withAlpha(TabletColors.SUCCESS, 28) : withAlpha(TabletColors.SURFACE_PANEL_ALT, 180);
        int border = enabled ? withAlpha(TabletColors.SUCCESS, 170) : TabletColors.BORDER_BASE;
        list.addWidget(panel(rowX, rowY, cardW, rowH, fill, border));

        Component[] tooltips = tooltips(option);
        ButtonWidget hit = flatHitButton(rowX, rowY, cardW, rowH, click -> toggle(option, refresh));
        hit.setHoverTexture(SurfaceFactory.transparentBorder(TabletColors.BORDER_ACCENT));
        hit.setClickedTexture(SurfaceFactory.fill(withAlpha(TabletColors.INTERACTIVE, 64)));
        hit.setHoverTooltips(tooltips);

        int switchX = Math.max(rowX + 104, rowX + cardW - ToggleSwitchWidget.DEFAULT_WIDTH - SWITCH_GAP);
        int textW = Math.max(16, switchX - rowX - 14);
        int crop = Math.max(14, textW / 6);
        int titleColor = enabled ? TabletColors.TEXT_PRIMARY : TabletColors.TEXT_SECONDARY;
        list.addWidget(label(rowX + 8, rowY + 7, SearchFilter.crop(TabletModalPanel.tr(option.labelKey()), crop), titleColor));
        list.addWidget(new ToggleSwitchWidget(
                option.id(),
                switchX,
                rowY + 3,
                ToggleSwitchWidget.DEFAULT_WIDTH,
                ToggleSwitchWidget.DEFAULT_HEIGHT,
                option::enabled,
                value -> setOption(option, value),
                refresh,
                tooltips
        ));
        list.addWidget(hit);
    }

    private static void renderActionOptionRow(WidgetGroup list, SettingsOptionDescriptor option, int rowY, int rowW) {
        int rowX = ROW_INSET;
        int rowH = ROW_H - ROW_INSET;
        int cardW = Math.max(1, rowW - ROW_INSET * 2);
        list.addWidget(panel(rowX, rowY, cardW, rowH, withAlpha(TabletColors.INTERACTIVE, 28), TabletColors.BORDER_BASE));
        Component[] tooltips = tooltips(option);
        int iconSize = 14;
        int iconX = rowX + cardW - iconSize - SWITCH_GAP;
        var texture = IconAtlas.iconTexture("hud_layout");
        if (texture != null) {
            list.addWidget(new ImageWidget(iconX, rowY + Math.max(1, (rowH - iconSize) / 2), iconSize, iconSize, texture));
        }
        int textW = Math.max(16, iconX - rowX - 14);
        int crop = Math.max(14, textW / 6);
        list.addWidget(label(rowX + 8, rowY + 7, SearchFilter.crop(TabletModalPanel.tr(option.labelKey()), crop), TabletColors.TEXT_PRIMARY));
        ButtonWidget hit = flatHitButton(rowX, rowY, cardW, rowH, click -> option.runAction());
        hit.setHoverTexture(SurfaceFactory.bordered(withAlpha(TabletColors.INTERACTIVE, 58), TabletColors.BORDER_ACCENT));
        hit.setClickedTexture(SurfaceFactory.fill(withAlpha(TabletColors.INTERACTIVE, 82)));
        hit.setHoverTooltips(tooltips);
        list.addWidget(hit);
    }

    private static void renderNumberOptionRow(WidgetGroup list, SettingsOptionDescriptor option, int rowY, int rowW, Runnable refresh) {
        int rowX = ROW_INSET;
        int rowH = ROW_H - ROW_INSET;
        int cardW = Math.max(1, rowW - ROW_INSET * 2);
        list.addWidget(panel(rowX, rowY, cardW, rowH, withAlpha(TabletColors.SURFACE_PANEL_ALT, 180), TabletColors.BORDER_BASE));

        Component[] tooltips = tooltips(option);
        int unitW = 18;
        int fieldW = 54;
        int fieldX = Math.max(rowX + 104, rowX + cardW - fieldW - unitW - SWITCH_GAP);
        int textW = Math.max(16, fieldX - rowX - 14);
        int crop = Math.max(14, textW / 6);
        list.addWidget(label(rowX + 8, rowY + 7, SearchFilter.crop(TabletModalPanel.tr(option.labelKey()), crop), TabletColors.TEXT_SECONDARY));

        final TextFieldWidget[] fieldRef = new TextFieldWidget[1];
        Runnable commit = () -> {
            int next = parseNumber(fieldRef[0], option.intValue(), option.min(), option.max());
            if (next == option.intValue()) {
                if (fieldRef[0] != null) {
                    fieldRef[0].setCurrentString(Integer.toString(next));
                }
                return;
            }
            option.setIntValue(next);
            QuestsAndStuffMod.debugLog("[QnS:UI] settings number {}={}", option.id(), next);
            refresh.run();
        };
        TextFieldWidget field = StyledTextFields.numberField(
                fieldX,
                rowY + 4,
                fieldW,
                14,
                option.intValue(),
                option.min(),
                option.max(),
                option.maxLength(),
                raw -> {
                },
                commit,
                () -> {
                },
                commit
        );
        field.setHoverTooltips(tooltips);
        fieldRef[0] = field;
        list.addWidget(field);
        list.addWidget(label(fieldX + fieldW + 4, rowY + 7, TabletModalPanel.tr("ui.questsandstuff.settings.duration_unit_ms"), TabletColors.TEXT_MUTED));
    }

    private static void toggle(SettingsOptionDescriptor option, Runnable refresh) {
        boolean from = option.enabled();
        boolean to = !from;
        ToggleSwitchWidget.beginAnimation(option.id(), from, to);
        setOption(option, to);
        refresh.run();
    }

    private static void setOption(SettingsOptionDescriptor option, boolean enabled) {
        if (option.enabled() == enabled) {
            return;
        }
        option.setEnabled(enabled);
        QuestsAndStuffMod.debugLog("[QnS:UI] settings toggle {}={}", option.id(), enabled);
    }

    private static int parseNumber(TextFieldWidget field, int fallback, int min, int max) {
        if (field == null || field.getRawCurrentString() == null || field.getRawCurrentString().isBlank()) {
            return Math.max(min, Math.min(max, fallback));
        }
        try {
            return Math.max(min, Math.min(max, Integer.parseInt(field.getRawCurrentString().trim())));
        } catch (NumberFormatException ignored) {
            return Math.max(min, Math.min(max, fallback));
        }
    }

    private static Component[] tooltips(SettingsOptionDescriptor option) {
        if (option.isAction()) {
            return new Component[]{
                    Component.translatable(option.labelKey()).withStyle(ChatFormatting.WHITE),
                    Component.translatable(option.descriptionKey()).withStyle(ChatFormatting.GRAY)
            };
        }
        if (!option.requiresGlobalAnimation()) {
            return new Component[]{
                    Component.translatable(option.labelKey()).withStyle(ChatFormatting.WHITE),
                    Component.translatable(option.descriptionKey()).withStyle(ChatFormatting.GRAY),
                    restartTooltip(option)
            };
        }
        return new Component[]{
                Component.translatable(option.labelKey()).withStyle(ChatFormatting.WHITE),
                Component.translatable(option.descriptionKey()).withStyle(ChatFormatting.GRAY),
                Component.translatable("ui.questsandstuff.settings.requires_global").withStyle(ChatFormatting.DARK_GRAY),
                restartTooltip(option)
        };
    }

    private static Component restartTooltip(SettingsOptionDescriptor option) {
        return Component.translatable(option.restartRequired()
                        ? "ui.questsandstuff.settings.restart_required"
                        : "ui.questsandstuff.settings.restart_not_required")
                .withStyle(option.restartRequired() ? ChatFormatting.YELLOW : ChatFormatting.GREEN);
    }
}
