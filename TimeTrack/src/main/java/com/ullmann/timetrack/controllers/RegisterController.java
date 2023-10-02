package com.ullmann.timetrack.controllers;

import org.mindrot.jbcrypt.BCrypt;

import com.ullmann.timetrack.models.Mitarbeiter;
import com.ullmann.timetrack.models.User;
import com.ullmann.timetrack.services.MitarbeiterService;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class RegisterController {
    private int mitarbeiterID;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private TextField vornameField;
    @FXML
    private TextField nachnameField;
    @FXML
    private ComboBox<String> positionComboBox;
    @FXML
    private ComboBox<String> abteilungComboBox;
    @FXML
    Button backButton;
    @FXML
    Label registerHeader;
    private boolean isManagerAddingEmployee = false;
    private static final String defaultPassword = "hallo";

    @FXML
    private void initialize() {
        positionComboBox.getItems().addAll("Manager", "Mitarbeiter");
        abteilungComboBox.getItems().addAll("Marketing", "IT", "Legal");
    }

    public void setManagerAddingEmployee(boolean value) {
        this.isManagerAddingEmployee = value;
    }

    @FXML
    private void closeManagerAddingEmployee(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.close();
    }

    public void viewEinstellung() {
        if(isManagerAddingEmployee) {
            registerHeader.setText("Mitarbeiter Hinzufügen");
            backButton.setText("Schließen");
            passwordField.setVisible(false);
            confirmPasswordField.setVisible(false);
            backButton.setOnAction(this::closeManagerAddingEmployee);
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        UserInput userInput = getUserInput();

        if (!isValidInput(userInput)) {
            return;
        }
        if (!saveToDatabase(userInput)) {
            return;
        }
        showSuccessAlert(userInput.username);

        if (isManagerAddingEmployee) {
            closeCurrentStage(event);
        } else {
            goToLogin(event);
        }
    }

    private UserInput getUserInput() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmpassword = confirmPasswordField.getText();
        String vorname = vornameField.getText();
        String nachname = nachnameField.getText();
        String position = positionComboBox.getValue();
        String abteilung = abteilungComboBox.getValue();

        if (isManagerAddingEmployee) {
            password = defaultPassword;
            confirmpassword = defaultPassword;
        }
        return new UserInput(username, password, confirmpassword, vorname, nachname, position, abteilung);
    }

    private boolean isValidInput(UserInput userInput){
        if (isEmptyField(userInput)) {
            showErrorAlert("Fehler", "Benutzername, Password, Vorname und Nachname dürfen nicht leer sein.");
            return false;
        }
        if (!userInput.password.equals(userInput.confirmpassword)) {
            showErrorAlert("Fehler", "Passwörter stimmen nicht überein.");
            return false;
        }
        MitarbeiterService mitarbeiterService = new MitarbeiterService();
        try {
            if (mitarbeiterService.isUsernameTaken(userInput.username)) {
                showErrorAlert("Fehler", "Benutzername bereits vergeben. Bitte wählen Sie einen anderen.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean isEmptyField(UserInput userInput) {
        return userInput.username.trim().isEmpty() || userInput.password.trim().isEmpty() ||
                userInput.vorname.trim().isEmpty() || userInput.nachname.trim().isEmpty();
    }

    private boolean saveToDatabase(UserInput userInput) {
        try {
            Mitarbeiter mitarbeiter = new Mitarbeiter(mitarbeiterID, userInput.vorname, userInput.nachname, userInput.position, userInput.abteilung, userInput.username);
            String hashedPassword = BCrypt.hashpw(userInput.password, BCrypt.gensalt());
            User user = new User(userInput.username, hashedPassword);

            MitarbeiterService mitarbeiterService = new MitarbeiterService();
            mitarbeiterService.saveMitarbeiter(mitarbeiter);
            mitarbeiterService.saveUser(user);

            return true;
        } catch (SQLException e) {
            System.err.println("Fehler beim Speichern in Datenbank " + e.getMessage());
            return false;
        }
    }

    private void showSuccessAlert(String username) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Willkommen " + username);
        alert.setHeaderText(null);
        alert.setContentText("Konto erfolgreich erstellt");
        alert.showAndWait();
    }

    private void closeCurrentStage(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.close();
    }

    private static class UserInput {
        String username, password, confirmpassword, vorname, nachname, position, abteilung;

        UserInput(String username, String password, String confirmpassword, String vorname, String nachname, String position, String abteilung) {
            this.username = username;
            this.password = password;
            this.confirmpassword = confirmpassword;
            this.vorname = vorname;
            this.nachname = nachname;
            this.position = position;
            this.abteilung = abteilung;
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ullmann/timetrack/LoginView.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            System.out.println("View konnte nicht geladen werden");
            e.printStackTrace();
        }
    }

    private void showErrorAlert(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }

    private void goToLogin(Event event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ullmann/timetrack/LoginView.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            System.out.println("View konnte nicht geladen werden");
            e.printStackTrace();
        }
    }
}
