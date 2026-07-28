package com.abo47.questsandstuff.quest.runtime;

import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.persistence.quest.QuestProgressSavedData;
import com.abo47.questsandstuff.quest.sync.SyncService;

record RuntimeContext(
    QuestDefinitionStore definitionStore,
    QuestProgressSavedData progressData,
    SyncService syncService,
    RuntimeEngine engine
) {
}
