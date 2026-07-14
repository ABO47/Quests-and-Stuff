package com.abo47.questsandstuff.client.tablet.chunkclaimer;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.ui.render.PlayerFaceTexture;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.chunkclaim.C2SChunkClaimActionPacket;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
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

    private final TabletUiState state;

    private DynamicTexture terrainTex;
    private ResourceLocation terrainTexLoc;
    private int texW = -1;
    private int texH = -1;
    private int gridW = -1;
    private int gridH = -1;
    private int sub = 16;
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
        int cell = Math.max(1, w / gw);
        int gh = Math.max(3, h / cell);
        int s = Math.max(1, Math.min(16, (int) Math.sqrt(160000.0 / (gw * gh))));
        long now = System.currentTimeMillis();
        boolean needs = terrainTex == null || gw != gridW || gh != gridH || s != sub
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
        if (terrainTex == null || texW != totalW || texH != totalH) {
            if (terrainTex != null) {
                terrainTex.close();
            }
            if (terrainTexLoc != null) {
                Minecraft.getInstance().getTextureManager().release(terrainTexLoc);
            }
            terrainTex = new DynamicTexture(totalW, totalH, false);
            terrainTex.setFilter(false, false);
            texW = totalW;
            texH = totalH;
            ResourceLocation terrainLoc = new ResourceLocation("questsandstuff", "chunkmap_terrain");
            Minecraft.getInstance().getTextureManager().register(terrainLoc, terrainTex);
            terrainTexLoc = terrainLoc;
        }

        int halfGw = gw / 2;
        int halfGh = gh / 2;
        NativeImage img = terrainTex.getPixels();
        int[][] heights = new int[totalW][totalH];
        for (int tx = 0; tx < totalW; tx++) {
            int worldChunkX = cx + (tx / sub - halfGw);
            for (int tz = 0; tz < totalH; tz++) {
                int worldChunkZ = cz + (tz / sub - halfGh);
                int blockX = worldChunkX * 16 + (int) ((tx % sub + 0.5) * (16.0 / sub));
                int blockZ = worldChunkZ * 16 + (int) ((tz % sub + 0.5) * (16.0 / sub));
                heights[tx][tz] = level.getHeight(Heightmap.Types.WORLD_SURFACE, blockX, blockZ);
            }
        }
        int maxY = level.getMaxBuildHeight();
        int sea = level.getSeaLevel();
        for (int tx = 0; tx < totalW; tx++) {
            int worldChunkX = cx + (tx / sub - halfGw);
            for (int tz = 0; tz < totalH; tz++) {
                int worldChunkZ = cz + (tz / sub - halfGh);
                int blockX = worldChunkX * 16 + (int) ((tx % sub + 0.5) * (16.0 / sub));
                int blockZ = worldChunkZ * 16 + (int) ((tz % sub + 0.5) * (16.0 / sub));
                int hC = heights[tx][tz];
                int hW = tx > 0 ? heights[tx - 1][tz] : hC;
                int hN = tz > 0 ? heights[tx][tz - 1] : hC;
                int reliefH = (hC - hW) + (hC - hN);
                int rgb = sampleColor(level, blockX, blockZ, hC, reliefH, sea, maxY);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                img.setPixelRGBA(tx, tz, (0xFF << 24) | (b << 16) | (g << 8) | r);
            }
        }
        terrainTex.upload();
    }

    private int sampleColor(ClientLevel level, int worldX, int worldZ, int topY, int reliefH, int sea, int maxY) {
        BlockPos pos = new BlockPos(worldX, Mth.clamp(topY - 1, 1, maxY - 1), worldZ);
        FluidState fluid = level.getFluidState(pos);

        int rgb;
        if (!fluid.isEmpty()) {
            int water = level.getBiome(pos).value().getWaterColor();
            int depth = Mth.clamp(sea - topY, 0, 30);
            rgb = shade(water, 1f - depth * 0.012f);
        } else {
            BlockState bs = level.getBlockState(pos);
            if (bs.is(Blocks.SNOW) || bs.is(Blocks.SNOW_BLOCK)) {
                rgb = 0xE9F1F6;
            } else if (bs.is(Blocks.ICE) || bs.is(Blocks.PACKED_ICE) || bs.is(Blocks.FROSTED_ICE)) {
                rgb = 0x9FD6E8;
            } else if (bs.is(Blocks.SAND) || bs.is(Blocks.SANDSTONE) || bs.is(Blocks.RED_SAND)) {
                rgb = 0xD8C79A;
            } else if (bs.is(Blocks.GRASS_BLOCK) || bs.is(Blocks.TALL_GRASS) || bs.is(Blocks.FERN)
                    || bs.getBlock() instanceof LeavesBlock) {
                int grass = level.getBiome(pos).value().getGrassColor(worldX, worldZ);
                rgb = bs.getBlock() instanceof LeavesBlock ? shade(grass, 0.85f) : grass;
            } else {
                MapColor mapColor = bs.getMapColor(level, pos);
                rgb = mapColor != null ? mapColor.col : 0x6E6E6E;
            }
        }

        float relief = Mth.clamp(1f + reliefH * 0.05f, 0.68f, 1.32f);
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
        int centerX = baseX + ChunkMapGeometry.cellPixelX(ox, cell, gw, 0) + cell / 2;
        int centerY = baseY + ChunkMapGeometry.cellPixelY(oy, cell, gh, 0) + cell / 2;

        if (terrainTexLoc != null) {
            graphics.blit(terrainTexLoc, baseX, baseY, w, h, 0, 0, texW, texH, texW, texH);
            graphics.blit(terrainTexLoc, baseX + ox, baseY + oy, gw * cell, gh * cell,
                    0, 0, texW, texH, texW, texH);
        }

        if (state.chunkClaimer.showGrid) {
            int endX = baseX + ox + gw * cell;
            int endY = baseY + oy + gh * cell;
            for (int i = 0; i <= gw; i++) {
                int gx = baseX + ox + i * cell;
                graphics.fill(gx, baseY + oy, gx + 1, endY, GRID_COLOR);
            }
            for (int j = 0; j <= gh; j++) {
                int gy = baseY + oy + j * cell;
                graphics.fill(baseX + ox, gy, endX, gy + 1, GRID_COLOR);
            }
            graphics.fill(baseX + w - 1, baseY, baseX + w, baseY + h, GRID_COLOR);
            graphics.fill(baseX, baseY + h - 1, baseX + w, baseY + h, GRID_COLOR);
        }

        int dxMin = -(gw / 2 + 2);
        int dxMax = gw / 2 + 2;
        int dzMin = -(gridH / 2 + 2);
        int dzMax = gridH / 2 + 2;
        var states = new java.util.HashMap<Long, Integer>();
        for (int dx = dxMin; dx <= dxMax; dx++) {
            for (int dz = dzMin; dz <= dzMax; dz++) {
                states.put(key(dx, dz), stateOf(ClientChunkClaimCache.INSTANCE, dim, cx + dx, cz + dz));
            }
        }

        for (int dx = dxMin; dx <= dxMax; dx++) {
            for (int dz = dzMin; dz <= dzMax; dz++) {
                int s = states.getOrDefault(key(dx, dz), 0);
                if (s == 0) {
                    continue;
                }
                int px = baseX + ChunkMapGeometry.cellPixelX(ox, cell, gw, dx);
                int py = baseY + ChunkMapGeometry.cellPixelY(oy, cell, gh, dz);
                graphics.fill(px, py, px + cell + 1, py + cell + 1, s == 2 ? FORCE_FILL : CLAIMED_FILL);
            }
        }

        for (int dx = dxMin; dx <= dxMax; dx++) {
            for (int dz = dzMin; dz <= dzMax; dz++) {
                int s = states.getOrDefault(key(dx, dz), 0);
                if (s == 0) {
                    continue;
                }
                int px = baseX + ChunkMapGeometry.cellPixelX(ox, cell, gw, dx);
                int py = baseY + ChunkMapGeometry.cellPixelY(oy, cell, gh, dz);
                int edge = s == 2 ? FORCE_EDGE : CLAIMED_EDGE;
                if (states.getOrDefault(key(dx - 1, dz), 0) != s) {
                    graphics.fill(px, py, px + 1, py + cell + 1, edge);
                }
                if (states.getOrDefault(key(dx + 1, dz), 0) != s) {
                    graphics.fill(px + cell, py, px + cell + 1, py + cell + 1, edge);
                }
                if (states.getOrDefault(key(dx, dz - 1), 0) != s) {
                    graphics.fill(px, py, px + cell + 1, py + 1, edge);
                }
                if (states.getOrDefault(key(dx, dz + 1), 0) != s) {
                    graphics.fill(px, py + cell, px + cell + 1, py + cell + 1, edge);
                }
            }
        }

        if (isMouseOverElement(mouseX, mouseY)) {
            int lmx = (int) mouseX - baseX;
            int lmy = (int) mouseY - baseY;
            int dx = (int) Math.floor((lmx - ox) / (double) cell) - gw / 2;
            int dz = (int) Math.floor((lmy - oy) / (double) cell) - gh / 2;
            int hpxLocal = ChunkMapGeometry.cellPixelX(ox, cell, gw, dx);
            int hpyLocal = ChunkMapGeometry.cellPixelY(oy, cell, gh, dz);
            int fx = baseX + hpxLocal + cell / 2;
            int fy = baseY + hpyLocal + cell / 2;
            com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper.drawGlow(
                    graphics, fx, fy, baseX + hpxLocal, baseY + hpyLocal, cell, cell, TabletColors.BORDER_ACCENT);
        }

        drawPlayer(graphics, centerX, centerY, cell);
    }

    private void drawPlayer(GuiGraphics graphics, int cx, int cy, int cell) {
        int size = Math.max(8, cell / 2);
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

    private static long key(int dx, int dz) {
        return ((long) (dx + 2048) << 32) | (dz + 2048);
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
        int dx = (int) Math.floor((localX - ox) / (double) cell) - gridW / 2;
        int dz = (int) Math.floor((localY - oy) / (double) cell) - gridH / 2;

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
            } else if (!force) {
                send(dim, C2SChunkClaimActionPacket.Action.TOGGLE_FORCE, chunkX, chunkZ);
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
