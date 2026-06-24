package com.abo47.questsandstuff.client.tablet.quest.details.objective;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public final class QuestDetailsObjectiveMenus {
    private QuestDetailsObjectiveMenus() {
    }

    public static void renderTypePicker(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String questId, CompoundTag quest, int modalW, int modalH) {
        QuestObjectiveTransientMenus.render(modal, state, player, refresh, modalW, modalH);
        QuestObjectiveTypePickerMenu.render(modal, state, player, refresh, questId, quest, modalW, modalH);
    }

    public static void renderContextMenu(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String questId) {
        QuestObjectiveContextMenu.render(modal, state, player, refresh, questId);
    }
}
