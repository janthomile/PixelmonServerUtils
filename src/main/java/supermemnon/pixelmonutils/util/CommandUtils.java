package supermemnon.pixelmonutils.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;

public class CommandUtils {
    public static String replaceCustomSelectors(String command, PlayerEntity player) {
        command = command.replaceAll("@pl", player.getName().getString());
        return command;
    }

    public static CommandSource getPlayerCommandSource(MinecraftServer server, PlayerEntity player) {
        CommandSource source = new CommandSource(server,//ICommandSource
                                                        new Vector3d(player.getX(), player.getY(), player.getZ()),
                                                        Vector2f.ZERO,
                                                        server.overworld(),
                                                        4,
                                                        "Server",
                                                        new StringTextComponent("Server"),
                                                        server,
                                                        null
                                                        );
        return source;
    }
    public static boolean executeCommandString(MinecraftServer server, PlayerEntity relevantPlayer, String command) throws CommandSyntaxException {
        if (command.length() > 1 && (command.charAt(0) == '/')) {
            command = command.replaceFirst("/", "");
        }
        command = replaceCustomSelectors(command, relevantPlayer);
        //server.createCommandSourceStack() // Alternative command source
        server.getCommands().getDispatcher().execute(command, getPlayerCommandSource(server, relevantPlayer));
        return false;
    }
    public static boolean executeCommandList(MinecraftServer server, PlayerEntity relevantPlayer, String[] commands) throws CommandSyntaxException {
        for (int i = 0; i < commands.length; i++) {
            executeCommandString(server, relevantPlayer, commands[i]);
        }
        return false;
    }
}
