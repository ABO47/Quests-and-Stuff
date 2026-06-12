package com.abo47.questsandstuff.client.tablet.shell;

import com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CANVAS_W;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_W;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.PANEL_GAP;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ROOT_PAD_X;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ROOT_PAD_Y;

final class TabletHomeOverviewPanel extends WidgetGroup {
    private static final int HOME_BTN_SIZE = 10;
    private static final int APP_ICON_SIZE = 48;

    TabletHomeOverviewPanel(int x, int y, int width, int height) {
        super(x, y, width, height);
        int innerW = CHAPTER_W + PANEL_GAP + CANVAS_W;
        int innerH = height - ROOT_PAD_Y * 2;
        int innerX = ROOT_PAD_X;
        int innerY = ROOT_PAD_Y;

        WidgetGroup innerContainer = new WidgetGroup(innerX, innerY, innerW, innerH) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                TabletPanelChrome.drawPanelChrome(graphics, this);
                drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
                TabletPanelChrome.drawPanelOutline(graphics, this);
            }
        };
        addWidget(innerContainer);

        int gutterX = innerX + innerW;
        int btnX = gutterX + (ROOT_PAD_X - HOME_BTN_SIZE) / 2;
        int btnY = innerY + (innerH - HOME_BTN_SIZE) / 2;
        ButtonWidget homeBtn = new ButtonWidget(btnX, btnY, HOME_BTN_SIZE, HOME_BTN_SIZE,
                Surfaces.bordered(ModColors.SURFACE_PANEL_ALT, ModColors.subtleBorder()),
                cd -> TabletClientHooks.closeTabletUi(null, false, "home_button"));
        homeBtn.setClientSideWidget();
        homeBtn.setHoverTexture(Surfaces.bordered(ModColors.elevatedSurface(), ModColors.focusBorder()));
        homeBtn.setClickedTexture(Surfaces.bordered(ModColors.SURFACE_PANEL_ALT, ModColors.BORDER_ACCENT));
        addWidget(homeBtn);

        ResourceTexture questTexture = new ResourceTexture(new ResourceLocation("questsandstuff", "textures/gui/questsandstuff.png"));
        int iconX = innerX + (innerW - APP_ICON_SIZE) / 2;
        int iconY = innerY + (innerH - APP_ICON_SIZE) / 2;
        ButtonWidget questApp = new ButtonWidget(iconX, iconY, APP_ICON_SIZE, APP_ICON_SIZE,
                questTexture,
                cd -> TabletClientHooks.openQuestsUiFromCurrentScreen());
        questApp.setClientSideWidget();
        questApp.setHoverTexture(Surfaces.group(questTexture, Surfaces.fill(ModColors.hoverFill(ModColors.INTERACTIVE))));
        questApp.setClickedTexture(Surfaces.group(questTexture, Surfaces.fill(ModColors.pressedFill(ModColors.INTERACTIVE))));
        addWidget(questApp);
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        TabletPanelChrome.drawRootChromeNoShadow(graphics, this);
        drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
    }
}
