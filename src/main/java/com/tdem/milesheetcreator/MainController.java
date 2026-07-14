package com.tdem.milesheetcreator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.CheckBox;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainController {

    @FXML
    private TextField inputFileField;

    @FXML
    private TextField outputFileField;

    @FXML
    private Label statusLabel;

    @FXML
    private TextField carCountField;

    @FXML
    private TextField exceptionsField;

    @FXML
    private CheckBox secondTransferCheckBox;

    @FXML
    private CheckBox openWhenFinishedCheckBox;

    @FXML
    private TextArea statusArea;

    private File generatedFile;

    public void initialize() {

        Config config = new Config(0, null, false);

        List<String> configList = config.getFromConfig();

        carCountField.setText(configList.get(0));
        exceptionsField.setText(configList.get(1));

    }


    @FXML
    private void handleGenerate() {
        statusArea.clear();
        statusArea.appendText("Starting report generation...\n");

        if (inputFileField.getText().isEmpty()
                || outputFileField.getText().isEmpty()) {

            statusLabel.setText("Please select files first.");
            return;
        }

        File inputFile = new File(inputFileField.getText());
        File outputFolder = new File(outputFileField.getText());

        int carCount = Integer.parseInt(carCountField.getText());

        List<String> exceptions = Arrays.stream(
                        exceptionsField.getText().split(","))
                .map(String::trim)
                .toList();

        boolean secondTransfer = secondTransferCheckBox.isSelected();

        Config config = new Config(
                carCount,
                exceptions,
                secondTransfer
        );

        // Save the configuration for next time
        config.writeToConfig();

        Task<Void> task = new Task<>() {

            @Override
            protected Void call() throws Exception {

                updateMessage("Creating spreadsheet...");

                generatedFile = MileageExceptionGenerator.generate(
                        inputFile,
                        outputFolder,
                        config,
                        this::updateMessage
                );



                return null;
            }
        };

        task.messageProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                statusArea.appendText(newValue + "\n");
            }
        });

        task.setOnSucceeded(e -> {
            statusLabel.textProperty().unbind();
            statusLabel.setText("Report complete!");

                try {
                    if (openWhenFinishedCheckBox.isSelected()) {

                        if (System.getProperty("os.name").toLowerCase().contains("linux")) {

                            new ProcessBuilder(
                                    "xdg-open",
                                    generatedFile.getAbsolutePath()
                            ).start();

                        } else {
                            Desktop.getDesktop().open(generatedFile);
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

        task.setOnFailed(e -> {
            statusLabel.textProperty().unbind();
            statusLabel.setText("Error creating report.");
            task.getException().printStackTrace();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleInputBrowse() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Mileage File");

        File defaultFolder = new File(
                "/home/tdem/Downloads"
        );

        if (defaultFolder.exists()) {
            fileChooser.setInitialDirectory(defaultFolder);
        }

        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null) {
            inputFileField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleOutputBrowse() {

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Save Location");

        File defaultFolder = new File(
                "/home/tdem/Documents/Mission/Monthly Miles/"
        );

        if (defaultFolder.exists()) {
            directoryChooser.setInitialDirectory(defaultFolder);
        }

        File folder = directoryChooser.showDialog(new Stage());

        if (folder != null) {
            outputFileField.setText(folder.getAbsolutePath());
        }
    }

}