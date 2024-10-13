package com.github.tonivade.bricks;

import static com.github.tonivade.purefun.data.Sequence.setOf;

import com.github.tonivade.purefun.data.ImmutableSet;

public record Position(int x, int y) {

  public ImmutableSet<Position> neighbors() {
    return setOf(up(), down(), right(), left());
  }

  public Position up() {
    return new Position(x, y + 1);
  }

  public Position down() {
    return new Position(x, y - 1);
  }

  public Position right() {
    return new Position(x + 1, y);
  }

  public Position left() {
    return new Position(x - 1, y);
  }

  public double distance(Position other) {
    int diffx = this.x - other.x;
    int diffy = this.y - other.y;
    return Math.sqrt(Math.pow(diffx, 2) + Math.pow(diffy, 2));
  }

  public boolean adjacent(Position other) {
    return distance(other) == 1.;
  }
}
