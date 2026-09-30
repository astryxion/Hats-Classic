package com.astryxion.hats.client.render;

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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class HatLauncherItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("hats", "textures/model/hat_launcher.png");
    private static final String TBL_PATH = "assets/hats/models/ModelHatLauncher.tbl";
    private static final ItemStack MAGMA = new ItemStack(Blocks.MAGMA_BLOCK);
    private static final Random RAND = new Random();
    private static final int RANDOM_SPEED = 5;

    private static HatLauncherItemRenderer instance;

    private final HatLauncherModel heads = new HatLauncherModel();
    private final TabulaHatModelData tblModel = loadTbl();

    public static HatLauncherItemRenderer getInstance() {
        if (instance == null) {
            instance = new HatLauncherItemRenderer();
        }
        return instance;
    }

    private HatLauncherItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext context,
            PoseStack poseStack,
            MultiBufferSource buffers,
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
            ticks = minecraft.level.getGameTime() + minecraft.getPartialTick();
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
        minecraft.getItemRenderer().renderStatic(
                MAGMA, ItemDisplayContext.FIXED, light, overlay, poseStack, buffers, minecraft.level, 0);
        poseStack.popPose();

        Set<String> hidden = leftHand ? Set.of("gasR", "headR") : Set.of("gasL", "headL");
        TabulaHatRenderer.renderModelData(poseStack, buffers, light, overlay, tblModel, TEXTURE, hidden, stack.hasFoil());

        AbstractClientPlayer player = minecraft.player;
        if (player != null) {
            poseStack.pushPose();
            heads.barrel.translateAndRotate(poseStack);
            (leftHand ? heads.headL : heads.headR).render(
                    poseStack,
                    buffers.getBuffer(RenderType.entityCutout(player.getSkinTextureLocation())),
                    light,
                    overlay);
            poseStack.popPose();
            renderSelectedHat(poseStack, buffers, light, overlay, stack, leftHand, player);
        }

        poseStack.popPose();
    }

    private void renderSelectedHat(
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay,
            ItemStack launcher,
            boolean leftHand,
            AbstractClientPlayer player
    ) {
        ResourceLocation id = resolveDisplayedHat(launcher, player);
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
            TabulaHatRenderer.render(poseStack, buffers, light, overlay, hat, 1.0F);
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
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    hat, ItemDisplayContext.HEAD, light, overlay, poseStack, buffers,
                    Minecraft.getInstance().level, 0);
        }
        poseStack.popPose();
    }

    private static ResourceLocation resolveDisplayedHat(ItemStack launcher, AbstractClientPlayer player) {
        String stored = HatLaunch.storedId(launcher);
        if (!HatLaunch.RANDOM_ID.equals(stored)) {
            return ResourceLocation.tryParse(stored);
        }
        if (player != Minecraft.getInstance().player) {
            return null;
        }
        List<ResourceLocation> source = randomDisplaySource(player);
        if (source.isEmpty()) {
            return null;
        }
        long ticks = player.tickCount;
        RAND.setSeed((1342L + ticks) / RANDOM_SPEED);
        return source.get(RAND.nextInt(source.size()));
    }

    private static List<ResourceLocation> randomDisplaySource(AbstractClientPlayer player) {
        List<ResourceLocation> source = new ArrayList<>();
        if (!HatLaunch.useInventory(player)) {
            for (Item item : HatItemRegistry.getAllHats()) {
                ResourceLocation id = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(item);
                if (TradeHats.isHat(id)) {
                    source.add(id);
                }
            }
            return source;
        }
        ResourceLocation wearing = TradeHats.equippedId(player);
        for (ResourceLocation id : ClientHatUnlocks.all()) {
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

    private static TabulaHatModelData loadTbl() {
        try {
            var url = HatLauncherItemRenderer.class.getClassLoader().getResource(TBL_PATH);
            if (url == null) {
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
