package com.github.tonivade.bricks;

import java.util.Random;

public enum Color {
  RED, GREEN, BLUE, YELLOW;

  private static final Random random = new Random();

  static Color random(Position position) {
    return values()[Math.abs(random.nextInt() % 4)];
  }
}
