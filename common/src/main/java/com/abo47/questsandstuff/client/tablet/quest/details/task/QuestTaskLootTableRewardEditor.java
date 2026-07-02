package com.abo47.questsandstuff.client.tablet.quest.details.task;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.format.DisplayNameFormatter;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

final class QuestTaskLootTableRewardEditor {
    private QuestTaskLootTableRewardEditor() {
    }

    static boolean isLootTable(JsonObject json) {
        String path = TaskJsonFactory.typePath(TaskJsonFactory.asString(json, "type", ""));
        return "loot_table".equals(path) || "loot".equals(path);
    }

    static void render(WidgetGroup parent, TabletUiState state, Player player, Runnable refresh, String questId, QuestDetailsTaskEntry entry, int x, int y, int rightX) {
        renderSummary(parent, entry.json(), x, y, Math.max(18, rightX - x));
    }

    static String displayName(String lootTable) {
        return DisplayNameFormatter.lootTable(lootTable, ClientQuestStateFacade.lootTableDisplays());
    }

    private static void renderSummary(WidgetGroup parent, JsonObject json, int x, int y, int w) {
        String lootTable = lootTable(json);
        String naturalName = displayName(lootTable);
        String title = TaskJsonFactory.asString(json, "title", "");
        String lootTableLabel = TabletTranslationKeys.text(QuestTranslationKeys.TYPE_LOOT_TABLE);
        if (title.isBlank() || "Loot table".equalsIgnoreCase(title) || lootTableLabel.equalsIgnoreCase(title)) {
            title = naturalName;
        }
        if (title.isBlank()) {
            title = TabletTranslationKeys.text(QuestTranslationKeys.CHOOSE_LOOT_TABLE);
        }
        QuestTaskInlineFields.renderDisplayText(parent, x, y, w, title, TabletColors.TEXT_PRIMARY, TextTexture.TextType.LEFT_HIDE);
    }

    private static String lootTable(JsonObject json) {
        return TaskJsonFactory.asString(json, "loot_table", "");
    }
}
