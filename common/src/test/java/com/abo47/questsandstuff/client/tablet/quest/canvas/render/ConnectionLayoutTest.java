package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMode;
import com.abo47.questsandstuff.quest.sync.QuestSyncKeys;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectionLayoutTest {
    @Test
    void prerequisiteLinesUseResolvedStyleBeforeDrawing() {
        TabletUiState state = new TabletUiState();
        state.canEdit = true;
        state.selectedGroup = "main";

        QuestCardLayout source = card("quest/source", new CompoundTag(), 10, 20, 80, 40);
        CompoundTag targetTag = targetTag("quest/source");
        QuestCardLayout target = card("quest/target", targetTag, 200, 20, 80, 40);

        List<ConnectionLine> lines = ConnectionLayout.prerequisiteConnectionLines(
                state,
                List.of(source, target),
                Map.of(source.questId(), source, target.questId(), target),
                500,
                300
        );

        assertEquals(1, lines.size());
        ConnectionLine line = lines.get(0);
        assertEquals("quest/source->quest/target", line.edgeId());
        assertEquals("quest/source", line.sourceQuestId());
        assertEquals("quest/target", line.targetQuestId());
        assertEquals(0x446688, line.color());
        assertFalse(line.direct());
        assertTrue(line.hidden());
        assertEquals(64, line.alpha());
        assertEquals(source.centerX(), line.startX());
        assertEquals(target.centerX(), line.endX());
    }

    private static QuestCardLayout card(String questId, CompoundTag tag, int x, int y, int width, int height) {
        return new QuestCardLayout(
                questId,
                tag,
                x,
                y,
                width,
                height,
                width,
                height,
                x,
                y,
                1.0f,
                x,
                y,
                width,
                height
        );
    }

    private static CompoundTag targetTag(String prerequisiteId) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(QuestSettings.SHOW_PREREQUISITE_ARROW_FIELD, true);

        ListTag prerequisites = new ListTag();
        prerequisites.add(StringTag.valueOf(prerequisiteId));
        tag.put(QuestDefinition.PREREQUISITES_FIELD, prerequisites);

        CompoundTag colors = new CompoundTag();
        colors.putInt(prerequisiteId, 0x446688);
        tag.put(QuestSyncKeys.Quest.CONNECTION_COLORS, colors);

        CompoundTag modes = new CompoundTag();
        modes.putString(prerequisiteId, QuestConnectionMode.GRID.serializedName());
        tag.put(QuestSyncKeys.Quest.CONNECTION_MODES, modes);

        ListTag hidden = new ListTag();
        hidden.add(StringTag.valueOf(prerequisiteId));
        tag.put(QuestSyncKeys.Quest.HIDDEN_CONNECTIONS, hidden);
        return tag;
    }
}
