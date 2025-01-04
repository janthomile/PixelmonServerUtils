package supermemnon.pixelmonutils.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pixelmonmod.pixelmon.api.command.PixelmonCommandUtils;
import com.pixelmonmod.pixelmon.command.impl.SpectateCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.server.permission.PermissionAPI;

public class SpectateOverride extends SpectateCommand {
    public SpectateOverride(CommandDispatcher<CommandSource> dispatcher) {
        super(dispatcher);
    }

    @Override
    public void execute(CommandSource sender, String[] args) throws CommandException {
        try {
            if (PermissionAPI.hasPermission(sender.getPlayerOrException(), PixelmonUtilsCommand.permissionSpectate)) {
                sender = sender.withPermission(2);
            }
            super.execute(sender, args);
        } catch (CommandSyntaxException e) {
            sender.sendFailure(new StringTextComponent("There was an error executing the command."));
        }
    }

    public String getName() {
        return "spectatebattle";
    }
    public String getUsage(CommandSource sender) {
        return "/spectatebattle <playerName>";
    }
}
