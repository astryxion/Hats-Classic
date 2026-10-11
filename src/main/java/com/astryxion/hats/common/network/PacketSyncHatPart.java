package com.astryxion.hats.common.network;

import com.astryxion.hats.common.hat.HatPart;
import com.astryxion.hats.common.hat.HatPartCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PacketSyncHatPart implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketSyncHatPart> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("hats", "sync_hat_part"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSyncHatPart> STREAM_CODEC =
            StreamCodec.of(PacketSyncHatPart::encode, PacketSyncHatPart::decode);

    private static final Map<Integer, Pending> PENDING = new HashMap<>();

    private final int playerId;
    private final CompoundTag tag;

    public PacketSyncHatPart(int playerId, CompoundTag tag) {
        this.playerId = playerId;
        this.tag = tag;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(RegistryFriendlyByteBuf buf, PacketSyncHatPart msg) {
        buf.writeInt(msg.playerId);
        buf.writeNbt(msg.tag);
    }

    public static PacketSyncHatPart decode(RegistryFriendlyByteBuf buf) {
        return new PacketSyncHatPart(buf.readInt(), buf.readNbt());
    }

    public static void handle(PacketSyncHatPart msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (FMLEnvironment.getDist() != Dist.CLIENT) {
                return;
            }
            if (!apply(msg.playerId, msg.tag) && msg.tag != null) {
                PENDING.put(msg.playerId, new Pending(msg.tag.copy()));
            }
        });
    }

    /** Spawn packets can arrive before the client has the entity. */
    public static void flushPending() {
        if (PENDING.isEmpty() || FMLEnvironment.getDist() != Dist.CLIENT) {
            return;
        }
        var mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level == null) {
            PENDING.clear();
            return;
        }
        Iterator<Map.Entry<Integer, Pending>> it = PENDING.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Pending> entry = it.next();
            if (apply(entry.getKey(), entry.getValue().tag) || ++entry.getValue().ticks > 40) {
                it.remove();
            }
        }
    }

    private static boolean apply(int entityId, CompoundTag tag) {
        var mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level == null || tag == null) {
            return false;
        }
        Entity entity = mc.level.getEntity(entityId);
        if (!(entity instanceof LivingEntity living)) {
            return false;
        }
        living.getData(HatPartCapability.HAT_PART).deserializeNBT(tag);
        return true;
    }

    private static final class Pending {
        private final CompoundTag tag;
        private int ticks;

        private Pending(CompoundTag tag) {
            this.tag = tag;
        }
    }
}
