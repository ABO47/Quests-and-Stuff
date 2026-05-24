package com.abo47.questsandstuff.client.tablet.modal.panel;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.tablet.animation.SourceOriginRevealWidget;
import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
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
import com.abo47.questsandstuff.client.tablet.modal.TabletRecipePickerModal;
import com.abo47.questsandstuff.client.tablet.modal.TabletSettingsModal;
import com.abo47.questsandstuff.client.tablet.modal.TabletStatPickerModal;
import com.abo47.questsandstuff.client.tablet.modal.TabletStructurePickerModal;
import com.abo47.questsandstuff.client.tablet.modal.TabletThemePickerModal;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

import static com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome.drawPanelLighting;
import static com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome.drawWindowShadow;

public final class ModalPanelRouter {
    private ModalPanelRouter() {
    }

    public static void rebuildChapterModal(WidgetGroup overlay, TabletUiState state, Player player, Runnable refresh) {
        overlay.clearAllWidgets();
        if (!ModalStateQueries.anyOpen(state) && !state.modalWindowClosing) {
            return;
        }
        TextFieldWidget iconSearchField = null;
        TextFieldWidget assetSearchField = null;
        TextFieldWidget biomeSearchField = null;
        TextFieldWidget advancementSearchField = null;
        TextFieldWidget recipeSearchField = null;
        TextFieldWidget structureSearchField = null;
        TextFieldWidget blockSearchField = null;
        TextFieldWidget statSearchField = null;
        TextFieldWidget dimensionSearchField = null;
        TextFieldWidget lootTableSearchField = null;
        TextFieldWidget itemInventorySearchField = null;
        TextFieldWidget entityVariantSearchField = null;
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
                graphics.fill(getPositionX(), getPositionY(), getPositionX() + getSizeWidth(), getPositionY() + getSizeHeight(), TabletUiFactory.withAlpha(ModColors.SURFACE_BASE, alpha));
            }
        };
        overlay.addWidget(dim);
        WidgetGroup modal = new WidgetGroup(mx, my, w, h) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                drawWindowShadow(graphics, this);
                super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
                drawPanelLighting(graphics, this);
            }
        };
        modal.setBackground(Surfaces.bordered(TabletUiFactory.withAlpha(ModColors.SURFACE_BASE, 252), ModColors.BORDER_ACCENT));
        if (state.iconPickerOpen) {
            iconSearchField = TabletIconPickerModal.rebuild(modal, state, player, refresh, w, h);
        } else if (state.assetPickerOpen) {
            assetSearchField = TabletAssetPickerModal.rebuild(modal, state, player, refresh, w, h);
        } else if (state.biomePickerOpen) {
            biomeSearchField = TabletBiomePickerModal.rebuild(modal, state, player, refresh, w, h);
        } else if (state.advancementPickerOpen) {
            advancementSearchField = TabletAdvancementPickerModal.rebuild(modal, state, player, refresh, w, h);
        } else if (state.recipePickerOpen) {
            recipeSearchField = TabletRecipePickerModal.rebuild(modal, state, player, refresh, w, h);
        } else if (state.structurePickerOpen) {
            structureSearchField = TabletStructurePickerModal.rebuild(modal, state, player, refresh, w, h);
        } else if (state.blockPickerOpen) {
            blockSearchField = TabletBlockPickerModal.rebuild(modal, state, player, refresh, w, h);
        } else if (state.statPickerOpen) {
            statSearchField = TabletStatPickerModal.rebuild(modal, state, player, refresh, w, h);
        } else if (state.dimensionPickerOpen) {
            dimensionSearchField = TabletDimensionPickerModal.rebuild(modal, state, player, refresh, w, h);
        } else if (state.lootTablePickerOpen) {
            lootTableSearchField = TabletLootTablePickerModal.rebuild(modal, state, player, refresh, w, h);
        } else if (state.itemInventoryPickerOpen) {
            itemInventorySearchField = TabletItemInventoryPickerModal.rebuild(modal, state, player, refresh, w, h);
        } else if (state.colorPickerOpen) {
            TabletColorPickerModal.rebuild(modal, state, player, refresh, w, h);
        } else if (state.themePickerOpen) {
            TabletThemePickerModal.rebuild(modal, state, refresh, w, h);
        } else if (state.entityVariantPickerOpen) {
            entityVariantSearchField = TabletEntityVariantModal.rebuild(modal, state, player, refresh, w, h);
        } else if (state.settingsPanelOpen) {
            TabletSettingsModal.rebuild(modal, state, refresh, w, h);
        }
        modal.setActive(!state.modalWindowClosing);
        if (QuestsAndStuffConfig.popupWindowAnimationsEnabled()) {
            overlay.addWidget(SourceOriginRevealWidget.window(
                    modal,
                    () -> state.modalWindowAnimationStartMs,
                    () -> !state.modalWindowClosing,
                    () -> sourceRect(state)
            ));
        } else {
            overlay.addWidget(modal);
        }
        if (!state.modalWindowClosing) {
            restoreSearchFocus(state, iconSearchField, assetSearchField, biomeSearchField, advancementSearchField, recipeSearchField, structureSearchField, blockSearchField, statSearchField, dimensionSearchField, lootTableSearchField, itemInventorySearchField, entityVariantSearchField);
        }
    }

    private static int dimAlpha(TabletUiState state) {
        if (!QuestsAndStuffConfig.popupWindowAnimationsEnabled()) {
            return 140;
        }
        float amount = SourceOriginRevealWidget.windowOpenAmount(state.modalWindowAnimationStartMs, !state.modalWindowClosing);
        return Math.round(140 * amount);
    }

    private static SourceOriginRevealWidget.SourceRect sourceRect(TabletUiState state) {
        if (!state.modalWindowAnimationHasSource) {
            return null;
        }
        return new SourceOriginRevealWidget.SourceRect(
                state.modalWindowAnimationSourceX,
                state.modalWindowAnimationSourceY,
                state.modalWindowAnimationSourceW,
                state.modalWindowAnimationSourceH
        );
    }

    private static void restoreSearchFocus(
            TabletUiState state,
            TextFieldWidget iconSearchField,
            TextFieldWidget assetSearchField,
            TextFieldWidget biomeSearchField,
            TextFieldWidget advancementSearchField,
            TextFieldWidget recipeSearchField,
            TextFieldWidget structureSearchField,
            TextFieldWidget blockSearchField,
            TextFieldWidget statSearchField,
            TextFieldWidget dimensionSearchField,
            TextFieldWidget lootTableSearchField,
            TextFieldWidget itemInventorySearchField,
            TextFieldWidget entityVariantSearchField
    ) {
        if (iconSearchField != null && state.iconSearchFocused) {
            iconSearchField.setFocus(true);
        }
        if (assetSearchField != null && state.assetSearchFocused) {
            assetSearchField.setFocus(true);
        }
        if (biomeSearchField != null && state.biomeSearchFocused) {
            biomeSearchField.setFocus(true);
        }
        if (advancementSearchField != null && state.advancementSearchFocused) {
            advancementSearchField.setFocus(true);
        }
        if (recipeSearchField != null && state.recipeSearchFocused) {
            recipeSearchField.setFocus(true);
        }
        if (structureSearchField != null && state.structureSearchFocused) {
            structureSearchField.setFocus(true);
        }
        if (blockSearchField != null && state.blockSearchFocused) {
            blockSearchField.setFocus(true);
        }
        if (statSearchField != null && state.statSearchFocused) {
            statSearchField.setFocus(true);
        }
        if (dimensionSearchField != null && state.dimensionSearchFocused) {
            dimensionSearchField.setFocus(true);
        }
        if (lootTableSearchField != null && state.lootTableSearchFocused) {
            lootTableSearchField.setFocus(true);
        }
        if (itemInventorySearchField != null && state.itemInventorySearchFocused) {
            itemInventorySearchField.setFocus(true);
        }
        if (entityVariantSearchField != null && state.entityVariantSearchFocused) {
            entityVariantSearchField.setFocus(true);
        }
    }
}
