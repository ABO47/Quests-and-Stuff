package com.abo47.questsandstuff.client.tablet.controls;

import com.abo47.questsandstuff.client.tablet.icons.IconAtlas;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.CycleButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;

public final class TabletCycleButton {
    private static final int DEFAULT_ICON_SIZE = 12;

    private TabletCycleButton() {
    }

    public static Widget addIconModeButton(
            WidgetGroup parent,
            int x,
            int y,
            int width,
            int height,
            int range,
            IntSupplier indexSupplier,
            Int2ObjectFunction<String> iconSupplier,
            Component[] tooltip,
            IntConsumer directionConsumer
    ) {
        parent.addWidget(SurfaceFactory.panel(x, y, width, height, withAlpha(TabletColors.INTERACTIVE, 120), TabletColors.BORDER_ACCENT));
        int iconSize = Math.min(DEFAULT_ICON_SIZE, Math.max(8, Math.min(width - 4, height - 4)));
        int iconX = x + (width - iconSize) / 2;
        int iconY = y + (height - iconSize) / 2;
        CyclingIconWidget icon = new CyclingIconWidget(iconX, iconY, iconSize, iconSize, indexSupplier, iconSupplier);
        parent.addWidget(icon);
        DirectionalCycleButton button = new DirectionalCycleButton(x, y, width, height, Math.max(1, range), indexSupplier, directionConsumer);
        button.setClientSideWidget();
        button.setHoverTexture(SurfaceFactory.bordered(withAlpha(TabletColors.INTERACTIVE, 66), TabletColors.BORDER_ACCENT));
        if (tooltip != null && tooltip.length > 0) {
            button.setHoverTooltips(tooltip);
        }
        parent.addWidget(button);
        return button;
    }

    private static final class DirectionalCycleButton extends CycleButtonWidget {
        private final IntSupplier currentIndex;
        private final IntConsumer directionConsumer;

        private DirectionalCycleButton(
                int x,
                int y,
                int width,
                int height,
                int range,
                IntSupplier currentIndex,
                IntConsumer directionConsumer
        ) {
            super(x, y, width, height, range, ignored -> SurfaceFactory.transparent(), ignored -> {
            });
            this.currentIndex = currentIndex;
            this.directionConsumer = directionConsumer;
            setIndex(safeIndex(currentIndex, range));
            setIndexSupplier(() -> safeIndex(currentIndex, range));
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!isMouseOverElement(mouseX, mouseY)) {
                return false;
            }
            int direction = button == 1 ? -1 : 1;
            if (directionConsumer != null) {
                directionConsumer.accept(direction);
            }
            setIndex(safeIndex(currentIndex, range));
            playButtonClickSound();
            return true;
        }
    }

    private static final class CyclingIconWidget extends ImageWidget {
        private final IntSupplier currentIndex;
        private final Int2ObjectFunction<String> iconSupplier;

        private CyclingIconWidget(int x, int y, int width, int height, IntSupplier currentIndex, Int2ObjectFunction<String> iconSupplier) {
            super(x, y, width, height, SurfaceFactory.transparent());
            this.currentIndex = currentIndex;
            this.iconSupplier = iconSupplier;
        }

        @Override
        public void drawInBackground(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            IGuiTexture texture = IconAtlas.iconTexture(iconSupplier.get(safeIndex(currentIndex, Integer.MAX_VALUE)));
            if (texture != null) {
                texture.draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
            }
        }
    }

    private static int safeIndex(IntSupplier supplier, int range) {
        int safeRange = Math.max(1, range);
        return Math.floorMod(supplier == null ? 0 : supplier.getAsInt(), safeRange);
    }
}
