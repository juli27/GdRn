package de.uulm.in.vs.grn.c1;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Pair;
import javax.imageio.ImageIO;

public final class MainStageController {

  @FXML
  private ComboBox<String> cbFilters;
  @FXML
  private ImageView imageView;

  private Stage stage;
  private FileChooser fileChooser = new FileChooser();
  private Path shownImagePath;
  private BufferedImage shownBufferedImage;
  private Dialog<Pair<String, String>> serverDialog = new Dialog<>();

  private String host = "134.60.77.151";
  private int port = 7777;

  @FXML
  private void initialize() {
    // only png files supported
    fileChooser.getExtensionFilters().addAll(
        new ExtensionFilter("Image Files", "*.png")
    );

    cbFilters.getItems().addAll(
        "NOFILTER",
        "BLACKWHITE",
        "EIGHTBIT",
        "YOLO",
        "SWAG",
        "SUMMER",
        "SEPIA"
    );

    // select the first filter
    cbFilters.getSelectionModel().clearAndSelect(0);

    // Server dialog
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    TextField host = new TextField();
    host.setPromptText("Hostname/IP-Address");
    TextField port = new TextField();
    port.setPromptText("port");

    grid.add(new Label("Host:"), 0, 0);
    grid.add(host, 1, 0);
    grid.add(new Label("Port:"), 0, 1);
    grid.add(port, 1, 1);

    serverDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    serverDialog.getDialogPane().setContent(grid);

    serverDialog.setResultConverter(dialogButton -> {
      if (dialogButton == ButtonType.OK) {
        return new Pair<>(host.getText(), port.getText());
      }

      return null;
    });

    serverDialog.setTitle("Sockagram Server");
  }


  @FXML
  private void onLoadButtonAction() {
    fileChooser.setTitle("Load Image");
    File file = fileChooser.showOpenDialog(stage);

    if (file != null) {
      try {
        shownImagePath = file.toPath();
        InputStream fileIn = Files.newInputStream(shownImagePath);
        imageView.setImage(new Image(fileIn));

        shownBufferedImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }

  @FXML
  private void onSaveButtonAction() {
    try (OutputStream out = Files.newOutputStream(shownImagePath)) {
      ImageIO.write(shownBufferedImage, "png", out);
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  @FXML
  private void onSaveAsButtonAction() {
    fileChooser.setTitle("Save Image");
    File file = fileChooser.showSaveDialog(stage);

    if (file != null) {
      shownImagePath = file.toPath();

      try (OutputStream out = Files.newOutputStream(shownImagePath)) {
        ImageIO.write(shownBufferedImage, "png", out);
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }

  public void onServerButtonAction() {
    Optional<Pair<String, String>> result = serverDialog.showAndWait();

    if (result.isPresent()) {
      Pair<String, String> pair = result.get();
      host = pair.getKey();
      port = Integer.parseInt(pair.getValue());
    }
  }

  @FXML
  private void onApplyButtonAction() {
    byte filterType;

    switch (cbFilters.getSelectionModel().getSelectedItem()) {
      case "BLACKWHITE":
        filterType = 1;
        break;
      case "EIGHTBIT":
        filterType = 2;
        break;
      case "YOLO":
        filterType = 3;
        break;
      case "SWAG":
        filterType = 4;
        break;
      case "SUMMER":
        filterType = 5;
        break;
      case "SEPIA":
        filterType = 6;
        break;
      default:
        filterType = 0;
    }

    // connect to the sockagram server
    try (Socket connection = new Socket(host, port)) {
      DataInputStream in = new DataInputStream(connection.getInputStream());
      DataOutputStream out = new DataOutputStream(connection.getOutputStream());

      ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
      ImageIO.write(shownBufferedImage, "png", byteBuffer);

      // request
      out.write(filterType);
      out.writeInt(byteBuffer.size());
      out.write(byteBuffer.toByteArray());

      byte status = in.readByte();
      int responseLength = in.readInt();

      if (status == 0) {
        shownBufferedImage = ImageIO.read(in);
        imageView.setImage(SwingFXUtils.toFXImage(shownBufferedImage, null));
      } else {
        byte[] buffer = new byte[responseLength];
        int bytesRead = in.read(buffer);

        if (bytesRead != responseLength) {
          throw new RuntimeException();
        }

        String errorMessage = new String(buffer);
        System.err.println("Sockagram error(" + status + "): " + errorMessage);
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  void setStage(Stage stage) {
    this.stage = stage;
  }
}
