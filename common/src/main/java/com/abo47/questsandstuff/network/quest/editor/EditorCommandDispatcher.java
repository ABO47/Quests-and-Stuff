package com.abo47.questsandstuff.network.quest.editor;

import com.abo47.questsandstuff.quest.QuestServiceRegistry;
import com.abo47.questsandstuff.quest.editor.command.EditorCommand;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandFamily;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

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
        EditorCommandRegistrar registrar = (type, family, handler) -> register(descriptors, type, family, handler);
        EditorCanvasCommandHandlers.register(registrar);
        EditorClipboardCommandHandlers.register(registrar);
        EditorPrerequisiteCommandHandlers.register(registrar);
        EditorQuestCommandHandlers.register(registrar);
        EditorDescriptionCommandHandlers.register(registrar);
        EditorTaskCommandHandlers.register(registrar);
        EditorCanvasLayerCommandHandlers.register(registrar);
        return Collections.unmodifiableMap(descriptors);
    }

    private static void register(Map<EditorCommandType, EditorCommandDescriptor> descriptors, EditorCommandType type, EditorCommandFamily family, EditorCommandHandler handler) {
        EditorCommandDescriptor descriptor = new EditorCommandDescriptor(type, family, handler);
        EditorCommandDescriptor previous = descriptors.put(type, descriptor);
        if (previous != null) {
            throw new IllegalStateException("Duplicate editor command descriptor: " + type);
        }
    }
}
