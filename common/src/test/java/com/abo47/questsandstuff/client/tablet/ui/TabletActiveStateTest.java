package com.abo47.questsandstuff.client.tablet.ui;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.state.TabletActiveState;

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
        ClientQuestStateFacade.resetStateForTests();
        TabletActiveState.setActiveTabletState(null);
        TabletActiveState.setActiveTabletRefresh(null);
        previousUiState = Files.exists(UI_STATE_FILE) ? Files.readString(UI_STATE_FILE, StandardCharsets.UTF_8) : null;
    }

    @AfterEach
    void restoreUiState() throws IOException {
        TabletActiveState.setActiveTabletState(null);
        TabletActiveState.setActiveTabletRefresh(null);
        ClientQuestStateFacade.resetStateForTests();
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
        state.root.selectedChapter = "old_group";
        state.chapterPanel.chapterDraft = "old_group";
        state.chapterPanel.chapterDraftName = "old_group";
        state.canvas.canvasSelection.questIds().add("old_quest");
        state.canvas.canvasSelection.imageIds().add("old_image");
        state.canvas.canvasSelection.textIds().add("old_text");
        state.clipboard.canvasClipboard.recordPastedImage(" local_image ");
        state.clipboard.canvasClipboard.recordPastedText(" local_text ");

        AtomicInteger refreshes = new AtomicInteger();
        TabletActiveState.setActiveTabletRefresh(refreshes::incrementAndGet);
        TabletActiveState.setActiveTabletState(state);

        TabletActiveState.selectPastedQuests(pastePayload());

        assertEquals("pasted_group", state.root.selectedChapter);
        assertEquals("pasted_group", state.chapterPanel.chapterDraft);
        assertEquals("pasted_group", state.chapterPanel.chapterDraftName);
        assertEquals("quest:new", state.chapterPanel.lastJumpQuest);
        assertTrue(state.canvas.canvasSelection.questIds().contains("quest:new"));
        assertTrue(state.canvas.canvasSelection.imageIds().contains("remote_image"));
        assertTrue(state.canvas.canvasSelection.imageIds().contains("local_image"));
        assertTrue(state.canvas.canvasSelection.textIds().contains("remote_text"));
        assertTrue(state.canvas.canvasSelection.textIds().contains("local_text"));
        assertEquals("local_image", state.canvas.canvasSelection.primaryImageId());
        assertEquals("local_text", state.canvas.canvasSelection.primaryTextId());
        assertTrue(state.clipboard.canvasClipboard.pendingPastedImageIds().isEmpty());
        assertTrue(state.clipboard.canvasClipboard.pendingPastedTextIds().isEmpty());
        assertTrue(ClientQuestStateFacade.chapterOrder().contains("pasted_group"));
        assertEquals(1, refreshes.get());
    }

    private static CompoundTag pastePayload() {
        CompoundTag payload = new CompoundTag();
        payload.putString("chapter", " pasted_group ");
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
