package com.abo47.questsandstuff.quest.persistence.chapter;

import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChapterMetadataSnapshotTest {
    @TempDir
    Path root;

    @Test
    void restoreBringsBackChapterMetadataAndCanvasLayers() {
        ChapterMetadataStore store = new ChapterMetadataStore(root);
        store.setGroupOrder(List.of("chapter_one"), Set.of("chapter_one"));
        store.setGroupIcon("chapter_one", "minecraft:diamond");
        store.setGroupBackground("chapter_one", "asset:backgrounds/one.png");
        store.setGroupCanvasBackground("chapter_one", "minecraft:stone");
        store.setGroupTextAlign("chapter_one", "right");
        store.setGroupTextColor("chapter_one", 0x55AAFF);
        store.setGroupTextStyle("chapter_one", "bold");
        store.setGroupTextSize("chapter_one", 18);
        store.setGroupLockUntilUnlocked("chapter_one", true);
        store.setGroupHideUntilUnlocked("chapter_one", true);
        store.putCanvasImage("chapter_one", new CanvasImageLayer("image_a", "item:minecraft:apple", 1, 2, 32, 24, 45));
        store.putCanvasText("chapter_one", new CanvasTextLayer("text_a", "Hello", 3, 4, 40, 16, 90, "center", "italic", 0xFFFFFF));
        store.setCanvasLayerOrder("chapter_one", List.of("image:image_a", "text:text_a"));

        ChapterMetadataSnapshot snapshot = store.snapshot();

        store.setGroupOrder(List.of("chapter_two"), Set.of("chapter_two"));
        store.setGroupIcon("chapter_two", "minecraft:dirt");
        store.setGroupLockUntilUnlocked("chapter_two", false);

        store.restore(snapshot);

        assertEquals(List.of("chapter_one"), store.groupOrder());
        assertEquals("minecraft:diamond", store.groupIcon("chapter_one"));
        assertEquals("asset:backgrounds/one.png", store.groupBackground("chapter_one"));
        assertEquals("minecraft:stone", store.groupCanvasBackground("chapter_one"));
        assertEquals("right", store.groupTextAlign("chapter_one"));
        assertEquals(0x55AAFF, store.groupTextColor("chapter_one"));
        assertEquals("bold", store.groupTextStyle("chapter_one"));
        assertEquals(18, store.groupTextSize("chapter_one"));
        assertTrue(store.groupLockUntilUnlocked("chapter_one"));
        assertTrue(store.groupHideUntilUnlocked("chapter_one"));
        assertEquals(List.of("image:image_a", "text:text_a"), store.canvasLayerOrder("chapter_one"));
        assertEquals("image_a", store.canvasImages("chapter_one").get(0).id());
        assertEquals("text_a", store.canvasTexts("chapter_one").get(0).id());
        assertFalse(store.groupOrder().contains("chapter_two"));
    }
}
