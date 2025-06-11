/*
 * Copyright (c) 2024-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
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

  private static final int TILE_SIZE = 20;
  private static final int PADDING = TILE_SIZE * 2;

  private final Pane pane;
  private final int boardHeight;
  private final int boardWidth;

  private Matrix matrix;

  public Main() {
    matrix = new Matrix(10, 15).shuffle(Color::random);
    pane = new Pane();
    boardHeight = matrix.height() * TILE_SIZE;
    boardWidth = matrix.width() * TILE_SIZE;
  }

  @Override
  public void start(Stage stage) throws Exception {

    Scene scene = new Scene(pane, boardWidth + PADDING, boardHeight + PADDING);
    scene.setFill(javafx.scene.paint.Color.WHITE);

    pane.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onClick);

    pane.getChildren().clear();
    pane.getChildren().addAll(paint());

    stage.setScene(scene);
    stage.show();
  }

  private void onClick(MouseEvent event) {
    int x = (Double.valueOf(event.getSceneX()).intValue() / TILE_SIZE) - 1;
    int y = matrix.height() - (Double.valueOf(event.getSceneY()).intValue() / TILE_SIZE);
    matrix = Matrix.clickS(new Position(x, y)).runS(matrix);
    pane.getChildren().clear();
    pane.getChildren().addAll(paint());

    if (matrix.gameOver()) {
      if (matrix.isEmpty()) {
        win();
      } else {
        gameOver();
      }
    }
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
    matrix = matrix.shuffle(Color::random);
    pane.getChildren().clear();
    pane.getChildren().addAll(paint());
  }

  private Collection<Rectangle> paint() {
    return matrix.bricks().values().map(this::toRectangle).toCollection();
  }

  private Rectangle toRectangle(Tile tile) {
    var rectangle = new Rectangle(
        TILE_SIZE + (tile.position().x() * TILE_SIZE),
       boardHeight - (tile.position().y() * TILE_SIZE),
       TILE_SIZE,
       TILE_SIZE);
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
