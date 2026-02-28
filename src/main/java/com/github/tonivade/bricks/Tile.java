/*
 * Copyright (c) 2024-2026, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.bricks;

public record Tile(Position position, Color color) {

  public boolean adjacent(Tile other) {
    return this.position.adjacent(other.position) && this.color == other.color;
  }
}
