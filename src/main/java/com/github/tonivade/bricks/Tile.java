package com.github.tonivade.bricks;

public record Tile(Position position, Color color) {

  public boolean adjacent(Tile other) {
    return this.position.adjacent(other.position) && this.color == other.color;
  }
}
