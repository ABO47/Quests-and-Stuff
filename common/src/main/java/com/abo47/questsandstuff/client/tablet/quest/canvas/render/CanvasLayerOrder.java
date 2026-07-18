package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

public record CanvasLayerOrder(List<CanvasLayerKey> backToFront) {
    public CanvasLayerOrder {
        if (backToFront == null || backToFront.isEmpty()) {
            backToFront = List.of();
        } else {
            backToFront = List.copyOf(backToFront);
        }
    }

    public static CanvasLayerOrder fromOrderKeys(Collection<String> orderKeys) {
        if (orderKeys == null || orderKeys.isEmpty()) {
            return new CanvasLayerOrder(List.of());
        }
        List<CanvasLayerKey> keys = new ArrayList<>();
        Set<CanvasLayerKey> seen = new LinkedHashSet<>();
        for (String orderKey : orderKeys) {
            CanvasLayerKey key = CanvasLayerKey.parse(orderKey);
            if (key != null && !key.id().isBlank() && seen.add(key)) {
                keys.add(key);
            }
        }
        return new CanvasLayerOrder(keys);
    }

    public List<String> orderKeys() {
        if (backToFront.isEmpty()) {
            return List.of();
        }
        List<String> keys = new ArrayList<>();
        for (CanvasLayerKey key : backToFront) {
            keys.add(key.orderKey());
        }
        return List.copyOf(keys);
    }

    public List<CanvasLayerKey> hitPriority() {
        if (backToFront.isEmpty()) {
            return List.of();
        }
        List<CanvasLayerKey> priority = new ArrayList<>(backToFront);
        Collections.reverse(priority);
        return List.copyOf(priority);
    }

    public boolean isAbove(CanvasLayerKey upper, CanvasLayerKey lower) {
        int upperIndex = indexOf(upper);
        int lowerIndex = indexOf(lower);
        return upperIndex >= 0 && lowerIndex >= 0 && upperIndex > lowerIndex;
    }

    public CanvasLayerKey topMost(Collection<CanvasLayerKey> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        Set<CanvasLayerKey> candidateSet = new LinkedHashSet<>();
        for (CanvasLayerKey candidate : candidates) {
            if (candidate != null && !candidate.id().isBlank()) {
                candidateSet.add(candidate);
            }
        }
        if (candidateSet.isEmpty()) {
            return null;
        }
        for (CanvasLayerKey key : hitPriority()) {
            if (candidateSet.contains(key)) {
                return key;
            }
        }
        return defaultTopMost(candidateSet);
    }

    public CanvasLayerHit resolveElementHit(QuestCardLayout quest, CanvasImageLayer image, CanvasTextLayer text) {
        return resolveElementHit(quest, image, text, null);
    }

    public CanvasLayerHit resolveElementHit(QuestCardLayout quest, CanvasImageLayer image, CanvasTextLayer text, CanvasExclusiveChoice exclusiveChoice) {
        List<CanvasLayerKey> candidates = new ArrayList<>();
        CanvasLayerKey questKey = quest == null ? null : CanvasLayerKey.quest(quest != null ? quest.questId() : "");
        CanvasLayerKey imageKey = image == null ? null : CanvasLayerKey.image(image.id());
        CanvasLayerKey textKey = text == null ? null : CanvasLayerKey.text(text.id());
        CanvasLayerKey ecKey = exclusiveChoice == null ? null : CanvasLayerKey.exclusiveChoice(exclusiveChoice.id());
        if (questKey != null) {
            candidates.add(questKey);
        }
        if (imageKey != null) {
            candidates.add(imageKey);
        }
        if (textKey != null) {
            candidates.add(textKey);
        }
        if (ecKey != null) {
            candidates.add(ecKey);
        }
        CanvasLayerKey top = topMost(candidates);
        if (top == null) {
            return CanvasLayerHit.EMPTY;
        }
        return new CanvasLayerHit(
                top.equals(questKey) ? quest : null,
                top.equals(imageKey) ? image : null,
                top.equals(textKey) ? text : null,
                top.equals(ecKey) ? exclusiveChoice : null
        );
    }

    private int indexOf(CanvasLayerKey key) {
        if (key == null) {
            return -1;
        }
        return backToFront.indexOf(key);
    }

    private static CanvasLayerKey defaultTopMost(Collection<CanvasLayerKey> candidates) {
        CanvasLayerKey best = null;
        int bestRank = Integer.MIN_VALUE;
        for (CanvasLayerKey candidate : candidates) {
            int rank = defaultHitRank(candidate.kind());
            if (rank > bestRank) {
                best = candidate;
                bestRank = rank;
            }
        }
        return best;
    }

    private static int defaultHitRank(CanvasLayerKind kind) {
        return switch (kind) {
            case CONNECTION -> 0;
            case EXCLUSIVE_CHOICE -> 1;
            case IMAGE -> 2;
            case TEXT -> 3;
            case QUEST -> 4;
        };
    }
}
