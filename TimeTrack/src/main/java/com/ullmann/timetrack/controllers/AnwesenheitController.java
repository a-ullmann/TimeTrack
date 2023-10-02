package com.ullmann.timetrack.controllers;

import com.ullmann.timetrack.models.Anwesenheit;
import com.ullmann.timetrack.models.Mitarbeiter;
import com.ullmann.timetrack.services.AnwesenheitService;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class AnwesenheitController {
    @FXML
    private TableColumn<Anwesenheit, String> mitarbeiterIDColumn;
    @FXML
    private TableColumn<Anwesenheit, String> checkInColumn;
    @FXML
    private TableColumn<Anwesenheit, String> checkOutColumn;
    @FXML
    private Button editButton;
    @FXML
    private TableColumn<Anwesenheit, String> workedHoursColumn;
    @FXML
    private Label mitarbeiterNameLabel;
    @FXML
    private TableView<Anwesenheit> anwesenheitTable;
    private Mitarbeiter mitarbeiter;
    private boolean isEditing = false;
    private Set<Anwesenheit> editedAnwesenheit = new HashSet<>();
    AnwesenheitService anwesenheitService = new AnwesenheitService();

    public void initialize() {
        mitarbeiterIDColumn.setCellValueFactory(new PropertyValueFactory<>("mitarbeiterID"));
        checkInColumn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        checkOutColumn.setCellValueFactory(new PropertyValueFactory<>("checkOut"));

        calculateWorkedHours();
        setupEditableColumns();
    }

    public void setMitarbeiterData(Mitarbeiter mitarbeiter) {
        this.mitarbeiter = mitarbeiter;
        mitarbeiterNameLabel.setText(mitarbeiter.getVorname() + " " + mitarbeiter.getNachname());
        loadAnwesenheitData();
    }

    private void loadAnwesenheitData() {
        List<Anwesenheit> anwesenheitList = anwesenheitService.getAllAnwesenheit(mitarbeiter.getMitarbeiterID());
        anwesenheitTable.getItems().setAll(anwesenheitList);
    }

    private void calculateWorkedHours() {
        workedHoursColumn.setCellValueFactory(cellData -> {
            Anwesenheit record = cellData.getValue();

            String checkInString = record.getCheckIn();
            String checkOutString = record.getCheckOut();

            LocalDateTime checkIn = LocalDateTime.parse(checkInString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime checkOut;

            if (checkOutString == null || checkOutString.isEmpty()) {
                checkOut = LocalDateTime.now();
            } else {
                checkOut = LocalDateTime.parse(checkOutString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }

            Duration duration = Duration.between(checkIn, checkOut);

            long hours = duration.toHours();
            long totalMinutes = duration.toMinutes();
            long minutes = totalMinutes % 60;
            long totalSeconds = duration.toSeconds();
            long seconds = totalSeconds % 60;

            return new SimpleStringProperty(hours + "h " + minutes + "m" + seconds + "s");
        });
    }

    @FXML
    private void handleDownloadReport() {
        String desktopPath = System.getProperty("user.home") + "/Desktop/";
        String fileName = "Anwesenheits_Report_" + mitarbeiter.getVorname() + "_" + mitarbeiter.getNachname();
        String fullPath = desktopPath + fileName;

        try (FileWriter fileWriter = new FileWriter(fullPath)) {
            fileWriter.append("Mitarbeiter ID, Check In, Check Out\n");

            List<Anwesenheit> anwesenheitList = anwesenheitService.getAllAnwesenheit(mitarbeiter.getMitarbeiterID());
            for (Anwesenheit anwesenheit : anwesenheitList) {
                fileWriter.append(String.valueOf(anwesenheit.getMitarbeiterID()));
                fileWriter.append(",");
                fileWriter.append(anwesenheit.getCheckIn().toString());
                fileWriter.append(",");
                if (anwesenheit.getCheckOut() != null) {
                    fileWriter.append(anwesenheit.getCheckOut().toString());
                } else {
                    fileWriter.append("N/A");
                }
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
    private void handleAdd() {
        String format = "yyyy-MM-dd HH:mm:ss";

        TextInputDialog checkInDialog = new TextInputDialog();
        checkInDialog.setTitle("Check In Hinzufügen");
        checkInDialog.setHeaderText("Geben Sie den CHECK-IN ein im Format " + format);
        Optional<String> checkInResult = checkInDialog.showAndWait();

        if (checkInResult.isPresent() && !isValidDateTimeFormat(checkInResult.get(), format)) {
            showErrorAlert("Fehler", "Ungültiges Datumsformat für CHECK-IN. Bitte geben Sie das Datum im Format " + format + " ein.");
            return;
        }

        TextInputDialog checkOutDialog = new TextInputDialog();
        checkOutDialog.setTitle("Check Out Hinzufügen");
        checkOutDialog.setHeaderText("Geben Sie den CHECK-OUT ein im Format yyyy-MM-dd HH:mm:ss");
        Optional<String> checkOutResult = checkOutDialog.showAndWait();

        if (checkOutResult.isPresent() && !isValidDateTimeFormat(checkOutResult.get(), format)) {
            showErrorAlert("Fehler", "Ungültiges Datumsformat für CHECK-OUT. Bitte geben Sie das Datum im Format " + format + " ein.");
            return;
        }

        if (checkInResult.isPresent() && checkOutResult.isPresent()) {
            try {
                int anwesenheitID = -1;
                String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String checkIn = currentDateTime;
                String checkOut = null;
                int currentMitarbeiterID = mitarbeiter.getMitarbeiterID();

                Anwesenheit newAnwesenheit = new Anwesenheit(anwesenheitID, checkIn, checkOut, currentMitarbeiterID);
                newAnwesenheit.setMitarbeiterID(mitarbeiter.getMitarbeiterID());
                newAnwesenheit.setCheckIn(checkInResult.get());
                newAnwesenheit.setCheckOut((checkOutResult.get()));

                anwesenheitService.addAnwesenheit(newAnwesenheit);

                loadAnwesenheitData();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Erfolgreich");
                alert.setHeaderText(null);
                alert.setContentText("Neuer Eintrag wurde erfolgreich gespeichert");
                alert.showAndWait();
            } catch (DateTimeParseException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Fehler");
                alert.setHeaderText(null);
                alert.setContentText("Ungültiges Datumsformat. Bitte geben Sie das Datum im Format yyyy-MM-dd HH:mm:ss ein.");
                alert.showAndWait();
            }
        }
    }

    private boolean isValidDateTimeFormat(String dateTime, String format) {
        try {
            LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(format));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    @FXML
    private void handleEdit(ActionEvent e) {
        if (isEditing) {
            saveChanges();
            editButton.setText("Bearbeiten");
            anwesenheitTable.setEditable(false);
        } else {
            editButton.setText("Speichern");
            anwesenheitTable.setEditable(true);
        }
        isEditing = !isEditing;
    }

    private void saveChanges() {
        for (Anwesenheit anwesenheit : editedAnwesenheit) {
            try {
                anwesenheitService.updateAnwesenheit(anwesenheit);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        loadAnwesenheitData();
        editedAnwesenheit.clear();
    }

    private void setupEditableColumns() {
        checkInColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        checkOutColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        checkInColumn.setOnEditCommit(event -> {
            int row = event.getTablePosition().getRow();
            Anwesenheit currentAnwesenheit = event.getTableView().getItems().get(row);
            currentAnwesenheit.setCheckIn(event.getNewValue());
            editedAnwesenheit.add(currentAnwesenheit);
        });

        checkOutColumn.setOnEditCommit(event -> {
            int row = event.getTablePosition().getRow();
            Anwesenheit currentAnwesenheit = event.getTableView().getItems().get(row);
            currentAnwesenheit.setCheckOut(event.getNewValue());
            editedAnwesenheit.add(currentAnwesenheit);
        });
    }

    @FXML
    private void handleDelete() {
        Anwesenheit selectedAnwesenheit = anwesenheitTable.getSelectionModel().getSelectedItem();

        if (selectedAnwesenheit == null) {
            showErrorAlert("Fehler", "Bitte wählen Sie einen Eintrag zum Löschen aus.");
            return;
        }
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Löschen bestätigen");
        confirmationAlert.setHeaderText("Eintrag löschen");
        confirmationAlert.setContentText("Sind Sie sicher, dass Sie diesen Eintrag löschen möchten?");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                anwesenheitService.deleteAnwesenheit(selectedAnwesenheit.getAnwesenheitID());
                anwesenheitTable.getItems().remove(selectedAnwesenheit);
            } catch (SQLException e) {
                showErrorAlert("Datenbankfehler", "Fehler beim Löschen des Eintrags. " + e.getMessage());
            }
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleCloseButton() {
        Stage stage = (Stage) anwesenheitTable.getScene().getWindow();
        stage.close();
    }
}
