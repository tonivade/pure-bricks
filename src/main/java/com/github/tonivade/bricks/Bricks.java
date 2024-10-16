/*
 * Copyright (c) 2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.bricks;

import static com.github.tonivade.purefun.core.Unit.unit;
import static java.lang.Integer.parseInt;

import com.github.tonivade.purefun.core.Unit;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.monad.IOOf;
import com.github.tonivade.purefun.transformer.StateT;
import com.github.tonivade.purefun.type.Try;
import com.github.tonivade.purefun.typeclasses.Console;
import com.github.tonivade.purefun.typeclasses.Instances;
import com.github.tonivade.purefun.typeclasses.Monad;

public class Bricks {

  static final Monad<IO<?>> monad = Instances.monad();

  static final Console<IO<?>> console = Instances.console();

  static final StateT<IO<?>, Matrix, String> read = StateT.lift(monad, console.readln());

  static final StateT<IO<?>, Matrix, Try<Integer>> readInt = read.map(s -> Try.of(() -> parseInt(s)));

  static final StateT<IO<?>, Matrix, Unit> print(String text, Object...args) {
    return StateT.lift(monad, console.printf(text, args));
  }

  static final StateT<IO<?>, Matrix, Unit> quit = StateT.pure(monad, unit());

  static final StateT<IO<?>, Matrix, Unit> click(Position position) {
    return print("Clicked %s", position)
        .andThen(StateT.lift(monad, Matrix.clickS(position)::run));
  }

  static final StateT<IO<?>, Matrix, String> matrixToString = StateT.inspect(monad, Matrix::toString);

  static final StateT<IO<?>, Matrix, Boolean> gameover = StateT.inspect(monad, Matrix::gameover);

  static final StateT<IO<?>, Matrix, Integer> numberOfTiles = StateT.inspect(monad, Matrix::size);

  static final StateT<IO<?>, Matrix, Unit> shuffle = StateT.modify(monad, m -> m.shuffle(Color::random));

  static final StateT<IO<?>, Matrix, Unit> printMatrix =
      matrixToString.flatMap(Bricks::print)
        .andThen(numberOfTiles)
        .flatMap(n -> print("%d tiles left", n));

  static final StateT<IO<?>, Matrix, Unit> exit =
      printMatrix.andThen(numberOfTiles)
        .flatMap(n -> n > 0 ? print("Gameover!!!") : print("You win!!!"));

  static final StateT<IO<?>, Matrix, Try<Position>> readPosition =
      StateT.map2(
          print("Please entry X").andThen(readInt),
          print("Please entry Y").andThen(readInt),
          (x, y) -> Try.map2(x, y, Position::new));

  static final StateT<IO<?>, Matrix, Unit> error(Throwable error) {
    return print("Invalid position! %s", error).andThen(loop());
  }

  static final StateT<IO<?>, Matrix, Unit> loop() {
    return printMatrix
      .andThen(readPosition)
      .flatMap(pos -> pos.fold(Bricks::error, Bricks::click))
      .andThen(gameover)
      .flatMap(end -> end ? exit : loop());
  }

  static final StateT<IO<?>, Matrix, Unit> mainLoop() {
    return print("Let's play a game")
      .andThen(shuffle)
      .andThen(loop())
      .andThen(print("Do you whant to play again?"))
      .andThen(read)
      .flatMap(s -> s.equals("n") ? quit : mainLoop());
  }

  public static void main(String[] args) {
    mainLoop().run(new Matrix(5, 5)).fix(IOOf::toIO).unsafeRunSync();
  }
}
