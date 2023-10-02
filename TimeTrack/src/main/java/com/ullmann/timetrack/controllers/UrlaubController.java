package com.ullmann.timetrack.controllers;

import com.ullmann.timetrack.models.Mitarbeiter;
import com.ullmann.timetrack.models.Urlaub;
import com.ullmann.timetrack.models.UserSession;
import com.ullmann.timetrack.services.UrlaubService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.prefs.Preferences;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

public class UrlaubController {
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Button notificationButton;
    private Preferences preferences = Preferences.userNodeForPackage(UrlaubController.class);
    private static final String STATUS_PREF_KEY = "LAST_KNOWN_STATUS";
    Mitarbeiter loggedInUser = UserSession.getInstance().getLoggedInUser();
    UrlaubService urlaubService = new UrlaubService();

    @FXML
    public void initialize() {
        checkUrlaubStatusChange();
    }

    @FXML
    private void handleSubmit(ActionEvent event) {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String requestDateString = LocalDateTime.now().format(formatter);
        LocalDateTime requestDate = LocalDateTime.parse(requestDateString, formatter);

        try {
            if (urlaubService.hasPendingUrlaub(loggedInUser.getMitarbeiterID())) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Urlaub Anfrage");
                alert.setHeaderText(null);
                alert.setContentText("Sie haben bereits einen ausstehenden Urlaub. Bitte warten Sie, bis dieser bearbeitet wurde.");
                alert.showAndWait();
                return;
            }

            if (startDate != null && endDate != null && !startDate.isAfter(endDate)) {
                UrlaubService urlaubService = new UrlaubService();
                urlaubService.addUrlaub(loggedInUser.getMitarbeiterID(), startDate, endDate, requestDate);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Urlaub Beantragt");
                alert.setHeaderText(null);
                alert.setContentText("Ihr Urlaub vom " + startDate + " bis " + endDate + " wurde erfolgreich beantragt. Bitte warten Sie auf die Zusage.");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Urlaub konnte nicht beantragt werden");
                alert.setHeaderText(null);
                alert.setContentText("Bitte geben Sie ein gültiges Datum ein");
                alert.showAndWait();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProfile(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ullmann/timetrack/ProfilView.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Mein Profil");
            stage.show();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            System.out.println("Cannot go back");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleNotificationButton() {
        try {
            Urlaub latestUrlaub = urlaubService.getLatestUrlaubForUser(loggedInUser.getMitarbeiterID());
            if (latestUrlaub != null) {
                preferences.put(STATUS_PREF_KEY + "_" + loggedInUser.getMitarbeiterID(), latestUrlaub.getStatus());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        notificationButton.setVisible(false);
    }

    private void checkUrlaubStatusChange() {
        try {
            Urlaub latestUrlaub = urlaubService.getLatestUrlaubForUser(loggedInUser.getMitarbeiterID());

            if (latestUrlaub == null || !statusHasChanged(latestUrlaub) ) {
                notificationButton.setVisible(false);
            } else {
                notificationButton.setVisible(true);
                notificationButton.setText("Der Status Ihres Urlaubantrags vom " + latestUrlaub.getRequestDate() + " wurde geändert auf: " + latestUrlaub.getStatus());

                notificationButton.getStyleClass().removeAll("button-declined", "button-confirmed", "button-pending");

                switch (latestUrlaub.getStatus()) {
                    case "Declined":
                        notificationButton.getStyleClass().add("button-declined");
                        break;
                    case "Confirmed":
                        notificationButton.getStyleClass().add("button-confirmed");
                        break;
                    case "Pending":
                        notificationButton.getStyleClass().add("button-pending");
                        break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean statusHasChanged(Urlaub urlaub) throws SQLException {
        String latestKnownStatus = preferences.get(STATUS_PREF_KEY + "_" + loggedInUser.getMitarbeiterID(), "");
        String currentStatus = urlaub.getStatus();

        preferences.put(STATUS_PREF_KEY + "_" + loggedInUser.getMitarbeiterID(), currentStatus);

        return !latestKnownStatus.equals(currentStatus);
    }

    @FXML
    private void handleDownloadReport() throws SQLException {
        String desktopPath = System.getProperty("user.home") + "/Desktop/";
        String formattedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy_HH.mm"));
        String fileName = "Urlaub_Report_" + formattedDate + ".csv";
        String fullPath = desktopPath + fileName;

        try (FileWriter fileWriter = new FileWriter(fullPath)) {
            fileWriter.append("Urlaub ID, Von, Bis, Status, Request Datum\n");

            List<Urlaub> urlaubList = urlaubService.getAllUrlaubForUser(loggedInUser.getMitarbeiterID());
            for (Urlaub urlaub : urlaubList) {
                fileWriter.append(String.valueOf(urlaub.getMitarbeiterID()));
                fileWriter.append(",");
                fileWriter.append(urlaub.getStartDate());
                fileWriter.append(",");
                fileWriter.append(urlaub.getEndDate());
                fileWriter.append(",");
                fileWriter.append(urlaub.getStatus());
                fileWriter.append(",");
                fileWriter.append(urlaub.getRequestDate());
                fileWriter.append("\n");
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Report Generiert");
            alert.setHeaderText(null);
            alert.setContentText("Ihr Report wurde auf Ihren Desktop als " + fileName + " gespeichert.");
            alert.showAndWait();

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Fehler beim generieren. Bitte erneut versuchen.");
            alert.showAndWait();
        }
    }
}
