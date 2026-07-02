package com.abo47.questsandstuff.client.tablet.controls;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;

import static com.abo47.questsandstuff.client.tablet.theme.render.Surfaces.withAlpha;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.entity.variant.EntityVariantCatalog;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.render.Surfaces;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import java.util.List;

public final class EntityIconControls {
    private EntityIconControls() {
    }

    public static void openIconPicker(TabletUiState state, IconPickerTarget target) {
        if (!target.quest().isBlank()) {
            ModalOpenActions.openQuestIconPicker(state, target.quest());
            return;
        }
        ModalOpenActions.openChapterIconPicker(state, target.chapter());
    }

    public static void addChangeIconHit(WidgetGroup parent, TabletUiState state, Runnable refresh, int x, int y, int size, Runnable openPicker) {
        var hit = TabletUiFactory.flatHitButton(x, y, size, size, click -> {
            ContextMenuState.clearDeleteConfirm(state);
            openPicker.run();
            refresh.run();
        });
        hit.setHoverTexture(iconHoverTexture());
        parent.addWidget(hit);
    }

    public static void addEntityVariantAndMotionActions(
            List<ContextAction> actions,
            TabletUiState state,
            String icon,
            String variantTarget,
            Runnable closeOwner,
            Runnable openMotion,
            Runnable refresh
    ) {
        if (!EntityPreviewRenderer.isEntityAsset(icon)) {
            return;
        }
        String entityId = EntityPreviewRenderer.entityId(icon);
        if (EntityVariantCatalog.hasVariants(entityId)) {
            actions.add(ContextActions.changeVariant(() -> {
                openVariantPicker(state, variantTarget, icon);
                closeOwner.run();
                QuestsAndStuffMod.debugLog("[QnS:UI] entity icon variant picker open target={} entity={}", variantTarget, entityId);
                refresh.run();
            }));
        }
        actions.add(ContextActions.editMotion(() -> {
            ContextMenuState.clearDeleteConfirm(state);
            openMotion.run();
            closeOwner.run();
            refresh.run();
        }));
    }

    public static boolean hasVariants(String icon) {
        return EntityVariantCatalog.hasVariants(EntityPreviewRenderer.entityId(icon));
    }

    public static boolean isEntityIcon(String icon) {
        return EntityPreviewRenderer.isEntityAsset(icon);
    }

    public static void openVariantPicker(TabletUiState state, String target, String icon) {
        ModalOpenActions.openEntityVariantPicker(state, target, icon);
    }

    public static IGuiTexture iconHoverTexture() {
        return Surfaces.bordered(withAlpha(ModColors.INTERACTIVE, 60), ModColors.BORDER_ACCENT);
    }

    public static String pendingRemoveIconLabel(TabletUiState state, String key, String fallback) {
        return TabletUiFactory.pendingDeleteLabel(state, key, fallback);
    }

    public static boolean confirmRemoveIcon(TabletUiState state, String key) {
        return TabletUiFactory.confirmDeleteClick(state, key);
    }

    public record IconPickerTarget(String chapter, String quest) {
        public static IconPickerTarget chapter(String chapter) {
            return new IconPickerTarget(chapter == null ? "" : chapter, "");
        }

        public static IconPickerTarget quest(String quest) {
            return new IconPickerTarget("", quest == null ? "" : quest);
        }
    }
}
