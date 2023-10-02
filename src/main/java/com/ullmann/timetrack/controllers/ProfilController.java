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
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class ProfilController {
    @FXML
    private TextField mitarbeiterIDTextField;
    @FXML
    private TextField vornameTextField;
    @FXML
    private TextField nachnameTextField;
    @FXML
    private TextField positionTextField;
    @FXML
    private TextField abteilungTextField;
    @FXML
    private TextField usernameTextField;
    @FXML
    private Button mitarbeiterListeButton;
    @FXML
    private Button editButton;
    private boolean isEditing = false;
    private MitarbeiterService mitarbeiterService = new MitarbeiterService();
    private Mitarbeiter loggedInUser;

    public void initialize() {
        loggedInUser = UserSession.getInstance().getLoggedInUser();
        fillProfileData();

        if (loggedInUser != null && "Manager".equals(loggedInUser.getPosition())) {
            mitarbeiterListeButton.setVisible(true);
        } else {
            mitarbeiterListeButton.setVisible(false);
        }
    }

    private void fillProfileData() {
        if (loggedInUser == null) {
            System.out.println("Logged in user data is missing");
            return;
        }
            mitarbeiterIDTextField.setText(String.valueOf(loggedInUser.getMitarbeiterID()));
            vornameTextField.setText(loggedInUser.getVorname());
            nachnameTextField.setText(loggedInUser.getNachname());
            positionTextField.setText(loggedInUser.getPosition());
            abteilungTextField.setText(loggedInUser.getAbteilung());
            usernameTextField.setText(loggedInUser.getUsername());
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            UserSession.getInstance().logout();

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
            System.out.println("Cannot go back");
            e.printStackTrace();
        }
    }

    @FXML
    private void goToHauptmenu(ActionEvent event) {
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
            System.out.println("Cannot go back");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMitarbeiterListe(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ullmann/timetrack/MitarbeiterListeView.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Mitarbeiter Liste");
            stage.show();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            System.out.println("Cannot go back");
            e.printStackTrace();
        }
    }

    private void setFieldsEditable(boolean editable) {
        vornameTextField.setEditable(editable);
        nachnameTextField.setEditable(editable);
        usernameTextField.setEditable(editable);
    }

    private void saveChanges() {
        Mitarbeiter currentMitarbeiter = UserSession.getInstance().getLoggedInUser();

        if (currentMitarbeiter != null) {
            currentMitarbeiter.setVorname(vornameTextField.getText());
            currentMitarbeiter.setNachname(nachnameTextField.getText());
            currentMitarbeiter.setPosition(positionTextField.getText());
            currentMitarbeiter.setAbteilung(abteilungTextField.getText());
            currentMitarbeiter.setUsername(usernameTextField.getText());

            try {
                mitarbeiterService.updateMitarbeiter(currentMitarbeiter);
                fillProfileData();
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("failed to update mitarbeiter");
            }
        }
    }

    @FXML
    private void handleEdit(ActionEvent e) {
        if (isEditing) {
            saveChanges();
            editButton.setText("Bearbeiten");
            setFieldsEditable(false);
        } else {
            editButton.setText("Speichern");
            setFieldsEditable(true);
        }
        isEditing = !isEditing;
    }
}
