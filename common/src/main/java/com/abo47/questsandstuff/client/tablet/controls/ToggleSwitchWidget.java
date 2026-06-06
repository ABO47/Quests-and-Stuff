package com.abo47.questsandstuff.client.tablet.controls;

import com.abo47.questsandstuff.client.tablet.animation.UiAnimationProgress;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.SwitchWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

public final class ToggleSwitchWidget extends SwitchWidget {
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
    private boolean lastInteractive = true;

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
        super(x, y, width, height, null);
        this.animationKey = animationKey == null ? "" : animationKey;
        this.valueSupplier = valueSupplier == null ? () -> false : valueSupplier;
        this.valueConsumer = valueConsumer == null ? value -> {
        } : valueConsumer;
        this.enabledSupplier = enabledSupplier == null ? () -> true : enabledSupplier;
        this.refresh = refresh == null ? () -> {
        } : refresh;
        setClientSideWidget();
        setSupplier(this.valueSupplier::getAsBoolean);
        setOnPressCallback(this::handleToggle);
        setPressed(this.valueSupplier.getAsBoolean());
        lastInteractive = this.enabledSupplier.getAsBoolean();
        refreshTextures();
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
    protected void onSizeUpdate() {
        super.onSizeUpdate();
        refreshTextures();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        boolean interactive = enabledSupplier.getAsBoolean();
        if (interactive != lastInteractive) {
            lastInteractive = interactive;
            refreshTextures();
        }
        setActive(interactive);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !isMouseOverElement(mouseX, mouseY)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (!enabledSupplier.getAsBoolean()) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleToggle(ClickData clickData, Boolean pressed) {
        if (!enabledSupplier.getAsBoolean()) {
            setPressed(valueSupplier.getAsBoolean());
            return;
        }
        boolean from = valueSupplier.getAsBoolean();
        boolean to = Boolean.TRUE.equals(pressed);
        startMotion(from, to);
        valueConsumer.accept(to);
        refresh.run();
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

    private void refreshTextures() {
        setBaseTexture(new SwitchVisualTexture(false));
        setPressedTexture(new SwitchVisualTexture(true));
        setHoverTexture(new SwitchHoverTexture());
    }

    private final class SwitchVisualTexture implements IGuiTexture {
        private final boolean target;

        private SwitchVisualTexture(boolean target) {
            this.target = target;
        }

        @Override
        public void draw(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
            boolean interactive = enabledSupplier.getAsBoolean();
            VisualState visual = visualState(target);
            float amount = visual.amount();
            float pulse = visual.pulse();
            int ix = Math.round(x);
            int iy = Math.round(y);
            int border = amount > 0.5f ? ModColors.SUCCESS : ModColors.BORDER_BASE;
            int track = withAlpha(ModColors.SURFACE_BASE, interactive ? 210 : 130);
            int activeTrack = withAlpha(ModColors.SUCCESS, interactive ? 70 + Math.round(amount * 78) : 70);
            int knob = amount > 0.5f ? ModColors.TEXT_PRIMARY : ModColors.TEXT_SECONDARY;
            if (!interactive) {
                border = withAlpha(border, 120);
                knob = withAlpha(knob, 165);
            }

            graphics.fill(ix, iy, ix + width, iy + height, border);
            graphics.fill(ix + 1, iy + 1, ix + Math.max(2, width - 1), iy + Math.max(2, height - 1), track);
            int activeW = Math.round((width - 2) * amount);
            if (activeW > 2) {
                graphics.fill(ix + 1, iy + 1, ix + 1 + activeW, iy + Math.max(2, height - 1), activeTrack);
            }

            int knobSize = Math.max(8, height - 4 + Math.round(pulse * 2.0f));
            int knobTravel = Math.max(0, width - knobSize - 4);
            int knobX = ix + 2 + Math.round(knobTravel * amount);
            int knobY = iy + Math.max(2, (height - knobSize) / 2);
            graphics.fill(knobX + 1, knobY + 1, knobX + knobSize + 1, knobY + knobSize + 1, withAlpha(ModColors.SURFACE_BASE, 100));
            graphics.fill(knobX, knobY, knobX + knobSize, knobY + knobSize, knob);
            graphics.fill(knobX + 2, knobY + 2, knobX + knobSize - 2, knobY + 3, withAlpha(ModColors.TEXT_PRIMARY, amount > 0.5f ? 90 : 48));
        }
    }

    private static final class SwitchHoverTexture implements IGuiTexture {
        @Override
        public void draw(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
            int ix = Math.round(x);
            int iy = Math.round(y);
            graphics.fill(ix - 1, iy - 1, ix + width + 1, iy + height + 1, withAlpha(ModColors.INTERACTIVE, 30));
        }
    }

    private record Motion(boolean from, boolean to, long startMs) {
    }

    private record VisualState(float amount, float pulse) {
    }
}
