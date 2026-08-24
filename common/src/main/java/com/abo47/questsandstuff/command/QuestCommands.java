package com.abo47.questsandstuff.command;

import java.util.Collection;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.QuestServiceRegistry;
import com.abo47.questsandstuff.quest.runtime.progress.ItemLockIndex;

public final class QuestCommands {
    private QuestCommands() {
    }

    private static final SuggestionProvider<CommandSourceStack> QUEST_IDS = (context, builder) -> {
        SharedSuggestionProvider.suggest(QuestServiceRegistry.engine(context.getSource().getServer()).questIds(), builder);
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(QuestsAndStuffMod.MODID)
                .then(reload())
                .then(complete())
                .then(reset())
                .then(resetAll())
                .then(pin())
                .then(manual())
                .then(perf())
                .then(locks())
        );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> locks() {
        return Commands.literal("locks")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    var engine = QuestServiceRegistry.engine(context.getSource().getServer());
                    ItemStack held = player.getMainHandItem();
                    String heldId = held.isEmpty()
                            ? "(empty hand - hold the item you want to inspect)"
                            : BuiltInRegistries.ITEM.getKey(held.getItem()).toString();
                    List<ItemLockIndex.LockBinding> bindings = held.isEmpty() ? List.of() : engine.itemLockBindings(held);
                    context.getSource().sendSuccess(() -> Component.literal(
                            "[QnS] lock entries defined: " + engine.itemLockCount()), false);
                    context.getSource().sendSuccess(() -> Component.literal(
                            "[QnS] held item: " + heldId), false);
                    if (bindings.isEmpty()) {
                        context.getSource().sendSuccess(() -> Component.literal(
                                "[QnS] no lock bindings match the held item"), false);
                    }
                    for (ItemLockIndex.LockBinding binding : bindings) {
                        boolean complete = engine.itemLockBindingComplete(player, binding);
                        context.getSource().sendSuccess(() -> Component.literal(
                                "[QnS] locked by quest '" + binding.questId() + "' task '" + binding.taskId()
                                        + "' complete=" + complete), false);
                    }
                    boolean locked = held.isEmpty() ? false : engine.isItemLocked(player, held);
                    context.getSource().sendSuccess(() -> Component.literal(
                            "[QnS] verdict: held item is " + (locked ? "LOCKED" : "not locked")), false);
                    return 1;
                });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> reload() {
        return Commands.literal("reload")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    QuestServiceRegistry.definitions(context.getSource().getServer()).load();
                    QuestServiceRegistry.engine(context.getSource().getServer()).rebuildIndex();
                    List<ServerPlayer> players = context.getSource().getServer().getPlayerList().getPlayers();
                    QuestServiceRegistry.engine(context.getSource().getServer()).preparePlayersForFullSync(players);
                    QuestServiceRegistry.sync(context.getSource().getServer()).syncFull(players);
                    context.getSource().sendSuccess(() -> Component.translatable("command.questsandstuff.reload.success"), true);
                    return 1;
                });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> complete() {
        return Commands.literal("complete")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("quest", StringArgumentType.string())
                        .suggests(QUEST_IDS)
                        .executes(context -> completeFor(List.of(context.getSource().getPlayerOrException()), context))
                        .then(Commands.argument("target", EntityArgument.players())
                                .executes(context -> completeFor(EntityArgument.getPlayers(context, "target"), context))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> reset() {
        return Commands.literal("reset")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("quest", StringArgumentType.string())
                        .suggests(QUEST_IDS)
                        .executes(context -> resetFor(List.of(context.getSource().getPlayerOrException()), context))
                        .then(Commands.argument("target", EntityArgument.players())
                                .executes(context -> resetFor(EntityArgument.getPlayers(context, "target"), context))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> resetAll() {
        return Commands.literal("resetall")
                .requires(source -> source.hasPermission(2))
                .executes(context -> resetAllFor(List.of(context.getSource().getPlayerOrException()), context))
                .then(Commands.argument("target", EntityArgument.players())
                        .executes(context -> resetAllFor(EntityArgument.getPlayers(context, "target"), context)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> pin() {
        return Commands.literal("pin")
                .then(Commands.argument("quest", StringArgumentType.string())
                        .suggests(QUEST_IDS)
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            QuestServiceRegistry.engine(context.getSource().getServer())
                                    .togglePin(player, StringArgumentType.getString(context, "quest"));
                            return 1;
                        }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> manual() {
        return Commands.literal("manual")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("task_key", StringArgumentType.string())
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            QuestServiceRegistry.engine(context.getSource().getServer())
                                    .runManualTask(player, StringArgumentType.getString(context, "task_key"));
                            return 1;
                        }))
                .then(Commands.literal("item_submit")
                        .then(Commands.argument("quest", StringArgumentType.string())
                                .suggests(QUEST_IDS)
                                .then(Commands.argument("task", StringArgumentType.string())
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            QuestServiceRegistry.engine(context.getSource().getServer()).submitManualItemTask(
                                                    player,
                                                    StringArgumentType.getString(context, "quest"),
                                                    StringArgumentType.getString(context, "task")
                                            );
                                            return 1;
                                        }))))
                .then(Commands.literal("xp_submit")
                        .then(Commands.argument("quest", StringArgumentType.string())
                                .suggests(QUEST_IDS)
                                .then(Commands.argument("task", StringArgumentType.string())
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            QuestServiceRegistry.engine(context.getSource().getServer()).submitManualXpTask(
                                                    player,
                                                    StringArgumentType.getString(context, "quest"),
                                                    StringArgumentType.getString(context, "task")
                                            );
                                            return 1;
                                        }))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> perf() {
        return Commands.literal("perf")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    var perf = QuestServiceRegistry.perf(context.getSource().getServer());
                    CompoundTag perfTag = perf.snapshotTag();
                    long signals = perfTag.getLong("signals");
                    long signalNanos = perfTag.getLong("signal_nanos");
                    long count = Math.max(1L, signals);
                    long avgMicros = (signalNanos / count) / 1000L;
                    long bindings = perfTag.getLong("bindings");
                    long updates = perfTag.getLong("quest_updates");
                    context.getSource().sendSuccess(() -> Component.translatable(
                            "command.questsandstuff.perf.summary",
                            signals, avgMicros, bindings, updates
                    ), false);
                    context.getSource().sendSuccess(() -> Component.translatable(
                            "command.questsandstuff.perf.sync",
                            perfTag.getLong("full_packets"),
                            perfTag.getLong("full_chunks"),
                            humanBytes(perfTag.getLong("full_bytes")),
                            perfTag.getLong("delta_packets"),
                            perfTag.getLong("delta_chunks"),
                            humanBytes(perfTag.getLong("delta_bytes"))
                    ), false);
                    return 1;
                })
                .then(Commands.literal("reset")
                        .executes(context -> {
                            QuestServiceRegistry.perf(context.getSource().getServer()).reset();
                            context.getSource().sendSuccess(() -> Component.translatable("command.questsandstuff.perf.reset"), false);
                            return 1;
                        }));
    }

    private static int completeFor(Collection<ServerPlayer> targets, CommandContext<CommandSourceStack> context) {
        String questId = StringArgumentType.getString(context, "quest");
        for (ServerPlayer target : targets) {
            QuestServiceRegistry.engine(context.getSource().getServer()).completeQuest(target, questId);
        }
        return 1;
    }

    private static int resetFor(Collection<ServerPlayer> targets, CommandContext<CommandSourceStack> context) {
        String questId = StringArgumentType.getString(context, "quest");
        for (ServerPlayer target : targets) {
            QuestServiceRegistry.engine(context.getSource().getServer()).resetQuest(target, questId);
        }
        return 1;
    }

    private static int resetAllFor(Collection<ServerPlayer> targets, CommandContext<CommandSourceStack> context) {
        for (ServerPlayer target : targets) {
            QuestServiceRegistry.engine(context.getSource().getServer()).resetAll(target);
        }
        return 1;
    }

    private static String humanBytes(long bytes) {
        if (bytes < 1024L) {
            return bytes + "B";
        }
        long kb = bytes / 1024L;
        if (kb < 1024L) {
            return kb + "KB";
        }
        return (kb / 1024L) + "MB";
    }

}
