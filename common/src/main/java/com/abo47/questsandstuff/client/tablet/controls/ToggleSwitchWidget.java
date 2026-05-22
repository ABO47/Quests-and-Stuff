package com.abo47.questsandstuff.client.tablet.controls;

import com.abo47.questsandstuff.client.tablet.animation.UiAnimationProgress;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class ToggleSwitchWidget extends WidgetGroup {
    public static final int DEFAULT_WIDTH = 34;
    public static final int DEFAULT_HEIGHT = 16;
    private static final long ANIMATION_MS = 170L;
    private static final Map<String, Motion> MOTIONS = new HashMap<>();

    private final String animationKey;
    private final BooleanSupplier valueSupplier;
    private final Consumer<Boolean> valueConsumer;
    private final BooleanSupplier enabledSupplier;
    private final Runnable refresh;
    private Motion localMotion;

    public ToggleSwitchWidget(
            int x,
            int y,
            int width,
            int height,
            BooleanSupplier valueSupplier,
            Consumer<Boolean> valueConsumer,
            Runnable refresh,
            Component[] tooltips
    ) {
        this("", x, y, width, height, valueSupplier, valueConsumer, () -> true, refresh, tooltips);
    }

    public ToggleSwitchWidget(
            String animationKey,
            int x,
            int y,
            int width,
            int height,
            BooleanSupplier valueSupplier,
            Consumer<Boolean> valueConsumer,
            Runnable refresh,
            Component[] tooltips
    ) {
        this(animationKey, x, y, width, height, valueSupplier, valueConsumer, () -> true, refresh, tooltips);
    }

    public ToggleSwitchWidget(
            int x,
            int y,
            int width,
            int height,
            BooleanSupplier valueSupplier,
            Consumer<Boolean> valueConsumer,
            BooleanSupplier enabledSupplier,
            Runnable refresh,
            Component[] tooltips
    ) {
        this("", x, y, width, height, valueSupplier, valueConsumer, enabledSupplier, refresh, tooltips);
    }

    public ToggleSwitchWidget(
            String animationKey,
            int x,
            int y,
            int width,
            int height,
            BooleanSupplier valueSupplier,
            Consumer<Boolean> valueConsumer,
            BooleanSupplier enabledSupplier,
            Runnable refresh,
            Component[] tooltips
    ) {
        super(x, y, width, height);
        this.animationKey = animationKey == null ? "" : animationKey;
        this.valueSupplier = valueSupplier == null ? () -> false : valueSupplier;
        this.valueConsumer = valueConsumer == null ? value -> {
        } : valueConsumer;
        this.enabledSupplier = enabledSupplier == null ? () -> true : enabledSupplier;
        this.refresh = refresh == null ? () -> {
        } : refresh;
        if (tooltips != null) {
            setHoverTooltips(tooltips);
        }
    }

    public static void beginAnimation(String key, boolean from, boolean to) {
        if (key == null || key.isBlank() || from == to) {
            return;
        }
        MOTIONS.put(key, new Motion(from, to, System.currentTimeMillis()));
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = getPositionX();
        int y = getPositionY();
        int width = getSizeWidth();
        int height = getSizeHeight();
        boolean enabled = valueSupplier.getAsBoolean();
        boolean interactive = enabledSupplier.getAsBoolean();
        boolean hovered = interactive && isMouseOverElement(mouseX, mouseY);
        VisualState visual = visualState(enabled);
        float amount = visual.amount();
        float pulse = visual.pulse();

        int border = amount > 0.5f ? ModColors.SUCCESS : ModColors.BORDER_BASE;
        int track = TabletUiFactory.withAlpha(ModColors.SURFACE_BASE, hovered ? 230 : 190);
        int activeTrack = TabletUiFactory.withAlpha(ModColors.SUCCESS, 54 + Math.round(amount * (hovered ? 104 : 84)));
        int knob = amount > 0.5f ? ModColors.TEXT_PRIMARY : ModColors.TEXT_SECONDARY;
        if (!interactive) {
            border = TabletUiFactory.withAlpha(border, 120);
            track = TabletUiFactory.withAlpha(ModColors.SURFACE_PANEL_ALT, 130);
            activeTrack = TabletUiFactory.withAlpha(ModColors.SURFACE_PANEL_ALT, 90);
            knob = TabletUiFactory.withAlpha(knob, 165);
        }

        if (hovered || pulse > 0.0f) {
            int halo = TabletUiFactory.withAlpha(amount > 0.5f ? ModColors.SUCCESS : ModColors.INTERACTIVE, hovered ? 30 + Math.round(pulse * 24) : Math.round(pulse * 34));
            graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, halo);
        }
        graphics.fill(x, y, x + width, y + height, border);
        graphics.fill(x + 1, y + 1, x + Math.max(2, width - 1), y + Math.max(2, height - 1), track);
        int activeW = Math.round((width - 2) * amount);
        if (activeW > 2) {
            graphics.fill(x + 1, y + 1, x + 1 + activeW, y + Math.max(2, height - 1), activeTrack);
        }

        int knobSize = Math.max(8, height - 4 + Math.round(pulse * 2.0f));
        int knobTravel = Math.max(0, width - knobSize - 4);
        int knobX = x + 2 + Math.round(knobTravel * amount);
        int knobY = y + Math.max(2, (height - knobSize) / 2);
        graphics.fill(knobX + 1, knobY + 1, knobX + knobSize + 1, knobY + knobSize + 1, TabletUiFactory.withAlpha(ModColors.SURFACE_BASE, 100));
        graphics.fill(knobX, knobY, knobX + knobSize, knobY + knobSize, knob);
        graphics.fill(knobX + 2, knobY + 2, knobX + knobSize - 2, knobY + 3, TabletUiFactory.withAlpha(ModColors.TEXT_PRIMARY, amount > 0.5f ? 90 : 48));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !isMouseOverElement(mouseX, mouseY)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (!enabledSupplier.getAsBoolean()) {
            return true;
        }
        boolean from = valueSupplier.getAsBoolean();
        boolean to = !from;
        startMotion(from, to);
        valueConsumer.accept(to);
        refresh.run();
        return true;
    }

    private void startMotion(boolean from, boolean to) {
        if (from == to) {
            return;
        }
        if (animationKey.isBlank()) {
            localMotion = new Motion(from, to, System.currentTimeMillis());
            return;
        }
        beginAnimation(animationKey, from, to);
    }

    private VisualState visualState(boolean target) {
        Motion motion = motion(target);
        if (motion == null) {
            return new VisualState(target ? 1.0f : 0.0f, 0.0f);
        }
        long now = System.currentTimeMillis();
        if (!UiAnimationProgress.running(motion.startMs(), ANIMATION_MS, now)) {
            if (!animationKey.isBlank()) {
                MOTIONS.remove(animationKey);
            } else {
                localMotion = null;
            }
            return new VisualState(target ? 1.0f : 0.0f, 0.0f);
        }
        float progress = UiAnimationProgress.easedProgress(motion.startMs(), ANIMATION_MS, now);
        float amount = UiAnimationProgress.interpolate(motion.from() ? 1.0f : 0.0f, motion.to() ? 1.0f : 0.0f, progress);
        float pulse = (float) Math.sin(progress * Math.PI);
        return new VisualState(amount, pulse);
    }

    private Motion motion(boolean target) {
        Motion motion = animationKey.isBlank() ? localMotion : MOTIONS.get(animationKey);
        if (motion == null) {
            return null;
        }
        return motion.to() == target ? motion : null;
    }

    private record Motion(boolean from, boolean to, long startMs) {
    }

    private record VisualState(float amount, float pulse) {
    }
}
