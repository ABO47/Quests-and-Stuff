package com.abo47.questsandstuff.client.tablet.quest.details.objective;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.DisplayNameFormatter;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

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
        String lootTableLabel = TabletVocabulary.text(QuestVocabulary.TYPE_LOOT_TABLE);
        if (title.isBlank() || "Loot table".equalsIgnoreCase(title) || lootTableLabel.equalsIgnoreCase(title)) {
            title = naturalName;
        }
        if (title.isBlank()) {
            title = TabletVocabulary.text(QuestVocabulary.CHOOSE_LOOT_TABLE);
        }
        QuestObjectiveInlineFields.renderDisplayText(parent, x, y, w, title, ModColors.TEXT_PRIMARY, TextTexture.TextType.LEFT_HIDE);
    }

    private static String lootTable(JsonObject json) {
        return QuestObjectiveJsons.asString(json, "loot_table", "");
    }
}
