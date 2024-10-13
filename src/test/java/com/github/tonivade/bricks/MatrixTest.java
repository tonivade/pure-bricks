package com.github.tonivade.bricks;

import static com.github.tonivade.purefun.data.Sequence.arrayOf;
import static com.github.tonivade.purefun.type.Option.none;
import static com.github.tonivade.purefun.type.Option.some;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MatrixTest {

  @Test
  void shouldBeEmptyWhenNoTiles() {
    var matrix = new Matrix(3, 3);

    System.out.println(matrix.toString());

    assertTrue(matrix.isEmpty());
    assertEquals(0, matrix.size());
  }

  @Test
  void shouldNotEmptyWhenShuffle() {
    var matrix = new Matrix(3, 3).shuffle(ignore -> Color.RED);

    System.out.println(matrix.toString());

    assertFalse(matrix.isEmpty());
    assertEquals(9, matrix.size());
    assertEquals(some(new Tile(new Position(1, 2), Color.RED)), matrix.atPosition(new Position(1, 2)));
    assertTrue(matrix.isPresent(new Position(1, 2)));
  }

  @Test
  void shouldMoveTiles() {
    var matrix0 = new Matrix(3, 3, arrayOf(new Tile(new Position(0, 0), Color.RED)));

    System.out.println(matrix0.toString());
    var matrix1 = matrix0.atPosition(new Position(0, 0)).map(tile -> matrix0.move(tile, new Position(1, 1))).getOrElseThrow();
    System.out.println(matrix1.toString());

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

    System.out.println(matrix0.toString());
    var matrix1 = matrix0.cleanColumn(0);
    System.out.println(matrix1.toString());

    assertFalse(matrix0.isEmpty());
    assertTrue(matrix1.isEmpty());
  }

  @Test
  void shouldMoveAColumnOfTiles() {
    var matrix0 = new Matrix(3, 3,
        arrayOf(new Tile(new Position(0, 0), Color.RED),
                new Tile(new Position(0, 1), Color.BLUE),
                new Tile(new Position(0, 2), Color.YELLOW)));

    System.out.println(matrix0.toString());
    var matrix1 = matrix0.moveColumn(0, 2);
    System.out.println(matrix1.toString());
  }
}
