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

        assertEquals("main", EditorCommandPayloads.group(movePayload));
        ListTag moveTags = EditorCommandPayloads.moves(movePayload);
        assertEquals(2, moveTags.size());
        assertEquals("quest/a", moveTags.getCompound(0).getString(EditorCommandPayloadKeys.QUEST));
        assertEquals(12, moveTags.getCompound(0).getInt(EditorCommandPayloadKeys.X));
        assertEquals(34, moveTags.getCompound(0).getInt(EditorCommandPayloadKeys.Y));
        assertTrue(EditorCommandPayloadLimits.isAllowed(EditorCommandType.MOVE_MANY, movePayload));

        Map<String, Float> scales = new LinkedHashMap<>();
        scales.put(" quest/a ", 1.25f);
        CompoundTag scalePayload = EditorCommandPayloads.scaleMany(" main ", scales);
        assertEquals("main", EditorCommandPayloads.group(scalePayload));
        assertEquals("quest/a", EditorCommandPayloads.scales(scalePayload).getCompound(0).getString(EditorCommandPayloadKeys.QUEST));
        assertEquals(1.25f, EditorCommandPayloads.scales(scalePayload).getCompound(0).getFloat(EditorCommandPayloadKeys.SCALE), 0.0001f);
        assertTrue(EditorCommandPayloadLimits.isAllowed(EditorCommandType.SCALE_MANY, scalePayload));

        CompoundTag copyPayload = EditorCommandPayloads.copyMany(" main ", List.of(" quest/a ", "", "quest/b"));
        assertEquals("main", EditorCommandPayloads.group(copyPayload));
        assertEquals(Set.of("quest/a", "quest/b"), EditorCommandPayloads.questIds(copyPayload));
        assertTrue(EditorCommandPayloadLimits.isAllowed(EditorCommandType.COPY_MANY, copyPayload));
    }

    @Test
    void questFactoriesUseSharedPayloadKeys() {
        CompoundTag icon = EditorCommandPayloads.questIcon(" quest/a ", " minecraft:book ");
        assertEquals("quest/a", EditorCommandPayloads.quest(icon));
        assertEquals("minecraft:book", EditorCommandPayloads.string(icon, EditorCommandPayloadKeys.ICON));

        CompoundTag background = EditorCommandPayloads.questBackgroundMany(List.of(" quest/a ", "quest/b"), " bg/path ", true);
        assertEquals(Set.of("quest/a", "quest/b"), EditorCommandPayloads.questIds(background));
        assertEquals("bg/path", EditorCommandPayloads.string(background, EditorCommandPayloadKeys.BACKGROUND));
        assertTrue(EditorCommandPayloads.bool(background, EditorCommandPayloadKeys.GRAYSCALE));

        CompoundTag taskMove = EditorCommandPayloads.taskMove(" quest/a ", " task/one ", -1);
        assertEquals("quest/a", EditorCommandPayloads.quest(taskMove));
        assertEquals("task/one", EditorCommandPayloads.task(taskMove));
        assertEquals(-1, EditorCommandPayloads.integer(taskMove, EditorCommandPayloadKeys.OFFSET));

        CompoundTag rewardMove = EditorCommandPayloads.rewardMove(" quest/a ", " reward/one ", 2);
        assertEquals("quest/a", EditorCommandPayloads.quest(rewardMove));
        assertEquals("reward/one", EditorCommandPayloads.reward(rewardMove));
        assertEquals(2, EditorCommandPayloads.integer(rewardMove, EditorCommandPayloadKeys.OFFSET));
    }

    @Test
    void canvasFactoriesUseKeysReadByPacketHelpers() {
        CanvasImageLayer image = new CanvasImageLayer(" image/a ", "asset:path", 1, 2, 40, 50, 0);
        CompoundTag imagePayload = EditorCommandPayloads.canvasImagePut(" main ", image);
        assertEquals("main", EditorCommandPayloads.group(imagePayload));
        assertEquals("image/a", EditorCommandPayloads.compound(imagePayload, EditorCommandPayloadKeys.IMAGE).getString(EditorCommandPayloadKeys.ID));

        CanvasTextLayer text = new CanvasTextLayer(" text/a ", "Label", 3, 4, 80, 20, 0, "left", "normal", 0xFFFFFF);
        CompoundTag textPayload = EditorCommandPayloads.canvasTextPut(" main ", text);
        assertEquals("main", EditorCommandPayloads.group(textPayload));
        assertEquals("text/a", EditorCommandPayloads.compound(textPayload, EditorCommandPayloadKeys.TEXT).getString(EditorCommandPayloadKeys.ID));
        assertTrue(EditorCommandPayloadLimits.isAllowed(EditorCommandType.CANVAS_TEXT_PUT, textPayload));

        CompoundTag orderPayload = EditorCommandPayloads.canvasLayerOrder(" main ", List.of("image:image/a", "", "text:text/a"));
        assertEquals(List.of("image:image/a", "text:text/a"), EditorCommandPayloads.nonBlankStringsFrom(EditorCommandPayloads.order(orderPayload)));
        assertTrue(EditorCommandPayloadLimits.isAllowed(EditorCommandType.CANVAS_LAYER_ORDER, orderPayload));
    }

    @Test
    void commandPacketAcceptsTypedCommandDescriptor() {
        CompoundTag payload = EditorCommandPayloads.pasteBlueprint("main", 10, 20, CanvasBlueprint.empty());
        C2SEditorCommandPacket packet = new C2SEditorCommandPacket(EditorCommandType.PASTE_BLUEPRINT, payload);

        assertEquals(EditorCommandType.PASTE_BLUEPRINT, packet.command().type());
        assertEquals("main", EditorCommandPayloads.group(packet.command().payload()));
        assertEquals(10, EditorCommandPayloads.integer(packet.command().payload(), EditorCommandPayloadKeys.X));
        assertEquals(20, EditorCommandPayloads.integer(packet.command().payload(), EditorCommandPayloadKeys.Y));
        assertTrue(packet.command().payload().contains(EditorCommandPayloadKeys.BLUEPRINT, Tag.TAG_COMPOUND));
    }
}
