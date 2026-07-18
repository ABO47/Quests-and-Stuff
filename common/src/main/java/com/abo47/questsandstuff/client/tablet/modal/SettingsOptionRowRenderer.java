package com.abo47.questsandstuff.client.tablet.modal;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.StyledTextFields;
import com.abo47.questsandstuff.client.tablet.controls.ToggleSwitchWidget;
import com.abo47.questsandstuff.client.tablet.icons.IconAtlas;
import com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.label;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.panel;

public final class SettingsOptionRowRenderer {
    public static final int ROW_H = ROW_H_26;

    static final int ROW_INSET = GRID_4;
    private static final int SWITCH_GAP = GRID_8;

    private SettingsOptionRowRenderer() {
    }

    public static void render(WidgetGroup list, SettingsOptionDescriptor option, int rowY, int rowW, Runnable refresh, boolean skinEditMode) {
        if (option.isAction()) {
            renderActionOptionRow(list, option, rowY, rowW, skinEditMode);
            return;
        }
        if (option.number()) {
            renderNumberOptionRow(list, option, rowY, rowW, refresh, skinEditMode);
            return;
        }
        boolean enabled = option.enabled();
        int rowH = ROW_H - ROW_INSET;
        int cardW = rowW;
        int fill = enabled ? withAlpha(TabletColors.SUCCESS, 28) : withAlpha(TabletColors.SURFACE_PANEL_ALT, 180);
        int border = enabled ? withAlpha(TabletColors.SUCCESS, 170) : TabletColors.BORDER_BASE;
        list.addWidget(panel(0, rowY, cardW, rowH, fill, border));
        list.addWidget(hoverFill(0, rowY, cardW, rowH));

        Component[] tooltips = tooltips(option);
        int switchX = Math.max(104, cardW - ToggleSwitchWidget.DEFAULT_WIDTH - SWITCH_GAP);
        int textW = Math.max(16, switchX - 14);
        int crop = Math.max(14, textW / 6);
        int titleColor = enabled ? TabletColors.TEXT_PRIMARY : TabletColors.TEXT_SECONDARY;
        list.addWidget(label(8, rowY + 7, SearchFilter.crop(TabletModalPanel.tr(option.labelKey()), crop), titleColor));
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
        if (!skinEditMode) {
            ButtonWidget hit = flatHitButton(0, rowY, cardW, rowH, click -> toggle(option, refresh));
            hit.setHoverTexture(GlowShaderHelper.hoverGlow());
            hit.setClickedTexture(SurfaceFactory.fill(withAlpha(TabletColors.INTERACTIVE, 64)));
            hit.setHoverTooltips(tooltips);
            list.addWidget(hit);
        }
    }

    private static void renderActionOptionRow(WidgetGroup list, SettingsOptionDescriptor option, int rowY, int rowW, boolean skinEditMode) {
        int rowH = ROW_H - ROW_INSET;
        int cardW = rowW;
        list.addWidget(panel(0, rowY, cardW, rowH, withAlpha(TabletColors.INTERACTIVE, 28), TabletColors.BORDER_BASE));
        list.addWidget(hoverFill(0, rowY, cardW, rowH));
        Component[] tooltips = tooltips(option);
        int iconSize = 14;
        int iconX = cardW - iconSize - SWITCH_GAP;
        var texture = IconAtlas.iconTexture("hud_layout");
        if (texture != null) {
            list.addWidget(new ImageWidget(iconX, rowY + Math.max(1, (rowH - iconSize) / 2), iconSize, iconSize, texture));
        }
        int textW = Math.max(16, iconX - 14);
        int crop = Math.max(14, textW / 6);
        list.addWidget(label(8, rowY + 7, SearchFilter.crop(TabletModalPanel.tr(option.labelKey()), crop), TabletColors.TEXT_PRIMARY));
        if (!skinEditMode) {
            ButtonWidget hit = flatHitButton(0, rowY, cardW, rowH, click -> option.runAction());
            hit.setHoverTexture(GlowShaderHelper.hoverGlow());
            hit.setClickedTexture(SurfaceFactory.fill(withAlpha(TabletColors.INTERACTIVE, 82)));
            hit.setHoverTooltips(tooltips);
            list.addWidget(hit);
        }
    }

    private static void renderNumberOptionRow(WidgetGroup list, SettingsOptionDescriptor option, int rowY, int rowW, Runnable refresh, boolean skinEditMode) {
        int rowH = ROW_H - ROW_INSET;
        int cardW = rowW;
        list.addWidget(panel(0, rowY, cardW, rowH, withAlpha(TabletColors.SURFACE_PANEL_ALT, 180), TabletColors.BORDER_BASE));

        Component[] tooltips = tooltips(option);
        int unitW = 18;
        int fieldW = 54;
        int fieldX = Math.max(104, cardW - fieldW - unitW - SWITCH_GAP);
        int textW = Math.max(16, fieldX - 14);
        int crop = Math.max(14, textW / 6);
        list.addWidget(label(8, rowY + 7, SearchFilter.crop(TabletModalPanel.tr(option.labelKey()), crop), TabletColors.TEXT_SECONDARY));

        final String[] live = { Integer.toString(option.intValue()) };
        Runnable commit = () -> {
            int next = parseNumber(live[0], option.intValue(), option.min(), option.max());
            if (next == option.intValue()) {
                live[0] = Integer.toString(next);
                return;
            }
            option.setIntValue(next);
            QuestsAndStuffMod.debugLog("[QnS:UI] settings number {}={}", option.id(), next);
            refresh.run();
        };
        if (!skinEditMode) {
            TextFieldWidget field = StyledTextFields.commitField(
                    fieldX,
                    rowY + 4,
                    fieldW,
                    GRID_14,
                    () -> live[0],
                    raw -> live[0] = raw,
                    commit,
                    () -> {
                        live[0] = Integer.toString(option.intValue());
                        refresh.run();
                    },
                    commit
            );
            StyledTextFields.applyStandardStyle(field, TabletColors.SURFACE_BASE, TabletColors.BORDER_BASE);
            field.setClientSideWidget();
            field.setNumbersOnly(option.min(), option.max());
            field.setMaxStringLength(option.maxLength());
            field.setHoverTooltips(tooltips);
            list.addWidget(field);
        } else {
            list.addWidget(label(fieldX, rowY + 7, Integer.toString(option.intValue()), TabletColors.TEXT_SECONDARY));
        }
        list.addWidget(label(fieldX + fieldW + 4, rowY + 7, TabletModalPanel.tr(option.unitKey()), TabletColors.TEXT_MUTED));
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

    private static int parseNumber(String raw, int fallback, int min, int max) {
        if (raw == null || raw.isBlank()) {
            return Math.max(min, Math.min(max, fallback));
        }
        try {
            return Math.max(min, Math.min(max, Integer.parseInt(raw.trim())));
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

    private static WidgetGroup hoverFill(int x, int y, int w, int h) {
        WidgetGroup fill = new WidgetGroup(x, y, w, h) {
            @Override
            public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                if (isMouseOverElement(mouseX, mouseY)) {
                    SurfaceFactory.fill(withAlpha(TabletColors.SURFACE_PANEL_ALT, 26)).draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
                }
            }
        };
        return fill;
    }
}
