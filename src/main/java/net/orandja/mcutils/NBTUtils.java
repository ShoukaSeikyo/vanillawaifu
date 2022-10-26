package net.orandja.mcutils;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.List;
import java.util.function.Consumer;

public abstract class NBTUtils {

    public static NbtList toNbtList(List<NbtElement> list) {
        NbtList nbtList = new NbtList();
        nbtList.addAll(list);
        return nbtList;
    }

    public static NbtList addTo(NbtList list, NbtElement element) {
        list.add(element);
        return list;
    }

    public static NbtList addTo(NbtList list, String string) {
        list.add(NbtString.of(string));
        return list;
    }

    public interface NbtSupplier<N> {
        N get(String id);
    }

    public interface NbtCompute<N> {
        N get();
    }

    public static <N extends NbtElement> N getOrCompute(NbtCompound tag, String id, NbtSupplier<N> supplier, NbtCompute<N> compute, Consumer<N> consumer) {
        N value;
        if(tag.contains(id)) {
            value = supplier.get(id);
        } else {
            value = compute.get();
            tag.put(id, value);
        }

        consumer.accept(value);

        return value;
    }

    public static <N extends NbtElement> N getOrCreate(NbtCompound tag, String id, NbtSupplier<N> supplier, NbtCompute<N> compute) {
        if(tag.contains(id)) {
            return (N) tag.get(id);
        } else {
            N value = compute.get();
            tag.put(id, value);
            return value;
        }
    }

    public static NbtCompound getTagOrCompute(NbtCompound tag, String id, Consumer<NbtCompound> consumer) {
        return getOrCompute(tag, id, tag::getCompound, NbtCompound::new, consumer);
    }

    public static NbtList getStringListOrCompute(NbtCompound tag, String id, Consumer<NbtList> consumer) {
        return getStringListOrCompute(tag, id, NbtList::new, consumer);
    }

    public static NbtList getStringListOrCompute(NbtCompound tag, String id, NbtCompute<NbtList> compute, Consumer<NbtList> consumer) {
        return getOrCompute(tag, id, key -> tag.getList(key, NbtElement.STRING_TYPE), compute, consumer);
    }
}
