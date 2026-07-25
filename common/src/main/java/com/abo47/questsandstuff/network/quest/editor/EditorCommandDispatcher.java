package com.abo47.questsandstuff.network.quest.editor;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.quest.QuestServiceRegistry;
import com.abo47.questsandstuff.quest.editor.command.EditorCommand;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;

final class EditorCommandDispatcher {
    private static final Map<EditorCommandType, EditorCommandDescriptor> DESCRIPTORS = descriptors();

    private EditorCommandDispatcher() {
    }

    static boolean dispatch(ServerPlayer player, EditorCommand command) {
        if (player == null || command == null) {
            return false;
        }
        EditorCommandDescriptor descriptor = descriptor(command.type());
        if (descriptor == null) {
            return false;
        }
        descriptor.apply(player, QuestServiceRegistry.editor(player.server), command.payload());
        return true;
    }

    static EditorCommandDescriptor descriptor(EditorCommandType type) {
        return type == null ? null : DESCRIPTORS.get(type);
    }

    static Set<EditorCommandType> registeredTypes() {
        return DESCRIPTORS.keySet();
    }

    private static Map<EditorCommandType, EditorCommandDescriptor> descriptors() {
        Map<EditorCommandType, EditorCommandDescriptor> descriptors = new EnumMap<>(EditorCommandType.class);
        Consumer<EditorCommandDescriptor> registrar = descriptor -> {
            EditorCommandDescriptor previous = descriptors.put(descriptor.type(), descriptor);
            if (previous != null) {
                throw new IllegalStateException("Duplicate editor command descriptor: " + descriptor.type());
            }
        };
        EditorCanvasCommandHandlers.register(registrar);
        EditorClipboardCommandHandlers.register(registrar);
        EditorPrerequisiteCommandHandlers.register(registrar);
        EditorQuestCommandHandlers.register(registrar);
        EditorDescriptionCommandHandlers.register(registrar);
        EditorTaskCommandHandlers.register(registrar);
        EditorCanvasLayerCommandHandlers.register(registrar);
        return Collections.unmodifiableMap(descriptors);
    }
}
