package dev.infernity.rollplayer.util;

public class RandomExt {
    public static int weighted_choice_index(int size, int[] weights) {
        double totalWeight = 0.0;
        for (int i = 0; i < size; i++) {
            totalWeight += weights[i];
        }
        int idx = 0;
        for (double r = Math.random() * totalWeight; idx < size - 1; ++idx) {
            r -= weights[idx];
            if (r <= 0.0) break;
        }
        return idx;
    }
}