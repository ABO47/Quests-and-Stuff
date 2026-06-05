package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.animation.UiAnimationProgress;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.Iterator;
import java.util.Map;

public final class CanvasConnectionAnimation {
    private static final long DURATION_MS = 160L;
    private static final String EDGE_SEPARATOR = "->";
    private static final AnimationState STOPPED = new AnimationState(false, 1.0f);

    private CanvasConnectionAnimation() {
    }

    public static String edgeKey(String sourceQuestId, String targetQuestId) {
        return normalize(sourceQuestId) + EDGE_SEPARATOR + normalize(targetQuestId);
    }

    public static String targetQuestId(String edgeKey) {
        if (edgeKey == null) {
            return "";
        }
        int separator = edgeKey.indexOf(EDGE_SEPARATOR);
        if (separator < 0 || separator + EDGE_SEPARATOR.length() >= edgeKey.length()) {
            return "";
        }
        return edgeKey.substring(separator + EDGE_SEPARATOR.length()).trim();
    }

    public static void startIfNew(TabletUiState state, String targetQuestId, String sourceQuestId) {
        String target = normalize(targetQuestId);
        String source = normalize(sourceQuestId);
        if (state == null || target.isBlank() || source.isBlank() || target.equals(source)) {
            return;
        }
        if (!QuestsAndStuffConfig.connectionAnimationsEnabled()) {
            state.canvasConnectionAnimationStarts.clear();
            return;
        }
        if (hasPrerequisite(target, source)) {
            return;
        }
        state.canvasConnectionAnimationStarts.put(edgeKey(source, target), System.currentTimeMillis());
    }

    public static boolean finishIfDone(TabletUiState state) {
        if (state == null || state.canvasConnectionAnimationStarts.isEmpty()) {
            return false;
        }
        if (!QuestsAndStuffConfig.connectionAnimationsEnabled()) {
            state.canvasConnectionAnimationStarts.clear();
            return true;
        }
        long now = System.currentTimeMillis();
        boolean changed = false;
        Iterator<Map.Entry<String, Long>> iterator = state.canvasConnectionAnimationStarts.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            if (!UiAnimationProgress.running(entry.getValue(), DURATION_MS, now)) {
                iterator.remove();
                changed = true;
            }
        }
        return changed;
    }

    public static AnimationState current(TabletUiState state, String edgeKey, long now) {
        if (state == null || edgeKey == null || edgeKey.isBlank() || !QuestsAndStuffConfig.connectionAnimationsEnabled()) {
            return STOPPED;
        }
        Long startMs = state.canvasConnectionAnimationStarts.get(edgeKey);
        if (startMs == null || !UiAnimationProgress.running(startMs, DURATION_MS, now)) {
            return STOPPED;
        }
        return new AnimationState(true, UiAnimationProgress.easedProgress(startMs, DURATION_MS, now));
    }

    private static boolean hasPrerequisite(String targetQuestId, String sourceQuestId) {
        CompoundTag target = ClientQuestCache.quest(targetQuestId);
        if (target == null) {
            return false;
        }
        ListTag prerequisites = target.getList(QuestDefinition.PREREQUISITES_FIELD, Tag.TAG_STRING);
        for (int i = 0; i < prerequisites.size(); i++) {
            if (sourceQuestId.equals(prerequisites.getString(i))) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    public record AnimationState(boolean running, float progress) {
    }
}
