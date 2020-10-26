package athensclub.speedtype;

import athensclub.speedtype.components.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        MainView mainView = new MainView();
        mainView.getController().addHandlerToStage(primaryStage);
        Scene scene = new Scene(mainView);
        primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("keyboard_icon.png")));
        primaryStage.setTitle("Typing Speed Test");
        primaryStage.setScene(scene);
        primaryStage.show();;
    }
}
