package com.ullmann.timetrack.controllers;

import com.ullmann.timetrack.services.MitarbeiterService;
import com.ullmann.timetrack.models.Mitarbeiter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class MitarbeiterListeController {
    @FXML
    private TableView<Mitarbeiter> mitarbeiterTable;
    @FXML
    private TableColumn<Mitarbeiter, Integer> mitarbeiterIDColumn;
    @FXML
    private TableColumn<Mitarbeiter, String> vornameColumn;
    @FXML
    private TableColumn<Mitarbeiter, String> nachnameColumn;
    @FXML
    private TableColumn<Mitarbeiter, String> positionColumn;
    @FXML
    private TableColumn<Mitarbeiter, String> abteilungColumn;
    @FXML
    private TableColumn<Mitarbeiter, String> usernameColumn;
    @FXML
    private Button editButton;
    private boolean isEditing = false;
    private MitarbeiterService mitarbeiterService;

    public MitarbeiterListeController() {
        mitarbeiterService = new MitarbeiterService();
    }

    @FXML
    public void initialize() {
        loadMitarbeiterData();
        mitarbeiterIDColumn.setCellValueFactory(new PropertyValueFactory<>("mitarbeiterID"));
        vornameColumn.setCellValueFactory(new PropertyValueFactory<>("vorname"));
        nachnameColumn.setCellValueFactory(new PropertyValueFactory<>("nachname"));
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("position"));
        abteilungColumn.setCellValueFactory(new PropertyValueFactory<>("abteilung"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        mitarbeiterTable.setRowFactory(tv -> {
            TableRow<Mitarbeiter> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Mitarbeiter rowData = row.getItem();
                    openAnwesenheitView(rowData);
                }
            });
            return row;
        });

        vornameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nachnameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        positionColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        abteilungColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        vornameColumn.setOnEditCommit(
                event -> event.getRowValue().setVorname(event.getNewValue())
        );
        nachnameColumn.setOnEditCommit(
                event -> event.getRowValue().setNachname(event.getNewValue())
        );
        positionColumn.setOnEditCommit(
                event -> event.getRowValue().setPosition(event.getNewValue())
        );
        abteilungColumn.setOnEditCommit(
                event -> event.getRowValue().setAbteilung(event.getNewValue())
        );
    }

    private void loadMitarbeiterData() {
        List<Mitarbeiter> mitarbeiters = mitarbeiterService.getAllMitarbeiter();
        mitarbeiterTable.getItems().setAll(mitarbeiters);
    }

    @FXML
    private void handleAdd(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ullmann/timetrack/RegisterView.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            RegisterController controller = fxmlLoader.getController();
            controller.setManagerAddingEmployee(true);
            controller.viewEinstellung();

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Mitarbeiter Hinzufügen");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEdit(ActionEvent e) {
        if (isEditing) {
            saveChanges();
            editButton.setText("Bearbeiten");
            mitarbeiterTable.setEditable(false);
        } else {
            editButton.setText("Speichern");
            mitarbeiterTable.setEditable(true);
        }
        isEditing = !isEditing;
    }

    private void saveChanges() {
        Mitarbeiter updatedMitarbeiter = mitarbeiterTable.getSelectionModel().getSelectedItem();
        System.out.println("saving changes for: " + updatedMitarbeiter.getVorname());
        try {
            if (updatedMitarbeiter != null) {
                mitarbeiterService.updateMitarbeiter(updatedMitarbeiter);
                loadMitarbeiterData();
            } else {
                System.out.println("did not select user to update");
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

    private void openAnwesenheitView(Mitarbeiter mitarbeiter) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ullmann/timetrack/AnwesenheitView.fxml"));
            Parent root = fxmlLoader.load();
            AnwesenheitController controller = fxmlLoader.getController();
            controller.setMitarbeiterData(mitarbeiter);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Anwesenheitsdaten für " + mitarbeiter.getVorname() + " " + mitarbeiter.getNachname());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDownloadReport() {
        String desktopPath = System.getProperty("user.home") + "/Desktop/";
        String formattedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy_HH.mm"));
        String fileName = "Mitarbeiter_Report_" + formattedDate + ".csv";
        String fullPath = desktopPath + fileName;

        try (FileWriter fileWriter = new FileWriter(fullPath)) {
            fileWriter.append("Mitarbeiter ID, Vorname, Nachname, Position, Abteilung, Username\n");

            List<Mitarbeiter> mitarbeiterList = mitarbeiterService.getAllMitarbeiter();
            for (Mitarbeiter mitarbeiter : mitarbeiterList) {
                fileWriter.append(String.valueOf(mitarbeiter.getMitarbeiterID()));
                fileWriter.append(",");
                fileWriter.append(mitarbeiter.getVorname());
                fileWriter.append(",");
                fileWriter.append(mitarbeiter.getNachname());
                fileWriter.append(",");
                fileWriter.append(mitarbeiter.getPosition());
                fileWriter.append(",");
                fileWriter.append(mitarbeiter.getAbteilung());
                fileWriter.append(",");
                fileWriter.append(mitarbeiter.getUsername());
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
    private void handleDeleteButton() {
        Mitarbeiter selectedMitarbeiter = mitarbeiterTable.getSelectionModel().getSelectedItem();

        if (selectedMitarbeiter == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Keine Zeile ausgewält");
            alert.setHeaderText(null);
            alert.setContentText("Bitte wählen Sie einen Mitarbeiter aus.");
            alert.showAndWait();
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Entfernung Bestätigen");
        confirmAlert.setHeaderText("Mitarbeiter Entfernen");
        confirmAlert.setContentText("Sind Sie sicher, dass Sie den ausgewählten Mitarbeiter und alle bezogenen Daten entfernen möchten?");
        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                mitarbeiterService.deleteMitarbeiter(selectedMitarbeiter.getMitarbeiterID(), selectedMitarbeiter.getUsername());
                mitarbeiterTable.getItems().remove(selectedMitarbeiter);
            } catch (SQLException e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Database Error");
                errorAlert.setContentText("Fehler beim entfernen des Mitarbeiters. Bitte erneut versuchen.");
                errorAlert.showAndWait();
                e.printStackTrace();
            }
        }
    }
}
