package com.abo47.questsandstuff.quest.persistence.chapter;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChapterMetadataSnapshotTest {
    @TempDir
    Path root;

    @Test
    void restoreBringsBackGroupMetadataAndCanvasLayers() {
        ChapterMetadataStore store = new ChapterMetadataStore(root);
        store.setChapterOrder(List.of("chapter_one"), Set.of("chapter_one"));
        store.setChapterIcon("chapter_one", "minecraft:diamond");
        store.setChapterBackground("chapter_one", "asset:backgrounds/one.png");
        store.setChapterCanvasBackground("chapter_one", "minecraft:stone");
        store.setChapterTextAlign("chapter_one", "right");
        store.setChapterTextColor("chapter_one", 0x55AAFF);
        store.setChapterTextStyle("chapter_one", "bold");
        store.setChapterTextSize("chapter_one", 18);
        store.setChapterLockUntilUnlocked("chapter_one", true);
        store.setChapterHideUntilUnlocked("chapter_one", true);
        store.putCanvasImage("chapter_one", new CanvasImageLayer("image_a", "item:minecraft:apple", 1, 2, 32, 24, 45));
        store.putCanvasText("chapter_one", new CanvasTextLayer("text_a", "Hello", 3, 4, 40, 16, 90, "center", "italic", 0xFFFFFF));
        store.setCanvasLayerOrder("chapter_one", List.of("image:image_a", "text:text_a"));

        ChapterMetadataSnapshot snapshot = store.snapshot();

        store.setChapterOrder(List.of("chapter_two"), Set.of("chapter_two"));
        store.setChapterIcon("chapter_two", "minecraft:dirt");
        store.setChapterLockUntilUnlocked("chapter_two", false);

        store.restore(snapshot);

        assertEquals(List.of("chapter_one"), store.chapterOrder());
        assertEquals("minecraft:diamond", store.chapterIcon("chapter_one"));
        assertEquals("asset:backgrounds/one.png", store.chapterBackground("chapter_one"));
        assertEquals("minecraft:stone", store.chapterCanvasBackground("chapter_one"));
        assertEquals("right", store.chapterTextAlign("chapter_one"));
        assertEquals(0x55AAFF, store.chapterTextColor("chapter_one"));
        assertEquals("bold", store.chapterTextStyle("chapter_one"));
        assertEquals(18, store.chapterTextSize("chapter_one"));
        assertTrue(store.chapterLockUntilUnlocked("chapter_one"));
        assertTrue(store.chapterHideUntilUnlocked("chapter_one"));
        assertEquals(List.of("image:image_a", "text:text_a"), store.canvasLayerOrder("chapter_one"));
        assertEquals("image_a", store.canvasImages("chapter_one").get(0).id());
        assertEquals("text_a", store.canvasTexts("chapter_one").get(0).id());
        assertFalse(store.chapterOrder().contains("chapter_two"));
    }

    @Test
    void renameKeepsGroupMetadataAndCanvasLayers() {
        ChapterMetadataStore store = new ChapterMetadataStore(root);
        store.setChapterOrder(List.of("old_chapter"), Set.of("old_chapter"));
        store.setChapterIcon("old_chapter", "minecraft:diamond");
        store.setChapterBackground("old_chapter", "asset:backgrounds/one.png");
        store.setChapterCanvasBackground("old_chapter", "minecraft:stone");
        store.setChapterLockUntilUnlocked("old_chapter", true);
        store.putCanvasImage("old_chapter", new CanvasImageLayer("image_a", "item:minecraft:apple", 1, 2, 32, 24, 45));
        store.putCanvasText("old_chapter", new CanvasTextLayer("text_a", "Hello", 3, 4, 40, 16, 90, "center", "italic", 0xFFFFFF));
        store.setCanvasLayerOrder("old_chapter", List.of("image:image_a", "text:text_a"));

        store.renameChapter("old_chapter", "new_chapter", Set.of("new_chapter"));

        assertEquals(List.of("new_chapter"), store.chapterOrder());
        assertEquals("minecraft:diamond", store.chapterIcon("new_chapter"));
        assertEquals("asset:backgrounds/one.png", store.chapterBackground("new_chapter"));
        assertEquals("minecraft:stone", store.chapterCanvasBackground("new_chapter"));
        assertTrue(store.chapterLockUntilUnlocked("new_chapter"));
        assertEquals(List.of("image:image_a", "text:text_a"), store.canvasLayerOrder("new_chapter"));
        assertEquals("image_a", store.canvasImages("new_chapter").get(0).id());
        assertEquals("text_a", store.canvasTexts("new_chapter").get(0).id());
    }
}
