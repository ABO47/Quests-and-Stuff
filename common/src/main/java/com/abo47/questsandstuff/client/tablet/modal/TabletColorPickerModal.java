package com.abo47.questsandstuff.client.tablet.modal;


import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.controls.SearchFieldController;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.controls.StyledTextFields;
import com.abo47.questsandstuff.client.tablet.controls.picker.TiledPickerPanel;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.HsbColorWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.addWindowsContextRow;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.button;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.confirmDeleteClick;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.panel;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.pendingDeleteLabel;
import static com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries.selectedGroupName;
import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;
import static com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions.closeColorPicker;

public final class TabletColorPickerModal {
    private TabletColorPickerModal() {
    }

    public static void rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        ModalShell.addTitleAndClose(modal, TabletModalPanel.tr("ui.questsandstuff.modal.color_picker"), w, state, refresh);
        String target = state.colorPickerTarget == null || state.colorPickerTarget.isBlank() ? selectedGroupName(state) : state.colorPickerTarget;
        int leftW = 232;
        int rightW = w - leftW - 20;
        int panelH = h - 30;
        WidgetGroup left = panel(8, 22, leftW, panelH, withAlpha(ModColors.SURFACE_PANEL_ALT, 160), ModColors.BORDER_BASE);
        WidgetGroup right = panel(8 + leftW + 4, 22, rightW, panelH, withAlpha(ModColors.SURFACE_PANEL_ALT, 160), ModColors.BORDER_BASE);
        int wheelSize = Math.min(leftW - 20, panelH - 84);
        HsbColorWidget picker = new HsbColorWidget(10, 10, wheelSize, wheelSize)
                .setShowAlpha(false)
                .setColor(TabletModalPanel.currentColorPickerValue(state, target))
                .setOnChanged(color -> {
                    state.colorDraft = color;
                    state.colorHexDraft = SearchFieldController.toHexColor(color);
                });
        left.addWidget(picker);
        TextFieldWidget hexField = StyledTextFields.hexField(
                10,
                wheelSize + 18,
                leftW - 20,
                12,
                () -> state.colorHexDraft,
                value -> state.colorHexDraft = value == null ? "" : value,
                () -> {
                    int parsed = SearchFieldController.parseHexColor(state.colorHexDraft, TabletModalPanel.currentColorPickerValue(state, target));
                    state.colorDraft = parsed;
                    state.colorHexDraft = SearchFieldController.toHexColor(parsed);
                    refresh.run();
                },
                () -> {
                },
                () -> {
                }
        );
        state.colorHexDraft = state.colorHexDraft.isBlank() ? SearchFieldController.toHexColor(TabletModalPanel.currentColorPickerValue(state, target)) : state.colorHexDraft;
        hexField.setCurrentString(state.colorHexDraft);
        left.addWidget(hexField);
        left.addWidget(button(10, panelH - 34, leftW - 20, 12, TabletModalPanel.tr("ui.questsandstuff.color.apply_hex"), ModColors.SURFACE_PANEL, ModColors.INTERACTIVE, click -> {
            int parsed = SearchFieldController.parseHexColor(state.colorHexDraft, TabletModalPanel.currentColorPickerValue(state, target));
            state.colorDraft = parsed;
            state.colorHexDraft = SearchFieldController.toHexColor(parsed);
            refresh.run();
        }));
        left.addWidget(button(10, panelH - 18, leftW - 20, 12, TabletModalPanel.tr("ui.questsandstuff.common.use"), ModColors.SURFACE_PANEL, ModColors.SUCCESS, click -> {
            TabletModalPanel.applyColorPickerValue(player, state, target, TabletModalPanel.currentColorPickerValue(state, target));
            closeColorPicker(state);
            refresh.run();
        }));

        int cell = 18;
        int paletteTop = 6;
        int paletteH = Math.max(cell, panelH - 46);
        TiledPickerPanel.add(
                right,
                4,
                paletteTop,
                rightW - 8,
                paletteH,
                cell - 2,
                cell - 2,
                2,
                4,
                4,
                state.textColorPalette,
                TabletModalPanel.tr("ui.questsandstuff.common.none_short"),
                ScrollState.bind(
                        () -> state.colorPaletteScroll,
                        value -> state.colorPaletteScroll = value,
                        () -> state.colorPaletteScrollDragging,
                        dragging -> state.colorPaletteScrollDragging = dragging
                ),
                () -> state.colorPaletteContextOpen = false,
                refresh,
                (surface, color, index, px, py, tileW, tileH, layout) -> {
            WidgetGroup chip = new WidgetGroup(px, py, 16, 16);
            chip.setBackground(Surfaces.bordered(color, ModColors.BORDER_BASE));
            surface.addWidget(chip);
            ButtonWidget hit = flatHitButton(px, py, 16, 16, click -> {
                if (click.button == 1) {
                    state.colorPaletteContextOpen = true;
                    state.colorPaletteContextX = 4 + px + 8;
                    state.colorPaletteContextY = paletteTop + py + 8;
                    state.colorPaletteContextValue = color;
                    refresh.run();
                    return;
                }
                boolean doubleClick = TabletModalPanel.acceptPickerDoubleClick(state, ModalTargets.doubleClickKey("color", target, color));
                state.colorDraft = color;
                state.colorHexDraft = SearchFieldController.toHexColor(color);
                if (doubleClick) {
                    TabletModalPanel.applyColorPickerValue(player, state, target, color);
                    closeColorPicker(state);
                }
                refresh.run();
            });
            hit.setHoverTexture(Surfaces.transparentBorder(ModColors.BORDER_ACCENT));
            surface.addWidget(hit);
                });
        addPaletteContext(right, state, player, refresh, target, rightW, panelH);
        right.addWidget(button(8, panelH - 34, rightW - 16, 12, TabletModalPanel.tr("ui.questsandstuff.color.save_to_palette"), ModColors.SURFACE_PANEL, ModColors.INTERACTIVE, click -> {
            int chosen = TabletModalPanel.currentColorPickerValue(state, target);
            if (!state.textColorPalette.contains(chosen)) {
                state.textColorPalette.add(chosen);
            }
            refresh.run();
        }));
        String removeLastKey = "palette:remove_last";
        right.addWidget(button(8, panelH - 18, rightW - 16, 12, pendingDeleteLabel(state, removeLastKey, TabletModalPanel.tr("ui.questsandstuff.color.remove_last")), ModColors.SURFACE_PANEL, ModColors.ERROR, click -> {
            if (!confirmDeleteClick(state, removeLastKey)) {
                refresh.run();
                return;
            }
            if (!state.textColorPalette.isEmpty()) {
                state.textColorPalette.remove(state.textColorPalette.size() - 1);
            }
            refresh.run();
        }));
        modal.addWidget(left);
        modal.addWidget(right);
    }

    private static void addPaletteContext(WidgetGroup right, TabletUiState state, Player player, Runnable refresh, String target, int rightW, int panelH) {
        if (!state.colorPaletteContextOpen || state.colorPaletteContextValue == Integer.MIN_VALUE) {
            return;
        }
        int ctxW = 96;
        int ctxH = 36;
        int ctxX = Math.max(4, Math.min(state.colorPaletteContextX, rightW - ctxW - 4));
        int ctxY = Math.max(4, Math.min(state.colorPaletteContextY, panelH - ctxH - 4));
        WidgetGroup ctx = panel(ctxX, ctxY, ctxW, ctxH, withAlpha(ModColors.SURFACE_BASE, 236), ModColors.BORDER_ACCENT);
        addWindowsContextRow(ctx, 4, ctxW - 8, TabletModalPanel.tr("ui.questsandstuff.common.use"), "add", click -> {
            TabletModalPanel.applyColorPickerValue(player, state, target, state.colorPaletteContextValue);
            closeColorPicker(state);
            refresh.run();
        });
        String key = "palette:delete:" + state.colorPaletteContextValue;
        addWindowsContextRow(ctx, 18, ctxW - 8, pendingDeleteLabel(state, key, TabletModalPanel.tr("ui.questsandstuff.menu.delete")), "delete", click -> {
            int chosen = state.colorPaletteContextValue;
            if (!confirmDeleteClick(state, key)) {
                refresh.run();
                return;
            }
            state.textColorPalette.removeIf(value -> value == chosen);
            state.colorPaletteContextOpen = false;
            state.colorPaletteContextValue = Integer.MIN_VALUE;
            refresh.run();
        });
        right.addWidget(ctx);
    }
}
