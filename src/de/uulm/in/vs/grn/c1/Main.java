package de.uulm.in.vs.grn.c1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class Main extends Application {

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("MainStage.fxml"));
    Parent root = loader.load();
    MainStageController controller = loader.getController();
    controller.setStage(primaryStage);

    Scene scene = new Scene(root);
    primaryStage.setScene(scene);
    primaryStage.setTitle("Sockagram");
    primaryStage.setResizable(false);
    primaryStage.show();
  }
}
