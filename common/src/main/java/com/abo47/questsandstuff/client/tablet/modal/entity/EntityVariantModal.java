package com.abo47.questsandstuff.client.tablet.modal.entity;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.modal.ModalShell;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

import static com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions.closeAll;

public final class EntityVariantModal {
    private EntityVariantModal() {
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        EntityVariantPickerModel model = EntityVariantPickerModel.create(state);
        if (model == null) {
            closeAll(state);
            refresh.run();
            return null;
        }

        ModalShell.addTitleAndClose(modal, "Entity variants", w, state, refresh);
        EntityVariantTiles.addPreview(modal, player, state, refresh, model, h);
        TextFieldWidget search = addControls(modal, state, refresh, model, w);
        EntityVariantTiles.addTiles(modal, player, state, refresh, model, w, h);
        return search;
    }

    private static TextFieldWidget addControls(WidgetGroup modal, TabletUiState state, Runnable refresh, EntityVariantPickerModel model, int w) {
        int rightX = EntityVariantTiles.RIGHT_X;
        int rightW = w - 174;
        int controlsY = 2;
        int controlsH = 16;
        int backY = 1;
        int backSize = 18;
        int backX = rightX + rightW - backSize - 22;
        int searchW = model.browsingFolder() ? Math.max(40, backX - rightX - 3) : Math.max(40, rightW - 22);
        TextFieldWidget search = ModalShell.addSearchField(modal, rightX, controlsY, searchW, controlsH, state.entityVariantSearch, 80, value -> {
            state.entityVariantSearch = SearchFilter.normalizeUserInput(value);
            state.entityVariantScroll = 0;
            refresh.run();
        }, focused -> state.entityVariantSearchFocused = focused);

        if (model.browsingFolder()) {
            EntityVariantTiles.addBackButton(modal, backX, backY, backSize, backSize, () -> {
                state.entityVariantFolder = "";
                state.entityVariantSearch = "";
                state.entityVariantScroll = 0;
                QuestsAndStuffMod.debugLog("[QnS:UI] entity variant folder back target={} entity={}", model.target(), model.entityId());
                refresh.run();
            });
        }
        return search;
    }
}
