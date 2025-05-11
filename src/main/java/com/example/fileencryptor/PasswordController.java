package com.example.fileencryptor;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

public class PasswordController {
    @FXML
    private PasswordField passwordArea;

    @FXML
    private PasswordField confirmPasswordArea;

    @FXML
    private Label passwordStrengthLabel;

    @FXML
    private ProgressBar passwordStrengthBar;

    @FXML
    private Label passwordMismatchLabel;

    public void initialize() {
        passwordArea.textProperty().addListener((observable, oldValue, newValue) -> {
            updatePasswordStrength(newValue);
            checkPasswordsMatch();
        });

        if (confirmPasswordArea != null) {
            confirmPasswordArea.textProperty().addListener((observable, oldValue, newValue) -> {
                checkPasswordsMatch();
            });
        }
    }

    public String getPassword() {
        return passwordArea.getText();
    }

    private void updatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            passwordStrengthBar.setProgress(0);
            passwordStrengthLabel.setText("Password strength: None");
            passwordStrengthBar.setStyle("-fx-accent: #95a5a6;"); // Gray
            return;
        }

        double strength = calculatePasswordStrength(password);
        passwordStrengthBar.setProgress(strength);

        if (strength < 0.3) {
            passwordStrengthLabel.setText("Password strength: Weak");
            passwordStrengthBar.setStyle("-fx-accent: #e74c3c;"); // Red
        } else if (strength < 0.6) {
            passwordStrengthLabel.setText("Password strength: Medium");
            passwordStrengthBar.setStyle("-fx-accent: #f39c12;"); // Orange
        } else if (strength < 0.8) {
            passwordStrengthLabel.setText("Password strength: Strong");
            passwordStrengthBar.setStyle("-fx-accent: #2ecc71;"); // Green
        } else {
            passwordStrengthLabel.setText("Password strength: Very Strong");
            passwordStrengthBar.setStyle("-fx-accent: #27ae60;"); // Dark Green
        }
    }

    private double calculatePasswordStrength(String password) {
        int length = password.length();
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[^a-zA-Z0-9].*");

        double score = 0;

        score += Math.min(0.5, length * 0.05);

        // Character variety contributes the other 0.5
        if (hasLower) score += 0.1;
        if (hasUpper) score += 0.1;
        if (hasDigit) score += 0.15;
        if (hasSpecial) score += 0.15;

        return Math.min(1.0, score);
    }

    private void checkPasswordsMatch() {
        if (confirmPasswordArea != null) {
            String password = passwordArea.getText();
            String confirm = confirmPasswordArea.getText();

            if (confirm.isEmpty()) {
                passwordMismatchLabel.setVisible(false);
            } else if (!password.equals(confirm)) {
                passwordMismatchLabel.setVisible(true);
                passwordMismatchLabel.setText("Passwords don't match!");
            } else {
                passwordMismatchLabel.setVisible(true);
                passwordMismatchLabel.setText("Passwords match!");
                passwordMismatchLabel.setTextFill(Color.valueOf("#2ecc71")); // Green
            }
        }
    }

    public String handlePassword() {
        return getPassword();
    }

    public void bindOkButtonState(Button okButton) {
        if (confirmPasswordArea != null) {
            BooleanBinding passwordsMatchBinding = Bindings.createBooleanBinding(
                    () -> {
                        String password = passwordArea.getText();
                        String confirm = confirmPasswordArea.getText();
                        // Enable OK button only if passwords match and are not empty
                        return !password.isEmpty() && password.equals(confirm);
                    },
                    passwordArea.textProperty(),
                    confirmPasswordArea.textProperty()
            );

            okButton.disableProperty().bind(passwordsMatchBinding.not());
        }
    }
}