package supermemnon.pixelmonutils.util;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.Arrays;

import static net.minecraft.util.math.MathHelper.floor;

public class NBTHelper {

    static String nbtCustomDialogueKey = "putils_dlg";
    static String nbtRequiredItemKey = "putils_item";
    static String nbtStareLocationKey = "putils_stareplace";
    static String itemListDelimiter = ",,,";
    static String altListDelimiter = "||";
    static String altListDelimiterRegex = "\\|\\|";
    static String defaultCustomDialogue = "...";


    public static boolean hasStarePlace(Entity entity) {
        CompoundNBT  nbt = entity.getPersistentData();
        return nbt.contains(nbtStareLocationKey);
    }
    public static Vector3d getStarePlace(Entity entity) {
        CompoundNBT  nbt = entity.getPersistentData();
        int[] array = nbt.getIntArray(nbtStareLocationKey);
        return new Vector3d(array[0],array[1],array[2]);
    }
    public static void setStarePlace(Entity entity, Vector3d location) {
        CompoundNBT  nbt = entity.getPersistentData();
        int[] array = {floor(location.x), floor(location.y), floor(location.z)};
        nbt.putIntArray(nbtStareLocationKey, array);
    }

    public static boolean removeStarePlace(Entity entity) {
        if (!hasStarePlace(entity)) {
            return false;
        }
        CompoundNBT  nbt = entity.getPersistentData();
        nbt.remove(nbtStareLocationKey);
        return true;
    }

    public static void appendCustomDialogue(Entity entity, String message) {
        if (!hasCustomDialogue(entity)) {
            setCustomDialogue(entity, message);
            return;
        }
        setCustomDialogue(entity,getCustomDialogue(entity).concat(altListDelimiter).concat(message));
    }

    public static void setCustomDialogue(Entity entity, String message) {
        CompoundNBT  nbt = entity.getPersistentData();
        nbt.putString(nbtCustomDialogueKey, message);
    }

    public static boolean hasCustomDialogue(Entity entity) {
        CompoundNBT  nbt = entity.getPersistentData();
        return nbt.contains(nbtCustomDialogueKey);
    }
    public static String[] getCustomDialogues(Entity entity) {
        CompoundNBT  nbt = entity.getPersistentData();
        return parseAltList(nbt.getString(nbtCustomDialogueKey));
    }

    public static String getCustomDialogue(Entity entity) {
        if (hasCustomDialogue(entity)) {
            CompoundNBT  nbt = entity.getPersistentData();
            return nbt.getString(nbtCustomDialogueKey);
        }
        else {
            return defaultCustomDialogue;
        }
    }

    public static boolean removeCustomDialogue(Entity entity, int index) {
        CompoundNBT  nbt = entity.getPersistentData();
        if (!nbt.contains(nbtCustomDialogueKey)) {
            return false;
        }
        ArrayList<String> newMessageList = new ArrayList<> (Arrays.asList(getCustomDialogues(entity)));
        newMessageList.remove(index);
        setCustomDialogue(entity, String.join(altListDelimiter, newMessageList));
        if (newMessageList.size() < 1) {
            nbt.remove(nbtCustomDialogueKey);
        }
        return true;
    }


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
