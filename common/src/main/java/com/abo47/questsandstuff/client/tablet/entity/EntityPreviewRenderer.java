package com.abo47.questsandstuff.client.tablet.entity;

import com.abo47.questsandstuff.client.tablet.entity.variant.EntityVariantCatalog;

import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class EntityPreviewRenderer {
    public static final String ENTITY_ASSET_PREFIX = "entity:";
    private static final Map<String, Entity> ENTITY_CACHE = new HashMap<>();
    public static final int FRONT_ENTITY_YAW = 205;
    private static final int DEFAULT_ICON_ENTITY_YAW = FRONT_ENTITY_YAW;
    private static final int DEFAULT_ICON_ENTITY_SPIN_SPEED = 0;

    private EntityPreviewRenderer() {
    }

    public static boolean isEntityAsset(String asset) {
        return !entityId(asset).isBlank();
    }

    public static String entityAsset(String entityId) {
        String normalized = normalizeEntityId(entityId);
        return normalized.isBlank() ? "" : ENTITY_ASSET_PREFIX + normalized;
    }

    public static String entityAsset(String entityId, String variantKey) {
        String normalizedEntity = normalizeEntityId(entityId);
        String normalizedVariant = normalizeVariantKey(variantKey);
        if (normalizedEntity.isBlank()) {
            return "";
        }
        return normalizedVariant.isBlank()
                ? ENTITY_ASSET_PREFIX + normalizedEntity
                : ENTITY_ASSET_PREFIX + normalizedEntity + "?variant=" + normalizedVariant;
    }

    public static String withEntityVariant(String asset, String variantKey) {
        String entityId = entityId(asset);
        return entityAsset(entityId, variantKey, entityYaw(asset), entitySpinSpeed(asset));
    }

    public static String withEntityMotion(String asset, int yawDegrees, int spinSpeed) {
        String entityId = entityId(asset);
        return entityAsset(entityId, entityVariant(asset), yawDegrees, spinSpeed);
    }

    public static String entityId(String asset) {
        String value = asset == null ? "" : asset.trim();
        if (!value.startsWith(ENTITY_ASSET_PREFIX)) {
            return "";
        }
        String body = value.substring(ENTITY_ASSET_PREFIX.length());
        int queryIndex = body.indexOf('?');
        return normalizeEntityId(queryIndex >= 0 ? body.substring(0, queryIndex) : body);
    }

    public static String entityVariant(String asset) {
        return entityQueryParam(asset, "variant");
    }

    public static int entityYaw(String asset) {
        return parseIntParam(entityQueryParam(asset, "yaw"), DEFAULT_ICON_ENTITY_YAW, 0, 359);
    }

    public static int entitySpinSpeed(String asset) {
        return parseIntParam(entityQueryParam(asset, "spin"), DEFAULT_ICON_ENTITY_SPIN_SPEED, CanvasImageLayer.MIN_ENTITY_SPIN_SPEED, CanvasImageLayer.MAX_ENTITY_SPIN_SPEED);
    }

    private static String entityQueryParam(String asset, String key) {
        String value = asset == null ? "" : asset.trim();
        if (!value.startsWith(ENTITY_ASSET_PREFIX)) {
            return "";
        }
        String body = value.substring(ENTITY_ASSET_PREFIX.length());
        int queryIndex = body.indexOf('?');
        if (queryIndex < 0 || queryIndex >= body.length() - 1) {
            return "";
        }
        String[] params = body.substring(queryIndex + 1).split("&");
        for (String param : params) {
            int equals = param.indexOf('=');
            if (equals > 0 && key.equals(param.substring(0, equals))) {
                return param.substring(equals + 1);
            }
        }
        return "";
    }

    public static String entityIdFromSpawnEgg(String itemId) {
        String clean = itemId == null ? "" : itemId.trim();
        if (clean.isBlank() || clean.startsWith("#")) {
            return "";
        }
        ResourceLocation id = ResourceLocation.tryParse(clean);
        if (id == null) {
            return "";
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        if (!(item instanceof SpawnEggItem egg) || item == Items.AIR) {
            return "";
        }
        EntityType<?> type = egg.getType(new ItemStack(item).getTag());
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        return entityId == null ? "" : entityId.toString();
    }

    public static String spawnEggIcon(String entityId) {
        EntityType<?> type = entityType(entityId);
        if (type == null) {
            return "minecraft:egg";
        }
        SpawnEggItem egg = SpawnEggItem.byId(type);
        if (egg == null) {
            return "minecraft:egg";
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(egg);
        return itemId == null ? "minecraft:egg" : itemId.toString();
    }

    public static String entityDisplayName(String entityId) {
        EntityType<?> type = entityType(entityId);
        if (type == null) {
            return entityId == null ? "" : entityId;
        }
        return type.getDescription().getString();
    }

    public static List<String> searchableSpawnEggEntries(String filter) {
        String rawQuery = SearchFilter.normalizeUserInput(filter);
        List<String> entries = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (!(item instanceof SpawnEggItem egg)) {
                continue;
            }
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            if (itemId == null) {
                continue;
            }
            EntityType<?> type = egg.getType(new ItemStack(item).getTag());
            ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            String itemKey = itemId.toString();
            String entityKey = entityId == null ? "" : entityId.toString();
            String itemName = item.getDescription().getString();
            String entityName = type.getDescription().getString();
            if (rawQuery.isBlank()
                    || SearchFilter.matches(rawQuery, itemKey, itemName)
                    || SearchFilter.matches(rawQuery, entityKey, entityName)) {
                entries.add(itemKey);
            }
        }
        entries.sort(String::compareTo);
        return entries;
    }

    public static boolean renderEntity(GuiGraphics graphics, int x, int y, int width, int height, String entityId, float partialTicks) {
        return renderEntity(graphics, x, y, width, height, entityId, DEFAULT_ICON_ENTITY_YAW, DEFAULT_ICON_ENTITY_SPIN_SPEED, partialTicks);
    }

    public static boolean renderEntity(GuiGraphics graphics, int x, int y, int width, int height, String entityId, int yawDegrees, int spinSpeed, float partialTicks) {
        return renderEntityAsset(graphics, x, y, width, height, entityAsset(entityId), yawDegrees, spinSpeed, partialTicks);
    }

    public static boolean renderEntityAsset(GuiGraphics graphics, int x, int y, int width, int height, String asset, int yawDegrees, int spinSpeed, float partialTicks) {
        return renderEntityAsset(graphics, x, y, width, height, asset, yawDegrees, spinSpeed, CanvasImageLayer.DEFAULT_MODEL_PITCH, partialTicks);
    }

    public static boolean renderEntityAsset(GuiGraphics graphics, int x, int y, int width, int height, String asset, int yawDegrees, int spinSpeed, int pitchDegrees, float partialTicks) {
        return renderEntityAssetAtCenter(graphics, x + width / 2, y + height / 2, width, height, asset, yawDegrees, spinSpeed, pitchDegrees, partialTicks);
    }

    public static boolean renderEntityAssetAtCenter(GuiGraphics graphics, int centerX, int centerY, int width, int height, String asset, int yawDegrees, int spinSpeed, int pitchDegrees, float partialTicks) {
        EntityAsset parsed = parseEntityAsset(asset);
        Entity entity = cachedEntity(parsed);
        if (entity == null || width <= 0 || height <= 0) {
            return false;
        }
        EntityVariantCatalog.apply(entity, parsed.variantKey());
        int speed = CanvasImageLayer.clampEntitySpinSpeed(spinSpeed);
        float yaw = currentYaw(yawDegrees, speed);
        float pitch = CanvasImageLayer.normalizeDegrees(pitchDegrees);
        prepareEntityForRender(entity, speed > 0);
        renderEntityInInventory(graphics, centerX, centerY, renderScale(entity, width, height), entity, yaw, pitch, speed > 0 ? partialTicks : 0.0F);
        return true;
    }

    private static double renderScale(Entity entity, int width, int height) {
        double entityW = Math.max(0.25D, entity.getBbWidth());
        double entityH = Math.max(0.25D, entity.getBbHeight());
        double scale = Math.min(width / entityW, height / entityH) * 0.82D;
        return Math.max(1.0D, Math.min(96.0D, scale));
    }

    private static Entity cachedEntity(EntityAsset asset) {
        if (asset.entityId().isBlank() || Minecraft.getInstance().level == null) {
            return null;
        }
        EntityType<?> type = entityType(asset.entityId());
        if (type == null) {
            return null;
        }
        Entity entity = ENTITY_CACHE.get(asset.cacheKey());
        if (entity == null || entity.getType() != type || entity.level() != Minecraft.getInstance().level) {
            entity = type.create(Minecraft.getInstance().level);
            if (entity == null) {
                return null;
            }
            ENTITY_CACHE.put(asset.cacheKey(), entity);
        }
        return entity;
    }

    private static EntityType<?> entityType(String entityId) {
        ResourceLocation id = ResourceLocation.tryParse(normalizeEntityId(entityId));
        if (id == null) {
            return null;
        }
        return BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
    }

    private static String normalizeEntityId(String entityId) {
        return entityId == null ? "" : entityId.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeVariantKey(String variantKey) {
        return variantKey == null ? "" : variantKey.trim().toLowerCase(Locale.ROOT);
    }

    private static String entityAsset(String entityId, String variantKey, int yawDegrees, int spinSpeed) {
        String normalizedEntity = normalizeEntityId(entityId);
        if (normalizedEntity.isBlank()) {
            return "";
        }
        String normalizedVariant = EntityVariantCatalog.normalizeVariantKey(normalizedEntity, variantKey);
        int yaw = CanvasImageLayer.normalizeDegrees(yawDegrees);
        int spin = CanvasImageLayer.clampEntitySpinSpeed(spinSpeed);
        List<String> params = new ArrayList<>();
        if (!normalizedVariant.isBlank()) {
            params.add("variant=" + normalizedVariant);
        }
        if (yaw != DEFAULT_ICON_ENTITY_YAW) {
            params.add("yaw=" + yaw);
        }
        if (spin != DEFAULT_ICON_ENTITY_SPIN_SPEED) {
            params.add("spin=" + spin);
        }
        return params.isEmpty() ? ENTITY_ASSET_PREFIX + normalizedEntity : ENTITY_ASSET_PREFIX + normalizedEntity + "?" + String.join("&", params);
    }

    private static EntityAsset parseEntityAsset(String asset) {
        String entityId = entityId(asset);
        String variantKey = EntityVariantCatalog.normalizeVariantKey(entityId, entityVariant(asset));
        return new EntityAsset(entityId, variantKey);
    }

    private static float currentYaw(int yawDegrees, int spinSpeed) {
        float base = CanvasImageLayer.normalizeDegrees(yawDegrees);
        int speed = CanvasImageLayer.clampEntitySpinSpeed(spinSpeed);
        if (speed <= 0) {
            return base;
        }
        float elapsedSeconds = (System.currentTimeMillis() % 3_600_000L) / 1000.0F;
        return CanvasImageLayer.normalizeDegrees(Math.round(base + elapsedSeconds * speed));
    }

    private static int parseIntParam(String value, int fallback, int min, int max) {
        try {
            return Math.max(min, Math.min(max, Integer.parseInt(value == null ? "" : value.trim())));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static void prepareEntityForRender(Entity entity, boolean animated) {
        int tick = animated ? (int) ((System.currentTimeMillis() / 50L) % 72000L) : 0;
        entity.tickCount = tick;
        entity.setYRot(0.0F);
        entity.setXRot(0.0F);
        entity.yRotO = 0.0F;
        entity.xRotO = 0.0F;
        if (entity instanceof LivingEntity living) {
            living.yBodyRot = 0.0F;
            living.yBodyRotO = 0.0F;
            living.yHeadRot = 0.0F;
            living.yHeadRotO = 0.0F;
        }
    }

    private static void renderEntityInInventory(GuiGraphics graphics, int x, int y, double scale, Entity entity, float yawDegrees, float pitchDegrees, float partialTicks) {
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        Quaternionf rotation = new Quaternionf().rotateXYZ((float) Math.toRadians(pitchDegrees), (float) Math.toRadians(yawDegrees), (float) Math.PI);
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0D);
        graphics.pose().mulPoseMatrix(new Matrix4f().scaling((float) scale, (float) scale, (float) -scale));
        graphics.pose().mulPose(rotation);
        graphics.pose().translate(0.0D, -entity.getBbHeight() / 2.0D, 0.0D);
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        dispatcher.setRenderShadow(false);
        RenderSystem.runAsFancy(() -> dispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, graphics.pose(), graphics.bufferSource(), 15728880));
        graphics.flush();
        dispatcher.setRenderShadow(true);
        graphics.pose().popPose();
        Lighting.setupFor3DItems();
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
    }

    private record EntityAsset(String entityId, String variantKey) {
        String cacheKey() {
            return variantKey.isBlank() ? entityId : entityId + "?variant=" + variantKey;
        }
    }
}
