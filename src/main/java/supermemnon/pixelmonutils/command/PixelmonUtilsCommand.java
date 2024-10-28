package supermemnon.pixelmonutils.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pixelmonmod.pixelmon.entities.npcs.NPCEntity;
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
        );
    }

    private static int runSetPokeLootCommand(CommandSource source, BlockPos pos, String commandString) throws CommandSyntaxException {
        World world = source.getLevel();
        boolean didOverwrite = PixelUtilsBlockData.CustomDataManager.setCommandAtBlock(world, pos, commandString);
        source.sendSuccess(new StringTextComponent("Added interact command: " + commandString), true);
        return 1;
    }
    private static int runGetPokeLootCommand(CommandSource source, BlockPos pos) throws CommandSyntaxException {
        World world = source.getLevel();
        String list = PixelUtilsBlockData.CustomDataManager.getBlockCommandListFormatted(world, pos);
        if (list == "") {
            source.sendFailure(new StringTextComponent(String.format("Invalid or empty list at %s.", pos.toString())));
            return 0;
        }
        source.sendSuccess(new StringTextComponent(String.format("Commands at %s: [\n%s\n]", pos.toString(), list)), true);
        return 1;
    }

    private static int runRemovePokeLootCommand(CommandSource source, BlockPos pos,  int index) throws CommandSyntaxException {
//        PixelUtilsBlockData.CustomDataManager.
        World world = source.getLevel();
        boolean success = PixelUtilsBlockData.CustomDataManager.removeCommandAtBlock(world, pos, index);
        if (!success) {
            source.sendFailure(new StringTextComponent("Invalid index or empty command list!"));
            return 0;
        }
        String text = String.format("Block Pos of %s, Index of %s", pos.toString(), String.valueOf(index));
        source.sendSuccess(new StringTextComponent(text), true);
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
