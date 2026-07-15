package com.tdem.milesheetcreator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        try {
            URL url = MainApplication.class.getResource("/fxml/MainView.fxml");

            if (url == null) {
                throw new RuntimeException("Couldn't find MainView.fxml");
            }

            FXMLLoader fxmlLoader = new FXMLLoader(url);

            Scene scene = new Scene(fxmlLoader.load());

            stage.setTitle("Mileage Exception Tool");
            stage.setScene(scene);
            stage.show();
        } catch (Throwable t) {
            try(PrintWriter out = new PrintWriter("error.log")) {
                t.printStackTrace(out);
            } catch (Exception ignored) {
                throw t;
            }
        }
    }

    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Throwable t) {
            try(PrintWriter out = new PrintWriter("error.log")) {
                t.printStackTrace(out);
            } catch (Exception ignored) {
                throw t;
            }
        }

    }
}