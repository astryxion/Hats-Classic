package com.astryxion.hats.client.render;

import com.astryxion.hats.common.hatstand.HatStandEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class HatStandModel extends EntityModel<HatStandEntity> {

    private final ModelPart plate;
    private final ModelPart rod;
    private final ModelPart head;

    public HatStandModel(ModelPart root) {
        plate = root.getChild("plate");
        rod = plate.getChild("rod");
        head = rod.getChild("head");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition plate = root.addOrReplaceChild(
                "plate",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4, 0, -4, 8, 1, 8),
                PartPose.offset(0, 23, 0)
        );
        PartDefinition rod = plate.addOrReplaceChild(
                "rod",
                CubeListBuilder.create().texOffs(0, 23).addBox(-1, -3, -1, 2, 3, 2),
                PartPose.ZERO
        );
        rod.addOrReplaceChild(
                "head",
                CubeListBuilder.create().texOffs(0, 9).addBox(-3.5F, -7, -3.5F, 7, 7, 7),
                PartPose.offset(0, -3, 0)
        );
        return LayerDefinition.create(mesh, 32, 32);
    }

    public void translateToHead(PoseStack stack) {
        plate.translateAndRotate(stack);
        rod.translateAndRotate(stack);
        head.translateAndRotate(stack);
    }

    public ModelPart getHead() {
        return head;
    }

    @Override
    public void setupAnim(HatStandEntity entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        head.xRot = headPitch * ((float) Math.PI / 180F);
    }

    @Override
    public void renderToBuffer(PoseStack stack, VertexConsumer consumer, int light, int overlay,
            float red, float green, float blue, float alpha) {
        plate.render(stack, consumer, light, overlay, red, green, blue, alpha);
    }
}
