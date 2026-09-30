package com.astryxion.hats.common.command;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.data.ServerHatUnlocks;
import com.astryxion.hats.common.hat.HatItem;
import com.astryxion.hats.common.hat.HatManager;
import com.astryxion.hats.common.network.HatPacketHandler;
import com.astryxion.hats.common.network.PacketRehatify;
import com.astryxion.hats.common.network.PacketSetUnlockedHats;
import com.astryxion.hats.common.registry.HatItemRegistry;
import com.astryxion.hats.common.trade.TradeHats;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

@Mod.EventBusSubscriber(modid = Hats.MODID)
public final class HatsCommand {

    private static final SimpleCommandExceptionType UNKNOWN_HAT =
            new SimpleCommandExceptionType(Component.translatable("commands.hats.unknownHat"));
    private static final SimpleCommandExceptionType NOT_LIVING =
            new SimpleCommandExceptionType(Component.translatable("commands.hats.get.fail"));

    private HatsCommand() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("hats")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("set")
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .then(Commands.argument("hat", StringArgumentType.word())
                                        .suggests(suggestHats(true))
                                        .executes(ctx -> setHat(ctx, false))
                                        .then(Commands.literal("silent")
                                                .executes(ctx -> setHat(ctx, true))))))
                .then(Commands.literal("get")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .executes(HatsCommand::getHat)))
                .then(Commands.literal("give")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("hat", StringArgumentType.word())
                                        .suggests(suggestHats(false))
                                        .executes(ctx -> give(ctx, 1))
                                        .then(Commands.argument("count", IntegerArgumentType.integer())
                                                .executes(ctx -> give(ctx, IntegerArgumentType.getInteger(ctx, "count")))))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("hat", StringArgumentType.word())
                                        .suggests(suggestHats(false))
                                        .executes(HatsCommand::remove))))
                .then(Commands.literal("clear")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(HatsCommand::clear))));
    }

    private static SuggestionProvider<CommandSourceStack> suggestHats(boolean includeNone) {
        return (ctx, builder) -> {
            List<String> names = new ArrayList<>();
            if (includeNone) {
                names.add("none");
            }
            for (Item item : HatItemRegistry.getAllHats()) {
                if (!(item instanceof HatItem)) {
                    continue;
                }
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
                if (id == null || !TradeHats.isHat(id)) {
                    continue;
                }
                names.add(id.getPath());
            }
            return SharedSuggestionProvider.suggest(names, builder);
        };
    }

    private static int setHat(CommandContext<CommandSourceStack> ctx, boolean silent)
            throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
        ResourceLocation id = parseHat(StringArgumentType.getString(ctx, "hat"), true);
        Item item = id == null ? null : TradeHats.item(id);
        int count = 0;
        for (Entity entity : targets) {
            if (!(entity instanceof LivingEntity living)) {
                continue;
            }
            HatManager.setHat(living, item);
            if (living instanceof ServerPlayer player) {
                PacketSetUnlockedHats.send(player);
            }
            if (!silent) {
                HatPacketHandler.CHANNEL.send(
                        PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> living),
                        new PacketRehatify(living.getId()));
            }
            count++;
        }
        int worn = count;
        ctx.getSource().sendSuccess(
                () -> Component.translatable("commands.hats.set.success", worn), true);
        return count;
    }

    private static int getHat(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(ctx, "target");
        if (!(entity instanceof LivingEntity living)) {
            throw NOT_LIVING.create();
        }
        ItemStack stack = HatManager.getHatStack(living);
        if (stack.isEmpty()) {
            ctx.getSource().sendSuccess(
                    () -> Component.translatable("commands.hats.get.empty", entity.getName()), true);
            return Command.SINGLE_SUCCESS;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        String shown = id == null ? stack.getHoverName().getString() : id.toString();
        if (living instanceof ServerPlayer player && id != null) {
            int copies = ServerHatUnlocks.get(player).count(player, id);
            ctx.getSource().sendSuccess(
                    () -> Component.translatable(
                            "commands.hats.get.value.count", entity.getName(), shown, copies),
                    true);
        } else {
            ctx.getSource().sendSuccess(
                    () -> Component.translatable("commands.hats.get.value", entity.getName(), shown),
                    true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int give(CommandContext<CommandSourceStack> ctx, int amount)
            throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        ResourceLocation id = parseHat(StringArgumentType.getString(ctx, "hat"), false);
        int next = ServerHatUnlocks.get(player).add(player, id, amount);
        PacketSetUnlockedHats.send(player);
        com.astryxion.hats.common.hat.HatAdvancements.onUnlockCounts(player, id);
        ctx.getSource().sendSuccess(
                () -> Component.translatable(
                        "commands.hats.give.success", id.toString(), next, player.getName()),
                true);
        return Command.SINGLE_SUCCESS;
    }

    private static int remove(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        ResourceLocation id = parseHat(StringArgumentType.getString(ctx, "hat"), false);
        ResourceLocation worn = HatManager.getEquippedHatId(player);
        boolean removed = ServerHatUnlocks.get(player).removeHat(player, id);
        if (!removed) {
            ctx.getSource().sendFailure(
                    Component.translatable("commands.hats.remove.fail", id.toString(), player.getName()));
            return 0;
        }
        if (id.equals(worn)) {
            HatManager.clearHat(player);
        }
        PacketSetUnlockedHats.send(player);
        ctx.getSource().sendSuccess(
                () -> Component.translatable("commands.hats.remove.success", id.toString(), player.getName()),
                true);
        return Command.SINGLE_SUCCESS;
    }

    private static int clear(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        int wiped = ServerHatUnlocks.get(player).clear(player);
        HatManager.clearHat(player);
        PacketSetUnlockedHats.send(player);
        ctx.getSource().sendSuccess(
                () -> Component.translatable("commands.hats.clear.success", wiped, player.getName()),
                true);
        return Command.SINGLE_SUCCESS;
    }

    private static ResourceLocation parseHat(String raw, boolean allowNone) throws CommandSyntaxException {
        String name = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        if (allowNone && (name.isEmpty() || name.equals("none") || name.equals("empty") || name.equals("off"))) {
            return null;
        }
        ResourceLocation id = name.contains(":")
                ? new ResourceLocation(name)
                : new ResourceLocation(Hats.MODID, name);
        if (!TradeHats.isHat(id)) {
            throw UNKNOWN_HAT.create();
        }
        return id;
    }
}
