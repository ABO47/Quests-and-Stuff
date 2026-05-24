package com.abo47.questsandstuff.client.tablet.modal;

public final class ModalTargetParser {
    private ModalTargetParser() {
    }

    public static Target parse(String raw) {
        String[] parts = (raw == null ? "" : raw).split("\\|", -1);
        return new Target(parts);
    }

    public record Target(String[] parts) {
        public Target {
            parts = parts == null ? new String[0] : parts.clone();
        }

        @Override
        public String[] parts() {
            return parts.clone();
        }

        public boolean is(String kind) {
            return kind != null && kind.equals(part(0));
        }

        public boolean isTaskItem() {
            return is(ModalTargets.TASK_ITEM);
        }

        public boolean isTaskInventoryItem() {
            return is(ModalTargets.TASK_INVENTORY_ITEM);
        }

        public boolean isTaskBiome() {
            return is(ModalTargets.TASK_BIOME);
        }

        public boolean isTaskAdvancement() {
            return is(ModalTargets.TASK_ADVANCEMENT);
        }

        public boolean isTaskRecipe() {
            return is(ModalTargets.TASK_RECIPE);
        }

        public boolean isTaskStructure() {
            return is(ModalTargets.TASK_STRUCTURE);
        }

        public boolean isTaskBlock() {
            return is(ModalTargets.TASK_BLOCK);
        }

        public boolean isTaskStat() {
            return is(ModalTargets.TASK_STAT);
        }

        public boolean isTaskDimension() {
            return is(ModalTargets.TASK_DIMENSION);
        }

        public boolean isTaskEntity() {
            return is(ModalTargets.TASK_ENTITY);
        }

        public boolean isTaskSimpleIcon() {
            return is(ModalTargets.TASK_SIMPLE_ICON);
        }

        public boolean isTaskIcon() {
            return is(ModalTargets.TASK_ICON);
        }

        public boolean isRewardItem() {
            return is(ModalTargets.REWARD_ITEM);
        }

        public boolean isRewardInventoryItem() {
            return is(ModalTargets.REWARD_INVENTORY_ITEM);
        }

        public boolean isRewardLootTable() {
            return is(ModalTargets.REWARD_LOOT_TABLE);
        }

        public boolean isRewardIcon() {
            return is(ModalTargets.REWARD_ICON);
        }

        public boolean isRewardCommandEditorIcon() {
            return is(ModalTargets.REWARD_COMMAND_EDITOR_ICON);
        }

        public boolean isObjectiveTask() {
            return is(ModalTargets.OBJECTIVE_TASK);
        }

        public boolean isObjectiveReward() {
            return is(ModalTargets.OBJECTIVE_REWARD);
        }

        public boolean isQuestIcon() {
            return is(ModalTargets.QUEST_ICON);
        }

        public boolean isChapterIcon() {
            return is(ModalTargets.CHAPTER_ICON);
        }

        public boolean isQuestDetailsImage() {
            return is(ModalTargets.QUEST_DETAILS);
        }

        public boolean isDescBackground() {
            return is(ModalTargets.DESC_BACKGROUND);
        }

        public boolean isDescImage() {
            return is(ModalTargets.DESC_IMAGE);
        }

        public boolean isDescImageNew() {
            return is(ModalTargets.DESC_IMAGE_NEW);
        }

        public boolean isDescEntity() {
            return is(ModalTargets.DESC_ENTITY);
        }

        public boolean isDescEntityNew() {
            return is(ModalTargets.DESC_ENTITY_NEW);
        }

        public boolean isQuestDescText() {
            return is(ModalTargets.QUEST_DESC_TEXT);
        }

        public boolean isCanvasImage() {
            return is(ModalTargets.CANVAS_IMAGE);
        }

        public boolean isCanvasEntityNew() {
            return is(ModalTargets.CANVAS_ENTITY_NEW);
        }

        public boolean isCanvasEntityChange() {
            return is(ModalTargets.CANVAS_ENTITY_CHANGE);
        }

        public boolean isCanvasText() {
            return is(ModalTargets.CANVAS_TEXT);
        }

        public boolean isConnection() {
            return is(ModalTargets.CONNECTION);
        }

        public boolean isConnectionSelection() {
            return is(ModalTargets.CONNECTION_SELECTION);
        }

        public boolean isEntityIconPickerTarget() {
            return isDescEntityNew() || isDescEntity() || isTaskEntity();
        }

        public boolean supportsEntityIconSelection() {
            return isTaskIcon() || isRewardIcon() || isRewardCommandEditorIcon();
        }

        public boolean hasAtLeast(int count) {
            return parts != null && parts.length >= count;
        }

        public String kind() {
            return part(0);
        }

        public String questId() {
            return part(1);
        }

        public String entryId() {
            return part(2);
        }

        public String type() {
            return part(3);
        }

        public String part(int index) {
            if (parts == null || index < 0 || index >= parts.length) {
                return "";
            }
            return parts[index] == null ? "" : parts[index].trim();
        }
    }
}
