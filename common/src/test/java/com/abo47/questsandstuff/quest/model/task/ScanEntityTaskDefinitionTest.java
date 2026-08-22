package com.abo47.questsandstuff.quest.model.task;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import org.junit.jupiter.api.Test;

import com.mojang.serialization.JsonOps;

import com.abo47.questsandstuff.quest.model.storage.IntegerTaskStorage;
import com.abo47.questsandstuff.quest.model.task.generic.SimpleQuestTaskDefinition;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignal;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignalType;

import com.google.gson.JsonObject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScanEntityTaskDefinitionTest {
    private static final ResourceLocation SCAN_ENTITY = new ResourceLocation("questsandstuff", "scan_entity");

    @Test
    void scanEntityStaysUnregisteredWithoutOresAndStuffInstalled() {
        org.junit.jupiter.api.Assertions.assertNull(QuestTasks.get(SCAN_ENTITY));
    }

    @Test
    void savedScanTasksFallBackToUnsupportedWithoutOresAndStuff() {
        QuestTaskDefinition parsed = QuestTaskDefinition.CODEC
                .parse(JsonOps.INSTANCE, scanJson("minecraft:cow"))
                .resultOrPartial(errors -> {})
                .orElse(null);

        assertNotNull(parsed);
        assertTrue(parsed instanceof com.abo47.questsandstuff.quest.model.task.fallback.UnsupportedQuestTaskDefinition);
    }

    @Test
    void scanDefinitionExposesBioScannedSignal() {
        SimpleQuestTaskDefinition task = scanTask("minecraft:cow");

        assertTrue(task.signals().contains(QuestSignalType.BIO_SCANNED));
    }

    @Test
    void bioScanSignalProgressesMatchingTargetUpToGoal() {
        SimpleQuestTaskDefinition task = scanTask("minecraft:cow");

        Tag progress = task.test(null, scanSignal("minecraft:cow"));
        assertEquals(1, IntegerTaskStorage.INSTANCE.readInt(progress));

        progress = task.test(progress, scanSignal("minecraft:cow"));
        assertEquals(2, IntegerTaskStorage.INSTANCE.readInt(progress));

        progress = task.test(progress, scanSignal("minecraft:cow"));
        assertEquals(2, IntegerTaskStorage.INSTANCE.readInt(progress));
    }

    @Test
    void bioScanSignalIgnoresOtherEntitiesWhenTargeted() {
        SimpleQuestTaskDefinition task = scanTask("minecraft:cow");

        Tag progress = task.test(null, scanSignal("minecraft:pig"));

        assertEquals(0, IntegerTaskStorage.INSTANCE.readInt(progress));
    }

    @Test
    void blankTargetAcceptsAnyScannedEntity() {
        SimpleQuestTaskDefinition task = scanTask("");

        Tag progress = task.test(null, scanSignal("minecraft:pig"));

        assertEquals(1, IntegerTaskStorage.INSTANCE.readInt(progress));
    }

    @Test
    void otherSignalsDoNotProgressScanTasks() {
        SimpleQuestTaskDefinition task = scanTask("minecraft:cow");
        QuestSignal kill = new QuestSignal(QuestSignalType.ENTITY_KILLED, null, "minecraft:cow", 1, BlockPos.ZERO, null);

        Tag progress = task.test(null, kill);

        assertEquals(0, IntegerTaskStorage.INSTANCE.readInt(progress));
    }

    private static SimpleQuestTaskDefinition scanTask(String target) {
        return new SimpleQuestTaskDefinition("task_scan", SCAN_ENTITY, QuestSignalType.BIO_SCANNED, 2, target, "", "");
    }

    private static QuestSignal scanSignal(String entity) {
        return new QuestSignal(QuestSignalType.BIO_SCANNED, null, entity, 1, BlockPos.ZERO, null);
    }

    private static JsonObject scanJson(String target) {
        JsonObject json = new JsonObject();
        json.addProperty("id", "task_scan");
        json.addProperty("type", "questsandstuff:scan_entity");
        json.addProperty("amount", 1);
        json.addProperty("target", target);
        return json;
    }
}
