package com.abo47.questsandstuff.client.tablet.chunkclaimer;

import java.util.UUID;
import javax.annotation.Nonnull;

import org.joml.Vector4f;

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
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import com.abo47.questsandstuff.client.tablet.animation.TabletAnimationTimings;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.ui.render.PlayerFaceTexture;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.chunkclaim.C2SChunkClaimActionPacket;

import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;

public class ChunkMapWidget extends Widget {
    private static final long RESAMPLE_MS = TabletAnimationTimings.CHUNK_MAP_RESAMPLE_MS;

    private static final int GRID_COLOR = TabletColors.DEFAULT_GRID_COLOR;

    private final TabletUiState state;

    private DynamicTexture terrainTex;
    private ResourceLocation terrainTexLoc;
    private int texW = -1;
    private int texH = -1;
    private int gridW = -1;
    private int gridH = -1;
    private int sub = GRID_16;
    private int cachedCx = Integer.MAX_VALUE;
    private int cachedCz = Integer.MAX_VALUE;
    private long lastSample = 0;
    private int lastHoverDx = Integer.MAX_VALUE;
    private int lastHoverDz = Integer.MAX_VALUE;

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
        int gw = Math.max(3, w / GRID_16);
        int cell = Math.max(1, w / gw);
        int gh = Math.max(3, h / cell);
        int s = Math.max(1, Math.min(16, (int) Math.sqrt(640000.0 / (gw * gh))));
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

        int totalW = (gw + 2) * sub;
        int totalH = (gh + 2) * sub;
        if (terrainTex == null || texW != totalW || texH != totalH) {
            if (terrainTex != null) {
                terrainTex.close();
            }
            if (terrainTexLoc != null) {
                Minecraft.getInstance().getTextureManager().release(terrainTexLoc);
            }
            terrainTex = new DynamicTexture(totalW, totalH, false);
            terrainTex.setFilter(true, true);
            texW = totalW;
            texH = totalH;
            ResourceLocation terrainLoc = new ResourceLocation("questsandstuff", "chunkmap_terrain");
            Minecraft.getInstance().getTextureManager().register(terrainLoc, terrainTex);
            terrainTexLoc = terrainLoc;
        }

        int halfGw = gw / 2;
        int halfGh = gh / 2;
        int bias = -1;
        var player = Minecraft.getInstance().player;
        int playerY = player != null ? player.blockPosition().getY() : level.getSeaLevel();
        NativeImage img = terrainTex.getPixels();
        int[][] surfaceY = new int[totalW][totalH];
        for (int tx = 0; tx < totalW; tx++) {
            int worldChunkX = cx + (tx / sub + bias - halfGw);
            for (int tz = 0; tz < totalH; tz++) {
                int worldChunkZ = cz + (tz / sub + bias - halfGh);
                int blockX = worldChunkX * 16 + (int) ((tx % sub + 0.5) * (16.0 / sub));
                int blockZ = worldChunkZ * 16 + (int) ((tz % sub + 0.5) * (16.0 / sub));
                surfaceY[tx][tz] = findSurfaceY(level, blockX, blockZ, playerY);
            }
        }
        int egw = gw + 2;
        int egh = gh + 2;
        int[][] lightGrid = new int[egw][egh];
        for (int cgx = 0; cgx < egw; cgx++) {
            for (int cgz = 0; cgz < egh; cgz++) {
                int txc = cgx * sub + sub / 2;
                int tzc = cgz * sub + sub / 2;
                int worldChunkX = cx + (cgx + bias - halfGw);
                int worldChunkZ = cz + (cgz + bias - halfGh);
                int bx = worldChunkX * 16 + 8;
                int bz = worldChunkZ * 16 + 8;
                int by = Mth.clamp(surfaceY[txc][tzc] + 1, level.getMinBuildHeight(), level.getMaxBuildHeight() - 1);
                lightGrid[cgx][cgz] = level.getMaxLocalRawBrightness(new BlockPos(bx, by, bz));
            }
        }
        float[] shadow = dimensionShadow(level.dimension().location());

        int maxY = level.getMaxBuildHeight();
        for (int tx = 0; tx < totalW; tx++) {
            int worldChunkX = cx + (tx / sub + bias - halfGw);
            int cgx = tx / sub;
            for (int tz = 0; tz < totalH; tz++) {
                int worldChunkZ = cz + (tz / sub + bias - halfGh);
                int cgz = tz / sub;
                int blockB = worldChunkX * 16 + (int) ((tx % sub + 0.5) * (16.0 / sub));
                int blockZ = worldChunkZ * 16 + (int) ((tz % sub + 0.5) * (16.0 / sub));
                int hC = surfaceY[tx][tz];
                int rgb = sampleColorAt(level, blockB, blockZ, hC, maxY);
                int hW = tx > 0 ? surfaceY[tx - 1][tz] : hC;
                int hN = tz > 0 ? surfaceY[tx][tz - 1] : hC;
                float slope = (hC - hW) + (hC - hN);
                float direct = Mth.clamp(slope * 0.04f, -0.4f, 0.4f);
                int sgx0 = Math.max(0, cgx);
                int sgx1 = Math.min(egw - 1, cgx + 1);
                int sgz0 = Math.max(0, cgz);
                int sgz1 = Math.min(egh - 1, cgz + 1);
                float fx = (tx % sub) / (float) sub;
                float fz = (tz % sub) / (float) sub;
                float lX0 = lightGrid[sgx0][sgz0] + (lightGrid[sgx1][sgz0] - lightGrid[sgx0][sgz0]) * fx;
                float lX1 = lightGrid[sgx0][sgz1] + (lightGrid[sgx1][sgz1] - lightGrid[sgx0][sgz1]) * fx;
                float rawLight = lX0 + (lX1 - lX0) * fz;
                float brightness = (9f + rawLight) / 24f;
                float heightTerm = Mth.clamp(0.92f + (hC - 64) / 320f, 0.82f, 1.08f);
                float whiteLight = 0.5f + direct;
                float mR = (shadow[0] * 0.2f + whiteLight) * brightness * heightTerm;
                float mG = (shadow[1] * 0.2f + whiteLight) * brightness * heightTerm;
                float mB = (shadow[2] * 0.2f + whiteLight) * brightness * heightTerm;
                int r = (int) Mth.clamp(((rgb >> 16) & 0xFF) * mR, 0, 255);
                int g = (int) Mth.clamp(((rgb >> 8) & 0xFF) * mG, 0, 255);
                int b = (int) Mth.clamp((rgb & 0xFF) * mB, 0, 255);
                img.setPixelRGBA(tx, tz, (0xFF << 24) | (b << 16) | (g << 8) | r);
            }
        }
        terrainTex.upload();
    }

    private static float[] dimensionShadow(ResourceLocation dim) {
        String ns = dim.toString();
        if (ns.contains("nether")) {
            return new float[]{1.0f, 0.25f, 0.2f};
        }
        if (ns.contains("end")) {
            return new float[]{0.7f, 0.55f, 1.0f};
        }
        return new float[]{0.518f, 0.678f, 1.0f};
    }

    private int findSurfaceY(ClientLevel level, int worldX, int worldZ, int playerY) {
        int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, worldX, worldZ);
        if (topY <= playerY + 4 || !state.chunkClaimer.surfaceScan) {
            return firstSolidDown(level, worldX, worldZ, topY);
        }
        int minY = level.getMinBuildHeight() + 1;
        int maxY = level.getMaxBuildHeight() - 1;
        int y = Mth.clamp(playerY, minY, maxY);
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos(worldX, y, worldZ);
        if (!level.getBlockState(p).isAir()) {
            return y;
        }
        for (int dy = 1; dy <= 12; dy++) {
            p.setY(y - dy);
            if (!level.getBlockState(p).isAir()) {
                return y - dy;
            }
        }
        for (int dy = 1; dy <= 12; dy++) {
            p.setY(y + dy);
            if (!level.getBlockState(p).isAir()) {
                return y + dy;
            }
        }
        return topY;
    }

    private static int firstSolidDown(ClientLevel level, int worldX, int worldZ, int fromY) {
        for (int dy = 0; dy <= 4; dy++) {
            BlockPos p = new BlockPos(worldX, fromY - dy, worldZ);
            if (!level.getBlockState(p).isAir()) {
                return fromY - dy;
            }
        }
        return fromY;
    }

    private int sampleColorAt(ClientLevel level, int worldX, int worldZ, int y, int maxY) {
        BlockPos pos = new BlockPos(worldX, Mth.clamp(y, level.getMinBuildHeight(), maxY - 1), worldZ);
        FluidState fluid = level.getFluidState(pos);

        int rgb;
        if (!fluid.isEmpty()) {
            if (fluid.getType() == Fluids.LAVA) {
                rgb = TabletColors.TERRAIN_LAVA;
            } else {
                rgb = level.getBiome(pos).value().getWaterColor();
                int depth = waterDepth(level, worldX, worldZ, y);
                rgb = shade(rgb, 1f - Mth.clamp(depth / 12f, 0f, 1f) * 0.5f);
            }
        } else {
            BlockState bs = level.getBlockState(pos);
            if (bs.is(Blocks.SNOW) || bs.is(Blocks.SNOW_BLOCK)) {
                rgb = TabletColors.TERRAIN_SNOW;
            } else if (bs.is(Blocks.ICE) || bs.is(Blocks.PACKED_ICE) || bs.is(Blocks.FROSTED_ICE)) {
                rgb = TabletColors.TERRAIN_ICE;
            } else if (bs.is(Blocks.SAND) || bs.is(Blocks.SANDSTONE) || bs.is(Blocks.RED_SAND)) {
                rgb = TabletColors.TERRAIN_SAND;
            } else if (bs.is(Blocks.GRASS_BLOCK) || bs.is(Blocks.TALL_GRASS) || bs.is(Blocks.FERN)) {
                rgb = level.getBiome(pos).value().getGrassColor(worldX, worldZ);
            } else if (bs.getBlock() instanceof LeavesBlock) {
                rgb = level.getBiome(pos).value().getFoliageColor();
            } else {
                MapColor mapColor = bs.getMapColor(level, pos);
                rgb = mapColor != null ? mapColor.col : TabletColors.TERRAIN_DEFAULT_FALLBACK;
            }
        }

        return rgb;
    }

    private int waterDepth(ClientLevel level, int worldX, int worldZ, int surfaceY) {
        int depth = 0;
        int minY = level.getMinBuildHeight();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos(worldX, 0, worldZ);
        for (int dy = 1; dy <= 14; dy++) {
            int yy = surfaceY - dy;
            if (yy < minY) {
                break;
            }
            p.setY(yy);
            if (!level.getBlockState(p).getCollisionShape(level, p).isEmpty()) {
                break;
            }
            if (!level.getFluidState(p).isEmpty()) {
                depth++;
            } else {
                break;
            }
        }
        return depth;
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

    private static int teamColor(UUID teamId, int state) {
        if (teamId == null) return 0x4F8DF7;
        float hue = ((float)(teamId.hashCode() & 0xFFFF)) / 65536f;
        return java.awt.Color.HSBtoRGB(hue, 0.5f, 0.85f);
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int w = getSizeWidth();
        int h = getSizeHeight();
        int baseX = getPositionX();
        int baseY = getPositionY();
        graphics.fill(baseX, baseY, baseX + w, baseY + h, TabletColors.SURFACE_BASE);

        var pose = graphics.pose().last().pose();
        var scissorMin = pose.transform(new Vector4f(baseX, baseY, 0, 1));
        var scissorMax = pose.transform(new Vector4f(baseX + w, baseY + h, 0, 1));
        graphics.enableScissor((int) scissorMin.x, (int) scissorMin.y, (int) scissorMax.x, (int) scissorMax.y);

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
            ResourceTexture tex = new ResourceTexture(terrainTexLoc);
            tex.draw(graphics, mouseX, mouseY, baseX + ox - cell, baseY + oy - cell, (gw + 2) * cell, (gh + 2) * cell);
        }

        int iLo = (int) Math.floor((0 - ox) / (double) cell);
        int iHi = (int) Math.floor((w - ox) / (double) cell);
        int jLo = (int) Math.floor((0 - oy) / (double) cell);
        int jHi = (int) Math.floor((h - oy) / (double) cell);

        var states = new java.util.HashMap<Long, Integer>();
        var teamCells = new java.util.HashMap<Long, UUID>();
        var claimKeys = new java.util.HashMap<Long, String>();
        for (int i = iLo; i <= iHi; i++) {
            int dx = i - gw / 2;
            for (int j = jLo; j <= jHi; j++) {
                int dz = j - gh / 2;
                long k = key(dx, dz);
                int cwX = cx + dx;
                int cwZ = cz + dz;
                states.put(k, stateOf(ClientChunkClaimCache.INSTANCE, dim, cwX, cwZ));
                UUID t = ClientChunkClaimCache.INSTANCE.teamIdOf(dim, cwX, cwZ);
                if (t != null) teamCells.put(k, t);
                int s = states.get(k);
                claimKeys.put(k, s == 0 ? "0" : s + ":" + (t != null ? t.toString() : ""));
            }
        }

        for (int i = iLo; i <= iHi; i++) {
            int dx = i - gw / 2;
            for (int j = jLo; j <= jHi; j++) {
                int dz = j - gh / 2;
                long k = key(dx, dz);
                int s = states.getOrDefault(k, 0);
                if (s == 0) continue;
                int px = baseX + ChunkMapGeometry.cellPixelX(ox, cell, gw, dx);
                int py = baseY + ChunkMapGeometry.cellPixelY(oy, cell, gh, dz);
                int cellColor = teamColor(teamCells.get(k), s);
                int fillColor = (0x99000000) | (cellColor & 0x00FFFFFF);
                graphics.fill(px, py, px + cell, py + cell, fillColor);
            }
        }

        if (state.chunkClaimer.showGrid) {
            int opacityPct = Math.max(0, Math.min(100, state.chunkClaimer.gridOpacityPercent));
            int alpha = Math.max(20, Math.min(220, (255 * opacityPct) / 100));
            int gridCol = (alpha << 24) | (GRID_COLOR & 0x00FFFFFF);
            for (int i = iLo; i <= iHi; i++) {
                int dx = i - gw / 2;
                for (int j = jLo; j <= jHi; j++) {
                    int dz = j - gh / 2;
                    long k = key(dx, dz);
                    if (states.getOrDefault(k, 0) != 0) continue;
                    int px = baseX + ChunkMapGeometry.cellPixelX(ox, cell, gw, dx);
                    int py = baseY + ChunkMapGeometry.cellPixelY(oy, cell, gh, dz);
                    if (i == iLo || states.getOrDefault(key(dx - 1, dz), 0) == 0) {
                        graphics.fill(px, py, px + 1, py + cell, gridCol);
                    }
                    if (i == iHi || states.getOrDefault(key(dx + 1, dz), 0) == 0) {
                        graphics.fill(px + cell, py, px + cell + 1, py + cell, gridCol);
                    }
                    if (j == jLo || states.getOrDefault(key(dx, dz - 1), 0) == 0) {
                        graphics.fill(px, py, px + cell, py + 1, gridCol);
                    }
                    if (j == jHi || states.getOrDefault(key(dx, dz + 1), 0) == 0) {
                        graphics.fill(px, py + cell, px + cell, py + cell + 1, gridCol);
                    }
                }
            }
        }

        for (int i = iLo; i <= iHi; i++) {
            int dx = i - gw / 2;
            for (int j = jLo; j <= jHi; j++) {
                int dz = j - gh / 2;
                long k = key(dx, dz);
                int s = states.getOrDefault(k, 0);
                if (s == 0) continue;
                String ck = claimKeys.get(k);
                int px = baseX + ChunkMapGeometry.cellPixelX(ox, cell, gw, dx);
                int py = baseY + ChunkMapGeometry.cellPixelY(oy, cell, gh, dz);
                int edge = teamColor(teamCells.get(k), s);
                boolean left = i == iLo || !ck.equals(claimKeys.getOrDefault(key(dx - 1, dz), "0"));
                boolean right = i == iHi || !ck.equals(claimKeys.getOrDefault(key(dx + 1, dz), "0"));
                boolean up = j == jLo || !ck.equals(claimKeys.getOrDefault(key(dx, dz - 1), "0"));
                boolean down = j == jHi || !ck.equals(claimKeys.getOrDefault(key(dx, dz + 1), "0"));
                if (left) graphics.fill(px, py, px + 1, py + cell + 1, edge);
                if (right) graphics.fill(px + cell, py, px + cell + 1, py + cell + 1, edge);
                if (up) graphics.fill(px, py, px + cell + 1, py + 1, edge);
                if (down) graphics.fill(px, py + cell, px + cell + 1, py + cell + 1, edge);
            }
        }

        int forceInner = 0xFFE06F73;
        for (int i = iLo; i <= iHi; i++) {
            int dx = i - gw / 2;
            for (int j = jLo; j <= jHi; j++) {
                int dz = j - gh / 2;
                long k = key(dx, dz);
                if (states.getOrDefault(k, 0) != 2) continue;
                String ck = claimKeys.get(k);
                int px = baseX + ChunkMapGeometry.cellPixelX(ox, cell, gw, dx);
                int py = baseY + ChunkMapGeometry.cellPixelY(oy, cell, gh, dz);
                boolean left = i == iLo || !ck.equals(claimKeys.getOrDefault(key(dx - 1, dz), "0"));
                boolean right = i == iHi || !ck.equals(claimKeys.getOrDefault(key(dx + 1, dz), "0"));
                boolean up = j == jLo || !ck.equals(claimKeys.getOrDefault(key(dx, dz - 1), "0"));
                boolean down = j == jHi || !ck.equals(claimKeys.getOrDefault(key(dx, dz + 1), "0"));
                if (left) graphics.fill(px, py, px + 1, py + cell + 1, forceInner);
                if (right) graphics.fill(px + cell, py, px + cell + 1, py + cell + 1, forceInner);
                if (up) graphics.fill(px, py, px + cell + 1, py + 1, forceInner);
                if (down) graphics.fill(px, py + cell, px + cell + 1, py + cell + 1, forceInner);
            }
        }

        if (isMouseOverElement(mouseX, mouseY)) {
            int lmx = (int) mouseX - baseX;
            int lmy = (int) mouseY - baseY;
            int dx = (int) Math.floor((lmx - ox) / (double) cell) - gw / 2;
            int dz = (int) Math.floor((lmy - oy) / (double) cell) - gh / 2;
            int iHov = dx + gw / 2;
            int jHov = dz + gh / 2;
            if (iHov >= iLo && iHov <= iHi && jHov >= jLo && jHov <= jHi) {
                if (dx != lastHoverDx || dz != lastHoverDz) {
                    lastHoverDx = dx;
                    lastHoverDz = dz;
                }
                int hpxLocal = ChunkMapGeometry.cellPixelX(ox, cell, gw, dx);
                int hpyLocal = ChunkMapGeometry.cellPixelY(oy, cell, gh, dz);
                int fx = baseX + hpxLocal + (cell + 1) / 2;
                int fy = baseY + hpyLocal + (cell + 1) / 2;
                com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper.drawGlow(
                        graphics, fx, fy, baseX + hpxLocal, baseY + hpyLocal, cell + 1, cell + 1, TabletColors.BORDER_ACCENT);
                int chunkX = cx + dx;
                int chunkZ = cz + dz;
                boolean claimedHere = ClientChunkClaimCache.INSTANCE.isClaimed(dim, chunkX, chunkZ);
                if (claimedHere) {
                    String name = ClientChunkClaimCache.INSTANCE.ownerName(dim, chunkX, chunkZ);
                    if (name.isEmpty()) {
                        name = "Claimed";
                    }
                    var font = Minecraft.getInstance().font;
                    int textX = baseX + hpxLocal;
                    int textY = baseY + hpyLocal + cell + 3;
                    graphics.drawString(font, name, textX, textY, 0xFFEAF1F4, true);
                }
            } else {
                lastHoverDx = Integer.MAX_VALUE;
                lastHoverDz = Integer.MAX_VALUE;
            }
        } else {
            lastHoverDx = Integer.MAX_VALUE;
            lastHoverDz = Integer.MAX_VALUE;
        }

        drawPlayer(graphics, centerX, centerY, cell);
        graphics.disableScissor();
    }

    private void drawPlayer(GuiGraphics graphics, int cx, int cy, int cell) {
        int size = Math.max(8, cell / 2) + 1;
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        int left = cx - size / 2;
        int top = cy - size / 2;
        graphics.fill(left - 1, top - 1, left + size + 1, top + size + 1, TabletColors.SURFACE_BASE);
        graphics.renderOutline(left - 1, top - 1, size + 2, size + 2, TabletColors.BORDER_ACCENT);
        new PlayerFaceTexture(player.getGameProfile().getId(), player.getGameProfile().getName())
                .draw(graphics, 0, 0, left + 1, top + 1, size - 2, size - 2);
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
        int i = dx + gridW / 2;
        int j = dz + gridH / 2;
        int clickILo = (int) Math.floor((0 - ox) / (double) cell);
        int clickIHi = (int) Math.floor((w - ox) / (double) cell);
        int clickJLo = (int) Math.floor((0 - oy) / (double) cell);
        int clickJHi = (int) Math.floor((h - oy) / (double) cell);
        if (i < clickILo || i > clickIHi || j < clickJLo || j > clickJHi) {
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
