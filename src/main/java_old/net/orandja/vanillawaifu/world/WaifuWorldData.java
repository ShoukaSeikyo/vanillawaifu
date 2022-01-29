package net.orandja.vanillawaifu.world;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public interface WaifuWorldData {
    void toNBT(NbtCompound tag);

    void fromNBT(NbtCompound tag);

    <T extends WaifuWorldData> T setWorld(World world);
}
