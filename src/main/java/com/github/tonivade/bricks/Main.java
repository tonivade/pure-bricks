/*
 * Copyright (c) 2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.bricks;

import java.util.Collection;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Main extends Application {

  private Matrix matrix = new Matrix(10, 15).shuffle(Color::random);
  private Pane pane = new Pane();

  private final int tileSize = 20;
  private final int padding = tileSize * 2;
  private final int boardHeight = matrix.height() * tileSize;
  private final int boardWidth = matrix.width() * tileSize;

  @Override
  public void start(Stage stage) throws Exception {

    Scene scene = new Scene(pane, boardWidth + padding, boardHeight + padding);
    scene.setFill(javafx.scene.paint.Color.WHITE);

    pane.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
      int x = (Double.valueOf(event.getSceneX()).intValue() / tileSize) - 1;
      int y = matrix.height() - (Double.valueOf(event.getSceneY()).intValue() / tileSize);
      this.matrix = Matrix.clickS(new Position(x, y)).runS(matrix);
      pane.getChildren().clear();
      pane.getChildren().addAll(paint());

      if (this.matrix.gameOver()) {
        if (this.matrix.isEmpty()) {
          win();
        } else {
          gameOver();
        }
      }
    });

    pane.getChildren().clear();
    pane.getChildren().addAll(paint());

    stage.setScene(scene);
    stage.show();
  }

  private void win() {
    new Alert(AlertType.INFORMATION, "YOU WIN!!").showAndWait();
  }

  private void gameOver() {
    var alert = new Alert(AlertType.CONFIRMATION, "GAME OVER!!!");
    alert.setHeaderText("GAME OVER!!!");
    alert.setContentText("Do you want to play again?");
    var result = alert.showAndWait();

    result.filter(button -> button == ButtonType.OK)
      .ifPresentOrElse(_ -> playAgain() , () -> Platform.exit());
  }

  private void playAgain() {
    this.matrix = matrix.shuffle(Color::random);
    pane.getChildren().clear();
    pane.getChildren().addAll(paint());
  }

  private Collection<Rectangle> paint() {
    return matrix.bricks().values().map(this::toRectangle).toCollection();
  }

  private Rectangle toRectangle(Tile tile) {
    var rectangle = new Rectangle(
        tileSize + (tile.position().x() * tileSize),
       boardHeight - (tile.position().y() * tileSize),
       tileSize,
       tileSize);
    rectangle.setFill(toColor(tile.color()));
    return rectangle;
  }

  private javafx.scene.paint.Color toColor(Color color) {
    return switch (color) {
    case RED -> javafx.scene.paint.Color.RED;
    case GREEN -> javafx.scene.paint.Color.GREEN;
    case BLUE -> javafx.scene.paint.Color.BLUE;
    case YELLOW -> javafx.scene.paint.Color.YELLOW;
    };
  }

  public static void main(String[] args) {
    launch(args);
  }
}
