package supermemnon.pixelmonutils.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pixelmonmod.pixelmon.api.command.PixelmonCommandUtils;
import com.pixelmonmod.pixelmon.command.impl.SpectateCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.server.permission.PermissionAPI;

public class SpectateOverride extends SpectateCommand {
    public SpectateOverride(CommandDispatcher<CommandSource> dispatcher) {
        super(dispatcher);
    }
    public String getName() {
        return "spectatebattle";
    }
    public String getUsage(CommandSource sender) {
        return "/spectatebattle <playerName>";
    }
}
