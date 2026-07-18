package com.abo47.questsandstuff.quest.model.storage;

import java.util.Objects;

import net.minecraft.nbt.Tag;

public interface TaskStorage<T, S extends Tag> {
    S createDefault();

    T read(Tag tag);

    default boolean same(Tag first, Tag second) {
        return Objects.equals(first, second);
    }
}
