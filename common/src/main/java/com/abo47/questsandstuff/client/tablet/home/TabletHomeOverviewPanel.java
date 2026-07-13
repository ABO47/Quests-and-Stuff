package com.abo47.questsandstuff.client.tablet.home;

import com.abo47.questsandstuff.client.tablet.app.AppDescriptor;
import com.abo47.questsandstuff.client.tablet.app.TabletAppRegistry;
import com.abo47.questsandstuff.client.tablet.bootstrap.TabletLifecycle;
import com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;

import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.ROOT_PAD_X;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.ROOT_PAD_Y;

final class TabletHomeOverviewPanel extends WidgetGroup {
    private static final int HOME_BTN_SIZE = 10;
    private static final int APP_ICON_SIZE = 48;
    private final WidgetGroup innerContainer;

    private final ButtonWidget homeBtn;

    WidgetGroup getInnerContainer() {
        return innerContainer;
    }

    ButtonWidget getHomeBtn() {
        return homeBtn;
    }

    TabletHomeOverviewPanel(int x, int y, int width, int height) {
        super(x, y, width, height);
        int innerW = Math.max(1, width - ROOT_PAD_X * 2);
        int innerH = Math.max(1, height - ROOT_PAD_Y * 2);
        int innerX = ROOT_PAD_X;
        int innerY = ROOT_PAD_Y;

        innerContainer = new WidgetGroup(innerX, innerY, innerW, innerH) {
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
        homeBtn = new ButtonWidget(btnX, btnY, HOME_BTN_SIZE, HOME_BTN_SIZE,
                SurfaceFactory.bordered(TabletColors.SURFACE_PANEL_ALT, TabletColors.subtleBorder()),
                cd -> TabletLifecycle.closeTabletUi(null, false, "home_button"));
        homeBtn.setClientSideWidget();
        homeBtn.setHoverTexture(GlowShaderHelper.hoverGlow());
        homeBtn.setClickedTexture(SurfaceFactory.bordered(TabletColors.SURFACE_PANEL_ALT, TabletColors.BORDER_ACCENT));
        addWidget(homeBtn);

        List<AppDescriptor> apps = TabletAppRegistry.all().values().stream()
                .filter(a -> !"home".equals(a.id()))
                .sorted(Comparator.comparingInt(TabletHomeOverviewPanel::appAnchorOrder)
                        .thenComparing(AppDescriptor::id))
                .toList();
        int appCount = apps.size();
        int iconPairW = APP_ICON_SIZE * appCount + Math.max(0, appCount - 1) * 16;
        int iconsStartX = innerX + (innerW - iconPairW) / 2;
        int iconY = innerY + (innerH - APP_ICON_SIZE) / 2;

        int col = 0;
        for (AppDescriptor app : apps) {
            int ix = iconsStartX + col * (APP_ICON_SIZE + 16);
            ResourceTexture tex = new ResourceTexture(app.iconTexture());
            ButtonWidget appBtn = new ButtonWidget(ix, iconY, APP_ICON_SIZE, APP_ICON_SIZE,
                    tex,
                    cd -> TabletLifecycle.openApp(app.id()));
            appBtn.setClientSideWidget();
            appBtn.setHoverTexture(GlowShaderHelper.hoverGlow());
            appBtn.setClickedTexture(SurfaceFactory.group(tex, SurfaceFactory.fill(TabletColors.pressedFill(TabletColors.INTERACTIVE))));
            addWidget(appBtn);
            col++;
        }
    }

    private static int appAnchorOrder(AppDescriptor app) {
        if ("QUESTS".equals(app.id())) return 0;
        if ("TEAMS".equals(app.id())) return Integer.MAX_VALUE;
        return 1;
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        TabletPanelChrome.drawRootChromeNoShadow(graphics, this);
        drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
    }
}
