package com.astryxion.hats.client.render;

import com.astryxion.hats.common.hatstand.HatStandEntity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class HatStandModel extends EntityModel<EntityRenderState> {

    private final ModelPart plate;
    private final ModelPart rod;
    private final ModelPart head;

    public HatStandModel(ModelPart root) {
        super(root);
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
    public void setupAnim(EntityRenderState state) {
        head.yRot = 0.0F;
        head.xRot = 0.0F;
    }
}
