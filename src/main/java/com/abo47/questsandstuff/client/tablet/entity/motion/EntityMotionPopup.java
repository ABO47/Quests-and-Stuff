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
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

final class EntityMotionPopup {
    private static final int W = 188;
    private static final int H = 82;
    private static final int PAD = 8;
    private static final int FIELD_W = 34;
    private static final int FIELD_H = 14;

    private EntityMotionPopup() {
    }

    static void render(WidgetGroup parent, TabletUiState state, Player player, Runnable refresh, EntityMotionValues motion, int maxW, int maxH) {
        int x = Math.max(4, Math.min(state.entityMotionEditorX, Math.max(4, maxW - W - 4)));
        int y = Math.max(4, Math.min(state.entityMotionEditorY, Math.max(4, maxH - H - 4)));
        state.entityMotionEditorX = x;
        state.entityMotionEditorY = y;
        state.entityMotionEditorW = W;
        state.entityMotionEditorH = H;

        WidgetGroup popup = panel(x, y, W, H, withAlpha(ModColors.SURFACE_BASE, 248), ModColors.BORDER_ACCENT);
        popup.addWidget(panel(1, 1, W - 2, H - 2, withAlpha(ModColors.SURFACE_PANEL_ALT, 170), ModColors.BORDER_BASE));
        popup.addWidget(label(PAD, 6, "Entity motion", ModColors.TEXT_PRIMARY));
        popup.addWidget(closeIconButton(W - 19, 3, 14, 14, click -> {
            EntityMotionEditor.close(state);
            refresh.run();
        }));
        TextFieldWidget yawField = addRow(popup, state, player, refresh, motion, 24, "Facing", true);
        TextFieldWidget spinField = addRow(popup, state, player, refresh, motion, 50, "Spin", false);
        parent.addWidget(popup);
        restoreFieldFocus(state, yawField, spinField);
    }

    private static TextFieldWidget addRow(WidgetGroup parent, TabletUiState state, Player player, Runnable refresh, EntityMotionValues motion, int y, String title, boolean yaw) {
        int value = yaw ? motion.yaw() : motion.spin();
        int max = yaw ? 359 : CanvasImageLayer.MAX_ENTITY_SPIN_SPEED;
        String draft = yaw ? state.entityMotionYawDraft : state.entityMotionSpinDraft;
        if (draft == null || draft.isBlank()) {
            draft = Integer.toString(value);
            if (yaw) {
                state.entityMotionYawDraft = draft;
            } else {
                state.entityMotionSpinDraft = draft;
            }
        }
        parent.addWidget(label(PAD, y + 4, title, ModColors.TEXT_SECONDARY));
        parent.addWidget(new EntityMotionSliderWidget(
                56,
                y + 1,
                80,
                16,
                0,
                max,
                value,
                next -> {
                    EntityMotionTargets.setDraft(state, yaw, next);
                    EntityMotionTargets.applyMotion(player, state, false);
                    QuestsAndStuffMod.debugLog("[QnS:UI] entity motion preview scope={} image={} field={} value={}", state.entityMotionEditorScope, state.entityMotionEditorImageId, yaw ? "yaw" : "spin", next);
                    refresh.run();
                },
                () -> {
                    EntityMotionTargets.applyMotion(player, state, true);
                    QuestsAndStuffMod.debugLog("[QnS:UI] entity motion commit scope={} image={} yaw={} spin={}", state.entityMotionEditorScope, state.entityMotionEditorImageId, state.entityMotionYawDraft, state.entityMotionSpinDraft);
                    refresh.run();
                },
                () -> yaw ? state.entityMotionYawSliderDragging : state.entityMotionSpinSliderDragging,
                dragging -> {
                    if (yaw) {
                        state.entityMotionYawSliderDragging = dragging;
                    } else {
                        state.entityMotionSpinSliderDragging = dragging;
                    }
                }
        ));
        TextFieldWidget field = numberField(player, state, refresh, W - PAD - FIELD_W, y + 2, FIELD_W, FIELD_H, max, yaw);
        parent.addWidget(field);
        return field;
    }

    private static TextFieldWidget numberField(Player player, TabletUiState state, Runnable refresh, int x, int y, int w, int h, int max, boolean yaw) {
        String fieldKey = yaw ? "yaw" : "spin";
        String draft = yaw ? state.entityMotionYawDraft : state.entityMotionSpinDraft;
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
                    if (yaw) {
                        state.entityMotionYawDraft = sanitizeNumber(value);
                    } else {
                        state.entityMotionSpinDraft = sanitizeNumber(value);
                    }
                },
                () -> {
                    state.entityMotionFocusedField = "";
                    commitNumberField(player, state, yaw);
                    refresh.run();
                },
                () -> {
                    state.entityMotionFocusedField = "";
                    EntityMotionTargets.resetDrafts(state, EntityMotionTargets.currentMotionValues(state));
                    refresh.run();
                },
                () -> {
                    if (state.entityMotionEditorOpen) {
                        state.entityMotionFocusedField = "";
                        commitNumberField(player, state, yaw);
                        refresh.run();
                    }
                },
                focused -> {
                    if (focused) {
                        state.entityMotionFocusedField = fieldKey;
                    }
                }
        );
        field.setCurrentString(draft);
        StyledTextFields.applyStandardStyle(field, ModColors.SURFACE_BASE, ModColors.BORDER_BASE);
        field.setHoverTooltips(new Component[]{Component.literal(yaw ? "Facing degrees" : "Spin speed")});
        return field;
    }

    private static void restoreFieldFocus(TabletUiState state, TextFieldWidget yawField, TextFieldWidget spinField) {
        String focused = state.entityMotionFocusedField == null ? "" : state.entityMotionFocusedField;
        if ("yaw".equals(focused)) {
            yawField.setFocus(true);
        } else if ("spin".equals(focused)) {
            spinField.setFocus(true);
        }
    }

    private static void commitNumberField(Player player, TabletUiState state, boolean yaw) {
        EntityMotionValues motion = EntityMotionTargets.currentMotionValues(state);
        if (motion == null) {
            EntityMotionEditor.close(state);
            return;
        }
        int value = EntityMotionTargets.parseDraft(yaw ? state.entityMotionYawDraft : state.entityMotionSpinDraft, yaw ? motion.yaw() : motion.spin(), yaw ? 359 : CanvasImageLayer.MAX_ENTITY_SPIN_SPEED);
        EntityMotionTargets.setDraft(state, yaw, value);
        EntityMotionTargets.applyMotion(player, state, true);
        QuestsAndStuffMod.debugLog("[QnS:UI] entity motion number commit scope={} image={} field={} value={}", state.entityMotionEditorScope, state.entityMotionEditorImageId, yaw ? "yaw" : "spin", value);
    }

    private static String sanitizeNumber(String value) {
        return value == null ? "" : value.trim();
    }
}
