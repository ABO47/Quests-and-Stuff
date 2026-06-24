package com.abo47.questsandstuff.client.tablet.modal;


import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.controls.ActionButtons;
import com.abo47.questsandstuff.client.tablet.controls.SearchFieldController;
import com.abo47.questsandstuff.client.tablet.controls.DragScrollBarWidget;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.controls.StyledTextFields;
import com.abo47.questsandstuff.client.tablet.controls.TileGridLayout;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextActions;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.HsbColorWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

import static com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions.closeColorPicker;
import static com.abo47.questsandstuff.client.tablet.modal.ModalSession.TargetSlot.COLOR_PICKER;
import static com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries.selectedGroupName;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.label;

public final class TabletColorPickerModal {
    private TabletColorPickerModal() {
    }

    public static void rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        ModalShell.addTitleAndClose(modal, TabletModalPanel.tr("ui.questsandstuff.modal.color_picker"), w, state, refresh);
        String resolvedTarget = ModalTargetState.target(state, COLOR_PICKER, state.pickers.colorPickerTarget);
        final String target = resolvedTarget.isBlank() ? selectedGroupName(state) : resolvedTarget;
        ModalLibraryLayout.Metrics lib = ModalLibraryLayout.calculate(w, h);
        int wheelSize = Math.min(lib.leftW() - 20, lib.bodyH() - 84);
        WidgetGroup left = ModalLibraryLayout.previewPanel(lib);
        HsbColorWidget picker = new HsbColorWidget(8, 8, wheelSize, wheelSize)
                .setShowAlpha(false)
                .setColor(TabletModalPanel.currentColorPickerValue(state, target))
                .setOnChanged(color -> {
                    state.pickers.colorDraft = color;
                    state.pickers.colorHexDraft = SearchFieldController.toHexColor(color);
                });
        left.addWidget(picker);
        TextFieldWidget hexField = StyledTextFields.hexField(
                8, wheelSize + 16, lib.leftW() - 16, 12,
                () -> state.pickers.colorHexDraft,
                value -> state.pickers.colorHexDraft = value == null ? "" : value,
                () -> {
                    int parsed = SearchFieldController.parseHexColor(state.pickers.colorHexDraft, TabletModalPanel.currentColorPickerValue(state, target));
                    state.pickers.colorDraft = parsed;
                    state.pickers.colorHexDraft = SearchFieldController.toHexColor(parsed);
                    refresh.run();
                },
                () -> {}, () -> {}
        );
        state.pickers.colorHexDraft = state.pickers.colorHexDraft.isBlank() ? SearchFieldController.toHexColor(TabletModalPanel.currentColorPickerValue(state, target)) : state.pickers.colorHexDraft;
        hexField.setCurrentString(state.pickers.colorHexDraft);
        left.addWidget(hexField);
        ActionButtons.iconAction(left, 8, lib.bodyH() - 20, lib.leftW() - 16,
                "mouse-pointer-click", TabletModalPanel.tr("ui.questsandstuff.common.use"), ModColors.SUCCESS, click -> {
            TabletModalPanel.applyColorPickerValue(player, state, target, TabletModalPanel.currentColorPickerValue(state, target));
            closeColorPicker(state);
            refresh.run();
        });
        modal.addWidget(left);

        WidgetGroup right = ModalShell.bodyPanel(lib.rightX(), lib.bodyY(), lib.rightW(), lib.bodyH());
        int paletteTop = 4;
        int paletteH = lib.bodyH() - 28;
        int cell = 18;
        ScrollState scroll = ScrollState.bind(
                () -> state.pickers.colorPaletteScroll,
                value -> state.pickers.colorPaletteScroll = value,
                () -> state.pickers.colorPaletteScrollDragging,
                dragging -> state.pickers.colorPaletteScrollDragging = dragging
        );
        TileGridLayout layout = TileGridLayout.calculate(
                lib.rightW() - 8, paletteH, cell - 2, cell - 2, 2, 4, 4,
                state.pickers.textColorPalette.size(), scroll.value()
        );
        scroll.setValue(layout.scrollStart());
        if (state.pickers.textColorPalette.isEmpty()) {
            right.addWidget(label(12, paletteTop + 8, TabletModalPanel.tr("ui.questsandstuff.common.none_short"), ModColors.TEXT_MUTED));
        } else {
            for (int i = layout.scrollStart(); i < layout.visibleEnd(); i++) {
                int visibleIndex = i - layout.scrollStart();
                int color = state.pickers.textColorPalette.get(i);
                int px = 4 + layout.tileX(visibleIndex);
                int py = paletteTop + layout.tileY(visibleIndex);
                ButtonWidget hit = flatHitButton(px, py, 16, 16, click -> {
                    if (click.button == 1) {
                        state.pickers.colorPaletteContextOpen = true;
                        Minecraft mc = Minecraft.getInstance();
                        state.pickers.colorPaletteContextX = (int) Math.round(mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getScreenWidth()) - (mc.getWindow().getGuiScaledWidth() - w) / 2;
                        state.pickers.colorPaletteContextY = (int) Math.round(mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getScreenHeight()) - (mc.getWindow().getGuiScaledHeight() - h) / 2;
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
                hit.setBackground(Surfaces.bordered(color, ModColors.BORDER_BASE));
                hit.setHoverTexture(Surfaces.transparentBorder(ModColors.BORDER_ACCENT));
                right.addWidget(hit);
            }
        }
        if (layout.showScroll()) {
            right.addWidget(new DragScrollBarWidget(
                    4 + layout.scrollBarX() + 1, paletteTop + layout.scrollBarY(),
                    DragScrollBarWidget.RESERVED_WIDTH, layout.scrollBarH(),
                    scroll::value, layout::maxStart, layout::knobH,
                    value -> scroll.setValue(value),
                    scroll::dragging, scroll::setDragging, refresh,
                    ModColors.scrollTrack(scroll.dragging()),
                    ModColors.scrollThumb(false), ModColors.scrollThumb(true),
                    DragScrollBarWidget.WIDTH
            ));
        }
        ActionButtons.iconAction(right, 4, lib.bodyH() - 20, lib.rightW() - 8,
                "add", TabletModalPanel.tr("ui.questsandstuff.color.save_to_palette"), ModColors.INTERACTIVE, click -> {
            int chosen = TabletModalPanel.currentColorPickerValue(state, target);
            if (!state.pickers.textColorPalette.contains(chosen)) {
                state.pickers.textColorPalette.add(chosen);
            }
            refresh.run();
        });
        modal.addWidget(right);
        addPaletteContext(modal, state, player, refresh, target, w, h);
    }

    private static void addPaletteContext(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String target, int w, int h) {
        if (!state.pickers.colorPaletteContextOpen || state.pickers.colorPaletteContextValue == Integer.MIN_VALUE) {
            return;
        }
        WidgetGroup dismiss = new WidgetGroup(0, 0, w, h) {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (state.pickers.colorPaletteContextOpen) {
                    state.pickers.colorPaletteContextOpen = false;
                    state.pickers.colorPaletteContextValue = Integer.MIN_VALUE;
                    refresh.run();
                }
                return false;
            }
        };
        modal.addWidget(dismiss);
        int ctxW = 96;
        int ctxX = state.pickers.colorPaletteContextX;
        int ctxY = state.pickers.colorPaletteContextY;
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
