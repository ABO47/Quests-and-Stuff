package com.abo47.questsandstuff.quest.editor.command;

import com.abo47.questsandstuff.network.quest.editor.C2SEditorCommandPacket;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditorCommandPayloadsTest {
    @Test
    void bulkFactoriesUseKeysReadByPacketHelpers() {
        Map<String, int[]> moves = new LinkedHashMap<>();
        moves.put(" quest/a ", new int[]{12, 34});
        moves.put("quest/b", new int[]{56, 78});
        CompoundTag movePayload = EditorCommandPayloads.moveMany(" main ", moves);

        assertEquals("main", EditorCommandPayloads.chapter(movePayload));
        ListTag moveTags = EditorCommandPayloads.moves(movePayload);
        assertEquals(2, moveTags.size());
        assertEquals("quest/a", moveTags.getCompound(0).getString(EditorCommandPayloads.QUEST));
        assertEquals(12, moveTags.getCompound(0).getInt(EditorCommandPayloads.X));
        assertEquals(34, moveTags.getCompound(0).getInt(EditorCommandPayloads.Y));
        assertTrue(EditorCommandPayloads.isAllowed(EditorCommandType.MOVE_MANY, movePayload));

        Map<String, Float> scales = new LinkedHashMap<>();
        scales.put(" quest/a ", 1.25f);
        CompoundTag scalePayload = EditorCommandPayloads.scaleMany(" main ", scales);
        assertEquals("main", EditorCommandPayloads.chapter(scalePayload));
        assertEquals("quest/a", EditorCommandPayloads.scales(scalePayload).getCompound(0).getString(EditorCommandPayloads.QUEST));
        assertEquals(1.25f, EditorCommandPayloads.scales(scalePayload).getCompound(0).getFloat(EditorCommandPayloads.SCALE), 0.0001f);
        assertTrue(EditorCommandPayloads.isAllowed(EditorCommandType.SCALE_MANY, scalePayload));

        CompoundTag copyPayload = EditorCommandPayloads.copyMany(" main ", List.of(" quest/a ", "", "quest/b"));
        assertEquals("main", EditorCommandPayloads.chapter(copyPayload));
        assertEquals(Set.of("quest/a", "quest/b"), EditorCommandPayloads.questIds(copyPayload));
        assertTrue(EditorCommandPayloads.isAllowed(EditorCommandType.COPY_MANY, copyPayload));
    }

    @Test
    void questFactoriesUseSharedPayloadKeys() {
        CompoundTag icon = EditorCommandPayloads.questIcon(" quest/a ", " minecraft:book ");
        assertEquals("quest/a", EditorCommandPayloads.quest(icon));
        assertEquals("minecraft:book", EditorCommandPayloads.string(icon, EditorCommandPayloads.ICON));

        CompoundTag background = EditorCommandPayloads.questBackgroundMany(List.of(" quest/a ", "quest/b"), " bg/path ", true);
        assertEquals(Set.of("quest/a", "quest/b"), EditorCommandPayloads.questIds(background));
        assertEquals("bg/path", EditorCommandPayloads.string(background, EditorCommandPayloads.BACKGROUND));
        assertTrue(EditorCommandPayloads.bool(background, EditorCommandPayloads.GRAYSCALE));

        CompoundTag taskMove = EditorCommandPayloads.taskMove(" quest/a ", " task/one ", -1);
        assertEquals("quest/a", EditorCommandPayloads.quest(taskMove));
        assertEquals("task/one", EditorCommandPayloads.task(taskMove));
        assertEquals(-1, EditorCommandPayloads.integer(taskMove, EditorCommandPayloads.OFFSET));

        CompoundTag rewardMove = EditorCommandPayloads.rewardMove(" quest/a ", " reward/one ", 2);
        assertEquals("quest/a", EditorCommandPayloads.quest(rewardMove));
        assertEquals("reward/one", EditorCommandPayloads.reward(rewardMove));
        assertEquals(2, EditorCommandPayloads.integer(rewardMove, EditorCommandPayloads.OFFSET));
    }

    @Test
    void canvasFactoriesUseKeysReadByPacketHelpers() {
        CanvasImageLayer image = new CanvasImageLayer(" image/a ", "asset:path", 1, 2, 40, 50, 0);
        CompoundTag imagePayload = EditorCommandPayloads.canvasImagePut(" main ", image);
        assertEquals("main", EditorCommandPayloads.chapter(imagePayload));
        assertEquals("image/a", EditorCommandPayloads.compound(imagePayload, EditorCommandPayloads.IMAGE).getString(EditorCommandPayloads.ID));

        CanvasTextLayer text = new CanvasTextLayer(" text/a ", "Label", 3, 4, 80, 20, 0, "left", "normal", 0xFFFFFF);
        CompoundTag textPayload = EditorCommandPayloads.canvasTextPut(" main ", text);
        assertEquals("main", EditorCommandPayloads.chapter(textPayload));
        assertEquals("text/a", EditorCommandPayloads.compound(textPayload, EditorCommandPayloads.TEXT).getString(EditorCommandPayloads.ID));
        assertTrue(EditorCommandPayloads.isAllowed(EditorCommandType.CANVAS_TEXT_PUT, textPayload));

        CompoundTag orderPayload = EditorCommandPayloads.canvasLayerOrder(" main ", List.of("image:image/a", "", "text:text/a"));
        assertEquals(List.of("image:image/a", "text:text/a"), EditorCommandPayloads.nonBlankStringsFrom(EditorCommandPayloads.order(orderPayload)));
        assertTrue(EditorCommandPayloads.isAllowed(EditorCommandType.CANVAS_LAYER_ORDER, orderPayload));
    }

    @Test
    void commandPacketAcceptsTypedCommandDescriptor() {
        CompoundTag payload = EditorCommandPayloads.pasteBlueprint("main", 10, 20, CanvasBlueprint.empty());
        C2SEditorCommandPacket packet = new C2SEditorCommandPacket(EditorCommandType.PASTE_BLUEPRINT, payload);

        assertEquals(EditorCommandType.PASTE_BLUEPRINT, packet.command().type());
        assertEquals("main", EditorCommandPayloads.chapter(packet.command().payload()));
        assertEquals(10, EditorCommandPayloads.integer(packet.command().payload(), EditorCommandPayloads.X));
        assertEquals(20, EditorCommandPayloads.integer(packet.command().payload(), EditorCommandPayloads.Y));
        assertTrue(packet.command().payload().contains(EditorCommandPayloads.BLUEPRINT, Tag.TAG_COMPOUND));
    }
}
