/*
 * Copyright (c) 2024-2026, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.bricks;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Main extends Application {

  private static final int TILE_SIZE = 20;
  private static final int PADDING = TILE_SIZE * 2;

  private final Pane pane;
  private final int boardHeight;
  private final int boardWidth;

  private final Label bricksLabel = new Label();
  private final Label timeLabel = new Label();
  private final Timeline timer;

  private Matrix matrix;
  private Instant startTime;

  public Main() {
    matrix = new Matrix(10, 15).shuffle(Color::random);
    pane = new Pane();
    boardHeight = matrix.height() * TILE_SIZE;
    boardWidth = matrix.width() * TILE_SIZE;
    timer = new Timeline(
        new KeyFrame(
            javafx.util.Duration.seconds(1),
            _ -> updateTime()
        )
    );
    timer.setCycleCount(Timeline.INDEFINITE);
  }

  @Override
  public void start(Stage stage) throws Exception {
    var status = new HBox(20, bricksLabel, timeLabel);
    status.setAlignment(Pos.CENTER);
    status.setPadding(new Insets(10));

    var root = new VBox(10, status, pane);
    root.setAlignment(Pos.CENTER);
    root.setPadding(new Insets(10));

    var scene = new Scene(
        root,
        boardWidth + PADDING,
        boardHeight + PADDING + 50
        );

    scene.setFill(javafx.scene.paint.Color.WHITE);

    pane.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onClick);

    pane.getChildren().clear();
    pane.getChildren().addAll(paint());

    updateBricks();
    startTimer();

    stage.setScene(scene);
    stage.show();
  }

  private void updateBricks() {
    bricksLabel.setText("Bricks: " + matrix.size());
  }

  private void startTimer() {
    startTime = Instant.now();
    updateTime();
    timer.playFromStart();
  }

  private void stopTimer() {
    timer.stop();
  }

  private void updateTime() {
    timeLabel.setText("Time: " + elapsedTime(Duration.between(startTime, Instant.now())));
  }

  private String elapsedTime(Duration elapsed) {
    long seconds = elapsed.getSeconds();
    long hours = seconds / 3600;
    long minutes = (seconds % 3600) / 60;
    long remainingSeconds = seconds % 60;

    if (hours == 0) {
      return "%02d:%02d".formatted(minutes, remainingSeconds);
    }
    return "%02d:%02d:%02d".formatted(hours, minutes, remainingSeconds);
  }

  private void onClick(MouseEvent event) {
    int x = (int) event.getX() / TILE_SIZE - 1;
    int y = matrix.height() - (int) event.getY() / TILE_SIZE;

    matrix = Matrix.clickS(new Position(x, y)).runS(matrix);

    pane.getChildren().clear();
    pane.getChildren().addAll(paint());

    updateBricks();

    if (matrix.gameOver()) {
      stopTimer();

      if (matrix.isEmpty()) {
        win(Duration.between(startTime, Instant.now()));
      } else {
        gameOver(matrix.size());
      }
    }
  }

  private void win(Duration elapsedTime) {
    new Alert(AlertType.INFORMATION, "YOU WIN!!\n" + elapsedTime(elapsedTime)).showAndWait();
  }

  private void gameOver(int bricks) {
    var alert = new Alert(AlertType.CONFIRMATION, "GAME OVER!!!");
    alert.setHeaderText("GAME OVER!!!\n" + bricks + " bricks remaning.");
    alert.setContentText("Do you want to play again?");
    var result = alert.showAndWait();

    result.filter(button -> button == ButtonType.OK)
      .ifPresentOrElse(_ -> playAgain() , () -> Platform.exit());
  }

  private void playAgain() {
    matrix = matrix.shuffle(Color::random);

    pane.getChildren().clear();
    pane.getChildren().addAll(paint());

    updateBricks();
    startTimer();
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
