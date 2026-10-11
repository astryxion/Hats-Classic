package com.astryxion.hats.mixin;

import java.util.List;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(HeadedModel.class)
public interface AgeableListModelAccessor {

    @Unique
    default Iterable<ModelPart> hats$headParts() {
        ModelPart head = ((HeadedModel) (Object) this).getHead();
        return head == null ? List.of() : List.of(head);
    }

    @Unique
    default Iterable<ModelPart> hats$bodyParts() {
        if (!(this instanceof EntityModel<?> model)) {
            return List.of();
        }
        ModelPart root = model.root();
        if (root != null && root.hasChild("body")) {
            return List.of(root.getChild("body"));
        }
        return List.of();
    }
}
