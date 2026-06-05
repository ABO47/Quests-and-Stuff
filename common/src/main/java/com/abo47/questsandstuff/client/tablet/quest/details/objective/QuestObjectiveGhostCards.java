package com.abo47.questsandstuff.client.tablet.quest.details.objective;

import com.abo47.questsandstuff.client.tablet.controls.CardDragGhosts;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

final class QuestObjectiveGhostCards {
    private QuestObjectiveGhostCards() {
    }

    static void render(WidgetGroup parent, QuestDetailsObjectiveEntry entry, boolean task, int x, int y, int w) {
        if (entry == null) {
            return;
        }
        float progress = task ? entry.tag().getFloat("progress") : 0.0f;
        String icon = task ? QuestObjectiveDisplayText.taskIcon(entry.json()) : QuestObjectiveDisplayText.rewardIcon(entry.json());
        CardDragGhosts.renderObjective(parent, x, y, w, QuestDetailsObjectivesPanel.CARD_H, icon, QuestObjectiveDisplayText.displayName(entry.json(), entry.type()), amount(entry, task), progress);
    }

    private static String amount(QuestDetailsObjectiveEntry entry, boolean task) {
        int amount = QuestObjectiveDisplayText.amount(entry.json());
        if (!QuestObjectiveDisplayText.usesAmountField(entry.json(), task)) {
            return "";
        }
        return task ? Math.max(0, entry.tag().getInt("count")) + "/" + amount : Integer.toString(amount);
    }
}
