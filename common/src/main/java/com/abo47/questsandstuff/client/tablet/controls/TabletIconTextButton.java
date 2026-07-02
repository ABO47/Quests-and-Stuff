package com.abo47.questsandstuff.client.tablet.controls;

import com.abo47.questsandstuff.client.tablet.icons.SmoothResourceTexture;
import com.abo47.questsandstuff.client.tablet.icons.UiIconAtlas;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.render.Surfaces;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public final class TabletIconTextButton extends ButtonWidget {
    private static final int MIN_ICON_SIZE = 8;
    private static final int DEFAULT_ICON_PAD = 4;

    private String iconName;
    private Component label;
    private Visuals visuals;
    private int iconSize;

    private TabletIconTextButton(int x, int y, int width, int height, String iconName, Component label, int iconSize, Visuals visuals, Consumer<ClickData> callback) {
        super(x, y, width, height, IGuiTexture.EMPTY, callback);
        this.iconName = iconName == null ? "" : iconName;
        this.label = label;
        this.iconSize = iconSize;
        this.visuals = visuals == null ? Visuals.defaultControl(ModColors.INTERACTIVE, ModColors.TEXT_PRIMARY) : visuals;
        setClientSideWidget();
        refreshTextures();
    }

    public static TabletIconTextButton icon(int x, int y, int width, int height, String iconName, Visuals visuals, Consumer<ClickData> callback) {
        return new TabletIconTextButton(x, y, width, height, iconName, null, 0, visuals, callback);
    }

    public static TabletIconTextButton iconText(int x, int y, int width, int height, String iconName, Component label, Visuals visuals, Consumer<ClickData> callback) {
        return new TabletIconTextButton(x, y, width, height, iconName, label, 0, visuals, callback);
    }

    public TabletIconTextButton iconSize(int iconSize) {
        this.iconSize = Math.max(MIN_ICON_SIZE, iconSize);
        refreshTextures();
        return this;
    }

    public TabletIconTextButton label(Component label) {
        this.label = label;
        refreshTextures();
        return this;
    }

    public TabletIconTextButton visuals(Visuals visuals) {
        this.visuals = visuals == null ? this.visuals : visuals;
        refreshTextures();
        return this;
    }

    public TabletIconTextButton tooltips(Component[] tooltips) {
        if (tooltips != null && tooltips.length > 0) {
            setHoverTooltips(tooltips);
        }
        return this;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (handled) {
            isClicked = false;
        }
        return handled;
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = isMouseOverElement(mouseX, mouseY);
        boolean pressed = isClicked && hovered;
        State state = pressed ? visuals.pressed() : (hovered ? visuals.hover() : visuals.idle());

        IGuiTexture bgOverride = getBackgroundTexture();
        if (bgOverride != null && !bgOverride.equals(IGuiTexture.EMPTY)) {
            bgOverride.draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
        } else {
            Surfaces.bordered(state.fillColor(), state.borderColor()).draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
            if (pressed) {
                Surfaces.bordered(visuals.pressed().fillColor(), visuals.pressed().borderColor()).draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
            }
        }

        IGuiTexture content = buildContent(state);
        if (content != null) {
            content.draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
        }
    }

    private IGuiTexture buildContent(State state) {
        IGuiTexture icon = iconTexture(state.iconColor());
        if (label == null) {
            return icon;
        }
        TextTexture text = TabletTextTextures.literalTexture(label.getString(), Math.max(1, getSizeWidth() - 4), state.textColor(), TextTexture.TextType.HIDE);
        text.transform(0, hasIcon() ? Math.max(4, getSizeHeight() / 4.0f) : 0);
        return Surfaces.group(icon, text);
    }

    @Override
    protected void onSizeUpdate() {
        super.onSizeUpdate();
        if (visuals != null) {
            refreshTextures();
        }
    }

    private void refreshTextures() {
        super.setBackground(IGuiTexture.EMPTY);
    }

    private IGuiTexture iconTexture(int color) {
        ResourceLocation id = resolveIcon(iconName);
        if (id == null) {
            return IGuiTexture.EMPTY;
        }
        int size = iconSize > 0 ? iconSize : Math.min(Math.max(MIN_ICON_SIZE, Math.min(getSizeWidth(), getSizeHeight()) - DEFAULT_ICON_PAD), Math.max(MIN_ICON_SIZE, getSizeHeight() - 2));
        float scale = Math.max(0.1f, size / (float) Math.max(1, Math.min(getSizeWidth(), getSizeHeight())));
        SmoothResourceTexture texture = new SmoothResourceTexture(id);
        texture.setColor(color);
        texture.scale(scale);
        if (label != null) {
            texture.transform(0, -Math.max(3, getSizeHeight() / 5.0f));
        }
        return texture;
    }

    private boolean hasIcon() {
        return resolveIcon(iconName) != null;
    }

    private static ResourceLocation resolveIcon(String iconName) {
        ResourceLocation id = UiIconAtlas.icon(iconName);
        if (id == null && iconName != null && !iconName.isBlank()) {
            id = UiIconAtlas.icon("context_" + iconName);
        }
        if (id == null) {
            id = UiIconAtlas.icon("text");
        }
        return id;
    }

    public record State(int fillColor, int borderColor, int iconColor, int textColor) {
        public static State of(int fillColor, int borderColor, int iconColor) {
            return new State(fillColor, borderColor, iconColor, iconColor);
        }
    }

    public record Visuals(State idle, State hover, State pressed) {
        public static Visuals defaultControl(int accentColor, int iconColor) {
            return new Visuals(
                    State.of(ModColors.SURFACE_PANEL_ALT, ModColors.BORDER_BASE, iconColor),
                    State.of(ModColors.hoverFill(accentColor), ModColors.BORDER_ACCENT, iconColor),
                    State.of(ModColors.pressedFill(accentColor), accentColor, iconColor)
            );
        }
    }
}
