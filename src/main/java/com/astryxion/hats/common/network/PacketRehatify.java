package com.astryxion.hats.common.network;

import com.astryxion.hats.common.sound.HatsSounds;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketRehatify {

    private final int entityId;

    public PacketRehatify(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(PacketRehatify msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
    }

    public static PacketRehatify decode(FriendlyByteBuf buf) {
        return new PacketRehatify(buf.readInt());
    }

    public static void handle(PacketRehatify msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft.level == null) {
                return;
            }
            Entity entity = minecraft.level.getEntity(msg.entityId);
            if (!(entity instanceof LivingEntity living)) {
                return;
            }
            living.playSound(HatsSounds.POOF.get(), 1.0F,
                    1.0F + (living.getRandom().nextFloat() * 2F - 1F) * 0.15F);
            for (int i = 0; i < 20; i++) {
                minecraft.level.addParticle(ParticleTypes.POOF,
                        living.getRandomX(1.0D), living.getRandomY(), living.getRandomZ(1.0D),
                        living.getRandom().nextGaussian() * 0.02D,
                        living.getRandom().nextGaussian() * 0.02D,
                        living.getRandom().nextGaussian() * 0.02D);
            }
            for (int i = 0; i < 10; i++) {
                minecraft.level.addParticle(ParticleTypes.FIREWORK,
                        living.getRandomX(1.0D), living.getRandomY(), living.getRandomZ(1.0D),
                        living.getRandom().nextGaussian() * 0.03D,
                        living.getRandom().nextGaussian() * 0.03D,
                        living.getRandom().nextGaussian() * 0.03D);
            }
        }));
        ctx.get().setPacketHandled(true);
    }
}
