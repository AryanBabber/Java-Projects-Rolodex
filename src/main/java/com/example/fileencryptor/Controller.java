package com.example.fileencryptor;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Optional;

public class Controller {
    @FXML
    private Button encryptBtn;

    @FXML
    private Button decryptBtn;

    @FXML
    private BorderPane mainWindow;

    @FXML
    private Label statusLabel;

    @FXML
    private Label fileInfoLabel;

    @FXML
    private ProgressBar progressBar;

    public void initialize() {
        // Initialize UI elements if needed
        statusLabel.setText("Ready to encrypt or decrypt files");
    }

    @FXML
    public void handleEncryption() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select a file to encrypt");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = chooser.showOpenDialog(mainWindow.getScene().getWindow());

        if (file != null) {
            // Update UI with file info
            fileInfoLabel.setText("Selected: " + file.getName());

            String password = getPassword();

            if (password != null && !password.isEmpty()) {
                Path inPath = Path.of(file.getPath());
                Path outPath = Path.of(file.getParent(), file.getName().split("\\.")[0] + ".enc");

                // Start encryption process
                showProgressUI("Encrypting file...");

                Task<Void> encryptTask = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        Encryptor encryptor = new Encryptor();
                        try {
                            encryptor.encrypt(inPath, outPath, password.toCharArray());
                            return null;
                        } catch (GeneralSecurityException | IOException e) {
                            throw e;
                        }
                    }
                };

                encryptTask.setOnSucceeded(e -> {
                    hideProgressUI();
                    showSuccessMessage("Encryption successful",
                            "File encrypted successfully and saved as:\n" + outPath.getFileName());
                });

                encryptTask.setOnFailed(e -> {
                    hideProgressUI();
                    showErrorMessage("Encryption failed",
                            "Could not encrypt the file. Please check the file and try again.");
                    e.getSource().getException().printStackTrace();
                });

                // Run the task
                Thread thread = new Thread(encryptTask);
                thread.setDaemon(true);
                thread.start();
            }
        }
    }

    @FXML
    public void handleDecryption() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select a file to decrypt");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Encrypted Files", "*.enc"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = chooser.showOpenDialog(mainWindow.getScene().getWindow());

        if (file != null) {
            // Update UI with file info
            fileInfoLabel.setText("Selected: " + file.getName());

            String password = getPassword();

            if (password != null && !password.isEmpty()) {
                Path inPath = Path.of(file.getPath());
                Path outPath = Path.of(file.getParent(), file.getName().split("\\.")[0] + ".txt");

                // Start decryption process
                showProgressUI("Decrypting file...");

                Task<Void> decryptTask = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        Decryptor decryptor = new Decryptor();
                        try {
                            decryptor.decrypt(inPath, outPath, password.toCharArray());
                            return null;
                        } catch (GeneralSecurityException | IOException e) {
                            throw e;
                        }
                    }
                };

                decryptTask.setOnSucceeded(e -> {
                    hideProgressUI();
                    showSuccessMessage("Decryption successful",
                            "File decrypted successfully and saved as:\n" + outPath.getFileName());
                });

                decryptTask.setOnFailed(e -> {
                    hideProgressUI();
                    showErrorMessage("Decryption failed",
                            "Could not decrypt the file. Please check the password and try again.");
                    e.getSource().getException().printStackTrace();
                });

                // Run the task
                Thread thread = new Thread(decryptTask);
                thread.setDaemon(true);
                thread.start();
            }
        }
    }

    private String getPassword() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainWindow.getScene().getWindow());
        dialog.setTitle("Password Required");
        dialog.setHeaderText(null);

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("password.fxml"));

        try {
            dialog.getDialogPane().setContent(loader.load());
        } catch (IOException e) {
            System.out.println("Couldn't load the password dialog");
            e.printStackTrace();
            showErrorMessage("Error", "Failed to load password dialog.");
            return null;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        // Disable OK button initially if we're using the password confirmation feature
        PasswordController controller = loader.getController();
        if (controller != null) {
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            controller.bindOkButtonState(okButton);
        }

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            return controller.getPassword();
        }

        return null;
    }

    private void showProgressUI(String message) {
        statusLabel.setText(message);
        progressBar.setVisible(true);
        encryptBtn.setDisable(true);
        decryptBtn.setDisable(true);
    }

    private void hideProgressUI() {
        Platform.runLater(() -> {
            progressBar.setVisible(false);
            statusLabel.setText("Ready to encrypt or decrypt files");
            encryptBtn.setDisable(false);
            decryptBtn.setDisable(false);
        });
    }

    private void showSuccessMessage(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.initOwner(mainWindow.getScene().getWindow());

            // Apply fade-in animation
            alert.getDialogPane().setOpacity(0);
            alert.show();

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), alert.getDialogPane());
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            // Update status with pulse animation
            statusLabel.setText("Operation completed successfully!");
            FadeTransition pulse = new FadeTransition(Duration.millis(700), statusLabel);
            pulse.setFromValue(0.7);
            pulse.setToValue(1.0);
            pulse.setCycleCount(2);
            pulse.setAutoReverse(true);
            pulse.play();
        });
    }

    private void showErrorMessage(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.initOwner(mainWindow.getScene().getWindow());

            // Apply shake animation by changing stylesheet
            alert.getDialogPane().getStyleClass().add("error-dialog");
            alert.show();

            // Update status
            statusLabel.setText("Operation failed!");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");

            // Reset status after a delay
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(e -> {
                statusLabel.setText("Ready to encrypt or decrypt files");
                statusLabel.setStyle("-fx-text-fill: #bdc3c7;");
            });
            pause.play();
        });
    }
}