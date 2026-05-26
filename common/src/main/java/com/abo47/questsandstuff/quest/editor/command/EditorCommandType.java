package com.abo47.questsandstuff.quest.editor.command;

import java.util.HashMap;
import java.util.Map;

public enum EditorCommandType {
    MOVE_MANY("move_many"),
    SCALE_MANY("scale_many"),
    COPY_MANY("copy_many"),
    PASTE_CLIPBOARD("paste_clipboard"),
    PREREQUISITE_ADD("prerequisite_add"),
    PREREQUISITE_REMOVE("prerequisite_remove"),
    QUEST_ICON("quest_icon"),
    QUEST_AUTO_CLAIM("quest_auto_claim"),
    QUEST_REPEATABLE("quest_repeatable"),
    QUEST_HIDDEN_MODE("quest_hidden_mode"),
    QUEST_VISUAL_HIDDEN("quest_visual_hidden"),
    QUEST_CHANGE_COMPLETION_SOUND("quest_change_completion_sound"),
    QUEST_CHANGE_COMPLETION_SOUND_MANY("quest_change_completion_sound_many"),
    QUEST_CHANGE_COMPLETION_SOUND_VOLUME("quest_change_completion_sound_volume"),
    QUEST_CHANGE_COMPLETION_SOUND_VOLUME_MANY("quest_change_completion_sound_volume_many"),
    QUEST_COMPLETION_HUD_BACKGROUND("quest_completion_hud_background"),
    QUEST_COMPLETION_HUD_BACKGROUND_MANY("quest_completion_hud_background_many"),
    QUEST_BACKGROUND("quest_background"),
    QUEST_BACKGROUND_MANY("quest_background_many"),
    DESCRIPTION_PUT("description_put"),
    CONNECTION_COLOR("connection_color"),
    CONNECTION_MODE("connection_mode"),
    CONNECTION_HIDDEN("connection_hidden"),
    TASK_PUT("task_put"),
    TASK_REMOVE("task_remove"),
    TASK_MOVE("task_move"),
    REWARD_PUT("reward_put"),
    REWARD_REMOVE("reward_remove"),
    REWARD_MOVE("reward_move"),
    CANVAS_IMAGE_PUT("canvas_image_put"),
    CANVAS_IMAGE_REMOVE("canvas_image_remove"),
    CANVAS_TEXT_PUT("canvas_text_put"),
    CANVAS_TEXT_REMOVE("canvas_text_remove"),
    CANVAS_LAYER_ORDER("canvas_layer_order"),
    UNKNOWN("");

    private static final Map<String, EditorCommandType> BY_WIRE_NAME = new HashMap<>();

    static {
        for (EditorCommandType type : values()) {
            BY_WIRE_NAME.put(type.wireName, type);
        }
    }

    private final String wireName;

    EditorCommandType(String wireName) {
        this.wireName = wireName;
    }

    public String wireName() {
        return wireName;
    }

    public static EditorCommandType fromWireName(String wireName) {
        if (wireName == null || wireName.isBlank()) {
            return UNKNOWN;
        }
        return BY_WIRE_NAME.getOrDefault(wireName, UNKNOWN);
    }
}
