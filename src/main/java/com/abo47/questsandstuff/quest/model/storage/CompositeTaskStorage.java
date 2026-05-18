package com.abo47.questsandstuff.quest.model.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.Map;

public final class CompositeTaskStorage implements TaskStorage<CompoundTag, CompoundTag> {
    public static final CompositeTaskStorage EMPTY = new CompositeTaskStorage(Map.of());

    private final Map<String, TaskStorage<?, ? extends Tag>> children;

    public CompositeTaskStorage(Map<String, TaskStorage<?, ? extends Tag>> children) {
        this.children = children == null ? Map.of() : Map.copyOf(children);
    }

    @Override
    public CompoundTag createDefault() {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<String, TaskStorage<?, ? extends Tag>> entry : children.entrySet()) {
            tag.put(entry.getKey(), entry.getValue().createDefault());
        }
        return tag;
    }

    @Override
    public CompoundTag read(Tag tag) {
        return tag instanceof CompoundTag compound ? compound : createDefault();
    }

    @Override
    public boolean same(Tag first, Tag second) {
        CompoundTag left = read(first);
        CompoundTag right = read(second);
        if (!left.getAllKeys().equals(right.getAllKeys())) {
            return false;
        }
        for (Map.Entry<String, TaskStorage<?, ? extends Tag>> entry : children.entrySet()) {
            if (!entry.getValue().same(left.get(entry.getKey()), right.get(entry.getKey()))) {
                return false;
            }
        }
        return true;
    }
}
