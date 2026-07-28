package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

public record CanvasLayerKey(CanvasLayerKind kind, String id) {
    public CanvasLayerKey {
        id = id == null ? "" : id.trim();
    }

    public static CanvasLayerKey exclusiveChoice(String ecId) {
        return new CanvasLayerKey(CanvasLayerKind.EXCLUSIVE_CHOICE, ecId);
    }

    public static CanvasLayerKey quest(String questId) {
        return new CanvasLayerKey(CanvasLayerKind.QUEST, questId);
    }

    public static CanvasLayerKey image(String imageId) {
        return new CanvasLayerKey(CanvasLayerKind.IMAGE, imageId);
    }

    public static CanvasLayerKey text(String textId) {
        return new CanvasLayerKey(CanvasLayerKind.TEXT, textId);
    }

    public static CanvasLayerKey connection(String connectionId) {
        return new CanvasLayerKey(CanvasLayerKind.CONNECTION, connectionId);
    }

    public static CanvasLayerKey parse(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        if (key.startsWith(CanvasLayerOrdering.EXCLUSIVE_CHOICE_PREFIX)) {
            return exclusiveChoice(key.substring(CanvasLayerOrdering.EXCLUSIVE_CHOICE_PREFIX.length()));
        }
        if (key.startsWith(CanvasLayerOrdering.QUEST_PREFIX)) {
            return quest(key.substring(CanvasLayerOrdering.QUEST_PREFIX.length()));
        }
        if (key.startsWith(CanvasLayerOrdering.IMAGE_PREFIX)) {
            return image(key.substring(CanvasLayerOrdering.IMAGE_PREFIX.length()));
        }
        if (key.startsWith(CanvasLayerOrdering.TEXT_PREFIX)) {
            return text(key.substring(CanvasLayerOrdering.TEXT_PREFIX.length()));
        }
        if (key.startsWith(CanvasLayerOrdering.CONNECTION_PREFIX)) {
            return connection(key.substring(CanvasLayerOrdering.CONNECTION_PREFIX.length()));
        }
        return null;
    }

    public String orderKey() {
        if (id.isBlank()) {
            return "";
        }
        return switch (kind) {
            case EXCLUSIVE_CHOICE -> CanvasLayerOrdering.EXCLUSIVE_CHOICE_PREFIX + id;
            case QUEST -> CanvasLayerOrdering.QUEST_PREFIX + id;
            case IMAGE -> CanvasLayerOrdering.IMAGE_PREFIX + id;
            case TEXT -> CanvasLayerOrdering.TEXT_PREFIX + id;
            case CONNECTION -> CanvasLayerOrdering.CONNECTION_PREFIX + id;
        };
    }

    public boolean selectable() {
        return kind != CanvasLayerKind.CONNECTION && !id.isBlank();
    }
}
