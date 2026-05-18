package com.abo47.questsandstuff.quest.runtime;

import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.storage.BooleanTaskStorage;
import com.abo47.questsandstuff.quest.model.storage.IntegerTaskStorage;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.progress.CompositeQuestTaskDefinition;
import com.abo47.questsandstuff.quest.runtime.progress.QuestProgressState;
import net.minecraft.nbt.Tag;

final class QuestCompletionRules {
    private QuestCompletionRules() {
    }

    static boolean isTaskComplete(QuestDefinition definition, QuestProgressState progress, String taskId, QuestTaskDefinition task) {
        if (task instanceof CompositeQuestTaskDefinition composite) {
            return isCompositeComplete(definition, progress, taskId, composite);
        }
        return task.isComplete(progress.getTaskProgress(taskId, task));
    }

    static Tag completeProgress(QuestTaskDefinition task) {
        if (task.storage() == BooleanTaskStorage.INSTANCE) {
            return BooleanTaskStorage.INSTANCE.set(true);
        }
        if (task.storage() == IntegerTaskStorage.INSTANCE) {
            return IntegerTaskStorage.INSTANCE.set(task.safeGoal());
        }
        return task.defaultProgress();
    }

    private static boolean isCompositeComplete(QuestDefinition definition, QuestProgressState progress, String taskId, CompositeQuestTaskDefinition composite) {
        if (composite.safeChildren().isEmpty()) {
            return composite.isComplete(progress.getTaskProgress(taskId, composite));
        }
        int completed = 0;
        for (String childId : composite.safeChildren()) {
            QuestTaskDefinition childTask = definition.tasks().get(childId);
            if (childTask != null && childTask.isComplete(progress.getTaskProgress(childId, childTask))) {
                completed++;
            }
        }
        return completed >= composite.safeGoal();
    }
}
