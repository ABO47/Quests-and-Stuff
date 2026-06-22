package com.abo47.questsandstuff.client.tablet.modal;


import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.controls.SearchFieldController;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.controls.StyledTextFields;
import com.abo47.questsandstuff.client.tablet.controls.picker.TiledPickerPanel;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextActions;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.HsbColorWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

import static com.abo47.questsandstuff.client.tablet.modal.ModalSession.TargetSlot.COLOR_PICKER;
import static com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions.closeColorPicker;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.button;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.confirmDeleteClick;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.panel;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.pendingDeleteLabel;
import static com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries.selectedGroupName;
import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;

public final class TabletColorPickerModal {
    private TabletColorPickerModal() {
    }

    public static void rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        ModalShell.addTitleAndClose(modal, TabletModalPanel.tr("ui.questsandstuff.modal.color_picker"), w, state, refresh);
        String resolvedTarget = ModalTargetState.target(state, COLOR_PICKER, state.pickers.colorPickerTarget);
        final String target = resolvedTarget.isBlank() ? selectedGroupName(state) : resolvedTarget;
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
                    state.pickers.colorDraft = color;
                    state.pickers.colorHexDraft = SearchFieldController.toHexColor(color);
                });
        left.addWidget(picker);
        TextFieldWidget hexField = StyledTextFields.hexField(
                10,
                wheelSize + 18,
                leftW - 20,
                12,
                () -> state.pickers.colorHexDraft,
                value -> state.pickers.colorHexDraft = value == null ? "" : value,
                () -> {
                    int parsed = SearchFieldController.parseHexColor(state.pickers.colorHexDraft, TabletModalPanel.currentColorPickerValue(state, target));
                    state.pickers.colorDraft = parsed;
                    state.pickers.colorHexDraft = SearchFieldController.toHexColor(parsed);
                    refresh.run();
                },
                () -> {
                },
                () -> {
                }
        );
        state.pickers.colorHexDraft = state.pickers.colorHexDraft.isBlank() ? SearchFieldController.toHexColor(TabletModalPanel.currentColorPickerValue(state, target)) : state.pickers.colorHexDraft;
        hexField.setCurrentString(state.pickers.colorHexDraft);
        left.addWidget(hexField);
        left.addWidget(button(10, panelH - 34, leftW - 20, 12, TabletModalPanel.tr("ui.questsandstuff.color.apply_hex"), ModColors.SURFACE_PANEL, ModColors.INTERACTIVE, click -> {
            int parsed = SearchFieldController.parseHexColor(state.pickers.colorHexDraft, TabletModalPanel.currentColorPickerValue(state, target));
            state.pickers.colorDraft = parsed;
            state.pickers.colorHexDraft = SearchFieldController.toHexColor(parsed);
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
                state.pickers.textColorPalette,
                TabletModalPanel.tr("ui.questsandstuff.common.none_short"),
                ScrollState.bind(
                        () -> state.pickers.colorPaletteScroll,
                        value -> state.pickers.colorPaletteScroll = value,
                        () -> state.pickers.colorPaletteScrollDragging,
                        dragging -> state.pickers.colorPaletteScrollDragging = dragging
                ),
                () -> state.pickers.colorPaletteContextOpen = false,
                refresh,
                (surface, color, index, px, py, tileW, tileH, layout) -> {
            WidgetGroup chip = new WidgetGroup(px, py, 16, 16);
            chip.setBackground(Surfaces.bordered(color, ModColors.BORDER_BASE));
            surface.addWidget(chip);
            ButtonWidget hit = flatHitButton(px, py, 16, 16, click -> {
                if (click.button == 1) {
                    state.pickers.colorPaletteContextOpen = true;
                    state.pickers.colorPaletteContextX = 4 + px + 8;
                    state.pickers.colorPaletteContextY = paletteTop + py + 8;
                    state.pickers.colorPaletteContextValue = color;
                    refresh.run();
                    return;
                }
                boolean doubleClick = TabletModalPanel.acceptPickerDoubleClick(state, ModalTargets.doubleClickKey("color", target, color));
                state.pickers.colorDraft = color;
                state.pickers.colorHexDraft = SearchFieldController.toHexColor(color);
                if (doubleClick) {
                    TabletModalPanel.applyColorPickerValue(player, state, target, color);
                    closeColorPicker(state);
                }
                refresh.run();
            });
            hit.setHoverTexture(Surfaces.transparentBorder(ModColors.BORDER_ACCENT));
            surface.addWidget(hit);
                });
        right.addWidget(button(8, panelH - 34, rightW - 16, 12, TabletModalPanel.tr("ui.questsandstuff.color.save_to_palette"), ModColors.SURFACE_PANEL, ModColors.INTERACTIVE, click -> {
            int chosen = TabletModalPanel.currentColorPickerValue(state, target);
            if (!state.pickers.textColorPalette.contains(chosen)) {
                state.pickers.textColorPalette.add(chosen);
            }
            refresh.run();
        }));
        String removeLastKey = "palette:remove_last";
        right.addWidget(button(8, panelH - 18, rightW - 16, 12, pendingDeleteLabel(state, removeLastKey, TabletModalPanel.tr("ui.questsandstuff.color.remove_last")), ModColors.SURFACE_PANEL, ModColors.ERROR, click -> {
            if (!confirmDeleteClick(state, removeLastKey)) {
                refresh.run();
                return;
            }
            if (!state.pickers.textColorPalette.isEmpty()) {
                state.pickers.textColorPalette.remove(state.pickers.textColorPalette.size() - 1);
            }
            refresh.run();
        }));
        modal.addWidget(left);
        modal.addWidget(right);
        addPaletteContext(modal, state, player, refresh, target, leftW);
    }

    private static void addPaletteContext(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String target, int leftW) {
        if (!state.pickers.colorPaletteContextOpen || state.pickers.colorPaletteContextValue == Integer.MIN_VALUE) {
            return;
        }
        int ctxW = 96;
        int ctxX = 8 + leftW + 4 + state.pickers.colorPaletteContextX;
        int ctxY = 22 + state.pickers.colorPaletteContextY;
        List<ContextAction> actions = new ArrayList<>();
        actions.add(ContextActions.action(TabletModalPanel.tr("ui.questsandstuff.common.use"), "add", ModColors.INTERACTIVE, () -> {
            TabletModalPanel.applyColorPickerValue(player, state, target, state.pickers.colorPaletteContextValue);
            closeColorPicker(state);
            refresh.run();
        }));
        String key = "palette:delete:" + state.pickers.colorPaletteContextValue;
        actions.add(ContextActions.warningDelete(state, key, TabletModalPanel.tr("ui.questsandstuff.menu.delete"), () -> {
            state.pickers.textColorPalette.removeIf(value -> value == state.pickers.colorPaletteContextValue);
            state.pickers.colorPaletteContextOpen = false;
            state.pickers.colorPaletteContextValue = Integer.MIN_VALUE;
            refresh.run();
        }));
        int rowCount = ContextMenuPanel.rowActionCount(actions);
        int visibleRows = ContextMenuPanel.safeVisibleRows(rowCount, rowCount);
        modal.addWidget(ContextMenuPanel.build(ctxX, ctxY, ctxW, actions, 0, visibleRows, ModColors.BORDER_ACCENT, state, action -> {
            if (action.closeAfterClick()) {
                state.pickers.colorPaletteContextOpen = false;
                state.pickers.colorPaletteContextValue = Integer.MIN_VALUE;
            }
            refresh.run();
        }, "color_palette"));
    }
}
