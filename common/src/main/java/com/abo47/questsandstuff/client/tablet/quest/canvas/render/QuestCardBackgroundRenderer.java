package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasViewportScissor;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.render.Surfaces;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.sync.QuestSyncKeys;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

import static com.abo47.questsandstuff.client.tablet.theme.render.Surfaces.withAlpha;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.chapterBackgroundTexture;

public final class QuestCardBackgroundRenderer {
    public static final ResourceLocation DEFAULT_QUEST_BACKGROUND_TEXTURE = ResourceLocation.tryBuild("questsandstuff", "textures/gui/quest_backgrounds/default_quest_bg.png");
    public static final ResourceTexture EXCLUSIVE_CHOICE_TEXTURE = new ResourceTexture(new ResourceLocation("questsandstuff", "textures/gui/other/exclusive_choice.png"));

    private QuestCardBackgroundRenderer() {
    }

    public static boolean renderWidgetBackground(WidgetGroup parent, int x, int y, int width, int height, CompoundTag tag, float progress) {
        String background = normalizedBackground(tag);
        if (usesDefaultBackground(background)) {
            parent.addWidget(new ImageWidget(x, y, width, height, defaultTexture(defaultTint(tag, 255))));
            return false;
        }
        IGuiTexture texture = chapterBackgroundTexture(background, grayscale(tag));
        if (texture == null) {
            parent.addWidget(new ImageWidget(x, y, width, height, defaultTexture(defaultTint(tag, 255))));
            return false;
        }
        parent.addWidget(new ImageWidget(x, y, width, height, texture));
        addWidgetFilter(parent, x, y, width, height, statusFilter(tag, 255));
        if (shouldShowProgressFill(tag, progress)) {
            int fillW = progressFillWidth(width, progress);
            addWidgetFilter(parent, x, y, fillW, height, progressFillColor(54));
        }
        return true;
    }

    public static void renderWidgetProgressFill(WidgetGroup parent, int x, int y, int width, int height, float progress) {
        int fillW = progressFillWidth(width, progress);
        ResourceTexture fillTexture = defaultTexture(progressFillColor(255));
        parent.addWidget(new WidgetGroup(x, y, width, height) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                CanvasViewportScissor.draw(graphics, getPositionX(), getPositionY(), fillW, getSizeHeight(),
                        () -> fillTexture.draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight()));
            }
        });
    }

    public static void drawTagBackground(GuiGraphics graphics, CompoundTag tag, int x, int y, int width, int height, int mouseX, int mouseY, int alpha) {
        String background = normalizedBackground(tag);
        if (usesDefaultBackground(background)) {
            drawTextureAlpha(graphics, defaultTexture(defaultTint(tag, 255)), mouseX, mouseY, x, y, width, height, alpha);
            return;
        }
        IGuiTexture texture = chapterBackgroundTexture(background, grayscale(tag));
        if (texture == null) {
            drawTextureAlpha(graphics, defaultTexture(defaultTint(tag, 255)), mouseX, mouseY, x, y, width, height, alpha);
            return;
        }
        drawTextureAlpha(graphics, texture, mouseX, mouseY, x, y, width, height, alpha);
        int filter = statusFilter(tag, alpha);
        if ((filter >>> 24) != 0) {
            Surfaces.fill(filter).draw(graphics, 0, 0, x, y, width, height);
        }
    }

    public static void drawDisplayBackground(GuiGraphics graphics, QuestDisplay display, boolean gated, int x, int y, int width, int height, int mouseX, int mouseY, int alpha) {
        QuestDisplay safeDisplay = display == null ? QuestDisplay.DEFAULT : display;
        String background = QuestDisplay.normalizeQuestBackground(safeDisplay.questBackground());
        if (usesDefaultBackground(background)) {
            defaultTexture(defaultTint(safeDisplay, gated, alpha)).draw(graphics, mouseX, mouseY, x, y, width, height);
            return;
        }
        IGuiTexture texture = chapterBackgroundTexture(background, safeDisplay.questBackgroundGrayscale());
        if (texture == null) {
            defaultTexture(defaultTint(safeDisplay, gated, alpha)).draw(graphics, mouseX, mouseY, x, y, width, height);
            return;
        }
        drawTextureAlpha(graphics, texture, mouseX, mouseY, x, y, width, height, alpha);
    }

    public static String normalizedBackground(CompoundTag tag) {
        return QuestDisplay.normalizeQuestBackground(tag == null ? null : tag.getString(QuestSyncKeys.Quest.QUEST_BACKGROUND));
    }

    public static boolean grayscale(CompoundTag tag) {
        return tag != null && tag.getBoolean(QuestSyncKeys.Quest.QUEST_BACKGROUND_GRAYSCALE);
    }

    public static boolean usesDefaultBackground(String background) {
        return QuestDisplay.DEFAULT_QUEST_BACKGROUND.equals(QuestDisplay.normalizeQuestBackground(background));
    }

    public static int defaultTint(CompoundTag tag, int alpha) {
        if (ClientQuestCache.questLockedPreview(tag)) {
            return withAlpha(ModColors.TEXT_SECONDARY, alpha);
        }
        if (tag != null && tag.getBoolean(QuestSyncKeys.Quest.CLAIMED)) {
            return withAlpha(ModColors.WARNING, alpha);
        }
        if (tag != null && tag.getBoolean(QuestSyncKeys.Quest.COMPLETED)) {
            return withAlpha(ModColors.SUCCESS, alpha);
        }
        return tag != null && tag.getBoolean(QuestSyncKeys.Quest.UNLOCKED)
                ? withAlpha(ModColors.INTERACTIVE, alpha)
                : withAlpha(ModColors.TEXT_SECONDARY, alpha);
    }

    public static int defaultTint(QuestDisplay display, boolean gated, int alpha) {
        QuestDisplay safeDisplay = display == null ? QuestDisplay.DEFAULT : display;
        return withAlpha(safeDisplay.visualHidden() || gated ? ModColors.TEXT_SECONDARY : ModColors.INTERACTIVE, alpha);
    }

    public static int statusFilter(CompoundTag tag, int alpha) {
        if (ClientQuestCache.questLockedPreview(tag)) {
            return scaledAlpha(ModColors.SURFACE_BASE, 138, alpha);
        }
        if (tag != null && tag.getBoolean(QuestSyncKeys.Quest.CLAIMED)) {
            return scaledAlpha(ModColors.WARNING, 94, alpha);
        }
        if (tag != null && tag.getBoolean(QuestSyncKeys.Quest.COMPLETED)) {
            return scaledAlpha(ModColors.SUCCESS, 82, alpha);
        }
        return 0x00000000;
    }

    public static float questProgress(CompoundTag tag) {
        if (tag == null) {
            return 0.0f;
        }
        boolean hasTasks = !tag.getCompound(QuestSyncKeys.Quest.TASKS).isEmpty();
        float progress = Math.max(0.0f, Math.min(1.0f, tag.getFloat(QuestSyncKeys.Quest.PROGRESS)));
        if (hasTasks && (tag.getBoolean(QuestSyncKeys.Quest.COMPLETED) || tag.getBoolean(QuestSyncKeys.Quest.CLAIMED))) {
            progress = 1.0f;
        }
        return progress;
    }

    public static int progressPercent(CompoundTag tag) {
        return Math.round(questProgress(tag) * 100.0f);
    }

    public static boolean shouldShowProgressFill(CompoundTag tag, float progress) {
        return tag != null
                && tag.getBoolean(QuestSyncKeys.Quest.UNLOCKED)
                && progress > 0.0f
                && progress < 1.0f
                && !tag.getBoolean(QuestSyncKeys.Quest.COMPLETED)
                && !tag.getBoolean(QuestSyncKeys.Quest.CLAIMED);
    }

    public static int progressFillWidth(int width, float progress) {
        return Math.max(1, Math.min(width, Math.round(width * Math.max(0.0f, Math.min(1.0f, progress)))));
    }

    public static int progressFillColor(int alpha) {
        return withAlpha(ModColors.SUCCESS, Math.max(0, Math.min(255, alpha)));
    }

    private static ResourceTexture defaultTexture(int tint) {
        return new ResourceTexture(DEFAULT_QUEST_BACKGROUND_TEXTURE).setColor(tint);
    }

    private static void addWidgetFilter(WidgetGroup parent, int x, int y, int width, int height, int color) {
        if (width <= 0 || height <= 0 || (color >>> 24) == 0) {
            return;
        }
        WidgetGroup rect = new WidgetGroup(x, y, width, height);
        rect.setBackground(Surfaces.fill(color));
        parent.addWidget(rect);
    }

    private static int scaledAlpha(int color, int baseAlpha, int alpha) {
        int safeAlpha = Math.max(0, Math.min(255, alpha));
        return withAlpha(color, Math.max(0, Math.min(255, Math.round(baseAlpha * (safeAlpha / 255.0f)))));
    }

    private static void drawTextureAlpha(GuiGraphics graphics, IGuiTexture texture, int mouseX, int mouseY, int x, int y, int width, int height, int alpha) {
        int safeAlpha = Math.max(0, Math.min(255, alpha));
        if (texture == null || safeAlpha <= 0 || width <= 0 || height <= 0) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, safeAlpha / 255.0f);
        try {
            texture.draw(graphics, mouseX, mouseY, x, y, width, height);
        } finally {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }
}
