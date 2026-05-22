package com.abo47.questsandstuff.quest.model.storage;

import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;

public final class IntegerTaskStorage implements TaskStorage<Integer, IntTag> {
    public static final IntegerTaskStorage INSTANCE = new IntegerTaskStorage();

    private IntegerTaskStorage() {
    }

    @Override
    public IntTag createDefault() {
        return IntTag.valueOf(0);
    }

    @Override
    public Integer read(Tag tag) {
        return readInt(tag);
    }

    @Override
    public boolean same(Tag first, Tag second) {
        return readInt(first) == readInt(second);
    }

    public int readInt(Tag tag) {
        return tag instanceof NumericTag numeric ? numeric.getAsInt() : 0;
    }

    public IntTag set(int value) {
        return IntTag.valueOf(Math.max(0, value));
    }

    public IntTag add(Tag tag, int delta, int cap) {
        int current = readInt(tag);
        int next = Math.min(Math.max(0, cap), Math.max(0, current + Math.max(0, delta)));
        return set(next);
    }

    public IntTag max(Tag tag, int value, int cap) {
        return set(Math.min(Math.max(0, cap), Math.max(readInt(tag), value)));
    }
}
