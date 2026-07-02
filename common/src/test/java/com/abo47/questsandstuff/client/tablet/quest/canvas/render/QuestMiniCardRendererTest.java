package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import org.junit.jupiter.api.Test;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static org.junit.jupiter.api.Assertions.assertEquals;

class QuestMiniCardRendererTest {
    @Test
    void iconRectUsesSamePaddingForMiniatures() {
        QuestMiniCardRenderer.IconRect rect = QuestMiniCardRenderer.iconRect(10, 20, 40, 30);

        assertEquals(20, rect.x());
        assertEquals(25, rect.y());
        assertEquals(20, rect.size());
    }

    @Test
    void overlayAndHighlightColorsScaleWithAlpha() {
        assertEquals(withAlpha(TabletColors.SURFACE_BASE, 64), QuestMiniCardRenderer.hiddenOverlayColor(128, 130));
        assertEquals(withAlpha(TabletColors.SURFACE_BASE, 120), QuestMiniCardRenderer.hiddenOverlayColor(255, 120));
        assertEquals(withAlpha(TabletColors.BORDER_ACCENT, 180), QuestMiniCardRenderer.highlightColor(180));
    }
}
