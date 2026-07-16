package com.abo47.questsandstuff.client.tablet.modal.panel;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.tablet.animation.SourceOriginRevealWidget;
import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.modal.ModalWindowManager;
import com.abo47.questsandstuff.client.tablet.modal.TabletAdvancementPickerModal;
import com.abo47.questsandstuff.client.tablet.modal.TabletAssetPickerModal;
import com.abo47.questsandstuff.client.tablet.modal.TabletBiomePickerModal;
import com.abo47.questsandstuff.client.tablet.modal.TabletBlockPickerModal;
import com.abo47.questsandstuff.client.tablet.modal.TabletColorPickerModal;
import com.abo47.questsandstuff.client.tablet.modal.TabletDimensionPickerModal;
import com.abo47.questsandstuff.client.tablet.modal.TabletEntityVariantModal;
import com.abo47.questsandstuff.client.tablet.modal.TabletIconPickerModal;
import com.abo47.questsandstuff.client.tablet.modal.TabletItemInventoryPickerModal;
import com.abo47.questsandstuff.client.tablet.modal.TabletLootTablePickerModal;
import com.abo47.questsandstuff.client.tablet.quest.prerequisite.QuestPrerequisitesModal;
import com.abo47.questsandstuff.client.tablet.modal.TabletRecipePickerModal;
import com.abo47.questsandstuff.client.tablet.modal.TabletSoundPickerModal;
import com.abo47.questsandstuff.client.tablet.modal.TabletStatPickerModal;
import com.abo47.questsandstuff.client.tablet.modal.TabletStructurePickerModal;
import com.abo47.questsandstuff.client.tablet.modal.TabletThemePickerModal;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.List;

import static com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome.drawWindowShadow;

public final class ModalPanelRouter {
    private static final List<ModalPanelDescriptor> MODAL_DESCRIPTORS = List.of(
            new ModalPanelDescriptor(ModalWindowManager.ModalType.ICON_PICKER, TabletIconPickerModal::rebuild, state -> state.pickers.iconSearchFocused),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.ASSET_PICKER, TabletAssetPickerModal::rebuild, state -> state.pickers.assetSearchFocused),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.BIOME_PICKER, TabletBiomePickerModal::rebuild, state -> state.pickers.biomeSearchFocused),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.ADVANCEMENT_PICKER, TabletAdvancementPickerModal::rebuild, state -> state.pickers.advancementSearchFocused),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.RECIPE_PICKER, TabletRecipePickerModal::rebuild, state -> state.pickers.recipeSearchFocused),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.STRUCTURE_PICKER, TabletStructurePickerModal::rebuild, state -> state.pickers.structureSearchFocused),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.BLOCK_PICKER, TabletBlockPickerModal::rebuild, state -> state.pickers.blockSearchFocused),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.STAT_PICKER, TabletStatPickerModal::rebuild, state -> state.pickers.statSearchFocused),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.DIMENSION_PICKER, TabletDimensionPickerModal::rebuild, state -> state.pickers.dimensionSearchFocused),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.LOOT_TABLE_PICKER, TabletLootTablePickerModal::rebuild, state -> state.pickers.lootTableSearchFocused),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.ITEM_INVENTORY_PICKER, TabletItemInventoryPickerModal::rebuild, state -> state.pickers.itemInventorySearchFocused),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.SOUND_PICKER, TabletSoundPickerModal::rebuild, state -> state.pickers.soundSearchFocused),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.COLOR_PICKER, (modal, state, player, refresh, w, h) -> {
                TabletColorPickerModal.rebuild(modal, state, player, refresh, w, h);
                return null;
            }, state -> false),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.THEME_PICKER, (modal, state, player, refresh, w, h) -> {
                TabletThemePickerModal.rebuild(modal, state, refresh, w, h);
                return null;
            }, state -> false),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.ENTITY_VARIANT_PICKER, TabletEntityVariantModal::rebuild, state -> state.pickers.entityVariantSearchFocused),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.PREREQUISITES_MANAGER, QuestPrerequisitesModal::rebuild, state -> state.modal.prerequisitesManagerSearchFocused)
    );

    private ModalPanelRouter() {
    }

    public static void rebuildChapterModal(WidgetGroup overlay, TabletUiState state, Player player, Runnable refresh) {
        overlay.clearAllWidgets();
        if (!ModalStateQueries.anyOpen(state) && !state.modal.modalWindowClosing) {
            return;
        }
        ModalPanelDescriptor descriptor = descriptor(ModalStateQueries.activeType(state));
        if (descriptor == null) {
            return;
        }
        int w = Math.min(432, overlay.getSize().width - 32);
        int h = Math.min(260, overlay.getSize().height - 32);
        int mx = (overlay.getSize().width - w) / 2;
        int my = (overlay.getSize().height - h) / 2;
        WidgetGroup dim = new WidgetGroup(0, 0, overlay.getSize().width, overlay.getSize().height) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                int alpha = dimAlpha(state);
                if (alpha <= 0) {
                    return;
                }
                SurfaceFactory.fill(withAlpha(TabletColors.SURFACE_BASE, alpha)).draw(graphics, 0, 0, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
            }
        };
        overlay.addWidget(dim);
        WidgetGroup modal = new WidgetGroup(mx, my, w, h) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                drawWindowShadow(graphics, this);
                super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            }
        };
        modal.setBackground(SurfaceFactory.bordered(withAlpha(TabletColors.SURFACE_BASE, 252), TabletColors.BORDER_ACCENT));
        TextFieldWidget searchField = descriptor.rebuild(modal, state, player, refresh, w, h);
        modal.setActive(!state.modal.modalWindowClosing);
        if (QuestsAndStuffConfig.popupWindowAnimationsEnabled()) {
            overlay.addWidget(SourceOriginRevealWidget.window(
                    modal,
                    () -> state.modal.modalWindowAnimationStartMs,
                    () -> !state.modal.modalWindowClosing,
                    () -> sourceRect(state)
            ));
        } else {
            overlay.addWidget(modal);
        }
        if (!state.modal.modalWindowClosing) {
            restoreSearchFocus(state, descriptor, searchField);
        }
    }

    private static ModalPanelDescriptor descriptor(ModalWindowManager.ModalType type) {
        for (ModalPanelDescriptor descriptor : MODAL_DESCRIPTORS) {
            if (descriptor.type() == type) {
                return descriptor;
            }
        }
        return null;
    }

    private static int dimAlpha(TabletUiState state) {
        if (!QuestsAndStuffConfig.popupWindowAnimationsEnabled()) {
            return 140;
        }
        float amount = SourceOriginRevealWidget.windowOpenAmount(state.modal.modalWindowAnimationStartMs, !state.modal.modalWindowClosing);
        return Math.round(140 * amount);
    }

    private static SourceOriginRevealWidget.SourceRect sourceRect(TabletUiState state) {
        if (!state.modal.modalWindowAnimationHasSource) {
            return null;
        }
        return new SourceOriginRevealWidget.SourceRect(
                state.modal.modalWindowAnimationSourceX,
                state.modal.modalWindowAnimationSourceY,
                state.modal.modalWindowAnimationSourceW,
                state.modal.modalWindowAnimationSourceH
        );
    }

    private static void restoreSearchFocus(TabletUiState state, ModalPanelDescriptor descriptor, TextFieldWidget searchField) {
        if (searchField != null && descriptor.searchFocused(state)) {
            searchField.setFocus(true);
        }
    }

    private record ModalPanelDescriptor(
            ModalWindowManager.ModalType type,
            ModalPanelBuilder builder,
            ModalSearchFocus searchFocus
    ) {
        TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
            return builder.rebuild(modal, state, player, refresh, w, h);
        }

        boolean searchFocused(TabletUiState state) {
            return searchFocus.focused(state);
        }
    }

    private interface ModalPanelBuilder {
        TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h);
    }

    private interface ModalSearchFocus {
        boolean focused(TabletUiState state);
    }
}
