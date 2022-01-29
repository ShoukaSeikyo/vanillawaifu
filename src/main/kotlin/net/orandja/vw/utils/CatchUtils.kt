package net.orandja.vw.utils

import java.util.function.Consumer

fun <T> catchNone(faultValue: T, debug: Boolean = false, m: () -> T): T {
    return try {
        m.invoke()
    } catch (ignored: Exception) {
        if(debug) {
            ignored.printStackTrace()
        }
        faultValue
    }
}

fun <T> catchNone(debug: Boolean = false, m: () -> T): T? {
    return try {
        m.invoke()
    } catch (ignored: Exception) {
        if(debug) {
            ignored.printStackTrace()
        }
        return null
    }
}

fun notNull(vararg objs: Any?): Boolean {
    for (obj in objs) {
        if(obj == null) {
            return false;
        }
    }

    return true;
}

fun <T> castAs(clazz: Class<T>, obj: Any?, consumer: Consumer<T>) {
    if (obj != null && clazz.isInstance(obj)) {
        consumer.accept(obj as T)
    }
}

fun <T, R> castAsAndReturn(clazz: Class<T>, obj: Any?, consumer: (T) -> R): R? {
    if (obj != null && clazz.isInstance(obj)) {
        return consumer.invoke(obj as T)
    }

    return null
}

fun <T> castAs(clazz: Class<T>, obj: Any?): T? {
    if (obj != null && clazz.isInstance(obj)) {
        return obj as T;
    }

    return null
}