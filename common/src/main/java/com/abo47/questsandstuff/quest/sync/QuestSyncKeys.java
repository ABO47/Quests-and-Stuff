package com.abo47.questsandstuff.quest.sync;

import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestSettings;

public final class QuestSyncKeys {
    public static final String SCHEMA = "schema";
    public static final String GROUPS = "groups";
    public static final String GROUP_PROPS = "group_props";
    public static final String QUESTS = "quests";
    public static final String CHANGED = "changed";
    public static final String REMOVED = "removed";
    public static final String DESCRIPTIONS = "descriptions";

    private QuestSyncKeys() {
    }

    public static final class GroupProps {
        public static final String ICON = "icon";
        public static final String BACKGROUND = "background";
        public static final String CANVAS_BACKGROUND = "canvas_background";
        public static final String TEXT_ALIGN = "text_align";
        public static final String TEXT_COLOR = "text_color";
        public static final String TEXT_STYLE = "text_style";
        public static final String TEXT_SIZE = "text_size";
        public static final String LOCK_UNTIL_UNLOCKED = "lock_until_unlocked";
        public static final String HIDE_UNTIL_UNLOCKED = "hide_until_unlocked";
        public static final String CANVAS_EXCLUSIVE_CHOICES = "canvas_exclusive_choices";
        public static final String CANVAS_IMAGES = "canvas_images";
        public static final String CANVAS_TEXTS = "canvas_texts";
        public static final String CANVAS_LAYER_ORDER = "canvas_layer_order";

        private GroupProps() {
        }
    }

    public static final class Quest {
        public static final String TITLE = "title";
        public static final String SUBTITLE = "subtitle";
        public static final String DESCRIPTION = "description";
        public static final String ICON = "icon";
        public static final String ICON_BACKGROUND = "icon_background";
        public static final String COMPLETION_SOUND = "completion_sound";
        public static final String COMPLETION_SOUND_VOLUME = "completion_sound_volume";
        public static final String COMPLETION_HUD_BACKGROUND = "completion_hud_background";
        public static final String VISUAL_HIDDEN = "visual_hidden";
        public static final String QUEST_BACKGROUND = "quest_background";
        public static final String QUEST_BACKGROUND_GRAYSCALE = "quest_background_grayscale";
        public static final String COMPLETED = "completed";
        public static final String UNLOCKED = "unlocked";
        public static final String CLAIMED = "claimed";
        public static final String PROGRESS = "progress";
        public static final String REPEATABLE = "repeatable";
        public static final String HIDDEN_MODE = "hidden_mode";
        public static final String SHOW_PREREQUISITE_ARROW = QuestSettings.SHOW_PREREQUISITE_ARROW_FIELD;
        public static final String TASKS = "tasks";
        public static final String TASKS_ORDER = "tasks_order";
        public static final String REWARDS = "rewards";
        public static final String REWARDS_ORDER = "rewards_order";
        public static final String PREREQUISITES = QuestDefinition.PREREQUISITES_FIELD;
        public static final String CONNECTION_COLORS = "connection_colors";
        public static final String CONNECTION_MODES = "connection_modes";
        public static final String HIDDEN_CONNECTIONS = "hidden_connections";
        public static final String CONNECTION_TEXTURES = "connection_textures";
        public static final String CONNECTION_TEXTURE_SPACINGS = "connection_texture_spacings";
        public static final String GROUPS = QuestSyncKeys.GROUPS;

        private Quest() {
        }
    }

    public static final class Objective {
        public static final String TYPE = "type";
        public static final String JSON = "json";
        public static final String PROGRESS = Quest.PROGRESS;
        public static final String COMPLETE = "complete";
        public static final String COUNT = "count";
        public static final String SELECTABLE = "selectable";
        public static final String MASS_CLAIMABLE = "mass_claimable";

        private Objective() {
        }
    }

    public static final class ChapterView {
        public static final String VISIBLE = "visible";
        public static final String X = "x";
        public static final String Y = "y";
        public static final String SCALE = "scale";

        private ChapterView() {
        }
    }

    public static final class DisplayCache {
        public static final String ADVANCEMENTS = "advancements";
        public static final String LOOT_TABLES = "loot_tables";
        public static final String BIOMES = "biomes";

        private DisplayCache() {
        }
    }

    public static final class EditorAction {
        public static final String ADD = "add";
        public static final String UPDATE = "update";
        public static final String REMOVE = "remove";
        public static final String PASTE_SELECT = "paste_select";

        private EditorAction() {
        }
    }

    public static final class EditorSelection {
        public static final String GROUP = "group";
        public static final String QUESTS = QuestSyncKeys.QUESTS;
        public static final String ALLOCATED_IDS = "allocated_ids";
        public static final String IMAGES = "images";
        public static final String TEXTS = "texts";
        public static final String ECS = "ecs";

        private EditorSelection() {
        }
    }
}
