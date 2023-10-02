package com.ullmann.timetrack.controllers;

import com.ullmann.timetrack.models.Mitarbeiter;
import com.ullmann.timetrack.models.UserSession;
import com.ullmann.timetrack.services.KrankenstandService;
import com.ullmann.timetrack.services.UrlaubService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class KrankenstandController {
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private TextArea grundTextArea;

    Mitarbeiter loggedInUser = UserSession.getInstance().getLoggedInUser();
    KrankenstandService krankenstandService = new KrankenstandService();

    @FXML
    public void initialize() {
    }

    @FXML
    private void handleSubmit(ActionEvent event) {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String grund = grundTextArea.getText();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String requestDateString = LocalDateTime.now().format(formatter);
        LocalDateTime requestDate = LocalDateTime.parse(requestDateString, formatter);

        try {
            krankenstandService.addKrankenstand(loggedInUser.getMitarbeiterID(), startDate, endDate, grund);
            if (startDate != null && endDate != null && !startDate.isAfter(endDate)) {
                UrlaubService urlaubService = new UrlaubService();
                urlaubService.addUrlaub(loggedInUser.getMitarbeiterID(), startDate, endDate, requestDate);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Krankenstand Beantragt");
                alert.setHeaderText(null);
                alert.setContentText("Ihr Krankenstand vom " + startDate + " bis " + endDate + " wurde erfolgreich beantragt. Vielen Dank.");

                Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
                okButton.addEventFilter(ActionEvent.ACTION, e -> {
                    startDatePicker.setValue(null);
                    endDatePicker.setValue(null);
                    grundTextArea.clear();
                });

                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Krankenstand konnte nicht beantragt werden");
                alert.setHeaderText(null);
                alert.setContentText("Bitte geben Sie ein gültiges Datum ein");
                alert.showAndWait();
            }
        } catch (SQLException e) {
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
}
