package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.quest.sound.QuestSoundPreview;
import com.abo47.questsandstuff.client.tablet.icons.IconAtlas;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.function.IntSupplier;

final class SoundPreviewPlayerWidget extends WidgetGroup {
    private static final int MIN_ICON_SIZE = 22;
    private static final int ICON_PAD = 8;

    private final String soundId;
    private final IntSupplier volumePercent;

    SoundPreviewPlayerWidget(int x, int y, int width, String soundId) {
        this(x, y, width, soundId, () -> 100);
    }

    SoundPreviewPlayerWidget(int x, int y, int width, String soundId, IntSupplier volumePercent) {
        this(x, y, width, 34, soundId, volumePercent);
    }

    SoundPreviewPlayerWidget(int x, int y, int width, int height, String soundId, IntSupplier volumePercent) {
        super(x, y, width, height);
        this.soundId = soundId == null ? "" : soundId.trim();
        this.volumePercent = volumePercent == null ? () -> 100 : volumePercent;
        updateTooltip();
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (soundId.isBlank()) {
            return;
        }
        IGuiTexture skinBg = getBackgroundTexture();
        if (skinBg != null && !skinBg.equals(IGuiTexture.EMPTY)) {
            skinBg.draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
        }
        int x = getPositionX();
        int y = getPositionY();
        int w = getSizeWidth();
        int h = getSizeHeight();
        boolean playing = QuestSoundPreview.isPlaying(soundId);
        int iconSize = Math.max(MIN_ICON_SIZE, Math.min(w, h) - ICON_PAD);
        int iconX = x + (w - iconSize) / 2;
        int iconY = y + (h - iconSize) / 2;
        var icon = IconAtlas.iconTexture(playing ? "pause" : "play");
        if (icon != null) {
            icon.draw(graphics, mouseX, mouseY, iconX, iconY, iconSize, iconSize);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || soundId.isBlank() || !isMouseOverElement(mouseX, mouseY)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        QuestSoundPreview.toggle(soundId, volumePercent.getAsInt());
        updateTooltip();
        return true;
    }

    private void updateTooltip() {
        setHoverTooltips(new Component[]{
                Component.literal(TabletModalPanel.tr(QuestSoundPreview.isPlaying(soundId)
                        ? "ui.questsandstuff.common.pause"
                        : "ui.questsandstuff.common.play")),
                Component.literal(soundId)
        });
    }
}
