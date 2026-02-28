/*
 * Copyright (c) 2024-2026, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.bricks;

import static com.github.tonivade.purefun.core.Precondition.checkNonNull;
import java.util.Random;

public enum Color {
  RED("\033[0m\033[0;41m \033[0m"),
  GREEN("\033[0m\033[0;42m \033[0m"),
  BLUE("\033[0m\033[0;44m \033[0m"),
  YELLOW("\033[0m\033[0;43m \033[0m");

  private final String code;

  private Color(String code) {
    this.code = checkNonNull(code);
  }

  private static final Random random = new Random();

  static Color random(Position position) {
    return values()[Math.abs(random.nextInt() % 4)];
  }

  @Override
  public String toString() {
    return code;
  }
}
