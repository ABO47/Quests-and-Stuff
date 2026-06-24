package com.abo47.questsandstuff.network.quest.editor;

import com.abo47.questsandstuff.quest.editor.command.EditorCommandFamily;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;

@FunctionalInterface
interface EditorCommandRegistrar {
    void register(EditorCommandType type, EditorCommandFamily family, EditorCommandHandler handler);
}
