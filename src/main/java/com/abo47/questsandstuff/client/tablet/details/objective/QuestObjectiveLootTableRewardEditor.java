package com.abo47.questsandstuff.client.tablet.details.objective;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.DisplayNameFormatter;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.label;

final class QuestObjectiveLootTableRewardEditor {
    private QuestObjectiveLootTableRewardEditor() {
    }

    static boolean isLootTable(JsonObject json) {
        String path = QuestObjectiveJsons.typePath(QuestObjectiveJsons.asString(json, "type", ""));
        return "loot_table".equals(path) || "loot".equals(path);
    }

    static void render(WidgetGroup parent, TabletUiState state, Player player, Runnable refresh, String questId, QuestDetailsObjectiveEntry entry, int x, int y, int rightX) {
        renderSummary(parent, entry.json(), x, y, Math.max(18, rightX - x));
    }

    static String displayName(String lootTable) {
        return DisplayNameFormatter.lootTable(lootTable, ClientQuestCache.lootTableDisplays());
    }

    private static void renderSummary(WidgetGroup parent, JsonObject json, int x, int y, int w) {
        String lootTable = lootTable(json);
        String naturalName = displayName(lootTable);
        String title = QuestObjectiveJsons.asString(json, "title", "");
        String lootTableLabel = QuestVocabulary.text(QuestVocabulary.TYPE_LOOT_TABLE);
        if (title.isBlank() || "Loot table".equalsIgnoreCase(title) || lootTableLabel.equalsIgnoreCase(title)) {
            title = naturalName;
        }
        if (title.isBlank()) {
            title = QuestVocabulary.text(QuestVocabulary.CHOOSE_LOOT_TABLE);
        }
        parent.addWidget(label(x, y + 3, QuestObjectiveInlineFields.fitText(title, w), ModColors.TEXT_PRIMARY));
    }

    private static String lootTable(JsonObject json) {
        return QuestObjectiveJsons.asString(json, "loot_table", "");
    }
}
