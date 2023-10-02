package com.ullmann.timetrack.controllers;

import com.ullmann.timetrack.models.Mitarbeiter;
import com.ullmann.timetrack.models.UserSession;
import com.ullmann.timetrack.services.MitarbeiterService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    MitarbeiterService mitarbeiterService = new MitarbeiterService();

    @FXML
    private void handleLogin(ActionEvent event) throws SQLException {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (validateCredentials(username, password)) {
            Mitarbeiter loggedInUser = null;
            try {
                loggedInUser = mitarbeiterService.getMitarbeiterByUsername(username);
            } catch (Exception e) {
                System.out.println("Failed to get user data: " + e.getMessage());
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Fehler");
                errorAlert.setHeaderText("Datenbank Fehler");
                errorAlert.setContentText("Mitarbeiter Daten konnten nicht geladen werden. Bitte erneut probieren.");
                errorAlert.showAndWait();
                e.printStackTrace();
            }

            if (loggedInUser == null) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Fehler");
                errorAlert.setHeaderText("Login Fehler");
                errorAlert.setContentText("Mitarbeiter nicht gefunden. Bitte einen anderen Username benützen");
                errorAlert.showAndWait();
                return;
            }

            UserSession.getInstance().setLoggedInUser(loggedInUser);

            if ("Manager".equals(loggedInUser.getPosition())) {
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ullmann/timetrack/HauptmenuView.fxml"));
                    Parent root = fxmlLoader.load();
                    Scene scene = new Scene(root);
                    scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.setTitle("Hauptmenü");
                    stage.show();

                    Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    currentStage.close();
                } catch (IOException e) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Fehler");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Bitte erneut versuchen.");
                    errorAlert.showAndWait();
                    e.printStackTrace();
                }
            } else {
                System.out.println(mitarbeiterService.getMitarbeiterByUsername(username).getPosition());
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ullmann/timetrack/HauptmenuView.fxml"));
                    Parent root = fxmlLoader.load();
                    Scene scene = new Scene(root);
                    scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.setTitle("Hauptmenü");
                    stage.show();

                    Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    currentStage.close();
                } catch (IOException e) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Fehler");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Bitte erneut versuchen.");
                    errorAlert.showAndWait();
                    e.printStackTrace();
                }
            }

        } else {
            System.out.println("ungültige Anmeldedaten");
            }
        }

    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ullmann/timetrack/RegisterView.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Neues Profil erstellen");
            stage.show();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Fehler");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Bitte erneut versuchen.");
            errorAlert.showAndWait();
            e.printStackTrace();
        }
    }

    public boolean validateCredentials(String enteredUsername, String enteredPassword) {
        try {
            String storedPasswordHash = mitarbeiterService.getPasswordForUsername(enteredUsername);
            if (storedPasswordHash == null) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Fehler");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("Username nicht gefunden! Bitte erneut versuchen.");
                errorAlert.showAndWait();
                return false;
            }

            if (BCrypt.checkpw(enteredPassword, storedPasswordHash)) {
                return true;
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Fehler");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("Password falsch eingegeben! Bitte erneut versuchen.");
                errorAlert.showAndWait();
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Fehler");
            errorAlert.setHeaderText("Datenbank Fehler");
            errorAlert.setContentText("Bitte erneut versuchen.");
            errorAlert.showAndWait();
            return false;
        }
    }
}