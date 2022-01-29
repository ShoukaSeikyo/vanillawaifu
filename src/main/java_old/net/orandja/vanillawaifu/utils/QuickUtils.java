package net.orandja.vanillawaifu.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class QuickUtils {

    public interface noCatchIntf<T> {
        T accept() throws Exception;
    }
    public static <T> T noCatch(noCatchIntf<T> noCatchM) {
        return noCatch(null, noCatchM);
    }

    public static <T> T noCatch(T faultValue, noCatchIntf<T> noCatchM) {
        try{
            return noCatchM.accept();
        } catch(Exception ignored) {
            return faultValue;
        }
    }

    public static <T> T castOrNull(Object object, Class<T> clazz) {
        return clazz.isInstance(object) ? (T) object : null;
    }

    public static Field quickField(Class<?> clazz, Predicate<Field> predicate) {
        return Arrays.stream(clazz.getDeclaredFields()).filter(predicate).map(field -> {field.setAccessible(true); return field; } ).findFirst().get();
    }

    public static Field quickField(Class<?> clazz, Class<?> clazzSearched) {
        return quickField(clazz, field -> field.getType().equals(clazzSearched));
    }

    public static <T> T quickGet(Field field, Object source) {
        return (T) noCatch(() -> field.get(source));
    }

    public static <T> T create(T object, Consumer<T> consumer) {
        consumer.accept(object);
        return object;
    }

    public static <T> T create(T object, Consumer<T>... consumer) {
        for (Consumer<T> tConsumer : consumer) {
            tConsumer.accept(object);
        }
        return object;
    }
}
