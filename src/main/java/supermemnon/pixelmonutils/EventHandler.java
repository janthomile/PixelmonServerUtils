package supermemnon.pixelmonutils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pixelmonmod.pixelmon.api.events.npc.NPCEvent;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import supermemnon.pixelmonutils.command.PixelmonUtilsCommand;
import supermemnon.pixelmonutils.util.NBTHelper;
import supermemnon.pixelmonutils.util.InventoryUtils;
import supermemnon.pixelmonutils.util.PixelmonModUtils;


//@Mod.EventBusSubscriber(modid = "pixelmonperms")

public class EventHandler {

    @Mod.EventBusSubscriber(modid = "pixelmonutils", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.DEDICATED_SERVER)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            PixelmonUtilsCommand.register(event.getDispatcher());
        }

    }

    //    @SubscribeEvent(priority = EventPriority.HIGHEST)
//    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
//        PixelmonPerms.getLOGGER().log(Level.INFO, "Interaction!!");
//        if (event.getEntity() instanceof NPCEntity && InteractionHandler.hasRequiredPermission(event.getEntity())) {
//            String perm = InteractionHandler.getRequiredPermission(event.getEntity());
//            PixelmonPerms.getLOGGER().log(Level.INFO, "NPC Interaction!");
//            if (!PermissionAPI.hasPermission(event.getPlayer(), perm)) {
//                PixelmonPerms.getLOGGER().log(Level.INFO, "NPC Interaction Cancelled!");
//                event.getPlayer().sendMessage(new StringTextComponent(InteractionHandler.getCancelMessage(event.getEntity())), null);
//                event.setCanceled(true);
//            }
//        }
//    }

    public static class ModEvents {
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onNPCBattleEvent(NPCEvent.StartBattle event) throws CommandSyntaxException {
            if (!NBTHelper.hasRequiredItem(event.npc)) {
                return;
            }
            String[] perms = NBTHelper.getRequiredItems(event.npc);
            if (!InventoryUtils.hasAllRequiredItems(event.player, perms)) {
                PixelmonModUtils.customNpcChat(event.npc, (ServerPlayerEntity) event.player, NBTHelper.getCancelMessages(event.npc));
//                event.player.sendMessage(new StringTextComponent(FormattingHelper.formatWithAmpersand(NBTHandler.getCancelMessage(event.npc))), event.player.getUUID());
                event.setCanceled(true);
            }
        }
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onNPCInteractEvent(NPCEvent.Interact event) throws CommandSyntaxException {
            if (!NBTHelper.hasRequiredItem(event.npc)) {
                return;
            }
            String[] items = NBTHelper.getRequiredItems(event.npc);
            if (!InventoryUtils.hasAllRequiredItems(event.player, items)) {
                PixelmonModUtils.customNpcChat(event.npc, (ServerPlayerEntity) event.player, NBTHelper.getCancelMessages(event.npc));
                event.setCanceled(true);
            }
        }
    }
}
