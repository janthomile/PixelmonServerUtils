package supermemnon.pixelmonutils;

import com.pixelmonmod.pixelmon.Pixelmon;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import supermemnon.pixelmonutils.command.PixelmonUtilsCommand;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("pixelmonutils")
public class PixelmonUtils
{
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    private static PixelmonUtils instance;

    public static PixelmonUtils getInstance() {
        return instance;
    }

    public static Logger getLOGGER() {
        return LOGGER;
    }

    public PixelmonUtils() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
//        MinecraftForge.EVENT_BUS.register(this);
        Pixelmon.EVENT_BUS.register(EventHandler.ModEvents.class);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
//        PermissionAPI.registerNode("pixelmonutils.iteminteract.entity", DefaultPermissionLevel.NONE, "");
        PermissionAPI.registerNode(PixelmonUtilsCommand.permissionSpectate, DefaultPermissionLevel.OP, "");
    }
}
