package supermemnon.pixelmonutils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pixelmonmod.pixelmon.api.events.PokeLootEvent;
import com.pixelmonmod.pixelmon.api.events.npc.NPCEvent;
import com.pixelmonmod.pixelmon.entities.pixelmon.StatueEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import supermemnon.pixelmonutils.command.PixelmonUtilsCommand;
import supermemnon.pixelmonutils.storage.PixelUtilsBlockData;
import supermemnon.pixelmonutils.util.CommandUtils;
import supermemnon.pixelmonutils.util.NBTHelper;
import supermemnon.pixelmonutils.util.InventoryUtils;
import supermemnon.pixelmonutils.util.PixelmonModUtils;

import java.util.UUID;


//@Mod.EventBusSubscriber(modid = "pixelmonperms")

public class EventHandler {

    @Mod.EventBusSubscriber(modid = "pixelmonutils", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.DEDICATED_SERVER)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            PixelmonUtilsCommand.register(event.getDispatcher());
        }

        @SubscribeEvent
        public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
            if (event.getTarget() instanceof StatueEntity) {
                if (!NBTHelper.hasCustomDialogue(event.getTarget())) {
                    return;
                }
                PixelmonModUtils.customNpcChat(event.getTarget(), (ServerPlayerEntity) event.getPlayer(), NBTHelper.getCustomDialogues(event.getTarget()));
            }
        }

    }

    public static class ModEvents {
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onPokeLootClaim(PokeLootEvent.Claim event) throws CommandSyntaxException {
            World world = event.player.getCommandSenderWorld();
            BlockPos pos = event.chest.getBlockPos();
            if (!PixelUtilsBlockData.CustomDataManager.hasCommandAtBlock(world, pos)) {
                return;
            }
            CommandUtils.executeCommandList(event.player.getServer(), event.player, PixelUtilsBlockData.CustomDataManager.getCommandListAtBlock(world, pos));
        }
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
