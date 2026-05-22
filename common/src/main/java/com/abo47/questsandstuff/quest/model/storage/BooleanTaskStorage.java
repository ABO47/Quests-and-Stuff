package com.abo47.questsandstuff.quest.model.storage;

import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;

public final class BooleanTaskStorage implements TaskStorage<Boolean, ByteTag> {
    public static final BooleanTaskStorage INSTANCE = new BooleanTaskStorage();

    private BooleanTaskStorage() {
    }

    @Override
    public ByteTag createDefault() {
        return ByteTag.valueOf(false);
    }

    @Override
    public Boolean read(Tag tag) {
        return readBoolean(tag);
    }

    @Override
    public boolean same(Tag first, Tag second) {
        return readBoolean(first) == readBoolean(second);
    }

    public boolean readBoolean(Tag tag) {
        return tag instanceof NumericTag numeric && numeric.getAsByte() != 0;
    }

    public ByteTag set(boolean value) {
        return ByteTag.valueOf(value);
    }
}
