package com.abo47.questsandstuff.client.tablet.quest.details.objective;

import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;

record QuestDetailsTypeChoice(String labelKey, String type, String icon) {
    String label() {
        return QuestVocabulary.text(labelKey);
    }
}
