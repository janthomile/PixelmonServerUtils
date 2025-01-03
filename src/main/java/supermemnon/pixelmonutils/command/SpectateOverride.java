package supermemnon.pixelmonutils.command;

import com.mojang.brigadier.CommandDispatcher;
import com.pixelmonmod.pixelmon.command.impl.SpectateCommand;
import net.minecraft.command.CommandSource;

public class SpectateOverride extends SpectateCommand {
    public SpectateOverride(CommandDispatcher<CommandSource> dispatcher) {
        super(dispatcher);
    }
//    public String getName() {
//        return "spectatebattle";
//    }
//    public String getUsage(CommandSource sender) {
//        return "/spectatebattle <playerName>";
//    }
}
