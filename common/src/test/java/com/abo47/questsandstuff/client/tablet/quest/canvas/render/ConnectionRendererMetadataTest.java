package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMetadata;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMode;
import com.abo47.questsandstuff.quest.sync.QuestSyncKeys;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectionRendererMetadataTest {
    @Test
    void metadataReadsSavedTargetTagsWithNormalizedSourceKey() {
        TabletUiState state = new TabletUiState();
        CompoundTag target = new CompoundTag();
        CompoundTag colors = new CompoundTag();
        colors.putInt("quest/source", 0x336699);
        target.put(QuestSyncKeys.Quest.CONNECTION_COLORS, colors);
        CompoundTag modes = new CompoundTag();
        modes.putString("quest/source", QuestConnectionMode.GRID.serializedName());
        target.put(QuestSyncKeys.Quest.CONNECTION_MODES, modes);
        ListTag hidden = new ListTag();
        hidden.add(StringTag.valueOf("quest/source"));
        target.put(QuestSyncKeys.Quest.HIDDEN_CONNECTIONS, hidden);

        QuestConnectionMetadata metadata = ConnectionStyleResolver.metadata(
                state,
                "main",
                " quest/source ",
                " quest/target ",
                target
        );

        assertEquals("quest/source->quest/target", metadata.edgeKey());
        assertEquals(0x336699, metadata.color());
        assertEquals(QuestConnectionMode.GRID, metadata.mode());
        assertTrue(metadata.hidden());
        assertFalse(metadata.direct());
    }

    @Test
    void metadataFallsBackToLocalUiEdgeStateWhenTargetHasNoSavedOverride() {
        TabletUiState state = new TabletUiState();
        String edgeKey = QuestConnectionMetadata.edgeKey("quest/source", "quest/target");
        state.canvas.connectionColorsByGroup.computeIfAbsent("main", ignored -> new HashMap<>()).put(edgeKey, 0xAA8844);
        state.canvas.gridConnectionsByGroup.computeIfAbsent("main", ignored -> new HashSet<>()).add(edgeKey);
        state.canvas.hiddenConnectionsByGroup.computeIfAbsent("main", ignored -> new HashSet<>()).add(edgeKey);

        QuestConnectionMetadata metadata = ConnectionStyleResolver.metadata(
                state,
                "main",
                "quest/source",
                "quest/target",
                new CompoundTag()
        );

        assertEquals(edgeKey, metadata.edgeKey());
        assertEquals(0xAA8844, metadata.color());
        assertEquals(QuestConnectionMode.GRID, metadata.mode());
        assertTrue(metadata.hidden());
    }
}
