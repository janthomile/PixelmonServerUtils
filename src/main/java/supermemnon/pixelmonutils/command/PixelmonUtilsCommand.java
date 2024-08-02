package supermemnon.pixelmonutils.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pixelmonmod.pixelmon.entities.npcs.NPCEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import supermemnon.pixelmonutils.util.NBTHelper;
import supermemnon.pixelmonutils.util.FormattingHelper;
import supermemnon.pixelmonutils.util.RayTraceHelper;

public class PixelmonUtilsCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> commandStructure = Commands.literal("pixelmonutils").requires(source -> source.hasPermission(2));
        commandStructure = appendSetItem(commandStructure);
        commandStructure = appendGetItem(commandStructure);
        commandStructure = appendRemoveItem(commandStructure);
        dispatcher.register(commandStructure);
    }

    private static LiteralArgumentBuilder<CommandSource> appendSetItem(LiteralArgumentBuilder<CommandSource> command) {
           return command.then(Commands.literal("set")
                .then(Commands.literal("requireditem")
                        .executes(context -> runSetRequiredItem(context.getSource()))
                )
           );
    }

    private static LiteralArgumentBuilder<CommandSource> appendGetItem(LiteralArgumentBuilder<CommandSource> command) {
        return command.then(Commands.literal("get")
                .then(Commands.literal("requireditem")
                        .executes(context -> runGetRequiredItem(context.getSource()))
                )
        );
    }

    private static LiteralArgumentBuilder<CommandSource> appendRemoveItem(LiteralArgumentBuilder<CommandSource> command) {
        return command.then(Commands.literal("remove")
                .then(Commands.literal("requireditem")
                    .executes(context -> runRemoveRequiredItem(context.getSource(), IntegerArgumentType.getInteger(context, "index"))
                    )
                )
        );
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
