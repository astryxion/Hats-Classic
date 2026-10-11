package com.astryxion.hats.common.network;

import com.astryxion.hats.common.sound.HatsSounds;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketRehatify implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketRehatify> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("hats", "rehatify"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketRehatify> STREAM_CODEC =
            StreamCodec.of(PacketRehatify::encode, PacketRehatify::decode);

    private final int entityId;

    public PacketRehatify(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(RegistryFriendlyByteBuf buf, PacketRehatify msg) {
        buf.writeInt(msg.entityId);
    }

    public static PacketRehatify decode(RegistryFriendlyByteBuf buf) {
        return new PacketRehatify(buf.readInt());
    }

    public static void handle(PacketRehatify msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (FMLEnvironment.getDist() == Dist.CLIENT) {
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
            }
        });
    }
}
