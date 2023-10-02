package com.ullmann.timetrack.services;

import com.ullmann.timetrack.models.Anwesenheit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnwesenheitService {
    public List<Anwesenheit> getAllAnwesenheit(int mitarbeiterID) {
        List<Anwesenheit> anwesenheitList = new ArrayList<>();
        String sql = "SELECT * FROM Anwesenheit WHERE mitarbeiterID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)){

            preparedStatement.setInt(1, mitarbeiterID);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Anwesenheit record = new Anwesenheit(
                        resultSet.getInt("anwesenheitID"),
                        resultSet.getString("checkIn"),
                        resultSet.getString("checkOut"),
                        resultSet.getInt("mitarbeiterID")
                        );
                anwesenheitList.add(record);
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return anwesenheitList;
    }

    public void saveCheckIn(int mitarbeiterID, String checkInDateTime) throws SQLException {
        String sql = "INSERT INTO Anwesenheit (checkIn, mitarbeiterID) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, checkInDateTime);
            preparedStatement.setInt(2, mitarbeiterID);

            preparedStatement.executeUpdate();
        }
    }

    public void saveCheckOut(int mitarbeiterID, String lastCheckIn, String checkOutTime) throws SQLException {
        String sql = "UPDATE Anwesenheit SET checkOut = ? WHERE mitarbeiterID = ? AND checkIn = ? AND date(checkIn) = date(?) AND checkOut IS NULL";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, checkOutTime);
            preparedStatement.setInt(2, mitarbeiterID);
            preparedStatement.setString(3, lastCheckIn);
            preparedStatement.setString(4, lastCheckIn);

            int updatedRows = preparedStatement.executeUpdate();
            if (updatedRows == 0) {
                throw new SQLException("failed to update check out, no matching check in");
            }
        }
    }

    public String getLastCheckInWithoutCheckOut(int mitarbeiterID, String checkInDateTime) throws SQLException {
        String sql = "SELECT checkIn FROM Anwesenheit WHERE mitarbeiterID = ? AND date(checkIn) = date(?) AND checkOut IS NULL ORDER BY checkIn DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, mitarbeiterID);
            preparedStatement.setString(2, checkInDateTime);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String result = resultSet.getString("checkIn");
                return result;
            } else {
                return null;
            }
        }
    }

    public void addAnwesenheit(Anwesenheit anwesenheit) {
        String sql = "INSERT INTO anwesenheit (mitarbeiterID, checkIn, checkOut) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setInt(1, anwesenheit.getMitarbeiterID());
            preparedStatement.setString(2, anwesenheit.getCheckIn());
            preparedStatement.setString(3, anwesenheit.getCheckOut());

            int rowsToAdd = preparedStatement.executeUpdate();

            if (rowsToAdd == 0) {
                throw new SQLException("Creating anwesenheit failed, no rows.");
            }
        } catch (SQLException e) {
            System.err.println("Fehler bei anwesenheit database: " + e.getMessage());
        }
    }

    public void updateAnwesenheit(Anwesenheit anwesenheit) throws SQLException {
        String sql = "UPDATE Anwesenheit SET checkIn = ?, checkOut = ? WHERE anwesenheitID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, anwesenheit.getCheckIn());
            preparedStatement.setString(2, anwesenheit.getCheckOut());
            preparedStatement.setInt(3, anwesenheit.getAnwesenheitID());

            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println(rowsAffected + " rows updated.");

            preparedStatement.executeUpdate();
        }
    }

    public void deleteAnwesenheit(int anwesenheitID) throws SQLException {
        String sql = "DELETE FROM Anwesenheit WHERE anwesenheitID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, anwesenheitID);
            preparedStatement.executeUpdate();
        }
    }
}
