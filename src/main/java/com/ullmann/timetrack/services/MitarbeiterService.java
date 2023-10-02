package com.ullmann.timetrack.services;

import com.ullmann.timetrack.models.Mitarbeiter;
import com.ullmann.timetrack.models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MitarbeiterService {

    public Mitarbeiter getMitarbeiterByUsername(String username) {
        String sql = "SELECT * FROM Mitarbeiter WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)){
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return new Mitarbeiter(
                        resultSet.getInt("mitarbeiterID"),
                        resultSet.getString("vorname"),
                        resultSet.getString("nachname"),
                        resultSet.getString("position"),
                        resultSet.getString("abteilung"),
                        resultSet.getString("username")
                );
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return null;
    }

    public Mitarbeiter getMitarbeiterById(int mitarbeiterID) {
        String sql = "SELECT * FROM Mitarbeiter WHERE mitarbeiterID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)){
            preparedStatement.setInt(1, mitarbeiterID);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return new Mitarbeiter(
                        resultSet.getInt("mitarbeiterID"),
                        resultSet.getString("vorname"),
                        resultSet.getString("nachname"),
                        resultSet.getString("position"),
                        resultSet.getString("abteilung"),
                        resultSet.getString("username")
                );
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return null;
    }

    public List<Mitarbeiter> getAllMitarbeiter() {
        List<Mitarbeiter> mitarbeiters = new ArrayList<>();
        String sql = "SELECT * FROM Mitarbeiter";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)){
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Mitarbeiter mitarbeiter = new Mitarbeiter(
                        resultSet.getInt("mitarbeiterID"),
                        resultSet.getString("vorname"),
                        resultSet.getString("nachname"),
                        resultSet.getString("position"),
                        resultSet.getString("abteilung"),
                        resultSet.getString("username")
                        );
                mitarbeiters.add(mitarbeiter);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return mitarbeiters;
    }

    public void saveMitarbeiter(Mitarbeiter mitarbeiter) throws SQLException {
        String sql = "INSERT INTO Mitarbeiter (vorname, nachname, position, abteilung, username) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, mitarbeiter.getVorname());
            preparedStatement.setString(2, mitarbeiter.getNachname());
            preparedStatement.setString(3, mitarbeiter.getPosition());
            preparedStatement.setString(4, mitarbeiter.getAbteilung());
            preparedStatement.setString(5, mitarbeiter.getUsername());

            preparedStatement.executeUpdate();
        }
    }

    public void updateMitarbeiter(Mitarbeiter mitarbeiter) throws SQLException {
        String sql = "UPDATE Mitarbeiter SET vorname = ?, nachname = ?, position = ?, abteilung = ?, username = ? WHERE mitarbeiterID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, mitarbeiter.getVorname());
            preparedStatement.setString(2, mitarbeiter.getNachname());
            preparedStatement.setString(3, mitarbeiter.getPosition());
            preparedStatement.setString(4, mitarbeiter.getAbteilung());
            preparedStatement.setString(5, mitarbeiter.getUsername());
            preparedStatement.setInt(6, mitarbeiter.getMitarbeiterID());

            preparedStatement.executeUpdate();
        }
    }

    public void saveUser(User user) throws SQLException {
        String sql = "INSERT INTO Users (username, password) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPassword());

            preparedStatement.executeUpdate();
        }
    }

    public String getPasswordForUsername(String username) throws SQLException {
        String sql = "SELECT password FROM Users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("password");
            }
        }
        return null;
    }

    public User getUserByUsername(String username) throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        User user = null;

        try {
            connection = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM Users WHERE username = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, username);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String hashedPassword = resultSet.getString("password");
                user = new User(username, hashedPassword);
            }
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
            if (connection != null) connection.close();
        }
        return user;
    }

    public boolean isUsernameTaken(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }
        }
        return false;
    }

    public void deleteMitarbeiter(int mitarbeiterID, String username) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {

            String sqlAnwesenheit = "DELETE FROM Anwesenheit WHERE mitarbeiterID = ?";
            try (PreparedStatement preparedStatement = conn.prepareStatement(sqlAnwesenheit)) {
                preparedStatement.setInt(1, mitarbeiterID);
                preparedStatement.executeUpdate();
            }

            String sqlUrlaub = "DELETE FROM Urlaub WHERE mitarbeiterID = ?";
            try (PreparedStatement preparedStatement = conn.prepareStatement(sqlUrlaub)) {
                preparedStatement.setInt(1, mitarbeiterID);
                preparedStatement.executeUpdate();
            }

            String sqlUsers = "DELETE FROM Users WHERE username = ?";
            try (PreparedStatement preparedStatement = conn.prepareStatement(sqlUsers)) {
                preparedStatement.setString(1, username);
                preparedStatement.executeUpdate();
            }

            String sqlMitarbeiter = "DELETE FROM Mitarbeiter WHERE mitarbeiterID = ?";
            try (PreparedStatement preparedStatement = conn.prepareStatement(sqlMitarbeiter)) {
                preparedStatement.setInt(1, mitarbeiterID);
                preparedStatement.executeUpdate();
            }
        }
    }
}
