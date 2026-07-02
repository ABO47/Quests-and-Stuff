package com.abo47.questsandstuff.client.tablet.quest.details.task;

import com.abo47.questsandstuff.client.tablet.controls.CardDragGhosts;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

final class QuestTaskGhostCards {
    private QuestTaskGhostCards() {
    }

    static void render(WidgetGroup parent, QuestDetailsTaskEntry entry, boolean task, int x, int y, int w) {
        if (entry == null) {
            return;
        }
        float progress = task ? entry.tag().getFloat("progress") : 0.0f;
        String icon = task ? QuestTaskDisplayText.taskIcon(entry.json()) : QuestTaskDisplayText.rewardIcon(entry.json());
        CardDragGhosts.renderTask(parent, x, y, w, QuestDetailsTasksPanel.CARD_H, icon, QuestTaskDisplayText.displayName(entry.json(), entry.type()), amount(entry, task), progress);
    }

    private static String amount(QuestDetailsTaskEntry entry, boolean task) {
        int amount = QuestTaskDisplayText.amount(entry.json());
        if (!QuestTaskDisplayText.usesAmountField(entry.json(), task)) {
            return "";
        }
        return task ? Math.max(0, entry.tag().getInt("count")) + "/" + amount : Integer.toString(amount);
    }
}
