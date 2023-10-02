package com.ullmann.timetrack.services;

import com.ullmann.timetrack.models.Urlaub;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UrlaubService {
    public void addUrlaub(int mitarbeiterID, LocalDate startDate, LocalDate endDate, LocalDateTime requestDate) throws SQLException {
        String sql = "INSERT INTO Urlaub (mitarbeiterID, startDate, endDate, requestDate) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, mitarbeiterID);
            preparedStatement.setString(2, startDate.toString());
            preparedStatement.setString(3, endDate.toString());
            preparedStatement.setString(4, requestDate.toString());

            preparedStatement.executeUpdate();
        }
    }

    public void updateUrlaubStatus(int requestID, String status) throws SQLException {
        String sql = "UPDATE Urlaub SET status = ? WHERE requestID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, status);
            preparedStatement.setInt(2, requestID);

            preparedStatement.executeUpdate();
        }
    }

    public List<Urlaub> getAllUrlaubRequests() {
        List<Urlaub> urlaubsList = new ArrayList<>();
        String sql = "SELECT * FROM Urlaub";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int requestID = resultSet.getInt("requestID");
                int mitarbeiterID = resultSet.getInt("mitarbeiterID");
                String startDate = resultSet.getString("startDate");
                String endDate = resultSet.getString("endDate");
                String status = resultSet.getString("status");
                String requestDate = resultSet.getString("requestDate");

                Urlaub urlaub = new Urlaub(requestID, mitarbeiterID, startDate, endDate, status, requestDate);
                urlaubsList.add(urlaub);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return urlaubsList;
    }

    public Urlaub getLatestUrlaubForUser(int mitarbeiterID) throws SQLException {
        String sql = "SELECT * FROM Urlaub WHERE mitarbeiterID = ? ORDER BY requestDate DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setInt(1, mitarbeiterID);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Urlaub urlaub = new Urlaub(
                            resultSet.getInt("requestID"),
                            resultSet.getInt("mitarbeiterID"),
                            resultSet.getString("startDate"),
                            resultSet.getString("endDate"),
                            resultSet.getString("status"),
                            resultSet.getString("requestDate")
                    );
                    return urlaub;
                }
            }
        }
        return null;
    }

    public List<Urlaub> getAllUrlaubForUser(int mitarbeiterID) throws SQLException {
        List<Urlaub> urlaubList = new ArrayList<>();
        String sql = "SELECT * FROM Urlaub WHERE mitarbeiterID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setInt(1, mitarbeiterID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Urlaub urlaub = new Urlaub(
                            resultSet.getInt("requestID"),
                            resultSet.getInt("mitarbeiterID"),
                            resultSet.getString("startDate"),
                            resultSet.getString("endDate"),
                            resultSet.getString("status"),
                            resultSet.getString("requestDate")
                    );
                    urlaubList.add(urlaub);
                }
            }
        }
        return urlaubList;
    }

    public boolean hasPendingUrlaub(int mitarbeiterID) throws SQLException {
        String sql = "SELECT COUNT(*) AS pending_count FROM Urlaub WHERE mitarbeiterID = ? AND status = 'Pending'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, mitarbeiterID);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt("pending_count");
                return count > 0;
            } else {
                return false;
            }
        }
    }
}
