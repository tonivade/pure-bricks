/*
 * Copyright (c) 2024-2026, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.bricks;

import static com.github.tonivade.purefun.core.Unit.unit;

import java.time.Duration;

import com.github.tonivade.purefun.core.Unit;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.monad.IOOf;
import com.github.tonivade.purefun.transformer.StateT;
import com.github.tonivade.purefun.type.Try;
import com.github.tonivade.purefun.typeclasses.Console;
import com.github.tonivade.purefun.typeclasses.Instances;
import com.github.tonivade.purefun.typeclasses.Monad;

public class Bricks {

  private static final Monad<IO<?>> monad = Instances.monad();
  private static final Console<IO<?>> console = Instances.console();

  public static void main(String... args) {
    mainLoop().run(new Matrix(10, 15)).fix(IOOf::toIO).unsafeRunSync();
  }

  private static final StateT<IO<?>, Matrix, Unit> mainLoop() {
    return print("Let's play a game")
      .andThen(shuffle)
      .andThen(play())
      .andThen(print("Do you want to play again?"))
      .andThen(read)
      .flatMap(s -> s.equals("n") ? quit : mainLoop());
  }

  private static final StateT<IO<?>, Matrix, Unit> play() {
    return printMatrix
      .andThen(readPosition)
      .flatMap(pos -> pos.fold(Bricks::error, Bricks::click))
      .andThen(gameOver)
      .flatMap(end -> end ? exit : play());
  }

  private static final StateT<IO<?>, Matrix, String> read = StateT.lift(monad, console.readln());

  private static final StateT<IO<?>, Matrix, Try<Integer>> readInt = read.map(Bricks::parseInt);

  private static final StateT<IO<?>, Matrix, Try<Position>> readPosition =
      StateT.map2(
          print("Please entry X").andThen(readInt),
          print("Please entry Y").andThen(readInt),
          (x, y) -> Try.map2(x, y, Position::new));

  private static Try<Integer> parseInt(String string) {
    return Try.of(() -> Integer.parseInt(string));
  }

  private static final StateT<IO<?>, Matrix, Unit> print(String text, Object...args) {
    return StateT.lift(monad, console.printf(text, args));
  }

  private static final StateT<IO<?>, Matrix, Unit> quit = StateT.pure(monad, unit());

  private static final StateT<IO<?>, Matrix, Unit> click(Position position) {
    return print("Clicked %s", position)
        .andThen(sleep)
        .andThen(StateT.lift(monad, Matrix.clickS(position)::run));
  }

  private static final StateT<IO<?>, Matrix, String> matrixToString =
      StateT.inspect(monad, Matrix::toString);

  private static final StateT<IO<?>, Matrix, Unit> clearScreen =
      StateT.lift(monad, IO.exec(() -> System.out.print("\033[H\033[2J")));

  private static final StateT<IO<?>, Matrix, Boolean> gameOver =
      StateT.inspect(monad, Matrix::gameOver);

  private static final StateT<IO<?>, Matrix, Integer> numberOfTiles =
      StateT.inspect(monad, Matrix::size);

  private static final StateT<IO<?>, Matrix, Unit> shuffle =
      StateT.modify(monad, m -> m.shuffle(Color::random));

  private static final StateT<IO<?>, Matrix, Unit> printMatrix =
      clearScreen
        .andThen(matrixToString)
        .flatMap(Bricks::print)
        .andThen(numberOfTiles)
        .flatMap(n -> print("%d tiles left", n));

  private static final StateT<IO<?>, Matrix, Unit> exit =
      printMatrix.andThen(numberOfTiles)
        .flatMap(n -> n > 0 ? print("Game over!!!") : print("You win!!!"));

  private static final StateT<IO<?>, Matrix, Unit> error(Throwable error) {
    return print("Invalid position! %s", error).andThen(sleep).andThen(play());
  }

  private static final StateT<IO<?>, Matrix, Unit> sleep =
      StateT.lift(monad, IO.sleep(Duration.ofSeconds(1)));
}
