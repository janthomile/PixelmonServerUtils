package supermemnon.pixelmonutils.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.battles.BattleType;
import com.pixelmonmod.pixelmon.api.command.PixelmonCommandUtils;
import com.pixelmonmod.pixelmon.api.events.battles.SpectateEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.api.util.helpers.NetworkHelper;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.api.rules.BattleRuleRegistry;
import com.pixelmonmod.pixelmon.battles.api.rules.teamselection.TeamSelectionRegistry;
import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.Spectator;
import com.pixelmonmod.pixelmon.blocks.tileentity.PokeChestTileEntity;
import com.pixelmonmod.pixelmon.client.gui.battles.PixelmonClientData;
import com.pixelmonmod.pixelmon.comm.packetHandlers.battles.*;
import com.pixelmonmod.pixelmon.command.impl.SpectateCommand;
import com.pixelmonmod.pixelmon.entities.npcs.NPCEntity;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import com.pixelmonmod.pixelmon.entities.pixelmon.StatueEntity;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import supermemnon.pixelmonutils.storage.PixelUtilsBlockData;
import supermemnon.pixelmonutils.util.AIOverrideUtil;
import supermemnon.pixelmonutils.util.NBTHelper;
import supermemnon.pixelmonutils.util.FormattingHelper;
import supermemnon.pixelmonutils.util.RayTraceHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class PixelmonUtilsCommand {

    public static final String permissionSpectate = "pixelmonutils.spectate";

    public static  SpectateOverride spectateOverride;

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        spectateOverride = new SpectateOverride(dispatcher);

        LiteralArgumentBuilder<CommandSource> commandStructure = Commands.literal("pixelmonutils").requires(source -> source.hasPermission(2));
        commandStructure = appendSetCommand(commandStructure);
        commandStructure = appendGetCommand(commandStructure);
        commandStructure = appendRemoveCommand(commandStructure);
        commandStructure = appendNPCBattleCommand(commandStructure);
        commandStructure = appendBetterSpectate(commandStructure);
        dispatcher.register(commandStructure);
    }

    private static LiteralArgumentBuilder<CommandSource> appendSetCommand(LiteralArgumentBuilder<CommandSource> command) {
           return command.then(Commands.literal("set")
                .then(Commands.literal("requireditem")
                        .executes(context -> runSetRequiredItem(context.getSource()))
                )
               .then(Commands.literal("pokelootcommand")
                       .then(Commands.argument("blockpos", BlockPosArgument.blockPos())
                               .then(Commands.argument("command", StringArgumentType.greedyString())
                                       .executes(
                                               context -> runSetPokeLootCommand(context.getSource(), BlockPosArgument.getOrLoadBlockPos(context, "blockpos"), StringArgumentType.getString(context, "command"))
                                       )
                               )
                       )
               )
               .then(Commands.literal("dialogue")
                       .then(Commands.argument("dialogue", StringArgumentType.greedyString())
                               .executes(context -> runSetCustomDialogue(context.getSource(), StringArgumentType.getString(context, "dialogue"))
                               )
                       )
               )
               .then(Commands.literal("npcstare")
                       .then(Commands.argument("entity", EntityArgument.entity())
                               .then(Commands.argument("blockpos", BlockPosArgument.blockPos())
                                       .executes(context -> runSetNpcStare(context.getSource(), EntityArgument.getEntity(context, "entity"),BlockPosArgument.getOrLoadBlockPos(context, "blockpos"))
                                       )
                               )
                       )
               )
           );
    }

    private static LiteralArgumentBuilder<CommandSource> appendGetCommand(LiteralArgumentBuilder<CommandSource> command) {
        return command.then(Commands.literal("get")
                .then(Commands.literal("requireditem")
                        .executes(context -> runGetRequiredItem(context.getSource()))
                )
                .then(Commands.literal("pokelootcommand")
                        .then(Commands.argument("blockpos", BlockPosArgument.blockPos())
                                .executes(
                                        context -> runGetPokeLootCommand(context.getSource(), BlockPosArgument.getOrLoadBlockPos(context, "blockpos"))
                                )
                        )
                )
                .then(Commands.literal("dialogue")
                        .executes(context -> runGetCustomDialogue(context.getSource()))
                )
                .then(Commands.literal("npcstare")
                        .then(Commands.argument("entity", EntityArgument.entity())
                                .executes(context -> runGetNpcStare(context.getSource(), EntityArgument.getEntity(context, "entity"))
                                )
                        )
                )
        );
    }

    private static LiteralArgumentBuilder<CommandSource> appendRemoveCommand(LiteralArgumentBuilder<CommandSource> command) {
        return command.then(Commands.literal("remove")
                .then(Commands.literal("requireditem")
                        .then(Commands.argument("index", IntegerArgumentType.integer())
                                .executes(
                                        context -> runRemoveRequiredItem(context.getSource(), IntegerArgumentType.getInteger(context, "index"))
                                )
                        )
                )
                .then(Commands.literal("pokelootcommand")
                        .then(Commands.argument("blockpos", BlockPosArgument.blockPos())
                                .then(Commands.argument("index", IntegerArgumentType.integer())
                                        .executes(
                                                context -> runRemovePokeLootCommand(context.getSource(), BlockPosArgument.getOrLoadBlockPos(context, "blockpos"), IntegerArgumentType.getInteger(context, "index"))
                                        )
                                )
                        )
                )
                .then(Commands.literal("dialogue")
                        .then(Commands.argument("index", IntegerArgumentType.integer())
                                .executes(context -> runRemoveCustomDialogue(context.getSource(), IntegerArgumentType.getInteger(context, "index"))
                                )
                        )
                )
                .then(Commands.literal("npcstare")
                        .then(Commands.argument("entity", EntityArgument.entity())
                                .executes(context -> runRemoveNpcStare(context.getSource(), EntityArgument.getEntity(context, "entity"))
                                )
                        )
                )
        );
    }

    private static LiteralArgumentBuilder<CommandSource> appendNPCBattleCommand(LiteralArgumentBuilder<CommandSource> command) {
        return command.then(Commands.literal("npcbattle")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("uuid", EntityArgument.entity())
                                .then(Commands.argument("showscreen", BoolArgumentType.bool())
                                        .executes(context -> runNpcBattle(context.getSource(), EntityArgument.getPlayer(context, "player"),
                                                EntityArgument.getEntity(context, "uuid"), BoolArgumentType.getBool(context, "showscreen"))
                                        )
                                )
                        )
                )
        );
    }

    private static LiteralArgumentBuilder<CommandSource> appendBetterSpectate(LiteralArgumentBuilder<CommandSource> command) {
        return command.then(Commands.literal("betterspectate")
                    .then(Commands.argument("audience", EntityArgument.players())
                            .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> runBetterSpectate(context.getSource(),
                                        EntityArgument.getPlayers(context, "audience"),
                                        EntityArgument.getPlayer(context, "target"))
                                )
                            )
                    )
        );
    }

//    private static int runBetterSpectate(CommandSource source, ServerPlayerEntity target) throws CommandException {
//        String[] targetInput = new String[]{target.getScoreboardName()};
//        spectateOverride.executeOverride(source, targetInput);
//        return 1;
//    }
    private static int runBetterSpectate(CommandSource source, Collection<ServerPlayerEntity> audience, ServerPlayerEntity target) throws CommandException, CommandSyntaxException {
//        ServerPlayerEntity player = source.getPlayerOrException();
        if (target == null) {
            for (ServerPlayerEntity player : audience) {
                BattleRegistry.removeSpectator(player);
                NetworkHelper.sendPacket(new EndSpectatePacket(), player);
            }
            return 0;
        }

        for (ServerPlayerEntity player : audience) {
            if (target == player) {
                source.sendFailure(new StringTextComponent("You can't spectate yourself!"));
                return 0;
            }

            if (BattleRegistry.getBattle(player) != null) {
                source.sendFailure(new StringTextComponent("You can't spectate while battling!"));
                return 0;
            }

            BattleController base = BattleRegistry.getBattle(target);

            if (base == null) {
                source.sendFailure(new StringTextComponent("The target is not in a battle!"));
                return 0;
            }

            PlayerParticipant watchedPlayer = base.getPlayer(target.getScoreboardName());

            if (watchedPlayer == null) {
                source.sendFailure(new StringTextComponent("An error occurred while executing this command."));
                return 0;
            }

            if (!Pixelmon.EVENT_BUS.post(new SpectateEvent.StartSpectate(player, base, target))) {
//                sender.func_197022_f().field_70144_Y = 1.0F; //Normally this sets collision reduction but I don't see a mapping for it on regular forge...

                NetworkHelper.sendPacket(new StartBattlePacket(base.battleIndex, base.getBattleType(watchedPlayer), base.rules), player);
                NetworkHelper.sendPacket(new SetAllBattlingPokemonPacket(PixelmonClientData.convertToGUI(Arrays.asList(watchedPlayer.allPokemon)), true), player);
                ArrayList<PixelmonWrapper> teamList = watchedPlayer.getTeamPokemonList();
                NetworkHelper.sendPacket(new SetBattlingPokemonPacket(teamList), player);
                NetworkHelper.sendPacket(new SetPokemonBattleDataPacket(PixelmonClientData.convertToGUI(teamList), false), player);
                NetworkHelper.sendPacket(new SetPokemonBattleDataPacket(watchedPlayer.getOpponentData(), true), player);
                if (base.getTeam(watchedPlayer).size() > 1) {
                    NetworkHelper.sendPacket(new SetPokemonTeamDataPacket(watchedPlayer.getAllyData()), player);
                }

                NetworkHelper.sendPacket(new StartSpectatePacket(watchedPlayer.player.getUUID(), (BattleType)base.rules.getOrDefault(BattleRuleRegistry.BATTLE_TYPE)), player);
                base.addSpectator(new Spectator(player, target.getScoreboardName()));
            }
        }
        return 1;
    }


    private static int runSetNpcStare(CommandSource source, Entity entity, BlockPos blockPos) throws CommandSyntaxException {
        if (!(entity instanceof NPCEntity)) {
            source.sendFailure(new StringTextComponent("Entity is not an NPC!"));
            return 0;
        }
        source.sendSuccess(new StringTextComponent("Added stare behaviour to npc!"), false);
        Vector3d pos = Vector3d.atCenterOf(blockPos);
        NBTHelper.setStarePlace(entity, pos);
        AIOverrideUtil.safeAddStare((NPCEntity) entity, pos);
        return 1;
    }

    private static int runGetNpcStare(CommandSource source, Entity entity) throws CommandSyntaxException {
        if (!(entity instanceof NPCEntity)) {
            source.sendFailure(new StringTextComponent("Entity is not an NPC!"));
            return 0;
        }
        if (!NBTHelper.hasStarePlace(entity)) {
            source.sendFailure(new StringTextComponent("NPC has no stare location set!"));
            return 0;
        }
        source.sendSuccess(new StringTextComponent(String.format("Stare Location: %s", NBTHelper.getStarePlace(entity))), false);
        return 1;
    }

    private static int runRemoveNpcStare(CommandSource source, Entity entity) throws CommandSyntaxException {
        if (!(entity instanceof NPCEntity)) {
            source.sendFailure(new StringTextComponent("Entity is not an NPC!"));
            return 0;
        }
        if (!NBTHelper.removeStarePlace(entity)) {
            source.sendFailure(new StringTextComponent("NPC does not have stare location!"));
            return 0;
        }
        source.sendSuccess(new StringTextComponent("Removed stare location from npc!"), false);
        AIOverrideUtil.removeStare((NPCEntity) entity);
        return 1;
    }

    private static int runNpcBattle(CommandSource source, ServerPlayerEntity playerBattler, Entity entity, boolean showscreen) throws CommandSyntaxException {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ServerWorld world = server.overworld().getWorldServer();
//        ServerPlayerEntity playerBattler = server.getPlayerList().getPlayerByName(playerName);
//        UUID entityUUID = UUID.fromString(npcUUID);
//        Entity entity = world.getEntity(entityUUID);
        if (playerBattler == null) {
            source.sendFailure(new StringTextComponent("No valid player found."));
            return 0;
        }
        else if (entity == null) {
            source.sendFailure(new StringTextComponent("No valid entity found."));
            return 0;
        }
        else if (!(entity instanceof NPCEntity)) {
            source.sendFailure(new StringTextComponent("Entity is not an NPC!"));
            return 0;
        }
        else {
            if (!(entity instanceof NPCTrainer)) {
                source.sendFailure(new StringTextComponent("NPC is not a trainer!"));
                return 0;
            }
            NPCTrainer trainer = (NPCTrainer) entity;
            Pokemon startingPixelmon = StorageProxy.getParty(playerBattler).getSelectedPokemon();
            if (startingPixelmon == null) {
                source.sendFailure(new StringTextComponent("Trainer has no pokemon!!"));
                return 0;
            }
            TeamSelectionRegistry.Builder builder = TeamSelectionRegistry.builder().members(trainer, playerBattler);
            if (showscreen) {
                builder = builder.showRules().showOpponentTeam().closeable(true);
            }
            builder.battleRules(trainer.battleRules).start();
        }
        return 1;
    }

    private static int runGetCustomDialogue(CommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrException();
        Entity lookEntity = RayTraceHelper.getEntityLookingAt(player, 8.0);
        if (lookEntity == null) {
            source.sendFailure(new StringTextComponent("No entity found."));
        }
        else if (lookEntity instanceof StatueEntity) {
            if (!NBTHelper.hasCustomDialogue(lookEntity)) {
                source.sendFailure(new StringTextComponent("NPC has no custom dialogue!"));
                return 0;
            }
            String[] CustomDialogues = NBTHelper.getCustomDialogues(lookEntity);
            source.sendSuccess(new StringTextComponent(String.format("Custom Dialogue:\n%s", FormattingHelper.formatIndexedStringList(CustomDialogues))), false);
        }
        else {
            source.sendFailure(new StringTextComponent("Entity is not NPC!"));
        }
        return 1;
    }

    private static int runSetCustomDialogue(CommandSource source, String customDialogue) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrException();
        Entity lookEntity = RayTraceHelper.getEntityLookingAt(player, 8.0);
        if (lookEntity == null) {
            source.sendFailure(new StringTextComponent("No entity found."));
        }
        else if (lookEntity instanceof StatueEntity) {
            NBTHelper.appendCustomDialogue(lookEntity, customDialogue);
            source.sendSuccess(new StringTextComponent(String.format("Added dialogue line: %s", customDialogue)), false);
        }
        else {
            source.sendFailure(new StringTextComponent("Entity is not NPC!"));
        }
        return 1;
    }

    private static int runRemoveCustomDialogue(CommandSource source, int index) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrException();
        Entity lookEntity = RayTraceHelper.getEntityLookingAt(player, 8.0);
        if (lookEntity == null) {
            source.sendFailure(new StringTextComponent("No entity found."));
        }
        else if (lookEntity instanceof StatueEntity) {
            if (!NBTHelper.hasCustomDialogue(lookEntity)) {
                source.sendFailure(new StringTextComponent("NPC does not have custom dialogue!!"));
                return 0;
            }
            String[] messages = NBTHelper.getCustomDialogues(lookEntity);
            if (messages.length < (index + 1)) {
                source.sendFailure(new StringTextComponent("NPC does not have a dialogue at that index!"));
                return 0;
            }
            NBTHelper.removeCustomDialogue(lookEntity, index);
            source.sendSuccess(new StringTextComponent(String.format("Removed NPC's dialogue at index %d.", index)), false);
        }
        else {
            source.sendFailure(new StringTextComponent("Entity is not NPC!"));
        }
        return 1;
    }
    
    private static int runSetPokeLootCommand(CommandSource source, BlockPos pos, String commandString) throws CommandSyntaxException {
        World world = source.getLevel();
        if (!(world.getBlockEntity(pos) instanceof PokeChestTileEntity)) {
            source.sendFailure(new StringTextComponent(String.format("Target block is not a PokeChest!", pos.toString())));
            return 0;
        }
        boolean didOverwrite = PixelUtilsBlockData.CustomDataManager.setCommandAtBlock(world, pos, commandString);
        source.sendSuccess(new StringTextComponent("Added interact command: " + commandString), true);
        return 1;
    }
    private static int runGetPokeLootCommand(CommandSource source, BlockPos pos) throws CommandSyntaxException {
        World world = source.getLevel();
        if (!(world.getBlockEntity(pos) instanceof PokeChestTileEntity)) {
            source.sendFailure(new StringTextComponent(String.format("Target block is not a PokeChest!", pos.toString())));
            return 0;
        }
        String list = PixelUtilsBlockData.CustomDataManager.getBlockCommandListFormatted(world, pos);
        if (list == "") {
            source.sendFailure(new StringTextComponent(String.format("Invalid or empty list at %s.", pos.toString())));
            return 0;
        }
        source.sendSuccess(new StringTextComponent(String.format("Commands at %s: [\n%s]", pos.toString(), list)), true);
        return 1;
    }

    private static int runRemovePokeLootCommand(CommandSource source, BlockPos pos,  int index) throws CommandSyntaxException {
//        PixelUtilsBlockData.CustomDataManager.
        World world = source.getLevel();
        if (!(world.getBlockEntity(pos) instanceof PokeChestTileEntity)) {
            source.sendFailure(new StringTextComponent(String.format("Target block is not a PokeChest!", pos.toString())));
            return 0;
        }
        boolean success = PixelUtilsBlockData.CustomDataManager.removeCommandAtBlock(world, pos, index);
        if (!success) {
            source.sendFailure(new StringTextComponent("Invalid index or empty command list!"));
            return 0;
        }
        source.sendSuccess(new StringTextComponent(String.format("Removed Command at Block %s at Index %s", pos.toString(), String.valueOf(index))), true);
        return 1;
    }

    private static int runGetRequiredItem(CommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrException();
        Entity lookEntity = RayTraceHelper.getEntityLookingAt(player, 8.0);
        if (lookEntity == null) {
            source.sendFailure(new StringTextComponent("No entity found."));
        }
        else if (lookEntity instanceof NPCEntity) {
            if (!NBTHelper.hasRequiredItem(lookEntity)) {
                source.sendFailure(new StringTextComponent("NPC has no required item!!"));
                return 0;
            }
            String[] itemList = NBTHelper.getRequiredItems(lookEntity);
            source.sendSuccess(new StringTextComponent(String.format("Required Items:\n%s", FormattingHelper.formatIndexedStringList(itemList))), true);
        }
        else {
            source.sendFailure(new StringTextComponent("Entity is not NPC!"));
        }
        return 1;
    }

    private static int runSetRequiredItem(CommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrException();
        Entity lookEntity = RayTraceHelper.getEntityLookingAt(player, 8.0);
        if (lookEntity == null) {
            source.sendFailure(new StringTextComponent("No entity found."));
        }
        else if (!(lookEntity instanceof NPCEntity)) {
            source.sendFailure(new StringTextComponent("Entity is not NPC!"));
        }
        else {
            NBTHelper.appendRequiredItem(lookEntity, player.getMainHandItem());
            source.sendSuccess(new StringTextComponent(String.format("Added required item: %s", player.getMainHandItem().toString())), true);
        }
        return 1;
    }

    private static int runRemoveRequiredItem(CommandSource source, int index) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrException();
        Entity lookEntity = RayTraceHelper.getEntityLookingAt(player, 8.0);
        if (lookEntity == null) {
            source.sendFailure(new StringTextComponent("No entity found."));
        }
        else if (lookEntity instanceof NPCEntity) {
            if (!NBTHelper.hasRequiredItem(lookEntity)) {
                source.sendFailure(new StringTextComponent("NPC does not have required item set!"));
                return 0;
            }
            String[] commands = NBTHelper.getRequiredItems(lookEntity);
            if (commands.length < (index + 1)) {
                source.sendFailure(new StringTextComponent("NPC does not have a required item at that index!"));
                return 0;
            }
            NBTHelper.removeRequiredItem(lookEntity, index);
            source.sendSuccess(new StringTextComponent(String.format("Removed NPC's required item at index %d.", index)), true);
        }
        else {
            source.sendFailure(new StringTextComponent("Entity is not NPC!"));
        }
        return 1;
    }
}
