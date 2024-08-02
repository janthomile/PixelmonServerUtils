package supermemnon.pixelmonutils.util;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import java.util.ArrayList;
import java.util.Arrays;

public class NBTHelper {

    static String nbtRequiredItemKey = "putils_item";
    static String itemListDelimiter = ",,,";
    static String altListDelimiter = "||";
    static String altListDelimiterRegex = "\\|\\|";


    public static String convertItemToNbt(ItemStack item) {
        String itemAsString = item.serializeNBT().getAsString();
        return itemAsString;
    }

    public static String[] getRequiredItems(Entity entity) {
        CompoundNBT  nbt = entity.getPersistentData();
        return parseStringList(nbt.getString(nbtRequiredItemKey), itemListDelimiter);
    }

    public static String getRequiredItemString(Entity entity) {
        CompoundNBT  nbt = entity.getPersistentData();
        return nbt.getString(nbtRequiredItemKey);
    }

    public static void setRequiredItem(Entity entity, String permission) {
        CompoundNBT  nbt = entity.getPersistentData();
        nbt.putString(nbtRequiredItemKey, permission);
    }

    public static void appendRequiredItem(Entity entity, ItemStack item) {
        if (!hasRequiredItem(entity)) {
            setRequiredItem(entity, convertItemToNbt(item));
            return;
        }
        setRequiredItem(entity, getRequiredItemString(entity).concat(itemListDelimiter).concat(convertItemToNbt(item)));
    }

    public static boolean removeRequiredItem(Entity entity, int index) {
        CompoundNBT  nbt = entity.getPersistentData();
        if (!nbt.contains(nbtRequiredItemKey)) {
            return false;
        }
        ArrayList<String> newItemList = new ArrayList<> (Arrays.asList(getRequiredItems(entity)));
        newItemList.remove(index);
        setRequiredItem(entity, String.join(itemListDelimiter, newItemList));
        if (newItemList.size() < 1) {
            nbt.remove(nbtRequiredItemKey);
        }
        return true;
    }

    public static boolean hasRequiredItem(Entity entity) {
        CompoundNBT  nbt = entity.getPersistentData();
        return nbt.contains(nbtRequiredItemKey);
    }

    public static String[] getCancelMessages(Entity entity) {
        return new String[]{"Sorry, come back later!"};
    }

    public static String[] parseStringList(String string, String delimiter) {
        return string.split(delimiter);
    }
    public static String[] parseAltList(String string) {
        return string.split(altListDelimiterRegex);
    }
}
