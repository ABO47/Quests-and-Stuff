package com.abo47.questsandstuff.client.tablet.details.objective;

import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;

import java.util.List;

final class QuestObjectiveTypeCatalog {
    private QuestObjectiveTypeCatalog() {
    }

    static List<QuestDetailsTypeChoice> taskChoices() {
        return List.of(
                new QuestDetailsTypeChoice(QuestVocabulary.TYPE_ACQUIRE_ITEM, "item", "icon"),
                new QuestDetailsTypeChoice(QuestVocabulary.TYPE_KILL_ENTITY, "kill_entity", "kill_entity"),
                new QuestDetailsTypeChoice(QuestVocabulary.TYPE_MANUAL_CHECK, "check", "manual_check"),
                new QuestDetailsTypeChoice(QuestVocabulary.TYPE_VISIT_BIOME, "biome", "biome"),
                new QuestDetailsTypeChoice(QuestVocabulary.TYPE_VISIT_LOCATION, "location", "orbit")
        );
    }

    static List<QuestDetailsTypeChoice> rewardChoices() {
        return List.of(
                new QuestDetailsTypeChoice(QuestVocabulary.TYPE_GIVE_ITEM, "item", "icon"),
                new QuestDetailsTypeChoice(QuestVocabulary.TYPE_XP, "xp", "claim_all"),
                new QuestDetailsTypeChoice(QuestVocabulary.TYPE_COMMAND, "command", "open"),
                new QuestDetailsTypeChoice(QuestVocabulary.TYPE_LOOT_TABLE, "loot_table", "icon")
        );
    }
}
