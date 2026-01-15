package com.example._jan.area;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.example._jan.db_connection.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class AreaMaster {

    @FXML
    private TextField txtArea;

    /**
     * Event handler to save a new area name into the database.
     * Performs basic validation before attempting a database transaction.
     */
    @FXML
    void doSave(ActionEvent event) {
        String areaName = txtArea.getText().trim();

        // Data validation to ensure the input field is not empty
        if (areaName.isEmpty()) {
            showMsg("Validation Error: Please enter an Area Name.", Alert.AlertType.WARNING);
            return;
        }

        // Establishing database connection and executing insert statement
        String query = "INSERT INTO areas (area_name) VALUES (?)";
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pstmt = con.prepareStatement(query);
            pstmt.setString(1, areaName);
            pstmt.executeUpdate();

            // Notify user of successful operation and reset input field
            showMsg("Success: Area '" + areaName + "' has been registered.", Alert.AlertType.INFORMATION);
            txtArea.clear();

        } catch (SQLException e) {
            // Handle database constraints or connection issues
            e.printStackTrace();
            showMsg("Database Error: Area might already exist or connection failed.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method to display localized alerts and user notifications.
     * @param message The content to be displayed in the alert.
     * @param type The AlertType (e.g., INFORMATION, ERROR, WARNING).
     */
    private void showMsg(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Management System: Area Master");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}