package com.ullmann.timetrack.services;

import com.ullmann.timetrack.models.Krankenstand;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class KrankenstandService {

    public void addKrankenstand(int mitarbeiterID, LocalDate anfangsDatum, LocalDate endeDatum, String grund) throws SQLException {
        String sql = "INSERT INTO Krankenstand (mitarbeiterID, anfang, ende, grund) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, mitarbeiterID);
            preparedStatement.setString(2, anfangsDatum.toString());
            preparedStatement.setString(3, endeDatum.toString());
            preparedStatement.setString(4, grund.toString());

            preparedStatement.executeUpdate();
        }
    }

    public List<Krankenstand> getAllKrankenstand() {
        List<Krankenstand> krankenstandList = new ArrayList<>();
        String sql = "SELECT * FROM Krankenstand";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)){

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int krankenstandID = resultSet.getInt("krankenstandID");
                int mitarbeiterID = resultSet.getInt("mitarbeiterID");
                String anfangsDatum = resultSet.getString("anfang");
                String endeDatum = resultSet.getString("ende");
                String grund = resultSet.getString("grund");

                krankenstandList.add(new Krankenstand(krankenstandID, mitarbeiterID, anfangsDatum, endeDatum, grund));
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return krankenstandList;
    }
}
