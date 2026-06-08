package com.abo47.questsandstuff.client.tablet.ui;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TabletActiveStateTest {
    private static final Path UI_STATE_FILE = Path.of("config", "questsandstuff", "ui_state.json");
    private String previousUiState;

    @BeforeEach
    void resetClientState() throws IOException {
        ClientQuestCache.resetStateForTests();
        TabletActiveState.setActiveTabletState(null);
        TabletActiveState.setActiveTabletRefresh(null);
        previousUiState = Files.exists(UI_STATE_FILE) ? Files.readString(UI_STATE_FILE, StandardCharsets.UTF_8) : null;
    }

    @AfterEach
    void restoreUiState() throws IOException {
        TabletActiveState.setActiveTabletState(null);
        TabletActiveState.setActiveTabletRefresh(null);
        ClientQuestCache.resetStateForTests();
        if (previousUiState == null) {
            Files.deleteIfExists(UI_STATE_FILE);
            return;
        }
        Files.createDirectories(UI_STATE_FILE.getParent());
        Files.writeString(UI_STATE_FILE, previousUiState, StandardCharsets.UTF_8);
    }

    @Test
    void selectPastedQuestsMergesServerIdsWithPendingClipboardLayerIds() {
        TabletUiState state = new TabletUiState();
        state.selectedGroup = "old_group";
        state.groupDraft = "old_group";
        state.chapterDraftName = "old_group";
        state.selectedQuestIds.add("old_quest");
        state.selectedCanvasImageIds.add("old_image");
        state.selectedCanvasTextIds.add("old_text");
        state.canvasClipboard.recordPastedImage(" local_image ");
        state.canvasClipboard.recordPastedText(" local_text ");

        AtomicInteger refreshes = new AtomicInteger();
        TabletActiveState.setActiveTabletRefresh(refreshes::incrementAndGet);
        TabletActiveState.setActiveTabletState(state);

        TabletActiveState.selectPastedQuests(pastePayload());

        assertEquals("pasted_group", state.selectedGroup);
        assertEquals("pasted_group", state.groupDraft);
        assertEquals("pasted_group", state.chapterDraftName);
        assertEquals("quest:new", state.lastJumpQuest);
        assertTrue(state.selectedQuestIds.contains("quest:new"));
        assertTrue(state.selectedCanvasImageIds.contains("remote_image"));
        assertTrue(state.selectedCanvasImageIds.contains("local_image"));
        assertTrue(state.selectedCanvasTextIds.contains("remote_text"));
        assertTrue(state.selectedCanvasTextIds.contains("local_text"));
        assertEquals("local_image", state.selectedCanvasImageId);
        assertEquals("local_text", state.selectedCanvasTextId);
        assertTrue(state.canvasClipboard.pendingPastedImageIds().isEmpty());
        assertTrue(state.canvasClipboard.pendingPastedTextIds().isEmpty());
        assertTrue(ClientQuestCache.groupOrder().contains("pasted_group"));
        assertEquals(1, refreshes.get());
    }

    private static CompoundTag pastePayload() {
        CompoundTag payload = new CompoundTag();
        payload.putString("group", " pasted_group ");
        payload.put("quests", stringList("quest:new"));
        payload.put("images", stringList("remote_image"));
        payload.put("texts", stringList("remote_text"));
        return payload;
    }

    private static ListTag stringList(String value) {
        ListTag list = new ListTag();
        list.add(StringTag.valueOf(value));
        return list;
    }
}
