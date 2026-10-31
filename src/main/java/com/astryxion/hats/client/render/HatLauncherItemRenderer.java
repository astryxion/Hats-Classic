package com.astryxion.hats.client.render;

import net.minecraft.core.registries.BuiltInRegistries;

import com.astryxion.hats.Hats;
import com.astryxion.hats.client.hat.ClientHatUnlocks;
import com.astryxion.hats.client.model.HatLauncherModel;
import com.astryxion.hats.client.tabula.TabulaHatRenderer;
import com.astryxion.hats.common.hat.HatItem;
import com.astryxion.hats.common.launcher.HatLaunch;
import com.astryxion.hats.common.registry.HatItemRegistry;
import com.astryxion.hats.common.tabula.TabulaHatModelData;
import com.astryxion.hats.common.tabula.TabulaTblReader;
import com.astryxion.hats.common.trade.TradeHats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

public class HatLauncherItemRenderer implements ItemModel, SpecialModelRenderer<HatLauncherItemRenderer.RenderArg> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("hats", "textures/model/hat_launcher.png");
    private static final String TBL_PATH = "assets/hats/models/ModelHatLauncher.tbl";
    private static ItemStack magmaStack;
    private static final Random RAND = new Random();
    private static final int RANDOM_SPEED = 5;
    private static final ItemTransforms TRANSFORMS = loadDisplay();
    private static final Vector3fc[] EXTENTS = new Vector3fc[]{
            new Vector3f(-1.0F, -1.0F, -1.0F),
            new Vector3f(1.0F, 1.0F, 1.0F)
    };

    private static HatLauncherItemRenderer instance;

    private final HatLauncherModel heads = new HatLauncherModel();
    private final TabulaHatModelData tblModel = loadTbl();

    private static ItemStack magmaStack() {
        if (magmaStack == null) {
            magmaStack = new ItemStack(Blocks.MAGMA_BLOCK);
        }
        return magmaStack;
    }

    public static HatLauncherItemRenderer getInstance() {
        if (instance == null) {
            instance = new HatLauncherItemRenderer();
        }
        return instance;
    }

    private HatLauncherItemRenderer() {}

    @Override
    public void update(
            ItemStackRenderState output,
            ItemStack item,
            ItemModelResolver resolver,
            ItemDisplayContext displayContext,
            @Nullable ClientLevel level,
            @Nullable ItemOwner owner,
            int seed
    ) {
        output.appendModelIdentityElement(item.getItem());
        output.setOversizedInGui(false);
        ItemStackRenderState.LayerRenderState layer = output.newLayer();
        layer.setItemTransform(TRANSFORMS.getTransform(displayContext));
        layer.setUsesBlockLight(false);
        layer.setExtents(() -> EXTENTS);
        layer.setupSpecialModel(this, new RenderArg(item, displayContext));
    }

    @Override
    public void submit(
            @Nullable RenderArg argument,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int light,
            int overlay,
            boolean hasFoil,
            int outlineColor
    ) {
        if (argument == null) {
            return;
        }
        render(argument.stack(), argument.context(), poseStack, collector, light, overlay);
    }

    public void render(
            ItemStack stack,
            ItemDisplayContext context,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int light,
            int overlay
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean leftHand = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
        heads.setLeftHanded(leftHand);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.scale(-1.0F, -1.0F, 1.0F);

        float ticks = 0.0F;
        if (minecraft.level != null) {
            ticks = minecraft.level.getGameTime() + minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        }

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, -0.14F);
        poseStack.scale(0.15F, 0.15F, 0.15F);
        poseStack.mulPose(Axis.ZP.rotation(leftHand ? -0.7853982F : 0.7853982F));
        poseStack.translate(
                (leftHand ? 1.75F : -1.75F)
                        + (float) Math.sin(Math.toRadians(ticks / 41.0F * 180.0F)) * (leftHand ? 0.35F : -0.35F),
                0.0F,
                0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) Math.sin(Math.toRadians(ticks / 37.0F * 180.0F)) * 180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees((float) Math.sin(Math.toRadians(ticks / 87.0F * 180.0F)) * 180.0F));
        ItemStackRenderState magma = new ItemStackRenderState();
        minecraft.getItemModelResolver().updateForTopItem(magma, magmaStack(), ItemDisplayContext.FIXED, minecraft.level, null, 0);
        magma.submit(poseStack, collector, light, overlay, 0);
        poseStack.popPose();

        Set<String> hidden = leftHand ? Set.of("gasR", "headR") : Set.of("gasL", "headL");
        TabulaHatRenderer.renderModelData(poseStack, collector, light, overlay, tblModel, TEXTURE, hidden, stack.hasFoil());

        AbstractClientPlayer player = minecraft.player;
        if (player != null) {
            poseStack.pushPose();
            heads.barrel.translateAndRotate(poseStack);
            Identifier skin = player.getSkin().body().texturePath();
            collector.submitModelPart(
                    leftHand ? heads.headL : heads.headR,
                    poseStack,
                    RenderTypes.entityCutoutCull(skin),
                    light,
                    overlay,
                    null
            );
            poseStack.popPose();
            renderSelectedHat(poseStack, collector, light, overlay, stack, leftHand, player);
        }

        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        output.accept(EXTENTS[0]);
        output.accept(EXTENTS[1]);
    }

    @Override
    public @Nullable RenderArg extractArgument(ItemStack stack) {
        return new RenderArg(stack, ItemDisplayContext.NONE);
    }

    public record RenderArg(ItemStack stack, ItemDisplayContext context) {}

    private void renderSelectedHat(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int light,
            int overlay,
            ItemStack launcher,
            boolean leftHand,
            AbstractClientPlayer player
    ) {
        Identifier id = resolveDisplayedHat(launcher, player);
        Item item = TradeHats.item(id);
        if (item == null || item == Items.AIR) {
            return;
        }
        ItemStack hat = new ItemStack(item);
        poseStack.pushPose();
        if (item instanceof HatItem hatItem && hatItem.isTabula()) {
            poseStack.translate(0.0F, 0.0F, -0.0625F);
            poseStack.scale(0.1876F, 0.1876F, 0.1876F);
            poseStack.mulPose(Axis.ZP.rotation(leftHand ? -0.7853982F : 0.7853982F));
            poseStack.translate(0.0F, -1.1875F, 0.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            TabulaHatRenderer.render(poseStack, collector, light, overlay, hat, 1.0F);
        } else {
            heads.barrel.translateAndRotate(poseStack);
            (leftHand ? heads.headL : heads.headR).translateAndRotate(poseStack);
            float inflate = -3.25F;
            float minX = -4.0F - inflate;
            float minY = -6.75F - inflate;
            float minZ = -3.0F - inflate;
            float maxX = 4.0F + inflate;
            float maxY = 1.25F + inflate;
            float maxZ = 5.0F + inflate;
            float width = maxX - minX;
            poseStack.translate(
                    (minX + maxX) / 2.0F / 16.0F,
                    (minY + width / 2.0F) / 16.0F,
                    (minZ + maxZ) / 2.0F / 16.0F);
            float sit = width / 8.0F;
            poseStack.scale(sit, sit, sit);
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            poseStack.scale(-0.625F, 0.625F, -0.625F);
            HatRenderLayer.submitHatItem(poseStack, collector, hat, light, overlay);
        }
        poseStack.popPose();
    }

    private static Identifier resolveDisplayedHat(ItemStack launcher, AbstractClientPlayer player) {
        String stored = HatLaunch.storedId(launcher);
        if (!HatLaunch.RANDOM_ID.equals(stored)) {
            return Identifier.tryParse(stored);
        }
        if (player != Minecraft.getInstance().player) {
            return null;
        }
        List<Identifier> source = randomDisplaySource(player);
        if (source.isEmpty()) {
            return null;
        }
        long ticks = player.tickCount;
        RAND.setSeed((1342L + ticks) / RANDOM_SPEED);
        return source.get(RAND.nextInt(source.size()));
    }

    private static List<Identifier> randomDisplaySource(AbstractClientPlayer player) {
        List<Identifier> source = new ArrayList<>();
        if (!HatLaunch.useInventory(player)) {
            for (Item item : HatItemRegistry.getAllHats()) {
                Identifier id = BuiltInRegistries.ITEM.getKey(item);
                if (TradeHats.isHat(id)) {
                    source.add(id);
                }
            }
            return source;
        }
        Identifier wearing = TradeHats.equippedId(player);
        for (Identifier id : ClientHatUnlocks.all()) {
            int available = ClientHatUnlocks.count(id);
            if (id.equals(wearing)) {
                available--;
            }
            if (available > 0) {
                source.add(id);
            }
        }
        return source;
    }

    private static ItemTransforms loadDisplay() {
        try (InputStream in = HatLauncherItemRenderer.class.getResourceAsStream("/assets/hats/models/item/hat_launcher.json")) {
            if (in == null) {
                return ItemTransforms.NO_TRANSFORMS;
            }
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            if (!root.has("display")) {
                return ItemTransforms.NO_TRANSFORMS;
            }
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(ItemTransform.class, new ItemTransform.Deserializer())
                    .registerTypeAdapter(ItemTransforms.class, new ItemTransforms.Deserializer())
                    .create();
            return gson.fromJson(root.get("display"), ItemTransforms.class);
        } catch (Exception ignored) {
            return ItemTransforms.NO_TRANSFORMS;
        }
    }

    private static TabulaHatModelData loadTbl() {
        try {
            var url = HatLauncherItemRenderer.class.getClassLoader().getResource(TBL_PATH);
            if (url == null) {
                var info = net.neoforged.fml.ModList.get().getModFileById(Hats.MODID);
                if (info != null) {
                    try (InputStream in = info.getFile().getContents().openFile(TBL_PATH)) {
                        if (in != null) {
                            return TabulaTblReader.readModel(in);
                        }
                    }
                }
                Hats.LOGGER.error("Missing {}", TBL_PATH);
                return new TabulaHatModelData();
            }
            if ("jar".equals(url.getProtocol())) {
                URI uri = url.toURI();
                String text = uri.toString();
                int sep = text.indexOf("!/");
                URI jarUri = URI.create(text.substring(0, sep));
                FileSystem fs;
                try {
                    fs = FileSystems.getFileSystem(jarUri);
                } catch (Exception e) {
                    fs = FileSystems.newFileSystem(jarUri, Map.of());
                }
                return TabulaTblReader.readModel(fs.getPath(TBL_PATH));
            }
            return TabulaTblReader.readModel(Path.of(url.toURI()));
        } catch (Exception e) {
            Hats.LOGGER.error("Failed to load hat launcher tabula model", e);
            return new TabulaHatModelData();
        }
    }
}
