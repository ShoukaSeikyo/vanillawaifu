package net.orandja.vanillawaifu.world;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public class WaifuInventory implements WaifuWorldData {
    @Override
    public void toNBT(NbtCompound tag) {

    }

    @Override
    public void fromNBT(NbtCompound tag) {

    }

    @Override
    public <T extends WaifuWorldData> T setWorld(World world) {
        return null;
    }
}
