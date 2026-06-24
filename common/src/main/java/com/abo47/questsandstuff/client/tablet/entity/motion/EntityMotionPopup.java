package com.abo47.questsandstuff.client.tablet.entity.motion;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.controls.StyledTextFields;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.closeIconButton;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.label;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.panel;
import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;

final class EntityMotionPopup {
    private static final int W = 188;
    private static final int H = 56;
    private static final int PAD = 8;
    private static final int FIELD_W = 34;
    private static final int FIELD_H = 14;

    private EntityMotionPopup() {
    }

    static void render(WidgetGroup parent, TabletUiState state, Player player, Runnable refresh, EntityMotionValues motion, int maxW, int maxH) {
        int x = Math.max(4, Math.min(state.questDetails.entityMotionEditorX, Math.max(4, maxW - W - 4)));
        int y = Math.max(4, Math.min(state.questDetails.entityMotionEditorY, Math.max(4, maxH - H - 4)));
        state.questDetails.entityMotionEditorX = x;
        state.questDetails.entityMotionEditorY = y;
        state.questDetails.entityMotionEditorW = W;
        state.questDetails.entityMotionEditorH = H;

        WidgetGroup popup = panel(x, y, W, H, withAlpha(ModColors.SURFACE_BASE, 248), ModColors.BORDER_ACCENT);
        popup.addWidget(panel(1, 1, W - 2, H - 2, withAlpha(ModColors.SURFACE_PANEL_ALT, 170), ModColors.BORDER_BASE));
        popup.addWidget(label(PAD, 6, "Entity spin", ModColors.TEXT_PRIMARY));
        popup.addWidget(closeIconButton(W - 19, 3, 14, 14, click -> {
            EntityMotionEditor.close(state);
            refresh.run();
        }));
        TextFieldWidget spinField = addSpinRow(popup, state, player, refresh, motion, 26);
        parent.addWidget(popup);
        restoreFieldFocus(state, spinField);
    }

    private static TextFieldWidget addSpinRow(WidgetGroup parent, TabletUiState state, Player player, Runnable refresh, EntityMotionValues motion, int y) {
        int value = motion.spin();
        int max = CanvasImageLayer.MAX_ENTITY_SPIN_SPEED;
        String draft = state.questDetails.entityMotionSpinDraft;
        if (draft == null || draft.isBlank()) {
            draft = Integer.toString(value);
            state.questDetails.entityMotionSpinDraft = draft;
        }
        parent.addWidget(label(PAD, y + 4, "Spin", ModColors.TEXT_SECONDARY));
        parent.addWidget(new EntityMotionSliderWidget(
                56,
                y + 1,
                80,
                16,
                0,
                max,
                value,
                next -> {
                    EntityMotionTargets.setDraft(state, false, next);
                    EntityMotionTargets.applyMotion(player, state, false);
                    QuestsAndStuffMod.debugLog("[QnS:UI] entity motion preview scope={} image={} spin={}", state.questDetails.entityMotionEditorScope, state.questDetails.entityMotionEditorImageId, next);
                    refresh.run();
                },
                () -> {
                    EntityMotionTargets.applyMotion(player, state, true);
                    QuestsAndStuffMod.debugLog("[QnS:UI] entity motion commit scope={} image={} spin={}", state.questDetails.entityMotionEditorScope, state.questDetails.entityMotionEditorImageId, state.questDetails.entityMotionSpinDraft);
                    refresh.run();
                },
                () -> state.questDetails.entityMotionSpinSliderDragging,
                dragging -> state.questDetails.entityMotionSpinSliderDragging = dragging
        ));
        TextFieldWidget field = numberField(player, state, refresh, W - PAD - FIELD_W, y + 2, FIELD_W, FIELD_H, max);
        parent.addWidget(field);
        return field;
    }

    private static TextFieldWidget numberField(Player player, TabletUiState state, Runnable refresh, int x, int y, int w, int h, int max) {
        String draft = state.questDetails.entityMotionSpinDraft;
        TextFieldWidget field = StyledTextFields.numberField(
                x,
                y,
                w,
                h,
                EntityMotionTargets.parseDraft(draft, 0, max),
                0,
                max,
                3,
                value -> {
                    state.questDetails.entityMotionSpinDraft = sanitizeNumber(value);
                },
                () -> {
                    state.questDetails.entityMotionFocusedField = "";
                    commitNumberField(player, state);
                    refresh.run();
                },
                () -> {
                    state.questDetails.entityMotionFocusedField = "";
                    EntityMotionTargets.resetDrafts(state, EntityMotionTargets.currentMotionValues(state));
                    refresh.run();
                },
                () -> {
                    if (state.questDetails.entityMotionEditorOpen) {
                        state.questDetails.entityMotionFocusedField = "";
                        commitNumberField(player, state);
                        refresh.run();
                    }
                },
                focused -> {
                    if (focused) {
                        state.questDetails.entityMotionFocusedField = "spin";
                    }
                }
        );
        field.setCurrentString(draft);
        StyledTextFields.applyStandardStyle(field, ModColors.SURFACE_BASE, ModColors.BORDER_BASE);
        field.setHoverTooltips(new Component[]{Component.literal("Spin speed")});
        return field;
    }

    private static void restoreFieldFocus(TabletUiState state, TextFieldWidget spinField) {
        String focused = state.questDetails.entityMotionFocusedField == null ? "" : state.questDetails.entityMotionFocusedField;
        if ("spin".equals(focused)) {
            spinField.setFocus(true);
        }
    }

    private static void commitNumberField(Player player, TabletUiState state) {
        EntityMotionValues motion = EntityMotionTargets.currentMotionValues(state);
        if (motion == null) {
            EntityMotionEditor.close(state);
            return;
        }
        int value = EntityMotionTargets.parseDraft(state.questDetails.entityMotionSpinDraft, motion.spin(), CanvasImageLayer.MAX_ENTITY_SPIN_SPEED);
        EntityMotionTargets.setDraft(state, false, value);
        EntityMotionTargets.applyMotion(player, state, true);
        QuestsAndStuffMod.debugLog("[QnS:UI] entity motion number commit scope={} image={} spin={}", state.questDetails.entityMotionEditorScope, state.questDetails.entityMotionEditorImageId, value);
    }

    private static String sanitizeNumber(String value) {
        return value == null ? "" : value.trim();
    }
}
