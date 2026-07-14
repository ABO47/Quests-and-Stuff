package com.abo47.questsandstuff.client.tablet.chunkclaimer;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.ui.render.PlayerFaceTexture;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.chunkclaim.C2SChunkClaimActionPacket;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nonnull;

public class ChunkMapWidget extends Widget {
    private static final int TARGET_CELL = 16;
    private static final long RESAMPLE_MS = 500;

    private static final int CLAIMED_FILL = 0x994F8DF7;
    private static final int FORCE_FILL = 0x9966D38D;
    private static final int CLAIMED_EDGE = 0xFF6FA8FF;
    private static final int FORCE_EDGE = 0xFF66D38D;
    private static final int GRID_COLOR = 0x33546770;
    private static final int HOVER_FILL = 0x55EAF1F4;
    private static final int HOVER_EDGE = 0xFF65B7C8;

    private final TabletUiState state;

    private int[][] terrain;
    private int gridW = -1;
    private int gridH = -1;
    private int sub = 4;
    private int cachedCx = Integer.MAX_VALUE;
    private int cachedCz = Integer.MAX_VALUE;
    private long lastSample = 0;

    public ChunkMapWidget(int x, int y, int w, int h, TabletUiState state) {
        super(x, y, w, h);
        this.state = state;
        setClientSideWidget();
    }

    private ResourceLocation currentDimension() {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return null;
        }
        return player.level().dimension().location();
    }

    private int playerChunkX() {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return 0;
        }
        return player.blockPosition().getX() >> 4;
    }

    private int playerChunkZ() {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return 0;
        }
        return player.blockPosition().getZ() >> 4;
    }

    private void ensureTerrain(ClientLevel level, int w, int h, int cx, int cz) {
        int gw = Math.max(3, w / TARGET_CELL);
        int gh = Math.max(3, h / TARGET_CELL);
        int s = Math.max(1, Math.min(8, 180 / Math.max(gw, gh)));
        long now = System.currentTimeMillis();
        boolean needs = terrain == null || gw != gridW || gh != gridH || s != sub
                || cx != cachedCx || cz != cachedCz
                || (now - lastSample) > RESAMPLE_MS;
        if (!needs) {
            return;
        }
        gridW = gw;
        gridH = gh;
        sub = s;
        cachedCx = cx;
        cachedCz = cz;
        lastSample = now;
        int totalW = gw * sub;
        int totalH = gh * sub;
        terrain = new int[totalW][totalH];
        for (int tx = 0; tx < totalW; tx++) {
            int chunkDX = tx / sub;
            int subX = tx % sub;
            int worldChunkX = cx + (chunkDX - gw / 2);
            int blockX = worldChunkX * 16 + (int) ((subX + 0.5) * (16.0 / sub));
            for (int tz = 0; tz < totalH; tz++) {
                int chunkDZ = tz / sub;
                int subZ = tz % sub;
                int worldChunkZ = cz + (chunkDZ - gh / 2);
                int blockZ = worldChunkZ * 16 + (int) ((subZ + 0.5) * (16.0 / sub));
                terrain[tx][tz] = sampleColor(level, blockX, blockZ);
            }
        }
    }

    private int sampleColor(ClientLevel level, int worldX, int worldZ) {
        int topY = level.getHeight(Heightmap.Types.WORLD_SURFACE, worldX, worldZ);
        BlockPos pos = new BlockPos(worldX, Mth.clamp(topY - 1, 1, level.getMaxBuildHeight() - 1), worldZ);
        BlockState block = level.getBlockState(pos);
        FluidState fluid = level.getFluidState(pos);

        int rgb;
        if (!fluid.isEmpty()) {
            rgb = fluid.createLegacyBlock().getMapColor(level, pos).col;
        } else {
            MapColor mapColor = block.getMapColor(level, pos);
            rgb = mapColor != null ? mapColor.col : 0x6E6E6E;
        }

        float relief = Mth.clamp(0.78f + (topY - 64) / 110f, 0.6f, 1.25f);
        return shade(rgb, relief);
    }

    private static int shade(int rgb, float factor) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        r = (int) Mth.clamp(r * factor, 0, 255);
        g = (int) Mth.clamp(g * factor, 0, 255);
        b = (int) Mth.clamp(b * factor, 0, 255);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static int stateOf(ClientChunkClaimCache cache, ResourceLocation dim, int x, int z) {
        if (cache.isForceLoaded(dim, x, z)) {
            return 2;
        }
        if (cache.isClaimed(dim, x, z)) {
            return 1;
        }
        return 0;
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int w = getSizeWidth();
        int h = getSizeHeight();
        int baseX = getPositionX();
        int baseY = getPositionY();
        graphics.fill(baseX, baseY, baseX + w, baseY + h, TabletColors.SURFACE_BASE);

        ResourceLocation dim = currentDimension();
        var level = Minecraft.getInstance().level;
        if (dim == null || level == null) {
            return;
        }

        int cx = playerChunkX();
        int cz = playerChunkZ();
        ensureTerrain(level, w, h, cx, cz);

        int gw = gridW;
        int gh = gridH;
        int cell = ChunkMapGeometry.cellSize(w, h, gw, gh);
        int ox = ChunkMapGeometry.gridOriginX(w, cell, gw);
        int oy = ChunkMapGeometry.gridOriginY(h, cell, gh);

        int totalW = gw * sub;
        int totalH = gh * sub;
        for (int tx = 0; tx < totalW; tx++) {
            int px = baseX + ox + (tx * cell) / sub;
            int px2 = baseX + ox + ((tx + 1) * cell) / sub;
            for (int tz = 0; tz < totalH; tz++) {
                int py = baseY + oy + (tz * cell) / sub;
                int py2 = baseY + oy + ((tz + 1) * cell) / sub;
                graphics.fill(px, py, px2, py2, terrain[tx][tz]);
            }
        }

        int gridEndX = baseX + ox + gw * cell;
        int gridEndY = baseY + oy + gh * cell;
        for (int i = 0; i <= gw; i++) {
            int gx = baseX + ox + i * cell;
            graphics.fill(gx, baseY + oy, gx + 1, gridEndY, GRID_COLOR);
        }
        for (int j = 0; j <= gh; j++) {
            int gy = baseY + oy + j * cell;
            graphics.fill(baseX + ox, gy, gridEndX, gy + 1, GRID_COLOR);
        }

        int[][] states = new int[gw][gh];
        for (int dx = 0; dx < gw; dx++) {
            for (int dz = 0; dz < gh; dz++) {
                int chunkX = cx + (dx - gw / 2);
                int chunkZ = cz + (dz - gh / 2);
                states[dx][dz] = stateOf(ClientChunkClaimCache.INSTANCE, dim, chunkX, chunkZ);
            }
        }

        for (int dx = 0; dx < gw; dx++) {
            for (int dz = 0; dz < gh; dz++) {
                int s = states[dx][dz];
                if (s == 0) {
                    continue;
                }
                int px = baseX + ChunkMapGeometry.cellPixelX(ox, cell, gw, dx - gw / 2);
                int py = baseY + ChunkMapGeometry.cellPixelY(oy, cell, gh, dz - gh / 2);
                graphics.fill(px, py, px + cell + 1, py + cell + 1, s == 2 ? FORCE_FILL : CLAIMED_FILL);
            }
        }

        for (int dx = 0; dx < gw; dx++) {
            for (int dz = 0; dz < gh; dz++) {
                int s = states[dx][dz];
                if (s == 0) {
                    continue;
                }
                int px = baseX + ChunkMapGeometry.cellPixelX(ox, cell, gw, dx - gw / 2);
                int py = baseY + ChunkMapGeometry.cellPixelY(oy, cell, gh, dz - gh / 2);
                int edge = s == 2 ? FORCE_EDGE : CLAIMED_EDGE;
                if (dx == 0 || states[dx - 1][dz] != s) {
                    graphics.fill(px, py, px + 2, py + cell + 1, edge);
                }
                if (dx == gw - 1 || states[dx + 1][dz] != s) {
                    graphics.fill(px + cell - 1, py, px + cell + 1, py + cell + 1, edge);
                }
                if (dz == 0 || states[dx][dz - 1] != s) {
                    graphics.fill(px, py, px + cell + 1, py + 2, edge);
                }
                if (dz == gh - 1 || states[dx][dz + 1] != s) {
                    graphics.fill(px, py + cell - 1, px + cell + 1, py + cell + 1, edge);
                }
            }
        }

        int hoverDx = ChunkMapGeometry.deltaX((int) mouseX - baseX, ox, cell, gw);
        int hoverDz = ChunkMapGeometry.deltaZ((int) mouseY - baseY, oy, cell, gh);
        if (ChunkMapGeometry.inGridX(hoverDx, gw) && ChunkMapGeometry.inGridZ(hoverDz, gh)
                && isMouseOverElement(mouseX, mouseY)) {
            int hpx = baseX + ChunkMapGeometry.cellPixelX(ox, cell, gw, hoverDx - gw / 2);
            int hpy = baseY + ChunkMapGeometry.cellPixelY(oy, cell, gh, hoverDz - gh / 2);
            graphics.fill(hpx, hpy, hpx + cell + 1, hpy + cell + 1, HOVER_FILL);
            graphics.fill(hpx, hpy, hpx + cell + 1, hpy + 1, HOVER_EDGE);
            graphics.fill(hpx, hpy + cell, hpx + cell + 1, hpy + cell + 1, HOVER_EDGE);
            graphics.fill(hpx, hpy, hpx + 1, hpy + cell + 1, HOVER_EDGE);
            graphics.fill(hpx + cell, hpy, hpx + cell + 1, hpy + cell + 1, HOVER_EDGE);
        }

        drawPlayer(graphics, baseX + ox, baseY + oy, cell, gw, gh);
    }

    private void drawPlayer(GuiGraphics graphics, int ox, int oy, int cell, int gw, int gh) {
        int cx = ox + (gw / 2) * cell + cell / 2;
        int cy = oy + (gh / 2) * cell + cell / 2;
        int size = Mth.clamp(cell + 6, 12, 30);
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        int left = cx - size / 2;
        int top = cy - size / 2;
        graphics.fill(left - 1, top - 1, left + size + 1, top + size + 1, TabletColors.SURFACE_BASE);
        graphics.renderOutline(left - 1, top - 1, size + 2, size + 2, TabletColors.BORDER_ACCENT);
        new PlayerFaceTexture(player.getGameProfile().getId(), player.getGameProfile().getName())
                .draw(graphics, 0, 0, left, top, size, size);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOverElement(mouseX, mouseY)) {
            return false;
        }
        ResourceLocation dim = currentDimension();
        if (dim == null) {
            return false;
        }

        int localX = (int) mouseX - getPositionX();
        int localY = (int) mouseY - getPositionY();
        int w = getSizeWidth();
        int h = getSizeHeight();
        int cell = ChunkMapGeometry.cellSize(w, h, gridW, gridH);
        int ox = ChunkMapGeometry.gridOriginX(w, cell, gridW);
        int oy = ChunkMapGeometry.gridOriginY(h, cell, gridH);
        int dx = ChunkMapGeometry.deltaX(localX, ox, cell, gridW);
        int dz = ChunkMapGeometry.deltaZ(localY, oy, cell, gridH);
        if (!ChunkMapGeometry.inGridX(dx, gridW) || !ChunkMapGeometry.inGridZ(dz, gridH)) {
            return false;
        }

        int chunkX = playerChunkX() + dx;
        int chunkZ = playerChunkZ() + dz;
        boolean claimed = ClientChunkClaimCache.INSTANCE.isClaimed(dim, chunkX, chunkZ);
        boolean force = ClientChunkClaimCache.INSTANCE.isForceLoaded(dim, chunkX, chunkZ);
        boolean shift = Screen.hasShiftDown();

        if (button == 1) {
            if (force) {
                send(dim, C2SChunkClaimActionPacket.Action.TOGGLE_FORCE, chunkX, chunkZ);
            } else if (claimed) {
                send(dim, C2SChunkClaimActionPacket.Action.UNCLAIM, chunkX, chunkZ);
            }
            return true;
        }

        if (shift) {
            if (!claimed) {
                send(dim, C2SChunkClaimActionPacket.Action.CLAIM, chunkX, chunkZ);
            }
            return true;
        }

        boolean claimOn = state.chunkClaimer.claimArmed;
        boolean forceOn = state.chunkClaimer.forceLoadArmed;
        if (claimOn && forceOn) {
            send(dim, C2SChunkClaimActionPacket.Action.CLAIM, chunkX, chunkZ);
            if (!force) {
                send(dim, C2SChunkClaimActionPacket.Action.TOGGLE_FORCE, chunkX, chunkZ);
            }
            return true;
        }
        if (forceOn) {
            if (claimed && !force) {
                send(dim, C2SChunkClaimActionPacket.Action.TOGGLE_FORCE, chunkX, chunkZ);
            }
            return true;
        }
        if (claimOn) {
            if (!claimed) {
                send(dim, C2SChunkClaimActionPacket.Action.CLAIM, chunkX, chunkZ);
            }
            return true;
        }
        return true;
    }

    private void send(ResourceLocation dim, C2SChunkClaimActionPacket.Action action, int x, int z) {
        ModNetwork.sendToServer(new C2SChunkClaimActionPacket(action, dim, x, z));
    }
}
