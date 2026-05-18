package com.abo47.questsandstuff.quest.model.task.player;

import com.abo47.questsandstuff.quest.model.storage.BooleanTaskStorage;
import com.abo47.questsandstuff.quest.model.storage.TaskStorage;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignal;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignalType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

public record LocationQuestTaskDefinition(
        String id,
        ResourceLocation type,
        String mode,
        String dimension,
        int x,
        int y,
        int z,
        int radius,
        String icon
) implements QuestTaskDefinition {
    public static Codec<LocationQuestTaskDefinition> codec(ResourceLocation type) {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(LocationQuestTaskDefinition::id),
                Codec.STRING.fieldOf("mode").orElse("dimension").forGetter(LocationQuestTaskDefinition::mode),
                Codec.STRING.fieldOf("dimension").orElse("").forGetter(LocationQuestTaskDefinition::dimension),
                Codec.INT.fieldOf("x").orElse(0).forGetter(LocationQuestTaskDefinition::x),
                Codec.INT.fieldOf("y").orElse(0).forGetter(LocationQuestTaskDefinition::y),
                Codec.INT.fieldOf("z").orElse(0).forGetter(LocationQuestTaskDefinition::z),
                Codec.INT.fieldOf("radius").orElse(6).forGetter(LocationQuestTaskDefinition::radius),
                Codec.STRING.fieldOf("icon").orElse("").forGetter(LocationQuestTaskDefinition::icon)
        ).apply(instance, (id, mode, dimension, x, y, z, radius, icon) -> new LocationQuestTaskDefinition(id, type, mode, dimension, x, y, z, radius, icon)));
    }

    public LocationQuestTaskDefinition {
        icon = icon == null ? "" : icon;
    }

    @Override
    public TaskStorage<?, ? extends Tag> storage() {
        return BooleanTaskStorage.INSTANCE;
    }

    @Override
    public Set<QuestSignalType> signals() {
        return Set.of(QuestSignalType.LOCATION_TICK);
    }

    @Override
    public Tag test(Tag progress, QuestSignal signal) {
        Tag current = progress == null ? defaultProgress() : progress;
        if (signal.type() != QuestSignalType.LOCATION_TICK) {
            return current;
        }
        if ("dimension".equals(mode)) {
            String actual = signal.dimension().location().toString();
            return dimension.isBlank() || dimension.equals(actual) ? BooleanTaskStorage.INSTANCE.set(true) : current;
        }
        BlockPos target = new BlockPos(x, y, z);
        int safeRadius = Math.max(0, radius);
        return signal.pos().distManhattan(target) <= safeRadius ? BooleanTaskStorage.INSTANCE.set(true) : current;
    }
}
