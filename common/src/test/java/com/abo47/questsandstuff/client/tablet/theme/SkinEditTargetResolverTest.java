package com.abo47.questsandstuff.client.tablet.theme;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestDetailsDescriptionCanvas extends WidgetGroup {
    QuestDetailsDescriptionCanvas(int x, int y, int w, int h) {
        super(x, y, w, h);
    }
}

class TabletHomeOverviewPanel extends WidgetGroup {
    TabletHomeOverviewPanel(int x, int y, int w, int h) {
        super(x, y, w, h);
    }
}

class QuestDetailsLayerWidget extends WidgetGroup {
    QuestDetailsLayerWidget(int x, int y, int w, int h) {
        super(x, y, w, h);
    }
}

class SkinEditTargetResolverTest {

    @Test
    void stableKeyForPlainWidgetGroupReturnsClassName() {
        WidgetGroup wg = new WidgetGroup(0, 0, 100, 100);
        String key = SkinEditTargetResolver.stableKeyFor(wg);
        assertEquals("WidgetGroup", key);
    }

    @Test
    void stableKeyForChildCreatesPath() {
        WidgetGroup root = new WidgetGroup(0, 0, 200, 200);
        WidgetGroup child = new WidgetGroup(10, 10, 100, 100);
        root.addWidget(child);

        String key = SkinEditTargetResolver.stableKeyFor(child);
        assertEquals("WidgetGroup/WidgetGroup", key);
    }

    @Test
    void siblingDisambiguationAddsIndex() {
        WidgetGroup root = new WidgetGroup(0, 0, 200, 200);
        WidgetGroup first = new WidgetGroup(10, 10, 80, 80);
        WidgetGroup second = new WidgetGroup(100, 10, 80, 80);
        root.addWidget(first);
        root.addWidget(second);

        assertEquals("WidgetGroup/WidgetGroup[0]", SkinEditTargetResolver.stableKeyFor(first));
        assertEquals("WidgetGroup/WidgetGroup[1]", SkinEditTargetResolver.stableKeyFor(second));
    }

    @Test
    void isTargetableWithoutBackgroundIsFalse() {
        WidgetGroup wg = new WidgetGroup(0, 0, 100, 100);
        assertFalse(SkinEditTargetResolver.isTargetable(wg));
    }

    @Test
    void isTargetableWithBackgroundIsTrue() {
        WidgetGroup wg = new WidgetGroup(0, 0, 100, 100);
        wg.setBackground(new ColorRectTexture(0xFF000000));
        assertTrue(SkinEditTargetResolver.isTargetable(wg));
    }

    @Test
    void isTargetableWithCustomChromeIsTrueEvenWithoutBackground() {
        WidgetGroup custom = new WidgetGroup(0, 0, 100, 100) {
            @Override
            public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            }
        };
        assertTrue(SkinEditTargetResolver.isTargetable(custom));
    }

    @Test
    void hasCustomChromeForPlainWidgetGroupIsFalse() {
        WidgetGroup wg = new WidgetGroup(0, 0, 100, 100);
        assertFalse(SkinEditTargetResolver.hasCustomChrome(wg));
    }

    @Test
    void hasCustomChromeForOverriddenDrawInBackgroundIsTrue() {
        WidgetGroup custom = new WidgetGroup(0, 0, 100, 100) {
            @Override
            public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            }
        };
        assertTrue(SkinEditTargetResolver.hasCustomChrome(custom));
    }

    @Test
    void widgetForKeyNullKeyReturnsNull() {
        WidgetGroup root = new WidgetGroup(0, 0, 100, 100);
        assertNull(SkinEditTargetResolver.widgetForKey(root, null));
        assertNull(SkinEditTargetResolver.widgetForKey(root, ""));
    }

    @Test
    void widgetForKeyMissingKeyReturnsNull() {
        WidgetGroup root = new WidgetGroup(0, 0, 100, 100);
        assertNull(SkinEditTargetResolver.widgetForKey(root, "nonexistent"));
    }

    @Test
    void widgetForKeyFindsByPathKey() {
        WidgetGroup root = new WidgetGroup(0, 0, 200, 200);
        WidgetGroup child = new WidgetGroup(10, 10, 100, 100);
        root.addWidget(child);

        String childKey = SkinEditTargetResolver.stableKeyFor(child);
        Widget found = SkinEditTargetResolver.widgetForKey(root, childKey);
        assertNotNull(found);
        assertEquals(child, found);
    }

    @Test
    void ancestorBoundsCollectsAncestorsUpToStopAt() {
        WidgetGroup root = new WidgetGroup(50, 50, 300, 300);
        WidgetGroup mid = new WidgetGroup(60, 60, 200, 200);
        WidgetGroup leaf = new WidgetGroup(70, 70, 100, 100);
        root.addWidget(mid);
        mid.addWidget(leaf);

        List<Rectangle> bounds = SkinEditTargetResolver.ancestorBounds(leaf, root);
        assertEquals(2, bounds.size());
        Rectangle midRect = bounds.get(0);
        assertEquals(110, midRect.x);
        assertEquals(110, midRect.y);
        assertEquals(200, midRect.width);
        assertEquals(200, midRect.height);
        Rectangle rootRect = bounds.get(1);
        assertEquals(50, rootRect.x);
        assertEquals(50, rootRect.y);
        assertEquals(300, rootRect.width);
        assertEquals(300, rootRect.height);
    }

    @Test
    void ancestorBoundsStopsAtRootExclusive() {
        WidgetGroup root = new WidgetGroup(50, 50, 300, 300);
        WidgetGroup leaf = new WidgetGroup(70, 70, 100, 100);
        root.addWidget(leaf);

        List<Rectangle> bounds = SkinEditTargetResolver.ancestorBounds(leaf, root);
        assertEquals(1, bounds.size(), "should include root bounds as the final entry");
        Rectangle rootRect = bounds.get(0);
        assertEquals(50, rootRect.x);
        assertEquals(50, rootRect.y);
        assertEquals(300, rootRect.width);
        assertEquals(300, rootRect.height);
    }

    @Test
    void targetKeyForWidgetWithBackgroundReturnsPathKey() {
        WidgetGroup root = new WidgetGroup(0, 0, 200, 200);
        root.setBackground(new ColorRectTexture(0xFF000000));
        WidgetGroup child = new WidgetGroup(10, 10, 100, 100);
        child.setBackground(new ColorRectTexture(0xFF000000));
        root.addWidget(child);

        String key = SkinEditTargetResolver.findTargetKeyAt(root, 15, 15);
        assertNotNull(key);
        assertTrue(key.contains("WidgetGroup"));
    }

    @Test
    void targetKeyForMissReturnsNull() {
        WidgetGroup root = new WidgetGroup(0, 0, 100, 100);
        String key = SkinEditTargetResolver.findTargetKeyAt(root, 999, 999);
        assertNull(key);
    }

    @Test
    void questDetailsDescriptionCanvasInsideLayerReturnsStableKey() {
        QuestDetailsLayerWidget layer = new QuestDetailsLayerWidget(0, 0, 300, 300);
        QuestDetailsDescriptionCanvas canvas = new QuestDetailsDescriptionCanvas(10, 10, 280, 280);
        layer.addWidget(canvas);

        String key = SkinEditTargetResolver.stableKeyFor(canvas);
        assertEquals("quest_details_description_canvas", key);
    }

    @Test
    void emptyCustomChromeWidgetInsideQuestDetailsLayerIsExcludedFromTargeting() {
        QuestDetailsLayerWidget layer = new QuestDetailsLayerWidget(0, 0, 300, 300);
        WidgetGroup dim = new WidgetGroup(0, 0, 300, 300) {
            @Override
            public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            }
        };
        layer.addWidget(dim);

        assertFalse(SkinEditTargetResolver.isTargetable(dim));
    }

    @Test
    void customChromeWidgetChildOfTabletHomeOverviewPanelResolvesToHomeInner() {
        TabletHomeOverviewPanel panel = new TabletHomeOverviewPanel(0, 0, 300, 300);
        WidgetGroup inner = new WidgetGroup(10, 10, 280, 280) {
            @Override
            public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            }
        };
        panel.addWidget(inner);

        String key = SkinEditTargetResolver.stableKeyFor(inner);
        assertEquals("home_inner", key);
    }

    @Test
    void nonEmptyCustomChromeWidgetInsideQuestDetailsLayerIsTargetable() {
        QuestDetailsLayerWidget layer = new QuestDetailsLayerWidget(0, 0, 300, 300);
        WidgetGroup modal = new WidgetGroup(10, 10, 280, 280) {
            @Override
            public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            }
        };
        modal.addWidget(new WidgetGroup(0, 0, 100, 100));
        layer.addWidget(modal);

        assertTrue(SkinEditTargetResolver.isTargetable(modal));
    }

    @Test
    void questDetailsDescriptionCanvasOutsideLayerFallsBackToPathKey() {
        QuestDetailsDescriptionCanvas canvas = new QuestDetailsDescriptionCanvas(10, 10, 100, 100);
        String key = SkinEditTargetResolver.stableKeyFor(canvas);
        assertEquals("QuestDetailsDescriptionCanvas", key);
    }
}
