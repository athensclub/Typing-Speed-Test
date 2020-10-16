package athensclub.speedtype.components;

import athensclub.speedtype.controllers.MainViewController;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class MainView extends BorderPane {

    private MainViewController controller;

    public MainView(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main_view.fxml"));
            loader.setRoot(this);
            loader.load();
            controller = loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MainViewController getController() {
        return controller;
    }
}
