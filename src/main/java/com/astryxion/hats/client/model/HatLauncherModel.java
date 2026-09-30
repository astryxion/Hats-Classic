package com.astryxion.hats.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Tabula export of the 1.16.5 hat launcher. Body UVs are 64x32 on the
 * 256x128 {@code hat_launcher.png}; the side heads are 64x64 player-skin cubes.
 */
public class HatLauncherModel {

    public final ModelPart grip;
    public final ModelPart barrel;
    public final ModelPart gasR;
    public final ModelPart gasL;
    public final ModelPart headR;
    public final ModelPart headL;

    public HatLauncherModel() {
        ModelPart body = createBodyLayer().bakeRoot();
        grip = body.getChild("grip");
        barrel = body.getChild("barrel");
        gasR = barrel.getChild("gasR");
        gasL = barrel.getChild("gasL");

        ModelPart heads = createHeadsLayer().bakeRoot();
        headR = heads.getChild("headR");
        headL = heads.getChild("headL");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(
                "grip",
                CubeListBuilder.create().texOffs(50, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F),
                PartPose.ZERO);
        PartDefinition barrel = root.addOrReplaceChild(
                "barrel",
                CubeListBuilder.create().texOffs(32, 11).addBox(-2.0F, -2.0F, -4.5F, 4.0F, 4.0F, 7.0F),
                PartPose.rotation(0.0F, 0.0F, 0.7853982F));
        barrel.addOrReplaceChild(
                "gasL",
                CubeListBuilder.create().texOffs(30, 0).addBox(-6.0F, -1.0F, -3.2F, 4.0F, 2.0F, 2.0F),
                PartPose.rotation(0.0F, 0.0F, 1.5707964F));
        barrel.addOrReplaceChild(
                "gasR",
                CubeListBuilder.create().texOffs(30, 0).addBox(-6.0F, -1.0F, -3.2F, 4.0F, 2.0F, 2.0F),
                PartPose.ZERO);
        CubeDeformation glass = new CubeDeformation(0.0F, -0.25F, 0.0F);
        barrel.addOrReplaceChild(
                "glass1",
                CubeListBuilder.create().texOffs(32, 0).addBox(-2.0F, 1.75F, -14.0F, 4.0F, 1.0F, 10.0F, glass),
                PartPose.ZERO);
        barrel.addOrReplaceChild(
                "glass2",
                CubeListBuilder.create().texOffs(32, 0).addBox(-2.0F, -2.75F, -14.0F, 4.0F, 1.0F, 10.0F, glass),
                PartPose.ZERO);
        barrel.addOrReplaceChild(
                "glass3",
                CubeListBuilder.create().texOffs(32, 0).addBox(-2.0F, -2.75F, -14.0F, 4.0F, 1.0F, 10.0F, glass),
                PartPose.rotation(0.0F, 0.0F, -1.5707964F));
        barrel.addOrReplaceChild(
                "glass4",
                CubeListBuilder.create().texOffs(32, 0).addBox(-2.0F, -2.75F, -14.0F, 4.0F, 1.0F, 10.0F, glass),
                PartPose.rotation(0.0F, 0.0F, 1.5707964F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static LayerDefinition createHeadsLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("headL", headCubes(), PartPose.rotation(0.0F, (float) Math.PI, -1.5707964F));
        root.addOrReplaceChild("headR", headCubes(), PartPose.rotation(0.0F, (float) Math.PI, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    private static CubeListBuilder headCubes() {
        return CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -6.75F, -3.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-3.25F))
                .texOffs(32, 0).addBox(-4.0F, -6.75F, -3.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-3.15625F));
    }

    public void renderBody(PoseStack poseStack, VertexConsumer consumer, int light, int overlay) {
        grip.render(poseStack, consumer, light, overlay);
        barrel.render(poseStack, consumer, light, overlay);
    }

    public void setLeftHanded(boolean leftHand) {
        gasL.visible = leftHand;
        gasR.visible = !leftHand;
    }
}
