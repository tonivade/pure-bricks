/*
 * Copyright (c) 2024-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.bricks;

import static com.github.tonivade.purefun.core.Function1.identity;
import static com.github.tonivade.purefun.core.Matcher1.not;
import static com.github.tonivade.purefun.core.Precondition.checkNonNull;
import static com.github.tonivade.purefun.core.Precondition.checkPositive;
import static com.github.tonivade.purefun.data.ImmutableMap.toImmutableMap;
import static com.github.tonivade.purefun.data.Sequence.arrayOf;
import static com.github.tonivade.purefun.data.Sequence.emptyArray;

import com.github.tonivade.purefun.core.Function1;
import com.github.tonivade.purefun.core.Tuple;
import com.github.tonivade.purefun.core.Tuple2;
import com.github.tonivade.purefun.core.Unit;
import com.github.tonivade.purefun.data.ImmutableArray;
import com.github.tonivade.purefun.data.ImmutableMap;
import com.github.tonivade.purefun.data.ImmutableSet;
import com.github.tonivade.purefun.data.Range;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.monad.State;
import com.github.tonivade.purefun.type.Option;

public record Matrix(int width, int height, ImmutableMap<Position, Tile> bricks) {

  public Matrix {
    checkPositive(width);
    checkPositive(height);
    checkNonNull(bricks);
  }

  public Matrix(int width, int height) {
    this(width, height, emptyArray());
  }

  public Matrix(int width, int height, Sequence<Tile> tiles) {
    this(width, height, tiles.stream().collect(toImmutableMap(Tile::position, identity())));
  }

  public static State<Matrix, Unit> clickS(Position position) {
    return lookup(position).flatMap(Matrix::cleanFallShift);
  }

  private static State<Matrix, Unit> cleanFallShift(Sequence<Position> toClean) {
    return cleanS(toClean).andThen(fallS).andThen(shiftS);
  }

  private static State<Matrix, Sequence<Position>> lookup(Position position) {
    return State.inspect(m -> m.adjacent(position));
  }

  private static State<Matrix, Unit> cleanS(Sequence<Position> toClean) {
    return State.modify(m -> m.clean(toClean));
  }

  private static State<Matrix, Unit> fallS = State.modify(Matrix::fall);

  private static State<Matrix, Unit> shiftS = State.modify(Matrix::shift);

  public Matrix shuffle(Function1<Position, Color> nextColor) {
    return new Matrix(width, height, positions().map(p -> new Tile(p, nextColor.apply(p))));
  }

  public Matrix move(Tile tile, Position position) {
    return clean(arrayOf(tile.position())).addTiles(arrayOf(new Tile(position, tile.color())));
  }

  public Matrix moveColumn(int from, int to) {
    var newColumn = col(from)
        .map(p -> atPosition(p).map(t -> new Tile(new Position(to, p.y()), t.color())))
        .flatMap(Option::sequence);
    return cleanColumn(from).addTiles(newColumn);
  }

  public Matrix moveRow(int from, int to) {
    var newRow = row(from)
        .map(p -> atPosition(p).map(t -> new Tile(new Position(p.x(), to), t.color())))
        .flatMap(Option::sequence);
    return cleanRow(from).addTiles(newRow);
  }

  public Matrix cleanRow(int y) {
    return clean(row(y));
  }

  public Matrix cleanColumn(int x) {
    return clean(col(x));
  }

  public Matrix clean(Sequence<Position> positions) {
    return new Matrix(width, height, bricks.removeAll(positions));
  }

  public Matrix addTiles(Sequence<Tile> toAdd) {
    var newTiles = toAdd.map(t -> Tuple.of(t.position(), t))
        .stream().collect(toImmutableMap(Tuple2::get1, Tuple2::get2));
    return new Matrix(width, height, bricks.putAll(newTiles));
  }

  public Sequence<Position> adjacent(Position position) {
    return atPosition(position)
        .map(t -> visit(t, ImmutableSet.empty()))
        .getOrElse(ImmutableSet.empty());
  }

  public Option<Tile> atPosition(int x, int y) {
    return atPosition(new Position(x, y));
  }

  public Option<Tile> atPosition(Position position) {
    return bricks.get(position);
  }

  public Sequence<Tile> atCol(int x) {
    return col(x).map(this::atPosition).flatMap(Option::sequence);
  }

  public Sequence<Tile> atRow(int y) {
    return row(y).map(this::atPosition).flatMap(Option::sequence);
  }

  public boolean isPresent(Position position) {
    return bricks.containsKey(position);
  }

  public int size() {
    return bricks.size();
  }

  public boolean isEmpty() {
    return bricks.isEmpty();
  }

  public boolean gameOver() {
    return bricks.values().flatMap(this::search).isEmpty();
  }

  public ImmutableArray<Position> positions() {
    var positions = Range.of(0, width).map(
        x -> Range.of(0, height).map(
            y -> new Position(x, y)));
    return positions.flatMap(identity());
  }

  public ImmutableArray<ImmutableArray<Position>> rows() {
    return Range.of(0, height).map(this::row);
  }

  public ImmutableArray<Position> row(int y) {
    return Range.of(0, width).map(x -> new Position(x, y)).asArray();
  }

  public ImmutableArray<ImmutableArray<Position>> cols() {
    return Range.of(0, width).map(this::col).asArray();
  }

  public ImmutableArray<Position> col(int x) {
    return Range.of(0, height).map(y -> new Position(x, y)).asArray();
  }

  public Matrix fall() {
    return fallCol(0);
  }

  public Matrix shift() {
    return shiftCol(0);
  }

  private Matrix fallCol(int col) {
    if (col < width) {
      return fallTile(col, 0).fallCol(col + 1);
    }
    return this;
  }

  private Matrix shiftCol(int col) {
    if (col < width) {
      return tryMoveCol(col).getOrElse(this).shiftCol(col + 1);
    }
    return this;
  }

  private Matrix fallTile(int col, int top) {
    if (top < height) {
      return tryMove(col, top).getOrElse(this).fallTile(col, top + 1);
    }
    return this;
  }

  private Option<Matrix> tryMoveCol(int col) {
    return nextX(col).map(nextX -> moveColumn(col, nextX));
  }

  private Option<Matrix> tryMove(int col, int top) {
    return atPosition(col, top)
      .flatMap(t -> nextY(col, top)
          .map(nextY -> move(t, new Position(col, nextY))));
  }

  private Option<Integer> nextX(int left) {
    return row(0).takeWhile(p -> p.x() < left)
        .findFirst(not(this::isPresent)).map(Position::x);
  }

  private Option<Integer> nextY(int col, int top) {
    return col(col).takeWhile(p -> p.y() < top)
        .findFirst(not(this::isPresent)).map(Position::y);
  }

  private ImmutableSet<Position> visit(Tile tile, ImmutableSet<Tile> visited) {
    var tiles = search(tile);
    return tiles.map(Tile::position)
        .appendAll(tiles.difference(visited).flatMap(t -> visit(t, visited.append(tile))));
  }

  private ImmutableSet<Tile> search(Tile current) {
    return neighbors(current).filter(current::adjacent);
  }

  private ImmutableSet<Tile> neighbors(Tile current) {
    return current.position().neighbors()
        .map(this::atPosition)
        .flatMap(Option::sequence);
  }

  @Override
  public final String toString() {
    var builder = new StringBuilder();

    builder.append("  ");
    Range.of(0, width).forEach(builder::append);
    builder.append("\n");

    Range.of(0, height).reverse().forEach(y -> {
      if (y < 10) {
        builder.append(" ");
      }
      builder.append(y);
      Range.of(0, width).forEach(x -> builder.append(printTile(x, y)));
      builder.append("\n");
    });

    return builder.toString();
  }

  private String printTile(Integer x, Integer y) {
    return atPosition(x, y)
        .map(tile -> tile.color().toString())
        .getOrElse(" ");
  }
}
