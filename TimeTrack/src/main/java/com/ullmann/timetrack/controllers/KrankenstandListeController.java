package com.ullmann.timetrack.controllers;

import com.ullmann.timetrack.models.Krankenstand;
import com.ullmann.timetrack.models.Mitarbeiter;
import com.ullmann.timetrack.services.KrankenstandService;
import com.ullmann.timetrack.services.MitarbeiterService;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class KrankenstandListeController {
    @FXML
    private TableView<Krankenstand> krankenstandTable;
    @FXML
    private TableColumn<Krankenstand, Integer> mitarbeiterIDColumn;
    @FXML
    private TableColumn<Krankenstand, String> vornameColumn;
    @FXML
    private TableColumn<Krankenstand, String> nachnameColumn;
    @FXML
    private TableColumn<Krankenstand, String> anfangColumn;
    @FXML
    private TableColumn<Krankenstand, String> endeColumn;
    @FXML
    private TableColumn<Krankenstand, String> grundColumn;
    @FXML
    private Button downloadButton;
    @FXML
    private Button closeButton;

    private KrankenstandService krankenstandService = new KrankenstandService();
    private MitarbeiterService mitarbeiterService = new MitarbeiterService();

    public void initialize() {
        bindDataToTableColumns();
        loadDataToTable();
    }

    private void bindDataToTableColumns() {
        mitarbeiterIDColumn.setCellValueFactory(new PropertyValueFactory<>("mitarbeiterID"));
        anfangColumn.setCellValueFactory(new PropertyValueFactory<>("anfangsDatum"));
        endeColumn.setCellValueFactory(new PropertyValueFactory<>("endeDatum"));
        grundColumn.setCellValueFactory(new PropertyValueFactory<>("grund"));

        vornameColumn.setCellValueFactory(cellData -> {
            Krankenstand krankenstand = cellData.getValue();
            Mitarbeiter mitarbeiter = mitarbeiterService.getMitarbeiterById(krankenstand.getMitarbeiterID());
            return new SimpleStringProperty(mitarbeiter.getVorname());
        });
        nachnameColumn.setCellValueFactory(cellData -> {
            Krankenstand krankenstand = cellData.getValue();
            Mitarbeiter mitarbeiter = mitarbeiterService.getMitarbeiterById(krankenstand.getMitarbeiterID());
            return new SimpleStringProperty(mitarbeiter.getNachname());
        });
    }

    private void loadDataToTable() {
        try {
            List<Krankenstand> allKrankenstand = krankenstandService.getAllKrankenstand();
            krankenstandTable.getItems().setAll(allKrankenstand);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCloseButton() {
        Stage stage = (Stage) krankenstandTable.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleDownloadReport() {
        String desktopPath = System.getProperty("user.home") + "/Desktop/";
        String formattedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy_HH.mm"));
        String fileName = "Krankenstand_Report_" + formattedDate + ".csv";
        String fullPath = desktopPath + fileName;

        try (FileWriter fileWriter = new FileWriter(fullPath)) {
            fileWriter.append("Mitarbeiter ID, Vorname, Nachname, Beginn, Ende, Grund\n");

            List<Krankenstand> krankenstandList = krankenstandService.getAllKrankenstand();
            for (Krankenstand krankenstand : krankenstandList) {
                Mitarbeiter mitarbeiter = mitarbeiterService.getMitarbeiterById(krankenstand.getMitarbeiterID());
                fileWriter.append(String.valueOf(krankenstand.getMitarbeiterID()));
                fileWriter.append(",");
                fileWriter.append(mitarbeiter.getVorname());
                fileWriter.append(",");
                fileWriter.append(mitarbeiter.getNachname());
                fileWriter.append(",");
                fileWriter.append(krankenstand.getAnfangsDatum());
                fileWriter.append(",");
                fileWriter.append(krankenstand.getEndeDatum());
                fileWriter.append(",");
                fileWriter.append(krankenstand.getGrund());
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
