package com.ullmann.timetrack.controllers;

import com.ullmann.timetrack.models.Mitarbeiter;
import com.ullmann.timetrack.models.Urlaub;
import com.ullmann.timetrack.services.MitarbeiterService;
import com.ullmann.timetrack.services.UrlaubService;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UrlaubRequestsController {
    @FXML
    private TableView<Urlaub> urlaubRequestTable;
    @FXML
    private TableColumn<Urlaub, Integer> mitarbeiterIDColumn;
    @FXML
    private TableColumn<Urlaub, String> vornameColumn;
    @FXML
    private TableColumn<Urlaub, String> nachnameColumn;
    @FXML
    private TableColumn<Urlaub, String> startDateColumn;
    @FXML
    private TableColumn<Urlaub, String> endDateColumn;
    @FXML
    private TableColumn<Urlaub, String> statusColumn;
    @FXML
    private TableColumn<Urlaub, String> requestDateColumn;
    private UrlaubService urlaubService = new UrlaubService();
    private MitarbeiterService mitarbeiterService = new MitarbeiterService();

    @FXML
    public void initialize() {
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        requestDateColumn.setCellValueFactory(new PropertyValueFactory<>("requestDate"));
        mitarbeiterIDColumn.setCellValueFactory(cellData -> {
            Urlaub urlaub = cellData.getValue();
            Mitarbeiter mitarbeiter = mitarbeiterService.getMitarbeiterById(urlaub.getMitarbeiterID());
            return new ReadOnlyIntegerWrapper(mitarbeiter.getMitarbeiterID()).asObject();
        });
        vornameColumn.setCellValueFactory(cellData -> {
            Urlaub urlaub = cellData.getValue();
            Mitarbeiter mitarbeiter = mitarbeiterService.getMitarbeiterById(urlaub.getMitarbeiterID());
            return new SimpleStringProperty(mitarbeiter.getVorname());
        });
        nachnameColumn.setCellValueFactory(cellData -> {
            Urlaub urlaub = cellData.getValue();
            Mitarbeiter mitarbeiter = mitarbeiterService.getMitarbeiterById(urlaub.getMitarbeiterID());
            return new SimpleStringProperty(mitarbeiter.getNachname());
        });

        statusColumn.setCellFactory(tc -> {
            TableCell<Urlaub, String> cell = new TableCell<>() {
                private ComboBox<String> comboBox;
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        if (comboBox == null) {
                            comboBox = new ComboBox<>();
                            comboBox.getItems().addAll("Pending", "Confirmed", "Declined");
                            comboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
                                TableRow<Urlaub> row = getTableRow();
                                if (row != null) {
                                    Urlaub urlaub = row.getItem();
                                    if (urlaub != null) {
                                        urlaub.setStatus(newValue);
                                        urlaub.setStatusChanged(true);
                                        try {
                                            urlaubService.updateUrlaubStatus(urlaub.getRequestID(), newValue);
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                        }
                        comboBox.setValue(item);
                        setGraphic(comboBox);
                    }
                }
            };
            return cell;
        });
        loadData();
    }

    private void loadData() {
        List<Urlaub> urlaubList = urlaubService.getAllUrlaubRequests();
        urlaubRequestTable.setItems(FXCollections.observableArrayList(urlaubList));
    }

    @FXML
    private void handleDownloadReport() {
        String desktopPath = System.getProperty("user.home") + "/Desktop/";
        String formattedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy_HH.mm"));
        String fileName = "Urlaub_Report_" + formattedDate + ".csv";
        String fullPath = desktopPath + fileName;

        try (FileWriter fileWriter = new FileWriter(fullPath)) {
            fileWriter.append("Mitarbeiter ID, Vorname, Nachname, Beginn, Ende, Status, Urlaub beantragt\n");

            List<Urlaub> urlaubList = urlaubService.getAllUrlaubRequests();
            for (Urlaub urlaub : urlaubList) {
                Mitarbeiter mitarbeiter = mitarbeiterService.getMitarbeiterById(urlaub.getMitarbeiterID());
                fileWriter.append(String.valueOf(urlaub.getMitarbeiterID()));
                fileWriter.append(",");
                fileWriter.append(mitarbeiter.getVorname());
                fileWriter.append(",");
                fileWriter.append(mitarbeiter.getNachname());
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

    @FXML
    private void handleCloseButton() {
        Stage stage = (Stage) urlaubRequestTable.getScene().getWindow();
        stage.close();
    }
}
