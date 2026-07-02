package com.abo47.questsandstuff.gametest;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.network.ModPacketContext;
import com.abo47.questsandstuff.network.quest.editor.C2SEditorAddQuestPacket;
import com.abo47.questsandstuff.network.quest.editor.C2SEditorCommandPacket;
import com.abo47.questsandstuff.network.quest.runtime.C2SClaimSelectableRewardPacket;
import com.abo47.questsandstuff.network.quest.runtime.C2STogglePinPacket;
import com.abo47.questsandstuff.network.quest.sync.S2CDeltaSyncPacket;
import com.abo47.questsandstuff.network.quest.sync.S2CDescriptionSyncPacket;
import com.abo47.questsandstuff.network.quest.sync.S2CDisplayCacheSyncPacket;
import com.abo47.questsandstuff.network.quest.sync.S2CEditorMutationPacket;
import com.abo47.questsandstuff.network.quest.sync.S2CFullSyncPacket;
import com.abo47.questsandstuff.network.quest.sync.S2CPinnedSyncPacket;
import com.abo47.questsandstuff.network.quest.sync.S2CQuestEventPacket;
import com.abo47.questsandstuff.network.quest.sync.SyncPacketPayloadLimits;
import com.abo47.questsandstuff.quest.editor.command.EditorCommand;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloadLimits;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

        C2STogglePinPacket togglePin = roundtrip(new C2STogglePinPacket("quest/a"));
        if (!"quest/a".equals(togglePin.questId())) {
            throw new GameTestAssertException("Toggle pin packet roundtrip mismatch");
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void syncPacketDecodeRejectsOversizedPayload(GameTestHelper helper) {
        FriendlyByteBuf oversized = new FriendlyByteBuf(Unpooled.buffer());
        oversized.writeLong(19L);
        oversized.writeVarInt(0);
        oversized.writeVarInt(1);
        CompoundTag payload = new CompoundTag();
        payload.putString("blob", "x".repeat((int) SyncPacketPayloadLimits.MAX_SYNC_NBT_BYTES + 1));
        oversized.writeNbt(payload);

        boolean rejected = false;
        try {
            S2CFullSyncPacket.decode(oversized);
        } catch (RuntimeException expected) {
            rejected = true;
        }
        if (!rejected) {
            throw new GameTestAssertException("Oversized sync payload should fail during decode");
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void syncPacketDecodeRejectsInvalidChunkMetadata(GameTestHelper helper) {
        FriendlyByteBuf invalidIndex = new FriendlyByteBuf(Unpooled.buffer());
        invalidIndex.writeLong(20L);
        invalidIndex.writeVarInt(2);
        invalidIndex.writeVarInt(2);

        try {
            S2CFullSyncPacket.decode(invalidIndex);
            throw new GameTestAssertException("Invalid full sync chunk index should fail during decode");
        } catch (IllegalArgumentException expected) {
        }

        FriendlyByteBuf tooManyChunks = new FriendlyByteBuf(Unpooled.buffer());
        tooManyChunks.writeLong(21L);
        tooManyChunks.writeVarInt(0);
        tooManyChunks.writeVarInt(SyncPacketPayloadLimits.MAX_SYNC_CHUNKS + 1);

        try {
            S2CDeltaSyncPacket.decode(tooManyChunks);
            throw new GameTestAssertException("Oversized delta sync chunk count should fail during decode");
        } catch (IllegalArgumentException expected) {
        }

        FriendlyByteBuf zeroChunks = new FriendlyByteBuf(Unpooled.buffer());
        zeroChunks.writeLong(22L);
        zeroChunks.writeVarInt(0);
        zeroChunks.writeVarInt(0);

        try {
            S2CDescriptionSyncPacket.decode(zeroChunks);
            throw new GameTestAssertException("Zero description sync chunk count should fail during decode");
        } catch (IllegalArgumentException expected) {
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void pinnedSyncPacketRejectsOversizedList(GameTestHelper helper) {
        List<String> pinned = new ArrayList<>();
        for (int i = 0; i <= SyncPacketPayloadLimits.MAX_PINNED_QUESTS; i++) {
            pinned.add("quest/" + i);
        }
        try {
            new S2CPinnedSyncPacket(23L, pinned).encode(new FriendlyByteBuf(Unpooled.buffer()));
            throw new GameTestAssertException("Oversized pinned sync packet should fail during encode");
        } catch (IllegalArgumentException expected) {
        }

        FriendlyByteBuf oversized = new FriendlyByteBuf(Unpooled.buffer());
        oversized.writeLong(24L);
        oversized.writeVarInt(SyncPacketPayloadLimits.MAX_PINNED_QUESTS + 1);
        try {
            S2CPinnedSyncPacket.decode(oversized);
            throw new GameTestAssertException("Oversized pinned sync packet should fail during decode");
        } catch (IllegalArgumentException expected) {
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void selectableRewardPacketRejectsOversizedSelection(GameTestHelper helper) {
        List<String> choices = new ArrayList<>();
        for (int i = 0; i < 65; i++) {
            choices.add("choice_" + i);
        }
        try {
            new C2SClaimSelectableRewardPacket("quest/a", "selector", choices).encode(new FriendlyByteBuf(Unpooled.buffer()));
            throw new GameTestAssertException("Oversized selectable reward packet should fail during encode");
        } catch (IllegalArgumentException expected) {
        }

        FriendlyByteBuf oversized = new FriendlyByteBuf(Unpooled.buffer());
        oversized.writeUtf("quest/a");
        oversized.writeUtf("selector");
        oversized.writeVarInt(65);
        try {
            C2SClaimSelectableRewardPacket.decode(oversized);
            throw new GameTestAssertException("Oversized selectable reward packet should fail during decode");
        } catch (IllegalArgumentException expected) {
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void editorCommandDecodeRejectsOversizedPayload(GameTestHelper helper) {
        FriendlyByteBuf oversized = new FriendlyByteBuf(Unpooled.buffer());
        oversized.writeUtf(EditorCommandType.DESCRIPTION_PUT.wireName());
        CompoundTag payload = new CompoundTag();
        ListTag lines = new ListTag();
        for (int i = 0; i <= EditorCommandPayloads.MAX_DESCRIPTION_LINES; i++) {
            lines.add(StringTag.valueOf("line_" + i));
        }
        payload.put("description", lines);
        oversized.writeNbt(payload);

        try {
            EditorCommand.decode(oversized);
            throw new GameTestAssertException("Oversized editor command payload should fail during decode");
        } catch (IllegalArgumentException expected) {
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void editorPacketsDoNotEnqueueForNonEditorPlayers(GameTestHelper helper) {
        ServerPlayer player = detachedPlayer(helper);
        ModPacketContext context = rejectingContext(player);

        new C2SEditorAddQuestPacket("Main", "quest/security", 0, 0, "Blocked").handle(context);

        CompoundTag rewardPayload = new CompoundTag();
        rewardPayload.putString("quest", "quest/security");
        rewardPayload.putString("json", "{\"id\":\"cmd\",\"type\":\"command\",\"command\":\"say blocked\"}");
        new C2SEditorCommandPacket(new EditorCommand(EditorCommandType.REWARD_PUT, rewardPayload)).handle(context);

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

    private static C2STogglePinPacket roundtrip(C2STogglePinPacket packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.encode(buf);
        return C2STogglePinPacket.decode(buf);
    }

    private static ServerPlayer detachedPlayer(GameTestHelper helper) {
        return new ServerPlayer(
                helper.getLevel().getServer(),
                helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "qas_packet_test")
        );
    }

    private static ModPacketContext rejectingContext(ServerPlayer player) {
        return new ModPacketContext() {
            @Override
            public ServerPlayer sender() {
                return player;
            }

            @Override
            public void enqueueWork(Runnable work) {
                throw new GameTestAssertException("Non-editor packet should not enqueue server work");
            }
        };
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
