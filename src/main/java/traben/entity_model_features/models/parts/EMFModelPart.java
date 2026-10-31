package traben.entity_model_features.models.parts;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.ModelPart;

import java.util.List;
import java.util.Map;

/**
 * Compile stand-in used only when {@code libs/entity_model_features} is absent.
 * Excluded from the mod jar so it never clashes with the real EMF class.
 */
public class EMFModelPart {

    public void renderLikeVanilla(
            PoseStack poseStack,
            VertexConsumer buffer,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        new ModelPart(List.of(), Map.of()).translateAndRotate(poseStack);
    }
}
