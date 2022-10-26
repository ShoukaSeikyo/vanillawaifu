package net.orandja.mcutils;

public abstract class MathUtils {

    public interface GridConsumer {
        void accept(int x, int y);
    }

    public static void grid(int width, GridConsumer consumer) {
        grid(width, 1, consumer);
    }

    public static void grid(int width, int height, GridConsumer consumer) {
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                consumer.accept(x, y);
            }
        }
    }

}
