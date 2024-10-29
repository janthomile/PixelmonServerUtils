package supermemnon.pixelmonutils.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pixelmonmod.pixelmon.blocks.tileentity.PokeChestTileEntity;
import com.pixelmonmod.pixelmon.entities.npcs.NPCEntity;
import com.pixelmonmod.pixelmon.entities.pixelmon.StatueEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import supermemnon.pixelmonutils.storage.PixelUtilsBlockData;
import supermemnon.pixelmonutils.util.NBTHelper;
import supermemnon.pixelmonutils.util.FormattingHelper;
import supermemnon.pixelmonutils.util.RayTraceHelper;

public class PixelmonUtilsCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> commandStructure = Commands.literal("pixelmonutils").requires(source -> source.hasPermission(2));
        commandStructure = appendSetCommand(commandStructure);
        commandStructure = appendGetCommand(commandStructure);
        commandStructure = appendRemoveCommand(commandStructure);
//        commandStructure = appendPokeLootCommand(commandStructure);
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
        );
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
            source.sendSuccess(new StringTextComponent(String.format("Custom Dialogue:\n%s", FormattingHelper.formatIndexedStringList(CustomDialogues))), true);
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
            source.sendSuccess(new StringTextComponent(String.format("Added dialogue line: %s", customDialogue)), true);
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
            source.sendSuccess(new StringTextComponent(String.format("Removed NPC's dialogue at index %d.", index)), true);
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
