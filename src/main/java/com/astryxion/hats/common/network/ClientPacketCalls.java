package com.astryxion.hats.common.network;

import com.astryxion.hats.Hats;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * Dedicated servers must not resolve client classes while payloads are registered.
 * Handlers are looked up by name, and only when this JVM is the physical client.
 */
public final class ClientPacketCalls {

    private static final String HANDLERS = "com.astryxion.hats.client.network.HatClientPacketHandlers";
    private static final String TRADE = "com.astryxion.hats.client.trade.ClientTradePackets";

    private ClientPacketCalls() {}

    public static void handlers(String method, Class<?>[] types, Object... args) {
        call(HANDLERS, method, types, args);
    }

    public static void trade(String method, Class<?> type, Object arg) {
        call(TRADE, method, new Class<?>[] {type}, arg);
    }

    public static void trade(String method, Class<?>[] types, Object... args) {
        call(TRADE, method, types, args);
    }

    private static void call(String className, String method, Class<?>[] types, Object... args) {
        if (FMLEnvironment.getDist() != Dist.CLIENT) {
            return;
        }
        try {
            Class.forName(className).getMethod(method, types).invoke(null, args);
        } catch (Throwable t) {
            Hats.LOGGER.error("Failed to dispatch client packet handler {}", method, t);
        }
    }
}
