package supermemnon.pixelmonutils.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraftforge.server.permission.PermissionAPI;
import org.apache.logging.log4j.Level;
import supermemnon.pixelmonutils.PixelmonUtils;


public class InventoryUtils {
    public static boolean hasAllRequiredItems(PlayerEntity player, String[] itemList) throws CommandSyntaxException {
//        PixelmonUtils.getLOGGER().log(Level.DEBUG, String.format("items: %s", String.join(",",itemList)));
        for (int i = 0; i < itemList.length; i++) {
            boolean itemInInventory = false;
            CompoundNBT requireItem = JsonToNBT.parseTag(itemList[i]);
            for (ItemStack item : player.inventory.items) {
                if (doesItemMatch(requireItem, item.serializeNBT())) {
                    itemInInventory = true;
//                    PixelmonUtils.getLOGGER().log(Level.DEBUG, String.format("item match found!: %s", item.serializeNBT().toString()));
                }
            }
            if (!itemInInventory) {
                return false;
            }
        }
        return true;
    }

    public static boolean doesItemMatch(CompoundNBT a, CompoundNBT b) {
        if (a.getString("id").equals(b.getString("id")) && a.getCompound("tag").equals(b.getCompound("tag"))) {
            return true;
        }
        else {
            return false;
        }
    }
}
