/*
 * Copyright (c) 2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.bricks;

import static com.github.tonivade.purefun.data.Sequence.arrayOf;
import static com.github.tonivade.purefun.data.Sequence.emptyArray;
import static com.github.tonivade.purefun.type.Option.none;
import static com.github.tonivade.purefun.type.Option.some;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.github.tonivade.purefun.data.ImmutableMap;
import org.junit.jupiter.api.Test;

class MatrixTest {

  @Test
  void shouldCheckHeightWidthMap() {
    assertThrows(IllegalArgumentException.class, () -> new Matrix(1, 0));
    assertThrows(IllegalArgumentException.class, () -> new Matrix(0, 1));
    assertThrows(IllegalArgumentException.class, () -> new Matrix(1, 1, (ImmutableMap<Position, Tile>) null));
  }

  @Test
  void shouldBeEmptyWhenNoTiles() {
    var matrix = new Matrix(3, 3);

    System.out.println(matrix);

    assertTrue(matrix.isEmpty());
    assertTrue(matrix.gameover());
    assertEquals(0, matrix.size());
  }

  @Test
  void shouldNotEmptyWhenShuffle() {
    var matrix = new Matrix(3, 3).shuffle(ignore -> Color.RED);

    System.out.println(matrix);

    assertFalse(matrix.isEmpty());
    assertEquals(9, matrix.size());
    assertEquals(some(new Tile(new Position(1, 2), Color.RED)), matrix.atPosition(new Position(1, 2)));
    assertTrue(matrix.isPresent(new Position(1, 2)));
  }

  @Test
  void shouldMoveTiles() {
    var matrix0 = new Matrix(3, 3, arrayOf(new Tile(new Position(0, 0), Color.RED)));

    System.out.println(matrix0);
    var matrix1 = matrix0.atPosition(new Position(0, 0)).map(tile -> matrix0.move(tile, new Position(1, 1))).getOrElseThrow();
    System.out.println(matrix1);

    assertEquals(some(new Tile(new Position(0, 0), Color.RED)), matrix0.atPosition(new Position(0, 0)));
    assertEquals(none(), matrix0.atPosition(new Position(1, 1)));
    assertEquals(none(), matrix1.atPosition(new Position(0, 0)));
    assertEquals(some(new Tile(new Position(1, 1), Color.RED)), matrix1.atPosition(new Position(1, 1)));
  }

  @Test
  void shouldCleanAColumnOfTiles() {
    var matrix0 = new Matrix(3, 3,
        arrayOf(new Tile(new Position(0, 0), Color.RED),
                new Tile(new Position(0, 1), Color.BLUE),
                new Tile(new Position(0, 2), Color.YELLOW)));

    System.out.println(matrix0);
    var matrix1 = matrix0.cleanColumn(0);
    System.out.println(matrix1);

    assertFalse(matrix0.isEmpty());
    assertTrue(matrix1.isEmpty());
  }

  @Test
  void shouldMoveAColumnOfTiles() {
    var matrix0 = new Matrix(3, 3,
        arrayOf(new Tile(new Position(0, 0), Color.RED),
                new Tile(new Position(0, 1), Color.BLUE),
                new Tile(new Position(0, 2), Color.YELLOW)));

    System.out.println(matrix0);
    var matrix1 = matrix0.moveColumn(0, 2);
    System.out.println(matrix1);

    assertEquals(emptyArray(), matrix1.atCol(0).map(Tile::color));
    assertEquals(arrayOf(Color.RED, Color.BLUE, Color.YELLOW), matrix1.atCol(2).map(Tile::color));
  }

  @Test
  void shouldCleanARowOfTiles() {
    var matrix0 = new Matrix(3, 3,
        arrayOf(new Tile(new Position(0, 0), Color.RED),
                new Tile(new Position(1, 0), Color.BLUE),
                new Tile(new Position(2, 0), Color.YELLOW)));

    System.out.println(matrix0);
    var matrix1 = matrix0.cleanRow(0);
    System.out.println(matrix1);

    assertFalse(matrix0.isEmpty());
    assertTrue(matrix1.isEmpty());
  }

  @Test
  void shouldMoveARowOfTiles() {
    var matrix0 = new Matrix(3, 3,
        arrayOf(new Tile(new Position(0, 0), Color.RED),
                new Tile(new Position(1, 0), Color.BLUE),
                new Tile(new Position(2, 0), Color.YELLOW)));

    System.out.println(matrix0);
    var matrix1 = matrix0.moveRow(0, 2);
    System.out.println(matrix1);

    assertEquals(emptyArray(), matrix1.atRow(0).map(Tile::color));
    assertEquals(arrayOf(Color.RED, Color.BLUE, Color.YELLOW), matrix1.atRow(2).map(Tile::color));
  }

  @Test
  void shouldMoveFallTilesDown() {
    var matrix0 = new Matrix(3, 3,
        arrayOf(new Tile(new Position(0, 2), Color.RED),
                new Tile(new Position(1, 2), Color.BLUE),
                new Tile(new Position(2, 2), Color.YELLOW)));

    System.out.println(matrix0);
    var matrix1 = matrix0.fall();
    System.out.println(matrix1);

    assertEquals(emptyArray(), matrix1.atRow(2).map(Tile::color));
    assertEquals(arrayOf(Color.RED, Color.BLUE, Color.YELLOW), matrix1.atRow(0).map(Tile::color));
  }

  @Test
  void shouldMoveShiftTilesDown() {
    var matrix0 = new Matrix(3, 3,
        arrayOf(new Tile(new Position(2, 0), Color.RED),
                new Tile(new Position(2, 1), Color.BLUE),
                new Tile(new Position(2, 2), Color.YELLOW)));

    System.out.println(matrix0);
    var matrix1 = matrix0.shift();
    System.out.println(matrix1);

    assertEquals(emptyArray(), matrix1.atCol(2).map(Tile::color));
    assertEquals(arrayOf(Color.RED, Color.BLUE, Color.YELLOW), matrix1.atCol(0).map(Tile::color));
  }

  @Test
  void shouldCleanShiftAndFallAtClick() {
    var matrix0 = new Matrix(3, 3,
        arrayOf(new Tile(new Position(2, 0), Color.RED),
                new Tile(new Position(2, 1), Color.RED),
                new Tile(new Position(2, 2), Color.GREEN)));

    System.out.println(matrix0);
    var matrix1 = Matrix.clickS(new Position(2, 0)).runS(matrix0);
    System.out.println(matrix1);

    assertEquals(arrayOf(Color.GREEN), matrix1.atRow(0).map(Tile::color));
    assertTrue(matrix1.gameover());
  }

  @Test
  void shouldCleanFallAndShiftAtClick() {
    var matrix0 = new Matrix(3, 3,
        arrayOf(new Tile(new Position(0, 2), Color.RED),
                new Tile(new Position(1, 2), Color.RED),
                new Tile(new Position(2, 2), Color.GREEN)));

    System.out.println(matrix0);
    var matrix1 = Matrix.clickS(new Position(0, 2)).runS(matrix0);
    System.out.println(matrix1);

    assertEquals(arrayOf(Color.GREEN), matrix1.atRow(0).map(Tile::color));
    assertTrue(matrix1.gameover());
  }
}
