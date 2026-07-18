package com.abo47.questsandstuff.client.tablet.modal;

import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.modal.entity.EntityVariantModal;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class TabletEntityVariantModal {
    private TabletEntityVariantModal() {
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        return EntityVariantModal.rebuild(modal, state, player, refresh, w, h);
    }
}
