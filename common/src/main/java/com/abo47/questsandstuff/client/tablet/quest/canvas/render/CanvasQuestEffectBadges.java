package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.render.Surfaces;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;

import static com.abo47.questsandstuff.client.tablet.theme.render.Surfaces.withAlpha;

public final class CanvasQuestEffectBadges {
    private static final int MAX_BADGES = 6;

    private CanvasQuestEffectBadges() {
    }

    public static void render(WidgetGroup parent, TabletUiState state, QuestCardLayout card) {
        if (!QuestsAndStuffConfig.questEffectIconsEnabled()) {
            return;
        }
        List<Badge> badges = badges(state, card.tag());
        if (badges.isEmpty()) {
            return;
        }
        int min = Math.min(card.width(), card.height());
        int iconSize = Math.max(4, Math.min(10, Math.round(min * 0.24f)));
        int boxSize = iconSize + 2;
        int gap = 1;
        int pad = 1;
        int x = pad;
        int y = pad;
        int maxX = Math.max(pad, card.width() - pad);
        int maxY = Math.max(pad, card.height() - pad);
        int rendered = 0;
        for (Badge badge : badges) {
            if (rendered >= MAX_BADGES || boxSize > card.width() || boxSize > card.height()) {
                return;
            }
            if (x + boxSize > maxX && x > pad) {
                x = pad;
                y += boxSize + gap;
            }
            if (y + boxSize > maxY) {
                return;
            }
            addBadge(parent, x, y, boxSize, iconSize, badge.icon());
            x += boxSize + gap;
            rendered++;
        }
    }

    private static List<Badge> badges(TabletUiState state, CompoundTag tag) {
        List<Badge> badges = new ArrayList<>();
        boolean editor = state != null && state.root.canEdit;
        if (editor ? tag.getBoolean("visual_hidden") : ClientQuestCache.questHiddenPreview(tag)) {
            badges.add(new Badge("eye-off"));
        }
        if (editor ? lockedSetting(tag) : ClientQuestCache.questLockedPreview(tag)) {
            badges.add(new Badge("lock_quest"));
        }
        if (tag.getBoolean("repeatable")) {
            badges.add(new Badge("repeat"));
        }
        if (hasCustomCompletionSound(tag)) {
            badges.add(new Badge("audio-lines"));
        }
        if (hasCompletionHudBackground(tag)) {
            badges.add(new Badge("completion_hud_background"));
        }
        return badges;
    }

    private static boolean lockedSetting(CompoundTag tag) {
        return QuestVisibilityMode.LOCKED.serializedName().equals(tag.getString("hidden_mode"));
    }

    private static boolean hasCustomCompletionSound(CompoundTag tag) {
        String sound = tag.getString("completion_sound");
        if (!sound.isBlank() && !QuestDisplay.DEFAULT_COMPLETION_SOUND.equals(sound)) {
            return true;
        }
        return tag.contains("completion_sound_volume")
                && tag.getInt("completion_sound_volume") != QuestDisplay.DEFAULT_COMPLETION_SOUND_VOLUME;
    }

    private static boolean hasCompletionHudBackground(CompoundTag tag) {
        String background = QuestDisplay.normalizeCompletionHudBackground(tag.getString("completion_hud_background"));
        return !QuestDisplay.DEFAULT_COMPLETION_HUD_BACKGROUND.equals(background);
    }

    private static void addBadge(WidgetGroup parent, int x, int y, int boxSize, int iconSize, String icon) {
        WidgetGroup badge = new WidgetGroup(x, y, boxSize, boxSize);
        badge.setBackground(Surfaces.fill(withAlpha(ModColors.SURFACE_BASE, 190)));
        parent.addWidget(badge);
        parent.addWidget(new DisplayIconWidget(x + 1, y + 1, iconSize, iconSize, icon));
    }

    private record Badge(String icon) {
    }
}
