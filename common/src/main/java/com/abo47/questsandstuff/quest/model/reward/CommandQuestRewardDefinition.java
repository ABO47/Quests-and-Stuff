package com.abo47.questsandstuff.quest.model.reward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.QuestsAndStuffConfig;

public record CommandQuestRewardDefinition(
        String id,
        ResourceLocation type,
        String command,
        String title,
        String icon,
        boolean selectable
) implements QuestRewardDefinition {
    public CommandQuestRewardDefinition(String id, ResourceLocation type, String command) {
        this(id, type, command, "Command", "minecraft:command_block", false);
    }

    public CommandQuestRewardDefinition(String id, ResourceLocation type, String command, String title, String icon) {
        this(id, type, command, title, icon, false);
    }

    public CommandQuestRewardDefinition {
        command = command == null ? "" : command.trim();
        title = title == null || title.isBlank() ? "Command" : title.trim();
        icon = icon == null || icon.isBlank() ? "minecraft:command_block" : icon.trim();
    }

    public static Codec<CommandQuestRewardDefinition> codec(ResourceLocation type) {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(CommandQuestRewardDefinition::id),
                Codec.STRING.fieldOf("command").forGetter(CommandQuestRewardDefinition::command),
                Codec.STRING.fieldOf("title").orElse("Command").forGetter(CommandQuestRewardDefinition::title),
                Codec.STRING.fieldOf("icon").orElse("minecraft:command_block").forGetter(CommandQuestRewardDefinition::icon),
                Codec.BOOL.fieldOf("selectable").orElse(false).forGetter(CommandQuestRewardDefinition::selectable)
        ).apply(instance, (id, command, title, icon, selectable) -> new CommandQuestRewardDefinition(id, type, command, title, icon, selectable)));
    }

    @Override
    public boolean canClaim(ServerPlayer player) {
        return QuestsAndStuffConfig.commandRewardsEnabled();
    }

    @Override
    public void grant(ServerPlayer player) {
        if (command == null || command.isBlank()) {
            return;
        }
        if (!QuestsAndStuffConfig.commandRewardsEnabled()) {
            return;
        }
        player.server.getCommands().performPrefixedCommand(
                player.createCommandSourceStack().withSuppressedOutput().withPermission(2),
                command
        );
    }
}
