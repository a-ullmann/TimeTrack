package com.ullmann.timetrack.controllers;

import com.ullmann.timetrack.models.Anwesenheit;
import com.ullmann.timetrack.models.Mitarbeiter;
import com.ullmann.timetrack.models.UserSession;
import com.ullmann.timetrack.services.AnwesenheitService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.time.Duration;

import java.io.IOException;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HauptmenuController {
    @FXML
    private Button urlaubRequestsButton;
    @FXML
    private Button mitarbeiterListeButton;
    @FXML
    private Button krankenstandButton;
    private int mitarbeiterID;
    private Timeline workingTimeTimeline;
    private static String staticCheckInDateTimeString;
    private String checkInDateTimeString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    private String checkOutDateTimeString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    @FXML
    private Label currentWorkingTimeLabel;
    @FXML
    private Label errorLabel;
    private Mitarbeiter loggedInUser;
    AnwesenheitService anwesenheitService = new AnwesenheitService();

    public void initialize() {
        mitarbeiterID = UserSession.getInstance().getLoggedInUser().getMitarbeiterID();

        try {
            String lastCheckIn = anwesenheitService.getLastCheckInWithoutCheckOut(mitarbeiterID, LocalDate.now().toString());
                    if (lastCheckIn != null) {
                        staticCheckInDateTimeString = lastCheckIn;
                        startCurrentWorkingTime(staticCheckInDateTimeString);
                    } else {
                        currentWorkingTimeLabel.setText("Bitte einchecken!");
                    }
        } catch (SQLException e) {
            System.out.println("Error getting check in status: " + e.getMessage());
        }

        loggedInUser = UserSession.getInstance().getLoggedInUser();

        if (loggedInUser != null && "Manager".equals(loggedInUser.getPosition())) {
            mitarbeiterListeButton.setVisible(true);
            urlaubRequestsButton.setVisible(true);
            krankenstandButton.setVisible(true);
        } else {
            mitarbeiterListeButton.setVisible(false);
            urlaubRequestsButton.setVisible(false);
            krankenstandButton.setVisible(false);
        }
    }

    @FXML
    public void handleCheckIn(ActionEvent event) {
        checkInDateTimeString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        try {
            String lastCheckIn = anwesenheitService.getLastCheckInWithoutCheckOut(mitarbeiterID, checkInDateTimeString);
            if (lastCheckIn == null) {
                System.out.println(checkInDateTimeString);
                anwesenheitService.saveCheckIn(mitarbeiterID, checkInDateTimeString);
                staticCheckInDateTimeString = checkInDateTimeString;
                startCurrentWorkingTime(checkInDateTimeString);
            } else {
                errorLabel.setText("Sie haben bereits um " + lastCheckIn + " eingecheck. Bitte zuerst auschecken!");
            }
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Check In Error");
            alert.setHeaderText(null);
            alert.setContentText("Es gab ein Problem bei der Datenbank. Bitte erneut versuchen. Error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void handleCheckOut(ActionEvent event) {
        try {
            String currentDate = LocalDate.now().toString();
            String lastCheckIn = anwesenheitService.getLastCheckInWithoutCheckOut(mitarbeiterID, currentDate);
            System.out.println(checkOutDateTimeString);
            if (lastCheckIn != null) {
                anwesenheitService.saveCheckOut(mitarbeiterID, lastCheckIn, checkOutDateTimeString);
                stopAndResetWorkingTime();
                errorLabel.setText("");
            } else {
                errorLabel.setText("Es wurde kein Check In gefunden. Bitte zuerst einchecken!");
                System.out.println("cannot check out, no check in found.");
            }
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Check Out Error");
            alert.setHeaderText(null);
            alert.setContentText("Es gab ein Problem bei der Datenbank. Bitte erneut versuchen. Error: " + e.getMessage());
            alert.showAndWait();
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
            stage.setTitle("Ihr Profil");
            stage.show();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            System.out.println("Cannot go back");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUrlaub(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ullmann/timetrack/UrlaubView.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Urlaub beantragen");
            stage.show();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            System.out.println("Cannot go back");
            e.printStackTrace();
        }
    }

    private void startCurrentWorkingTime(String checkInDateTimeString) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime checkInDateTime = LocalDateTime.parse(checkInDateTimeString, formatter);

        workingTimeTimeline = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), event -> {
            Duration duration = Duration.between(checkInDateTime, LocalDateTime.now());
            long hours = duration.toHours();
            long minutes = duration.toMinutesPart();
            long seconds = duration.toSecondsPart();

            String formattedDuration = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            currentWorkingTimeLabel.setText(formattedDuration);
        }));

        workingTimeTimeline.setCycleCount(Timeline.INDEFINITE);
        workingTimeTimeline.play();
    }

    private void stopAndResetWorkingTime() {
        if (workingTimeTimeline != null) {
            workingTimeTimeline.stop();
            workingTimeTimeline = null;
        }
        staticCheckInDateTimeString = null;
        currentWorkingTimeLabel.setText("Bitte einchecken!");
    }

    @FXML
    private void handleDownloadReport() {
        String desktopPath = System.getProperty("user.home") + "/Desktop/";
        String formattedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy_HH.mm"));
        String fileName = "Meine_Zeit_Report_" + formattedDate + ".csv";
        String fullPath = desktopPath + fileName;

        try (FileWriter fileWriter = new FileWriter(fullPath)) {
            fileWriter.append("Anwesenheit ID, Check In, Check Out\n");

            List<Anwesenheit> anwesenheitList = anwesenheitService.getAllAnwesenheit(mitarbeiterID);
            for (Anwesenheit anwesenheit : anwesenheitList) {
                fileWriter.append(String.valueOf(anwesenheit.getAnwesenheitID()));
                fileWriter.append(",");
                fileWriter.append(anwesenheit.getCheckIn());
                fileWriter.append(",");
                fileWriter.append(anwesenheit.getCheckOut());

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

    @FXML
    private void goToUrlaubRequests(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ullmann/timetrack/UrlaubRequestsView.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Urlaub Requests");
            stage.show();
        } catch (IOException e) {
            System.out.println("Cannot go back");
            e.printStackTrace();
        }
    }

    @FXML
    private void goToKrankenstand(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ullmann/timetrack/KrankenstandListeView.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Krankenstand Liste");
            stage.show();

        } catch (IOException e) {
            System.out.println("Cannot go back");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleKrankenstand(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ullmann/timetrack/KrankenstandView.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Krankenstand beantragen");
            stage.show();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            System.out.println("Cannot go back");
            e.printStackTrace();
        }
    }
}
