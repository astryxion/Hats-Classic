package com.astryxion.hats.client.render;

import com.astryxion.hats.client.compat.FreshAnimationsHatCompat;
import com.astryxion.hats.client.compat.MoBendsHatCompat;
import com.astryxion.hats.client.compat.SkinLayers3dHatCompat;
import com.astryxion.hats.client.tabula.TabulaHatPlacement;
import com.astryxion.hats.client.tabula.TabulaHatRenderer;
import com.astryxion.hats.common.hat.HatColor;
import com.astryxion.hats.common.hat.HatManager;
import com.astryxion.hats.common.tabula.TabulaHats;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BabyModelTransform;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.SheepWoolLayer;
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Sit hats on the actual head cube, then apply the vanilla HEAD-item orientation.
 * <p>
 * Player heads are 8px cubes whose pivot is at the neck. Cow heads are 8px cubes
 * whose pivot is already in the center. Using Simple Hats' hardcoded
 * {@code translate(0, -0.25)} after the bone therefore hits the crown on cows
 * and the center on players. Centering on the real cube first makes every mob
 * look like a player head to the hat item.
 */
public class HatRenderLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>>
        extends RenderLayer<S, M> {

    public static final ContextKey<LivingEntity> HAT_ENTITY =
            new ContextKey<>(Identifier.fromNamespaceAndPath("hats", "hat_entity"));
    /** Captured during extract. Submit runs later, after GUI previews restore the live hat. */
    public static final ContextKey<ItemStack> HAT_STACK =
            new ContextKey<>(Identifier.fromNamespaceAndPath("hats", "hat_stack"));

    private static final String[] HEAD_PART_NAMES = {
            "head", "Head", "body", "Body"
    };

    private static final float PLAYER_HEAD_SIZE = 8.0F;
    private static final float ITEM_HEAD_SCALE = 0.625F;
    /**
     * Vanilla {@code HumanoidArmorLayer} outer helmets use
     * {@code CubeDeformation(1.0)} on an 8px skull → a 10px shell. Sit on that
     * shell volume geometrically (like shulker lids), not via a Y offset.
     */
    private static final float HELMET_SHELL_INFLATE = 1.0F;
    /** Half-width of an 8px head in the centered item space {@code renderStatic} uses. */
    private static final float HEAD_SIT_RADIUS = PLAYER_HEAD_SIZE / 2.0F / 16.0F;

    /**
     * Vanilla {@code SheepModel.createFurLayer} / {@code SheepFurModel} head cube:
     * {@code addBox(-3, -4, -4, 6, 6, 6, new CubeDeformation(0.6))}.
     * Used only if the live fur-layer cubes cannot be read.
     */
    private static final float SHEEP_WOOL_INFLATE = 0.6F;
    private static final float[] SHEEP_WOOL_HEAD = {
            -3.0F, -4.0F, -4.0F, 3.0F, 2.0F, 2.0F
    };

    /** Vanilla {@code SlimeModel.createOuterBodyLayer} cube. */
    private static final float[] SLIME_OUTER_CUBE = {
            -4.0F, 16.0F, -4.0F, 4.0F, 24.0F, 4.0F
    };

    private final LivingEntityRenderer<?, S, M> parent;

    public HatRenderLayer(LivingEntityRenderer<?, S, M> parent) {
        super(parent);
        this.parent = parent;
    }

    public static void pushPreviewHat(ItemStack stack) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player != null) {
            ClientHatPreview.set(mc.player.getUUID(), stack);
        }
    }

    public static void popPreviewHat() {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player != null) {
            ClientHatPreview.clear(mc.player.getUUID());
        }
    }

    @Override
    public void submit(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int light,
            S state,
            float yRot,
            float xRot
    ) {
        LivingEntity entity = state.getRenderData(HAT_ENTITY);
        if (entity == null) {
            return;
        }
        ItemStack hat = state.getRenderData(HAT_STACK);
        if (hat == null) {
            hat = ClientHatPreview.orWorn(entity);
        }
        if (hat.isEmpty()) {
            return;
        }
        if (!isPrimaryHatLayer()) {
            return;
        }

        poseStack.pushPose();

        if (TabulaHats.isTabula(hat)) {
            boolean back = hatOnBack(entity);
            // forceHead skips the rump path. Horse-family Tabula hats must use
            // the same body anchor as JSON hats or they sit on the skull.
            ModelPart tabulaHead = attachToHead(poseStack, entity, !back);
            float geometryScale = 1.0F;
            try {
                if (back && tabulaHead != null) {
                    float[] cube = cubeFor(entity, tabulaHead);
                    if (cube != null) {
                        applyHorseBackCube(poseStack, cube);
                    }
                } else if (tabulaHead != null && HatManager.wearsHelmet(entity)) {
                    geometryScale = applyHelmetShellTabula(poseStack, tabulaHead);
                } else if (tabulaHead != null) {
                    geometryScale = TabulaHatPlacement.applyGeometryOffset(poseStack, tabulaHead);
                }
            } catch (Throwable ignored) {
            }
            // Hats Renewed snow golem placement scale.
            if (entity instanceof SnowGolem) {
                geometryScale *= 1.1F;
            }
            applySkinLayersClearance(poseStack, entity);
            TabulaHatPlacement.applyZFight(poseStack);
            TabulaHatRenderer.render(
                    poseStack,
                    collector,
                    light,
                    OverlayTexture.NO_OVERLAY,
                    hat,
                    geometryScale
            );
            poseStack.popPose();
            return;
        }

        ModelPart head = attachToHead(poseStack, entity);
        try {
            if (head != null) {
                applyHeadCubeCenter(poseStack, head, entity);
            }
        } catch (Throwable ignored) {
        }
        applySkinLayersClearance(poseStack, entity);
        applyHatItemOrientation(poseStack, hatOnBack(entity));
        if (hatOnBack(entity)) {
            glueHatToRump(poseStack, hat, entity);
        }

        submitHatItem(poseStack, collector, entity, hat, light, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }

    public static void submitHatItem(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            LivingEntity entity,
            ItemStack hat,
            int light,
            int overlay
    ) {
        ItemStackRenderState state = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForLiving(state, hat, ItemDisplayContext.HEAD, entity);
        submitResolved(poseStack, collector, state, hat, light, overlay);
    }

    public static void submitHatItem(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            ItemStack hat,
            int light,
            int overlay
    ) {
        submitHatItem(poseStack, collector, hat, light, overlay, 0);
    }

    public static void submitHatItem(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            ItemStack hat,
            int light,
            int overlay,
            int seed
    ) {
        ItemStackRenderState state = new ItemStackRenderState();
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getItemModelResolver().updateForTopItem(
                state, hat, ItemDisplayContext.HEAD, minecraft.level, null, seed);
        submitResolved(poseStack, collector, state, hat, light, overlay);
    }

    private static void submitResolved(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            ItemStackRenderState state,
            ItemStack hat,
            int light,
            int overlay
    ) {
        int alpha = HatColor.alpha(hat);
        if (alpha >= 255 || hasSpecialLayer(state)) {
            state.submit(poseStack, collector, light, overlay, 0);
            return;
        }
        float multiplier = Math.max(0, alpha) / 255.0F;
        for (ItemStackRenderState.LayerRenderState layer : itemLayers(state)) {
            List<BakedQuad> quads = layer.prepareQuadList();
            if (quads.isEmpty()) {
                continue;
            }
            ItemTransform transform = transformOf(layer);
            PoseStack transformed = new PoseStack();
            transformed.last().set(poseStack.last());
            transform.apply(false, transformed.last());
            collector.submitCustomGeometry(transformed, Sheets.translucentItemSheet(), (pose, buffer) -> {
                AlphaVertexConsumer consumer = new AlphaVertexConsumer(buffer, multiplier);
                for (BakedQuad quad : quads) {
                    Vector3fc normal = quad.direction().getUnitVec3f();
                    for (int vertex = 0; vertex < 4; vertex++) {
                        Vector3fc position = quad.position(vertex);
                        long packedUv = quad.packedUV(vertex);
                        int baked = quad.bakedColors().color(vertex);
                        consumer.addVertex(pose, position.x(), position.y(), position.z())
                                .setColor(baked)
                                .setUv(UVPair.unpackU(packedUv), UVPair.unpackV(packedUv))
                                .setOverlay(overlay)
                                .setLight(light)
                                .setNormal(pose, normal.x(), normal.y(), normal.z());
                    }
                }
            });
        }
    }

    static final class AlphaVertexConsumer implements VertexConsumer {

        private final VertexConsumer delegate;
        private final float alpha;

        private AlphaVertexConsumer(VertexConsumer delegate, float alpha) {
            this.delegate = delegate;
            this.alpha = alpha;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            delegate.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            delegate.setColor(red, green, blue, Math.round(alpha * this.alpha));
            return this;
        }

        @Override
        public VertexConsumer setColor(int color) {
            int a = (color >>> 24) & 0xFF;
            int scaled = Math.round(a * this.alpha);
            delegate.setColor((color & 0x00FFFFFF) | (scaled << 24));
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            delegate.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            delegate.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            delegate.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            delegate.setNormal(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            delegate.setLineWidth(width);
            return this;
        }
    }

    private static boolean hasSpecialLayer(ItemStackRenderState state) {
        try {
            Field field = ItemStackRenderState.LayerRenderState.class.getDeclaredField("specialRenderer");
            field.setAccessible(true);
            for (ItemStackRenderState.LayerRenderState layer : itemLayers(state)) {
                if (field.get(layer) != null) {
                    return true;
                }
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return false;
    }

    private static List<ItemStackRenderState.LayerRenderState> itemLayers(ItemStackRenderState state) {
        try {
            Field count = ItemStackRenderState.class.getDeclaredField("activeLayerCount");
            Field layers = ItemStackRenderState.class.getDeclaredField("layers");
            count.setAccessible(true);
            layers.setAccessible(true);
            int n = count.getInt(state);
            ItemStackRenderState.LayerRenderState[] all =
                    (ItemStackRenderState.LayerRenderState[]) layers.get(state);
            List<ItemStackRenderState.LayerRenderState> result = new ArrayList<>(n);
            for (int i = 0; i < n && i < all.length; i++) {
                result.add(all[i]);
            }
            return result;
        } catch (ReflectiveOperationException ignored) {
            return List.of();
        }
    }

    private static ItemTransform transformOf(ItemStackRenderState.LayerRenderState layer) {
        try {
            Field field = ItemStackRenderState.LayerRenderState.class.getDeclaredField("itemTransform");
            field.setAccessible(true);
            Object value = field.get(layer);
            if (value instanceof ItemTransform transform) {
                return transform;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return ItemTransform.NO_TRANSFORM;
    }

    /**
     * 8px skulls (player, cow, piglin): item origin at cube center, same as
     * vanilla HEAD items. Taller neck-pivot skulls (illager/villager 10px):
     * origin at cube top so hats perch on the crown instead of wrapping the
     * extra face. Scale is uniform from skull width so the hat keeps its
     * shape; ear children only shift the XZ center (piglin).
     */
    private void applyHeadCubeCenter(PoseStack poseStack, ModelPart head, LivingEntity entity) {
        float[] cube = cubeFor(entity, head);
        if (cube == null) {
            return;
        }

        // Real helmet: sit on the outer armor shell (skull + CubeDeformation 1),
        // same idea as shulker lids — bare heads keep the skull cube unchanged.
        if (HatManager.wearsHelmet(entity) && !hatOnBack(entity)) {
            cube = inflateCube(cube, HELMET_SHELL_INFLATE);
        }

        if (hatOnBack(entity)) {
            applyHorseBackCube(poseStack, cube);
            return;
        }

        if (entity instanceof Sheep sheep && !sheep.isSheared()) {
            applyWoollySheepCube(poseStack, head, cube);
            return;
        }

        // Snow golem: Hats Renewed — no pumpkin special case. Sit on the real
        // head cube (deflated snowball); pumpkin is just a layer around it.
        if (entity instanceof SnowGolem) {
            poseStack.translate(
                    (cube[0] + cube[3]) / 2.0F / 16.0F,
                    cubeOriginY(cube, false),
                    (cube[2] + cube[5]) / 2.0F / 16.0F
            );
            float scale = axisScale(cube[3] - cube[0]) * 1.1F;
            if (scale != 1.0F) {
                poseStack.scale(scale, scale, scale);
            }
            return;
        }

        float[] sit = usesFrontalSitCube(entity) ? frontalSitCube(cube) : cube;
        float[] horiz = usesFrontalSitCube(entity) || usesUprightSkullCube(entity)
                ? sit.clone()
                : horizontalBounds(head, sit);

        poseStack.translate(
                (horiz[0] + horiz[3]) / 2.0F / 16.0F,
                cubeOriginY(sit, bodyHat(entity)),
                (horiz[2] + horiz[5]) / 2.0F / 16.0F
        );

        // Scale from the full skull/body cube so frontal Z bias does not
        // shrink guardian/camel hats down to an 8px slab.
        if (bodyHat(entity)) {
            applyBodyHatScale(poseStack, cube);
        } else {
            applyUniformHeadScale(poseStack, cube);
        }
    }

    private static boolean hatOnBack(LivingEntity entity) {
        return entity.getType() == EntityType.HORSE
                || entity.getType() == EntityType.DONKEY
                || entity.getType() == EntityType.MULE
                || entity.getType() == EntityType.SKELETON_HORSE
                || entity.getType() == EntityType.ZOMBIE_HORSE;
    }

    /**
     * 1.7.10 glued every hat's wearable origin to the rump — it never measured
     * hat bottoms. Face hats hang from that origin, crown hats sit on it.
     * The rear face of the body cube is the edge; sitting there hangs off the
     * butt. Place the origin on the top of the back, inset so an 8px sit
     * volume stays on the rump.
     */
    private static void applyHorseBackCube(PoseStack poseStack, float[] cube) {
        poseStack.translate(
                (cube[0] + cube[3]) / 2.0F / 16.0F,
                cube[1] / 16.0F,
                (cube[5] - PLAYER_HEAD_SIZE / 2.0F) / 16.0F
        );
    }

    /**
     * Skull cube plus non-hat children (piglin ears, including their pose
     * rotation). The illager "hat" overlay is an invisible 12px box and must
     * not drive placement.
     */
    private static float[] horizontalBounds(ModelPart head, float[] skull) {
        float[] bounds = skull.clone();
        Map<String, ModelPart> children = childrenOf(head);
        if (!children.isEmpty()) {
            for (Map.Entry<String, ModelPart> entry : children.entrySet()) {
                if (skipHorizChild(entry.getKey())) {
                    continue;
                }
                includePartBounds(bounds, entry.getValue());
            }
        }
        for (String name : EAR_CHILD_NAMES) {
            if (children.containsKey(name)) {
                continue;
            }
            try {
                includePartBounds(bounds, head.getChild(name));
            } catch (Exception ignored) {
            }
        }
        return bounds;
    }

    private static final String[] EAR_CHILD_NAMES = {
            "left_ear", "right_ear", "leftEar", "rightEar"
    };

    private static boolean skipHorizChild(String name) {
        String key = name.toLowerCase();
        return key.equals("hat")
                || key.equals("inside_cube")
                || key.equals("tail")
                || key.startsWith("layer")
                || key.startsWith("spike")
                || key.contains("nose")
                || key.contains("tentacle")
                || key.contains("wattle")
                || key.contains("crest")
                || key.contains("comb")
                || key.contains("beak")
                || key.contains("gill")
                || key.contains("antenna")
                || key.contains("tendril")
                || key.contains("freshlx");
    }

    private static void includePartBounds(float[] bounds, ModelPart part) {
        org.joml.Quaternionf rotation = new org.joml.Quaternionf()
                .rotationZYX(part.zRot, part.yRot, part.xRot);
        org.joml.Vector3f point = new org.joml.Vector3f();

        for (ModelPart.Cube cube : cubesOf(part)) {
            for (float x : new float[]{cube.minX, cube.maxX}) {
                for (float y : new float[]{cube.minY, cube.maxY}) {
                    for (float z : new float[]{cube.minZ, cube.maxZ}) {
                        point.set(x, y, z);
                        rotation.transform(point);
                        point.add(part.x, part.y, part.z);
                        bounds[0] = Math.min(bounds[0], point.x);
                        bounds[1] = Math.min(bounds[1], point.y);
                        bounds[2] = Math.min(bounds[2], point.z);
                        bounds[3] = Math.max(bounds[3], point.x);
                        bounds[4] = Math.max(bounds[4], point.y);
                        bounds[5] = Math.max(bounds[5], point.z);
                    }
                }
            }
        }

        for (ModelPart child : childrenOf(part).values()) {
            includePartBounds(bounds, child);
        }
    }

    private static void applyUniformHeadScale(PoseStack poseStack, float[] cube) {
        float scale = axisScale(cube[3] - cube[0]);
        if (scale != 1.0F) {
            poseStack.scale(scale, scale, scale);
        }
    }

    /**
     * Body-covering mobs (slime, guardian) should grow from the largest sit
     * axis so deep/tall shells get slime-like hat coverage instead of a
     * width-only miniature.
     */
    private static void applyBodyHatScale(PoseStack poseStack, float[] cube) {
        float w = cube[3] - cube[0];
        float h = cube[4] - cube[1];
        float d = cube[5] - cube[2];
        float scale = axisScale(Math.max(w, Math.max(h, d)));
        if (scale != 1.0F) {
            poseStack.scale(scale, scale, scale);
        }
    }

    /**
     * Tabula crown-sit on the inflated helmet shell instead of the bare skull.
     */
    private static float applyHelmetShellTabula(PoseStack poseStack, ModelPart head) {
        float[] skull = primaryCube(head);
        if (skull == null) {
            return TabulaHatPlacement.applyGeometryOffset(poseStack, head);
        }
        float[] shell = inflateCube(skull, HELMET_SHELL_INFLATE);
        poseStack.translate(
                (shell[0] + shell[3]) / 2.0F / 16.0F,
                shell[1] / 16.0F,
                (shell[2] + shell[5]) / 2.0F / 16.0F
        );
        return Mth.clamp((shell[3] - shell[0]) / PLAYER_HEAD_SIZE, 0.2F, 3.0F);
    }

    /**
     * 3D Skin Layers voxel heads only — helmet clearance is geometric now.
     */
    private static void applySkinLayersClearance(PoseStack poseStack, LivingEntity entity) {
        if (hatOnBack(entity)) {
            return;
        }
        float scale = SkinLayers3dHatCompat.playerHatScale(entity);
        if (scale > 1.0F) {
            poseStack.scale(scale, scale, scale);
        }
    }

    /**
     * HEAD items are authored for the center of an 8px player skull — 4px
     * below the crown, which is also half the hat's scaled height when the
     * hat is sized to the skull's width. Sit that volume flush with the
     * crown so a 6px chicken head does not stick out above a 4px hat.
     * Body cubes (slime, magma, ghast) stay on the true cube center.
     */
    private static float cubeOriginY(float[] cube, boolean bodyCube) {
        float width = cube[3] - cube[0];
        float height = cube[4] - cube[1];
        if (bodyCube) {
            return (cube[1] + cube[4]) / 2.0F / 16.0F;
        }
        // Short skulls (axolotl 5px) must use height, not width, or the sit
        // point overshoots past the crown.
        float hatHalf = Math.min(width, height) / 2.0F;
        if (cube[1] < 0.0F || cube[4] <= 1.0F) {
            return (cube[1] + hatHalf) / 16.0F;
        }
        return (cube[4] - hatHalf) / 16.0F;
    }

    private static boolean bodyHat(LivingEntity entity) {
        return entity instanceof Slime
                || entity instanceof MagmaCube
                || entity instanceof Guardian
                || entity.getType() == EntityType.GHAST;
    }

    private static boolean usesFrontalSitCube(LivingEntity entity) {
        return entity instanceof Guardian
                || entity.getType() == EntityType.BEE
                || entity.getType() == EntityType.CAMEL;
    }

    private static boolean usesUprightSkullCube(LivingEntity entity) {
        EntityType<?> type = entity.getType();
        return type == EntityType.CAMEL
                || type == EntityType.LLAMA
                || type == EntityType.TRADER_LLAMA;
    }

    private static float axisScale(float size) {
        return Mth.clamp(size / PLAYER_HEAD_SIZE, 0.2F, 3.0F);
    }

    /** Only the first attached copy of this layer draws, if one was added twice. */
    private boolean isPrimaryHatLayer() {
        int seen = 0;
        for (Object layer : layersOf(parent)) {
            if (!(layer instanceof HatRenderLayer)) {
                continue;
            }
            if (layer == this) {
                return seen == 0;
            }
            seen++;
        }
        return true;
    }

    private float[] cubeFor(LivingEntity entity, ModelPart head) {
        if (hatOnBack(entity)) {
            float[] back = primaryCube(head);
            if (back != null) {
                return back;
            }
        }
        if (entity instanceof MagmaCube) {
            float[] fromAttached = collectMagmaBounds(head, 0.0F, 0.0F, 0.0F);
            if (fromAttached != null) {
                return fromAttached;
            }
            float[] shell = magmaShellCube();
            if (shell != null) {
                return shell;
            }
        } else if (entity instanceof Slime) {
            float[] shell = slimeOuterCube();
            if (shell != null) {
                return shell;
            }
            return SLIME_OUTER_CUBE.clone();
        }
        if (entity instanceof Sheep sheep && !sheep.isSheared()) {
            float[] wool = sheepWoolHeadCube(entity);
            if (wool != null) {
                return wool;
            }
            return inflateCube(SHEEP_WOOL_HEAD, SHEEP_WOOL_INFLATE);
        }
        if (usesUprightSkullCube(entity)) {
            float[] upright = uprightSkullCube(head);
            if (upright != null) {
                return upright;
            }
        }
        return primaryCube(head);
    }

    /**
     * Read the live sheep fur layer's head cubes so woolly hats sit on wool
     * and sheared hats fall back to the skull.
     */
    private float[] sheepWoolHeadCube(LivingEntity entity) {
        for (Object layer : layersOf(parent)) {
            if (!(layer instanceof SheepWoolLayer)) {
                continue;
            }
            Object furModel = entity.isBaby() ? namedEntityModelField(layer, "babyModel") : namedEntityModelField(layer, "adultModel");
            if (furModel == null) {
                furModel = firstEntityModelField(layer);
            }
            if (furModel instanceof HeadedModel headed) {
                float[] cube = primaryCube(headed.getHead());
                if (cube != null) {
                    return cube;
                }
            }
            if (furModel instanceof EntityModel<?> woolModel) {
                ModelPart woolHead = findDescendant(woolModel.root(), "head");
                if (woolHead != null) {
                    float[] cube = primaryCube(woolHead);
                    if (cube != null) {
                        return cube;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Vanilla slimes draw a 6px inner cube plus an 8px translucent shell.
     * Fresh Animations puts that shell on {@code body2} and squashes it on jump.
     */
    private float[] slimeOuterCube() {
        for (Object layer : layersOf(parent)) {
            if (!(layer instanceof SlimeOuterLayer)) {
                continue;
            }
            Object outerModel = firstEntityModelField(layer);
            if (outerModel instanceof EntityModel<?> entityModel) {
                ModelPart body2 = findDescendant(entityModel.root(), "body2");
                if (body2 != null) {
                    float[] box = largestCube(cubesOf(body2), 0.0F, 0.0F, 0.0F);
                    if (box != null) {
                        return box;
                    }
                }
                ModelPart cubePart = findDescendant(entityModel.root(), "cube");
                if (cubePart != null) {
                    float[] box = primaryCube(cubePart);
                    if (box != null) {
                        return box;
                    }
                }
                float[] fromRoot = primaryCube(entityModel.root());
                if (fromRoot != null) {
                    return fromRoot;
                }
            }
        }
        return null;
    }

    private static ModelPart findDescendant(ModelPart root, String... ids) {
        for (Map.Entry<String, ModelPart> entry : childrenOf(root).entrySet()) {
            String key = entry.getKey().toLowerCase();
            while (key.startsWith("emf_")) {
                key = key.substring(4);
            }
            int colon = key.lastIndexOf(':');
            if (colon >= 0 && colon + 1 < key.length()) {
                key = key.substring(colon + 1);
            }
            for (String id : ids) {
                if (key.equals(id)) {
                    return entry.getValue();
                }
            }
            ModelPart nested = findDescendant(entry.getValue(), ids);
            if (nested != null) {
                return nested;
            }
        }
        return null;
    }

    /**
     * Magma cubes are stacked 8x1x8 segments around a smaller inside cube.
     * Fresh Animations nests those slices under {@code core}, so walk the
     * whole tree (including {@code layer*} children) and ignore the dummy.
     */
    private float[] magmaShellCube() {
        EntityModel<?> model = getParentModel();
        return collectMagmaBounds(model.root(), 0.0F, 0.0F, 0.0F);
    }

    private static float[] collectMagmaBounds(ModelPart part, float ox, float oy, float oz) {
        float[] bounds = null;
        for (ModelPart.Cube cube : cubesOf(part)) {
            bounds = unionBounds(bounds, new float[]{
                    cube.minX + ox, cube.minY + oy, cube.minZ + oz,
                    cube.maxX + ox, cube.maxY + oy, cube.maxZ + oz
            });
        }
        for (Map.Entry<String, ModelPart> entry : childrenOf(part).entrySet()) {
            String key = entry.getKey().toLowerCase();
            if (key.contains("freshlx")) {
                continue;
            }
            ModelPart child = entry.getValue();
            bounds = unionBounds(
                    bounds,
                    collectMagmaBounds(child, ox + child.x, oy + child.y, oz + child.z)
            );
        }
        return bounds;
    }

    private static float[] unionBounds(float[] current, float[] box) {
        if (current == null) {
            return box;
        }
        current[0] = Math.min(current[0], box[0]);
        current[1] = Math.min(current[1], box[1]);
        current[2] = Math.min(current[2], box[2]);
        current[3] = Math.max(current[3], box[3]);
        current[4] = Math.max(current[4], box[4]);
        current[5] = Math.max(current[5], box[5]);
        return current;
    }

    private static ModelPart largestSolidChild(ModelPart root) {
        ModelPart best = null;
        float bestVol = 0.0F;
        for (Map.Entry<String, ModelPart> entry : childrenOf(root).entrySet()) {
            if (skipHorizChild(entry.getKey())) {
                continue;
            }
            float[] cube = largestCube(cubesOf(entry.getValue()), 0.0F, 0.0F, 0.0F);
            if (cube == null) {
                continue;
            }
            float vol = (cube[3] - cube[0]) * (cube[4] - cube[1]) * (cube[5] - cube[2]);
            if (vol > bestVol) {
                bestVol = vol;
                best = entry.getValue();
            }
        }
        return best;
    }

    private static List<ModelPart> buildChain(ModelPart root, ModelPart target) {
        List<ModelPart> chain = new ArrayList<>();
        if (buildChainInto(root, target, chain)) {
            return chain;
        }
        return null;
    }

    private static boolean buildChainInto(ModelPart current, ModelPart target, List<ModelPart> chain) {
        chain.add(current);
        if (current == target) {
            return true;
        }
        for (ModelPart child : childrenOf(current).values()) {
            if (buildChainInto(child, target, chain)) {
                return true;
            }
        }
        chain.remove(chain.size() - 1);
        return false;
    }

    /**
     * Same orientation Simple Hats uses for HEAD items, minus the player-only
     * {@code -0.25} Y offset. That offset is already handled by centering on
     * the real cube. Villager {@code 0.1875} is vanilla CustomHeadLayer's
     * extra shove for profession hats — not head geometry.
     * <p>
     * Horse-family hats skip the extra yaw so they face the tail, matching
     * 1.7.10 {@code HelperHorse} ({@code renderYawOffset + 180}).
     */
    private static void applyHatItemOrientation(PoseStack poseStack, boolean faceTail) {
        if (!faceTail) {
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        }
        poseStack.scale(ITEM_HEAD_SCALE, -ITEM_HEAD_SCALE, -ITEM_HEAD_SCALE);
    }

    /**
     * 1.7.10 glued a shared wearable origin to the rump. JSON hats encode that
     * origin with {@code display.head} plus cubes, so the same rump point is a
     * tophat brim, a raincloud, or a fox chin. Sit on the lowest vertex that
     * still overlaps an 8px player head — not the hat's own outline, which is
     * how jester points and tentacles lifted the whole model.
     */
    private static void glueHatToRump(PoseStack poseStack, ItemStack hat, LivingEntity entity) {
        try {
            ItemStackRenderState state = new ItemStackRenderState();
            Minecraft.getInstance().getItemModelResolver().updateForLiving(state, hat, ItemDisplayContext.HEAD, entity);
            List<ItemStackRenderState.LayerRenderState> layers = itemLayers(state);
            if (layers.isEmpty()) {
                return;
            }
            ItemTransform head = transformOf(layers.get(0));
            List<org.joml.Vector3f> verts = bakedVertices(layers);
            if (verts.isEmpty()) {
                return;
            }

            PoseStack scratch = new PoseStack();
            head.apply(false, scratch.last());
            scratch.translate(-0.5F, -0.5F, -0.5F);
            org.joml.Matrix4f matrix = scratch.last().pose();

            float cx = head.translation().x();
            float cz = head.translation().z();
            float radius = HEAD_SIT_RADIUS * Math.max(head.scale().x(), 1.0F);

            float sitY = Float.POSITIVE_INFINITY;
            float nearestDist = Float.POSITIVE_INFINITY;
            float nearestY = 0.0F;
            org.joml.Vector4f point = new org.joml.Vector4f();
            for (org.joml.Vector3f vert : verts) {
                point.set(vert.x, vert.y, vert.z, 1.0F);
                matrix.transform(point);
                float dx = point.x - cx;
                float dz = point.z - cz;
                float dist = Mth.sqrt(dx * dx + dz * dz);
                if (dist <= radius) {
                    sitY = Math.min(sitY, point.y);
                }
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearestY = point.y;
                }
            }
            if (sitY == Float.POSITIVE_INFINITY) {
                sitY = nearestY;
            }
            if (sitY != 0.0F && sitY != Float.POSITIVE_INFINITY) {
                poseStack.translate(0.0F, -sitY, 0.0F);
            }
        } catch (Throwable ignored) {
        }
    }

    private static List<org.joml.Vector3f> bakedVertices(List<ItemStackRenderState.LayerRenderState> layers) {
        List<org.joml.Vector3f> verts = new ArrayList<>();
        for (ItemStackRenderState.LayerRenderState layer : layers) {
            collectVertices(layer.prepareQuadList(), verts);
        }
        return verts;
    }

    private static void collectVertices(List<BakedQuad> quads, List<org.joml.Vector3f> verts) {
        if (quads == null || quads.isEmpty()) {
            return;
        }
        for (BakedQuad quad : quads) {
            for (int i = 0; i < 4; i++) {
                Vector3fc position = quad.position(i);
                verts.add(new org.joml.Vector3f(position.x(), position.y(), position.z()));
            }
        }
    }

    private ModelPart attachToHead(PoseStack poseStack, LivingEntity entity) {
        return attachToHead(poseStack, entity, false);
    }

    private ModelPart attachToHead(PoseStack poseStack, LivingEntity entity, boolean forceHead) {
        EntityModel<?> model = getParentModel();

        if (MoBendsHatCompat.isMoBendsLoaded() && MoBendsHatCompat.tryApplyAnimatedMobendsHead(poseStack)) {
            if (model instanceof HeadedModel headed) {
                return headed.getHead();
            }
            // Mo' Bends attaches spiders at the animated head pivot. Return the
            // vanilla reference cube without applying its pose so the existing
            // cube-centering step still moves the hat from that pivot to the
            // center/front of the spider's skull.
            if (entity instanceof Spider) {
                try {
                    return model.root().getChild("head");
                } catch (Exception ignored) {
                }
            }
            return null;
        }

        if (!forceHead && hatOnBack(entity)) {
            applyBabyBodyTransforms(poseStack, model, entity);
            ModelPart body = findHorseBody(model);
            if (body != null) {
                body.translateAndRotate(poseStack);
            }
            return body;
        }

        if (entity instanceof Rabbit) {
            // RabbitModel is not HeadedModel, and production renames the head
            // field. The 0.6 scale lives inside renderToBuffer, so layers never
            // inherit it — without both, the hat sits at the body origin.
            applyRabbitRenderScale(poseStack, entity.isBaby());
            ModelPart head = findDescendant(model.root(), "head");
            if (head == null) {
                head = findRabbitHead(model);
            }
            if (head != null) {
                head.translateAndRotate(poseStack);
            }
            return head;
        }

        ModelPart freshAnimationsHead = FreshAnimationsHatCompat.tryAttachAnimatedHead(poseStack, model);
        if (freshAnimationsHead != null) {
            return freshAnimationsHead;
        }

        applyMobModelScale(poseStack, model, entity);

        if (entity.getType() == EntityType.AXOLOTL) {
            // headParts() is empty; head is a child of body and only renders
            // through body.render(). Hats must apply body then head.
            ModelPart body = null;
            ModelPart axolotlRoot = model.root();
            if (axolotlRoot != null && axolotlRoot.hasChild("body")) {
                body = axolotlRoot.getChild("body");
            }
            if (body == null) {
                body = findNamedPart(model, "body");
            }
            ModelPart head = findNamedPart(model, "head");
            if (body != null) {
                body.translateAndRotate(poseStack);
            }
            if (head != null) {
                head.translateAndRotate(poseStack);
                return head;
            }
            return body;
        }

        if (entity instanceof Shulker) {
            // Field name "lid" is official-only. Production SRG renames the
            // field, so look up the baked bone name, which survives remapping.
            // The head stays inside the box; the lid is what opens.
            ModelPart lid = findBone(model, "lid");
            if (lid != null) {
                List<ModelPart> chain = buildChain(model.root(), lid);
                if (chain != null) {
                    for (ModelPart bone : chain) {
                        bone.translateAndRotate(poseStack);
                    }
                    return lid;
                }
                lid.translateAndRotate(poseStack);
                return lid;
            }
        }

        if (entity instanceof Slime || entity instanceof MagmaCube) {
            ModelPart root = model.root();
            root.translateAndRotate(poseStack);
            return root;
        }

        if (entity.getType() == EntityType.SILVERFISH) {
            ModelPart root = model.root();
            ModelPart head = findDescendant(root, "segment0");
            if (head != null) {
                List<ModelPart> chain = buildChain(root, head);
                if (chain != null) {
                    for (ModelPart bone : chain) {
                        bone.translateAndRotate(poseStack);
                    }
                } else {
                    root.translateAndRotate(poseStack);
                    head.translateAndRotate(poseStack);
                }
                return head;
            }
        }

        if (model instanceof HeadedModel headed) {
            ModelPart head = headed.getHead();
            List<ModelPart> chain = buildChain(model.root(), head);
            if (chain != null && chain.size() > 1) {
                for (ModelPart bone : chain) {
                    bone.translateAndRotate(poseStack);
                }
                return head;
            }
            head.translateAndRotate(poseStack);
            return head;
        }

        {
            ModelPart root = model.root();
            ModelPart target = null;
            for (String name : HEAD_PART_NAMES) {
                ModelPart found = findDescendant(root, name);
                if (found != null) {
                    target = found;
                    break;
                }
            }
            if (target == null) {
                target = largestSolidChild(root);
            }
            if (target != null) {
                List<ModelPart> chain = buildChain(root, target);
                if (chain != null) {
                    for (ModelPart bone : chain) {
                        bone.translateAndRotate(poseStack);
                    }
                } else {
                    root.translateAndRotate(poseStack);
                    target.translateAndRotate(poseStack);
                }
                return target;
            }
        }

        if (model instanceof HeadedModel headedFallback) {
            ModelPart head = headedFallback.getHead();
            if (head != null) {
                head.translateAndRotate(poseStack);
                return head;
            }
        }

        ModelPart fallback = findHeadPart(model);
        if (fallback != null) {
            fallback.translateAndRotate(poseStack);
        }
        return fallback;
    }

    /**
     * Wool is a shorter box than the skull, so its Z-center sits toward the
     * nape and leaves the snout uncovered. Sit on wool in Y, keep the hat
     * centered on the skull in XZ, and scale from the puff.
     */
    private void applyWoollySheepCube(PoseStack poseStack, ModelPart head, float[] wool) {
        float[] skull = primaryCube(head);
        float originX;
        float originZ;
        if (skull != null) {
            originX = (skull[0] + skull[3]) / 2.0F / 16.0F;
            originZ = (skull[2] + skull[5]) / 2.0F / 16.0F;
        } else {
            originX = (wool[0] + wool[3]) / 2.0F / 16.0F;
            originZ = (wool[2] + wool[5]) / 2.0F / 16.0F;
        }

        poseStack.translate(originX, cubeOriginY(wool, false), originZ);
        applyUniformHeadScale(poseStack, wool);
    }

    /**
     * Same matrices {@code RabbitModel.renderToBuffer} applies around every part.
     * Ears and nose are siblings of the head, all pivoted at (0, 16, -1); the
     * skull is the largest cube there.
     */
    private static void applyRabbitRenderScale(PoseStack poseStack, boolean baby) {
        if (baby) {
            poseStack.scale(0.56666666F, 0.56666666F, 0.56666666F);
            poseStack.translate(0.0F, 1.375F, 0.125F);
        } else {
            poseStack.scale(0.6F, 0.6F, 0.6F);
            poseStack.translate(0.0F, 1.0F, 0.0F);
        }
    }

    private static ModelPart findRabbitHead(EntityModel<?> model) {
        ModelPart best = null;
        float bestVolume = 0.0F;
        Class<?> search = model.getClass();
        while (search != null && search != Object.class) {
            for (Field field : search.getDeclaredFields()) {
                if (!ModelPart.class.isAssignableFrom(field.getType())) {
                    continue;
                }
                field.setAccessible(true);
                try {
                    if (!(field.get(model) instanceof ModelPart part)) {
                        continue;
                    }
                    if (Math.abs(part.y - 16.0F) > 0.5F || Math.abs(part.z + 1.0F) > 0.5F) {
                        continue;
                    }
                    float[] cube = primaryCube(part);
                    if (cube == null) {
                        continue;
                    }
                    float volume = Math.abs((cube[3] - cube[0]) * (cube[4] - cube[1]) * (cube[5] - cube[2]));
                    if (volume > bestVolume) {
                        bestVolume = volume;
                        best = part;
                    }
                } catch (IllegalAccessException ignored) {
                }
            }
            search = search.getSuperclass();
        }
        return best;
    }

    /**
     * Same matrices {@code AgeableListModel}/{@code AgeableHierarchicalModel}
     * apply to baby head parts. Hats Renewed copies this so hats inherit the
     * baby head scale and neck offset instead of sitting at the adult pose.
     * Axolotl / bee nest the head under the body, so babies use the body scale path.
     * Rabbits are handled earlier: their scale is inside {@code RabbitModel.renderToBuffer}.
     */
    private void applyMobModelScale(PoseStack poseStack, EntityModel<?> model, LivingEntity entity) {
        if (usesBodyBabyScale(entity)) {
            applyBabyBodyTransforms(poseStack, model, entity);
            return;
        }
        applyBabyHeadTransforms(poseStack, model, entity);
    }

    private static boolean usesBodyBabyScale(LivingEntity entity) {
        EntityType<?> type = entity.getType();
        return type == EntityType.AXOLOTL || type == EntityType.BEE;
    }

    private void applyBabyHeadTransforms(PoseStack poseStack, EntityModel<?> model, LivingEntity entity) {
        if (!entity.isBaby()) {
            return;
        }

        if (YOUNG_SCALE_FACTOR_FIELD != null && YOUNG_SCALE_FACTOR_FIELD.getDeclaringClass().isInstance(model)) {
            applyAgeableHierarchicalBabyTransforms(poseStack, model);
            return;
        }

        if (SCALE_HEAD_FIELD == null || !SCALE_HEAD_FIELD.getDeclaringClass().isInstance(model)) {
            return;
        }

        if (readBoolean(model, SCALE_HEAD_FIELD)) {
            float s = 1.5F / readFloat(model, BABY_HEAD_SCALE_FIELD, 2.0F);
            poseStack.scale(s, s, s);
        }
        poseStack.translate(
                0.0F,
                readFloat(model, BABY_Y_HEAD_OFFSET_FIELD, 0.0F) / 16.0F,
                readFloat(model, BABY_Z_HEAD_OFFSET_FIELD, 0.0F) / 16.0F
        );
    }

    private void applyBabyBodyTransforms(PoseStack poseStack, EntityModel<?> model, LivingEntity entity) {
        if (!entity.isBaby()) {
            return;
        }
        if (BABY_BODY_SCALE_FIELD == null || !BABY_BODY_SCALE_FIELD.getDeclaringClass().isInstance(model)) {
            return;
        }
        float bodyScale = readFloat(model, BABY_BODY_SCALE_FIELD, 1.0F);
        if (bodyScale != 0.0F && bodyScale != 1.0F) {
            float s = 1.0F / bodyScale;
            poseStack.scale(s, s, s);
        }
        poseStack.translate(0.0F, readFloat(model, LIST_BODY_Y_OFFSET_FIELD, 0.0F) / 16.0F, 0.0F);
    }

    private void applyAgeableHierarchicalBabyTransforms(PoseStack poseStack, EntityModel<?> model) {
        float scale = readFloat(model, YOUNG_SCALE_FACTOR_FIELD, 1.0F);
        float yOff = readFloat(model, HIER_BODY_Y_OFFSET_FIELD, 0.0F);
        if (scale != 0.0F && scale != 1.0F) {
            poseStack.scale(scale, scale, scale);
        }
        if (yOff != 0.0F) {
            poseStack.translate(0.0F, yOff / 16.0F, 0.0F);
        }
    }

    private static Field CUBES_FIELD;
    private static Field CHILDREN_FIELD;
    private static Field LAYERS_FIELD;
    private static Field SCALE_HEAD_FIELD;
    private static Field BABY_Y_HEAD_OFFSET_FIELD;
    private static Field BABY_Z_HEAD_OFFSET_FIELD;
    private static Field BABY_HEAD_SCALE_FIELD;
    private static Field BABY_BODY_SCALE_FIELD;
    private static Field LIST_BODY_Y_OFFSET_FIELD;
    private static Field YOUNG_SCALE_FACTOR_FIELD;
    private static Field HIER_BODY_Y_OFFSET_FIELD;

    static {
        CUBES_FIELD = findTypedField(ModelPart.class, List.class, "cubes", "f_104212_");
        CHILDREN_FIELD = findTypedField(ModelPart.class, Map.class, "children", "f_104213_");
        SCALE_HEAD_FIELD = findField(BabyModelTransform.class, "scaleHead", "f_102007_");
        BABY_Y_HEAD_OFFSET_FIELD = findField(BabyModelTransform.class, "babyYHeadOffset", "f_170338_");
        BABY_Z_HEAD_OFFSET_FIELD = findField(BabyModelTransform.class, "babyZHeadOffset", "f_170339_");
        BABY_HEAD_SCALE_FIELD = findField(BabyModelTransform.class, "babyHeadScale", "f_102010_");
        BABY_BODY_SCALE_FIELD = findField(BabyModelTransform.class, "babyBodyScale", "f_102011_");
        LIST_BODY_Y_OFFSET_FIELD = findField(BabyModelTransform.class, "bodyYOffset", "f_102012_");
        YOUNG_SCALE_FACTOR_FIELD = findField(BabyModelTransform.class, "youngScaleFactor", "f_271539_");
        HIER_BODY_Y_OFFSET_FIELD = findField(BabyModelTransform.class, "bodyYOffset", "f_271124_");
        LAYERS_FIELD = findTypedField(LivingEntityRenderer.class, List.class, "layers", "f_115291_");
    }

    /**
     * Official names work in {@code runClient}. Production only has SRG names, so
     * also match by type: a wrong cubes SRG used to bind the children map, skip
     * cube-centering, and leave hats on the nape pivot.
     */
    private static Field findTypedField(Class<?> owner, Class<?> type, String... names) {
        for (String name : names) {
            try {
                Field field = owner.getDeclaredField(name);
                if (type.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    return field;
                }
            } catch (NoSuchFieldException ignored) {
            }
        }
        for (Field field : owner.getDeclaredFields()) {
            if (type.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                return field;
            }
        }
        return null;
    }

    private static Field findField(Class<?> owner, String official, String srg) {
        for (String name : new String[]{official, srg}) {
            try {
                Field field = owner.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }

    private static boolean readBoolean(Object owner, Field field) {
        if (field == null) {
            return false;
        }
        try {
            return field.getBoolean(owner);
        } catch (IllegalAccessException ignored) {
            return false;
        }
    }

    private static float readFloat(Object owner, Field field, float fallback) {
        if (field == null) {
            return fallback;
        }
        try {
            return field.getFloat(owner);
        } catch (IllegalAccessException ignored) {
            return fallback;
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Object> layersOf(LivingEntityRenderer<?, ?, ?> renderer) {
        if (LAYERS_FIELD == null) {
            return List.of();
        }
        try {
            Object value = LAYERS_FIELD.get(renderer);
            if (value instanceof List<?> list) {
                return (List<Object>) list;
            }
        } catch (IllegalAccessException ignored) {
        }
        return List.of();
    }

    private static Object namedEntityModelField(Object layer, String name) {
        Class<?> search = layer.getClass();
        while (search != null && search != Object.class) {
            try {
                Field field = search.getDeclaredField(name);
                if (EntityModel.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    return field.get(layer);
                }
            } catch (ReflectiveOperationException ignored) {
            }
            search = search.getSuperclass();
        }
        return null;
    }

    private static Object firstEntityModelField(Object layer) {
        Class<?> search = layer.getClass();
        while (search != null && search != Object.class) {
            for (Field field : search.getDeclaredFields()) {
                if (!EntityModel.class.isAssignableFrom(field.getType())) {
                    continue;
                }
                field.setAccessible(true);
                try {
                    return field.get(layer);
                } catch (IllegalAccessException ignored) {
                }
            }
            search = search.getSuperclass();
        }
        return null;
    }

    private static float[] inflateCube(float[] cube, float inflate) {
        return new float[]{
                cube[0] - inflate, cube[1] - inflate, cube[2] - inflate,
                cube[3] + inflate, cube[4] + inflate, cube[5] + inflate
        };
    }

    /** Grow a cube around its center until every axis reaches {@code size}. */
    private static float[] expandCubeToSize(float[] cube, float size) {
        float cx = (cube[0] + cube[3]) / 2.0F;
        float cy = (cube[1] + cube[4]) / 2.0F;
        float cz = (cube[2] + cube[5]) / 2.0F;
        float half = size / 2.0F;
        return new float[]{cx - half, cy - half, cz - half, cx + half, cy + half, cz + half};
    }

    /**
     * Camel / llama heads store a long neck cube and a taller skull. Prefer the
     * upright skull so JSON hats sit on the face instead of mid-neck.
     */
    private static float[] uprightSkullCube(ModelPart head) {
        List<ModelPart.Cube> cubes = cubesOf(head);
        ModelPart.Cube best = null;
        float bestScore = Float.NEGATIVE_INFINITY;
        for (ModelPart.Cube cube : cubes) {
            float w = cube.maxX - cube.minX;
            float h = cube.maxY - cube.minY;
            float d = cube.maxZ - cube.minZ;
            if (h < d * 0.85F && h < w * 0.85F) {
                continue;
            }
            // Prefer taller-than-deep boxes that sit higher (more negative minY).
            float score = (h / Math.max(1.0F, d)) * 10.0F - cube.minY;
            if (score > bestScore) {
                bestScore = score;
                best = cube;
            }
        }
        if (best == null) {
            return null;
        }
        return new float[]{best.minX, best.minY, best.minZ, best.maxX, best.maxY, best.maxZ};
    }

    /**
     * Deep body cubes (bee, guardian, camel) push the sit origin toward the
     * rear. Keep full width/height so body-scale hats stay slime-sized; only
     * pull Z to an 8px-or-smaller front slab at minZ (model forward).
     */
    private static float[] frontalSitCube(float[] cube) {
        float w = cube[3] - cube[0];
        float h = cube[4] - cube[1];
        float d = cube[5] - cube[2];
        float sitD = Math.min(PLAYER_HEAD_SIZE, Math.min(w, Math.min(h, d)));
        return new float[]{
                cube[0], cube[1], cube[2],
                cube[3], cube[4], cube[2] + sitD
        };
    }

    @SuppressWarnings("unchecked")
    private static List<ModelPart.Cube> cubesOf(ModelPart part) {
        if (CUBES_FIELD == null) {
            return List.of();
        }
        try {
            Object value = CUBES_FIELD.get(part);
            if (value instanceof List<?> list) {
                return (List<ModelPart.Cube>) list;
            }
        } catch (IllegalAccessException ignored) {
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, ModelPart> childrenOf(ModelPart part) {
        if (CHILDREN_FIELD == null) {
            return Map.of();
        }
        try {
            Object value = CHILDREN_FIELD.get(part);
            if (value instanceof Map<?, ?> map) {
                return (Map<String, ModelPart>) map;
            }
        } catch (IllegalAccessException ignored) {
        }
        return Map.of();
    }

    private static float[] primaryCube(ModelPart head) {
        float[] fromCubes = largestCube(cubesOf(head), 0.0F, 0.0F, 0.0F);
        if (fromCubes != null) {
            return fromCubes;
        }

        float[] best = null;
        float bestVol = 0.0F;
        for (Map.Entry<String, ModelPart> entry : childrenOf(head).entrySet()) {
            if (skipHorizChild(entry.getKey())) {
                continue;
            }
            float[] cube = largestCube(cubesOf(entry.getValue()), entry.getValue().x, entry.getValue().y, entry.getValue().z);
            if (cube == null) {
                continue;
            }
            float vol = (cube[3] - cube[0]) * (cube[4] - cube[1]) * (cube[5] - cube[2]);
            if (vol > bestVol) {
                bestVol = vol;
                best = cube;
            }
        }
        return best;
    }

    private static float[] largestCube(List<ModelPart.Cube> cubes, float px, float py, float pz) {
        if (cubes == null || cubes.isEmpty()) {
            return null;
        }
        ModelPart.Cube best = null;
        float bestVol = 0.0F;
        for (ModelPart.Cube cube : cubes) {
            float vol = (cube.maxX - cube.minX) * (cube.maxY - cube.minY) * (cube.maxZ - cube.minZ);
            if (best == null || vol > bestVol) {
                best = cube;
                bestVol = vol;
            }
        }
        if (best == null) {
            return null;
        }
        return new float[]{
                best.minX + px, best.minY + py, best.minZ + pz,
                best.maxX + px, best.maxY + py, best.maxZ + pz
        };
    }

    private static ModelPart firstPart(Iterable<ModelPart> parts) {
        for (ModelPart part : parts) {
            return part;
        }
        return null;
    }

    /**
     * Horse-family models store the torso as {@code body}. That field name is
     * official-only; production SRG names fail {@link #findNamedPart}. Use the
     * AgeableList {@code bodyParts()} iterable (first entry is the torso) and
     * hierarchical names that survive remapping.
     */
    /**
     * Baked bone names ({@code lid}, {@code body}, {@code head}) survive
     * production remapping. Java field names do not.
     */
    private static ModelPart findBone(EntityModel<?> model, String name) {
        ModelPart found = findDescendant(model.root(), name);
        if (found != null) {
            return found;
        }
        return findNamedPart(model, name);
    }

    private static ModelPart findHorseBody(EntityModel<?> model) {
        ModelPart found = findDescendant(model.root(), "body");
        if (found != null) {
            return found;
        }

        ModelPart named = findNamedPart(model, "body");
        if (named != null) {
            return named;
        }

        try {
            ModelPart root = model.root();
            if (root != null && root.hasChild("body")) {
                return root.getChild("body");
            }
        } catch (Throwable ignored) {
        }

        return largestModelPartField(model);
    }

    private static ModelPart largestModelPartField(Object model) {
        ModelPart best = null;
        float bestVol = 0.0F;
        Class<?> search = model.getClass();
        while (search != null && search != Object.class) {
            for (Field field : search.getDeclaredFields()) {
                if (!ModelPart.class.isAssignableFrom(field.getType())) {
                    continue;
                }
                try {
                    field.setAccessible(true);
                    Object value = field.get(model);
                    if (!(value instanceof ModelPart part)) {
                        continue;
                    }
                    float[] cube = primaryCube(part);
                    if (cube == null) {
                        continue;
                    }
                    float vol = (cube[3] - cube[0]) * (cube[4] - cube[1]) * (cube[5] - cube[2]);
                    if (vol > bestVol) {
                        bestVol = vol;
                        best = part;
                    }
                } catch (Exception ignored) {
                }
            }
            search = search.getSuperclass();
        }
        return best;
    }

    private static ModelPart findNamedPart(Object model, String name) {
        Class<?> search = model.getClass();
        while (search != null && search != Object.class) {
            for (Field field : search.getDeclaredFields()) {
                if (!ModelPart.class.isAssignableFrom(field.getType())) {
                    continue;
                }
                if (!name.equals(field.getName())) {
                    continue;
                }
                try {
                    field.setAccessible(true);
                    Object value = field.get(model);
                    if (value instanceof ModelPart part) {
                        return part;
                    }
                } catch (Exception ignored) {
                }
            }
            search = search.getSuperclass();
        }
        return null;
    }

    private ModelPart findHeadPart(Object model) {
        if (model instanceof HeadedModel headed) {
            return headed.getHead();
        }

        Class<?> search = model.getClass();
        ModelPart firstPart = null;

        while (search != null && search != Object.class) {
            for (Field field : search.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(model);
                    if (!(value instanceof ModelPart part)) {
                        continue;
                    }

                    String name = field.getName().toLowerCase();
                    if (name.contains("head")) {
                        return part;
                    }
                    if (firstPart == null) {
                        firstPart = part;
                    }
                } catch (Exception ignored) {
                }
            }
            search = search.getSuperclass();
        }

        return firstPart;
    }
}
