package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.modal.actions.AssetPickerApplyActions;
import com.abo47.questsandstuff.client.tablet.quest.canvas.actions.CanvasEntityPickerActions;
import com.abo47.questsandstuff.client.tablet.quest.canvas.actions.CanvasModelPickerActions;
import com.abo47.questsandstuff.client.tablet.model.ModelAssetPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.modal.actions.ColorPickerApplyActions;
import com.abo47.questsandstuff.client.tablet.modal.panel.ModalPanelRouter;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.icons.FluidIconCodec;
import com.abo47.questsandstuff.client.tablet.icons.ItemStackIconCodec;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;

import static com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions.closeAll;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.closeIconButton;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;

public final class TabletModalPanel {
    private TabletModalPanel() {
    }

    public static void rebuildChapterModal(WidgetGroup overlay, TabletUiState state, Player player, Runnable refresh) {
        ModalPanelRouter.rebuildChapterModal(overlay, state, player, refresh);
    }

    public static void addModalClose(WidgetGroup modal, int x, int y, int size, TabletUiState state, Runnable refresh) {
        int closeX = x + 1;
        int closeY = Math.max(0, y - 3);
        Runnable close = () -> {
            QuestsAndStuffMod.debugLog("[QnS:UI] modal close click");
            state.iconSearchFocused = false;
            closeAll(state);
            refresh.run();
        };
        modal.addWidget(closeIconButton(closeX, closeY, size, size, click -> close.run()));
    }

    public static boolean acceptPickerDoubleClick(TabletUiState state, String key) {
        String safeKey = key == null ? "" : key;
        long now = System.currentTimeMillis();
        boolean accepted = !safeKey.isBlank()
                && safeKey.equals(state.pickerLastClickKey)
                && now - state.pickerLastClickAtMs <= 350L;
        state.pickerLastClickKey = safeKey;
        state.pickerLastClickAtMs = now;
        return accepted;
    }

    static void runAssetBackgroundAction(Player player, TabletUiState state, String background) {
        AssetPickerApplyActions.run(player, state, background);
    }

    static boolean runCanvasEntityAction(Player player, TabletUiState state, String target, String pickedItem) {
        return CanvasEntityPickerActions.run(player, state, target, pickedItem);
    }

    static boolean runCanvasModelAction(TabletUiState state, String target, String pickedValue) {
        return CanvasModelPickerActions.run(state, target, pickedValue);
    }

    public static Component[] iconTooltip(String entry) {
        if (entry == null || entry.isBlank()) {
            return new Component[]{Component.translatable("ui.questsandstuff.icon.unknown").withStyle(ChatFormatting.RED)};
        }
        if (ItemStackIconCodec.isStackIcon(entry)) {
            Component[] tooltip = ItemStackIconCodec.tooltip(entry);
            if (tooltip.length > 0) {
                return tooltip;
            }
        }
        if (FluidIconCodec.isFluidIcon(entry)) {
            return FluidIconCodec.tooltip(entry);
        }
        if (entry.startsWith("#")) {
            return PickerTooltips.item(entry);
        }
        if (ModelAssetPreviewRenderer.isModelAsset(entry)) {
            return ModelAssetPreviewRenderer.modelTooltip(entry);
        }
        String entityId = EntityPreviewRenderer.entityId(entry);
        if (!entityId.isBlank()) {
            return new Component[]{
                    Component.literal(EntityPreviewRenderer.entityDisplayName(entityId)).withStyle(ChatFormatting.WHITE),
                    Component.literal(entityId).withStyle(ChatFormatting.DARK_GRAY)
            };
        }
        ResourceLocation id = ResourceLocation.tryParse(entry);
        if (id != null) {
            Item item = BuiltInRegistries.ITEM.get(id);
            if (item != null) {
                return PickerTooltips.item(entry);
            }
        }
        return new Component[]{Component.literal(entry).withStyle(ChatFormatting.GRAY)};
    }

    static String fileNameFromRelativePath(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return "";
        }
        int slash = relativePath.lastIndexOf('/');
        return slash >= 0 ? relativePath.substring(slash + 1) : relativePath;
    }

    static String parentRelativePath(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return "";
        }
        int slash = relativePath.lastIndexOf('/');
        return slash >= 0 ? relativePath.substring(0, slash) : "";
    }

    static int currentColorPickerValue(TabletUiState state, String target) {
        return ColorPickerApplyActions.currentValue(state, target);
    }

    static void applyColorPickerValue(Player player, TabletUiState state, String target, int color) {
        ColorPickerApplyActions.apply(player, state, target, color);
    }

    static String tr(String key, Object... args) {
        return I18n.get(key, args);
    }

}
