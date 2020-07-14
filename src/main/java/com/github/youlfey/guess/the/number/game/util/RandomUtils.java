package com.github.youlfey.guess.the.number.game.util;

import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {
    public static Integer random() {
        return ThreadLocalRandom.current().nextInt(1, 51); // Random 0...100 % correspond 1..50 values
    }
}
