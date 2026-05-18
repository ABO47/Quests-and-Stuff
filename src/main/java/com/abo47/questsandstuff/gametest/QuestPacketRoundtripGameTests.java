package com.abo47.questsandstuff.gametest;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.network.editor.C2SEditorCommandPacket;
import com.abo47.questsandstuff.network.sync.S2CDeltaSyncPacket;
import com.abo47.questsandstuff.network.sync.S2CDescriptionSyncPacket;
import com.abo47.questsandstuff.network.sync.S2CDisplayCacheSyncPacket;
import com.abo47.questsandstuff.network.sync.S2CEditorMutationPacket;
import com.abo47.questsandstuff.network.sync.S2CFullSyncPacket;
import com.abo47.questsandstuff.network.sync.S2CPinnedSyncPacket;
import com.abo47.questsandstuff.network.sync.S2CQuestEventPacket;
import com.abo47.questsandstuff.quest.editor.command.EditorCommand;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import io.netty.buffer.Unpooled;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(QuestsAndStuffMod.MODID)
public final class QuestPacketRoundtripGameTests {
    private QuestPacketRoundtripGameTests() {
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void editorCommandsEncodeDecodeRoundtrip(GameTestHelper helper) {
        for (EditorCommandType type : EditorCommandType.values()) {
            CompoundTag payload = commandPayload(type);
            EditorCommand decoded = roundtripEditorCommand(new EditorCommand(type, payload));
            if (decoded.type() != type) {
                throw new GameTestAssertException("EditorCommand type mismatch for " + type);
            }
            assertTagEquals(payload, decoded.payload(), "EditorCommand payload mismatch for " + type);

            C2SEditorCommandPacket packet = new C2SEditorCommandPacket(new EditorCommand(type, payload));
            C2SEditorCommandPacket decodedPacket = roundtrip(packet);
            if (decodedPacket.command().type() != type) {
                throw new GameTestAssertException("C2SEditorCommandPacket type mismatch for " + type);
            }
            assertTagEquals(payload, decodedPacket.command().payload(), "C2SEditorCommandPacket payload mismatch for " + type);
        }

        if (EditorCommandType.fromWireName("not_real") != EditorCommandType.UNKNOWN) {
            throw new GameTestAssertException("Unknown editor command wire name should decode to UNKNOWN");
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void syncPacketsEncodeDecodeRoundtrip(GameTestHelper helper) {
        CompoundTag payload = samplePayload();

        S2CFullSyncPacket full = roundtrip(new S2CFullSyncPacket(11L, 1, 3, payload));
        assertChunkPacket(full.sequence(), full.chunkIndex(), full.chunkCount(), full.payload(), 11L, 1, 3, payload, "full");

        S2CDeltaSyncPacket delta = roundtrip(new S2CDeltaSyncPacket(12L, 2, 4, payload));
        assertChunkPacket(delta.sequence(), delta.chunkIndex(), delta.chunkCount(), delta.payload(), 12L, 2, 4, payload, "delta");

        S2CDescriptionSyncPacket descriptions = roundtrip(new S2CDescriptionSyncPacket(13L, 0, 2, payload));
        assertChunkPacket(descriptions.sequence(), descriptions.chunkIndex(), descriptions.chunkCount(), descriptions.payload(), 13L, 0, 2, payload, "description");

        S2CDisplayCacheSyncPacket displayCache = roundtrip(new S2CDisplayCacheSyncPacket(14L, payload));
        if (displayCache.sequence() != 14L) {
            throw new GameTestAssertException("Display cache sequence mismatch");
        }
        assertTagEquals(payload, displayCache.payload(), "Display cache payload mismatch");

        S2CEditorMutationPacket mutation = roundtrip(new S2CEditorMutationPacket(15L, "update", "quest/a", payload));
        if (mutation.sequence() != 15L || !"update".equals(mutation.action()) || !"quest/a".equals(mutation.questId())) {
            throw new GameTestAssertException("Editor mutation packet metadata mismatch");
        }
        assertTagEquals(payload, mutation.questTag(), "Editor mutation payload mismatch");

        S2CPinnedSyncPacket pinned = roundtrip(new S2CPinnedSyncPacket(16L, List.of("quest/a", "quest/b")));
        if (pinned.sequence() != 16L || !pinned.pinned().equals(List.of("quest/a", "quest/b"))) {
            throw new GameTestAssertException("Pinned packet roundtrip mismatch");
        }

        S2CQuestEventPacket event = roundtrip(new S2CQuestEventPacket(17L, "quest_unlocked", "quest/a", "reward/x"));
        if (event.sequence() != 17L || !"quest_unlocked".equals(event.eventType()) || !"quest/a".equals(event.questId()) || !"reward/x".equals(event.rewardId())) {
            throw new GameTestAssertException("Quest event packet roundtrip mismatch");
        }
        helper.succeed();
    }

    private static EditorCommand roundtripEditorCommand(EditorCommand command) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        command.encode(buf);
        return EditorCommand.decode(buf);
    }

    private static C2SEditorCommandPacket roundtrip(C2SEditorCommandPacket packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.encode(buf);
        return C2SEditorCommandPacket.decode(buf);
    }

    private static S2CFullSyncPacket roundtrip(S2CFullSyncPacket packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.encode(buf);
        return S2CFullSyncPacket.decode(buf);
    }

    private static S2CDeltaSyncPacket roundtrip(S2CDeltaSyncPacket packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.encode(buf);
        return S2CDeltaSyncPacket.decode(buf);
    }

    private static S2CDescriptionSyncPacket roundtrip(S2CDescriptionSyncPacket packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.encode(buf);
        return S2CDescriptionSyncPacket.decode(buf);
    }

    private static S2CDisplayCacheSyncPacket roundtrip(S2CDisplayCacheSyncPacket packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.encode(buf);
        return S2CDisplayCacheSyncPacket.decode(buf);
    }

    private static S2CEditorMutationPacket roundtrip(S2CEditorMutationPacket packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.encode(buf);
        return S2CEditorMutationPacket.decode(buf);
    }

    private static S2CPinnedSyncPacket roundtrip(S2CPinnedSyncPacket packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.encode(buf);
        return S2CPinnedSyncPacket.decode(buf);
    }

    private static S2CQuestEventPacket roundtrip(S2CQuestEventPacket packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.encode(buf);
        return S2CQuestEventPacket.decode(buf);
    }

    private static CompoundTag commandPayload(EditorCommandType type) {
        CompoundTag payload = samplePayload();
        payload.putString("type_name", type.name());
        payload.putString("group", "Main");
        payload.putString("quest", "quest/a");
        payload.putString(C2SEditorCommandPacket.PREREQUISITE_FIELD, "quest/b");
        payload.putInt("x", 42);
        payload.putInt("y", 84);
        payload.putFloat("scale", 1.25f);
        payload.putBoolean("grid", true);
        payload.putBoolean("hidden", false);
        return payload;
    }

    private static CompoundTag samplePayload() {
        CompoundTag payload = new CompoundTag();
        payload.putString("title", "Roundtrip");
        payload.putInt("count", 3);
        payload.putBoolean("enabled", true);

        ListTag quests = new ListTag();
        quests.add(StringTag.valueOf("quest/a"));
        quests.add(StringTag.valueOf("quest/b"));
        payload.put("quests", quests);

        CompoundTag nested = new CompoundTag();
        nested.putString("id", "nested/value");
        nested.putInt("color", 0x55AAFF);
        payload.put("nested", nested);
        return payload;
    }

    private static void assertChunkPacket(long actualSequence, int actualIndex, int actualCount, CompoundTag actualPayload, long expectedSequence, int expectedIndex, int expectedCount, CompoundTag expectedPayload, String label) {
        if (actualSequence != expectedSequence || actualIndex != expectedIndex || actualCount != expectedCount) {
            throw new GameTestAssertException(label + " chunk metadata mismatch");
        }
        assertTagEquals(expectedPayload, actualPayload, label + " chunk payload mismatch");
    }

    private static void assertTagEquals(CompoundTag expected, CompoundTag actual, String message) {
        if (!expected.equals(actual)) {
            throw new GameTestAssertException(message + ": expected " + expected + " got " + actual);
        }
    }
}
