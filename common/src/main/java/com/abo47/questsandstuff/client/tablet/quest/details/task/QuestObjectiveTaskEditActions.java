package com.abo47.questsandstuff.client.tablet.quest.details.task;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientManager;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

final class QuestObjectiveTaskEditActions {
    private QuestObjectiveTaskEditActions() {
    }

    static void beginTaskAdd(Player player, TabletUiState state, String questId, CompoundTag quest, String typePath) {
        QuestDetailsTypeChoice choice = QuestObjectiveTypeCatalog.taskChoice(typePath);
        String type = choice == null ? TaskJsonFactory.MOD + typePath : choice.fullType();
        String id = TaskJsonFactory.nextId(quest.getCompound("tasks"), "task_" + typePath);
        beginTask(player, state, questId, id, type, typePath, choice, true);
    }

    static void beginTaskChange(Player player, TabletUiState state, String questId, String id, String typePath) {
        QuestDetailsTypeChoice choice = QuestObjectiveTypeCatalog.taskChoice(typePath);
        String type = choice == null ? TaskJsonFactory.MOD + typePath : choice.fullType();
        beginTask(player, state, questId, id, type, typePath, choice, false);
    }

    private static void beginTask(Player player, TabletUiState state, String questId, String id, String type, String typePath, QuestDetailsTypeChoice choice, boolean add) {
        QuestObjectiveEditFlow flow = choice == null ? QuestObjectiveEditFlow.DIRECT_JSON : choice.editFlow();
        if (flow == QuestObjectiveEditFlow.ITEM_SOURCE_PICKER) {
            QuestDetailsTransientManager.openItemSourcePicker(state, ModalTargets.taskItem(questId, id, type));
            return;
        }
        if (flow == QuestObjectiveEditFlow.BIOME_PICKER) {
            QuestDetailsWindow.openBiomePicker(state, ModalTargets.taskBiome(questId, id, type));
            return;
        }
        if (flow == QuestObjectiveEditFlow.ADVANCEMENT_PICKER) {
            QuestDetailsWindow.openAdvancementPicker(state, ModalTargets.taskAdvancement(questId, id, type));
            return;
        }
        if (flow == QuestObjectiveEditFlow.RECIPE_PICKER) {
            QuestDetailsWindow.openRecipePicker(state, ModalTargets.taskRecipe(questId, id, type));
            return;
        }
        if (flow == QuestObjectiveEditFlow.STRUCTURE_PICKER) {
            QuestDetailsWindow.openStructurePicker(state, ModalTargets.taskStructure(questId, id, type));
            return;
        }
        if (flow == QuestObjectiveEditFlow.BLOCK_PICKER) {
            QuestDetailsWindow.openBlockPicker(state, ModalTargets.taskBlock(questId, id, type));
            return;
        }
        if (flow == QuestObjectiveEditFlow.STAT_PICKER) {
            QuestDetailsWindow.openStatPicker(state, ModalTargets.taskStat(questId, id, type));
            return;
        }
        if (flow == QuestObjectiveEditFlow.DIMENSION_PICKER) {
            QuestDetailsWindow.openDimensionPicker(state, ModalTargets.taskDimension(questId, id, type));
            return;
        }
        if (flow == QuestObjectiveEditFlow.ENTITY_ICON_PICKER) {
            QuestDetailsWindow.openIconPicker(state, ModalTargets.taskEntity(questId, id, type));
            return;
        }
        if (flow == QuestObjectiveEditFlow.XP_PICKER) {
            QuestDetailsTransientManager.openXpPicker(state, questId, id, true);
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details xp requirement picker open quest={} task={} add={}", questId, id, add);
            return;
        }
        if (flow == QuestObjectiveEditFlow.SIMPLE_ICON_PICKER) {
            QuestDetailsWindow.openIconPicker(state, ModalTargets.taskSimpleIcon(questId, id, type));
            return;
        }
        EditorQuestCommandClient.putQuestTaskJson(player, questId, defaultTaskJson(id, typePath, choice).toString());
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details {} task quest={} task={} type={}", add ? "add" : "change", questId, id, typePath);
    }

    private static JsonObject defaultTaskJson(String id, String typePath, QuestDetailsTypeChoice choice) {
        return choice == null ? TaskJsonFactory.defaultTask(id, typePath) : choice.defaultJson(id);
    }
}
