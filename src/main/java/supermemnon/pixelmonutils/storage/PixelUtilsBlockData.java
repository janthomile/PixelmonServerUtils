package supermemnon.pixelmonutils.storage;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.nbt.ListNBT;
import supermemnon.pixelmonutils.util.FormattingHelper;

import java.util.*;

public class PixelUtilsBlockData extends WorldSavedData {

    private static final String DATA_NAME = "pixelutils_block_data";
    private static final String NBT_ROOT_NAME = "PixelUtilsBlockData";
    private static final int COMPOUND_NBT_ENUM_VAL = 10;// 10 for CompoundNBT

    private static final String NBT_BLOCKPOS_KEY = "Pos";
    private static final String NBT_STRDATA_KEY = "Data";

    // A map to store block positions and their associated custom strings
    private final Map<BlockPos, String> blockDataMap = new HashMap<>();

    public PixelUtilsBlockData() {
        super(DATA_NAME);
    }

    // This constructor is called when loading the data from NBT
    public PixelUtilsBlockData(String name) {
        super(name);
    }

    // Add custom data for a specific block position
    public void setCustomData(BlockPos pos, String data) {
        blockDataMap.put(pos, data);
        setDirty(); // Mark this data as dirty to ensure it saves
    }

    public boolean hasCustomData(BlockPos pos) {
        return blockDataMap.containsKey(pos);
    }

    // Retrieve custom data for a specific block position
    public String getCustomData(BlockPos pos) {
        return blockDataMap.getOrDefault(pos, "");
    }

    // Write the data to NBT format for saving
    @Override
    public CompoundNBT save(CompoundNBT compound) {
        ListNBT list = new ListNBT();
        for (Map.Entry<BlockPos, String> entry : blockDataMap.entrySet()) {
            CompoundNBT entryNBT = new CompoundNBT();
            entryNBT.putLong(NBT_BLOCKPOS_KEY, entry.getKey().asLong());
            entryNBT.putString(NBT_STRDATA_KEY, entry.getValue());
            list.add(entryNBT);
        }
        compound.put(NBT_ROOT_NAME, list);
        return compound;
    }

    // Read the data from NBT format for loading
    @Override
    public void load(CompoundNBT compound) {
        blockDataMap.clear();
        ListNBT list = compound.getList(NBT_ROOT_NAME, COMPOUND_NBT_ENUM_VAL);
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT entryNBT = list.getCompound(i);
            BlockPos pos = BlockPos.of(entryNBT.getLong(NBT_BLOCKPOS_KEY));
            String data = entryNBT.getString(NBT_STRDATA_KEY);
            blockDataMap.put(pos, data);
        }
    }

    public static PixelUtilsBlockData get(World world) {
        return world.getServer().overworld().getDataStorage().computeIfAbsent(PixelUtilsBlockData::new, DATA_NAME);
    }

    public static class CustomDataManager {
        public static final String separatorString = "///";

        public static boolean hasCommandAtBlock(World world, BlockPos pos) {
            PixelUtilsBlockData savedData = PixelUtilsBlockData.get(world);
            return savedData.hasCustomData(pos);
        }

        public static String getCommandAtIndex(String commands, int index) {
            List<String> commandList = Arrays.asList(commands.split(separatorString));
            if (commandList.size() < (index + 1)) {
                return "";
            }
            else {
                return commandList.get(index);
            }
        }

        public static String appendCommandToString(String commands, String commandString) {
            ArrayList<String> commandList = new ArrayList<> (Arrays.asList(commands.split(separatorString)));
            commandList.add(commandString);
            return String.join(separatorString, commandList);
        }
        public static boolean setCommandAtBlock(World world, BlockPos pos, String data) {
            PixelUtilsBlockData savedData = PixelUtilsBlockData.get(world);
            boolean exists = savedData.hasCustomData(pos);
            if (!exists) {
                savedData.setCustomData(pos, data);
            }
            else {
                String newData = appendCommandToString(savedData.getCustomData(pos), data);
                savedData.setCustomData(pos, newData);
            }
            return exists;
        }

        public static String getBlockCommandListFormatted(World world, BlockPos pos) {
            PixelUtilsBlockData savedData = PixelUtilsBlockData.get(world);
            String data = savedData.getCustomData(pos);
            if (data.equals("")) {
                return data;
            }
            else {
                return FormattingHelper.formatIndexedStringList(data.split(separatorString));
//                List<String> commandList = Arrays.asList(data.split(separatorString));
                //                for (int i = 0; i < commandList.size(); i++) {
//                    commandList.set(i, String.format("\"%s: %s\"", i, commandList.get(i)));
//                }
//                return String.join(",\n", commandList);
            }
        }

        public static String[] getCommandListAtBlock(World world, BlockPos pos) {
            PixelUtilsBlockData savedData = PixelUtilsBlockData.get(world);
            String data = savedData.getCustomData(pos);
            if (data.equals("")) {
                return new String[0];
            }
            else {
                return data.split(separatorString);
            }
        }

        public static String getCommandAtBlock(World world, BlockPos pos, int index) {
            PixelUtilsBlockData savedData = PixelUtilsBlockData.get(world);
            String data = savedData.getCustomData(pos);
            if (data.equals("")) {
                return data;
            }
            else {
                return getCommandAtIndex(data, index);
            }
        }

        public static boolean removeCommandAtBlock(World world, BlockPos pos, int index) {
            PixelUtilsBlockData savedData = PixelUtilsBlockData.get(world);
            String data = savedData.getCustomData(pos);
            if (data.equals("")) {
                return false;
            }
            ArrayList<String> commandList = new ArrayList<> (Arrays.asList(data.split(separatorString)));
            if ((index + 1) < commandList.size()) {
                return false;
            }
            commandList.remove(index);
            savedData.setCustomData(pos, String.join(separatorString, commandList));
            return true;
        }

    }

}
