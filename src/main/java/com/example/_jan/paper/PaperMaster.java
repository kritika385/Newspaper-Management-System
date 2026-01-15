package com.example._jan.paper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.example._jan.db_connection.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class PaperMaster {

    @FXML
    private ComboBox<String> comboPaper;

    @FXML
    private TextField txtLanguage;

    @FXML
    private TextField txtPrice;

    @FXML
    void initialize() {
        // Populates the ComboBox with existing data from the DB on startup
        fetchPaperNames();
    }

    private void fetchPaperNames() {
        comboPaper.getItems().clear();
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement("SELECT paper_name FROM papers");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                comboPaper.getItems().add(rs.getString("paper_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void doFind(ActionEvent event) {
        String paperName = comboPaper.getEditor().getText();
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM papers WHERE paper_name=?");
            pst.setString(1, paperName);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                txtLanguage.setText(rs.getString("language"));
                txtPrice.setText(String.valueOf(rs.getFloat("price")));
            } else {
                showAlert("Search Information", "No record found for: " + paperName, Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void doSave(ActionEvent event) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement("INSERT INTO papers (paper_name, language, price) VALUES (?, ?, ?)");
            pst.setString(1, comboPaper.getEditor().getText());
            pst.setString(2, txtLanguage.getText());
            pst.setFloat(3, Float.parseFloat(txtPrice.getText()));
            pst.executeUpdate();

            showAlert("Success", "Record saved successfully.", Alert.AlertType.INFORMATION);
            fetchPaperNames(); // Update list after saving
        } catch (Exception e) {
            showAlert("Error", "Could not save. Check if the paper name is unique.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    void doUpdate(ActionEvent event) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement("UPDATE papers SET language=?, price=? WHERE paper_name=?");
            pst.setString(1, txtLanguage.getText());
            pst.setFloat(2, Float.parseFloat(txtPrice.getText()));
            pst.setString(3, comboPaper.getEditor().getText());

            int count = pst.executeUpdate();
            if (count > 0) showAlert("Success", "Record updated successfully.", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void doDelete(ActionEvent event) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement("DELETE FROM papers WHERE paper_name=?");
            pst.setString(1, comboPaper.getEditor().getText());
            pst.executeUpdate();

            showAlert("Success", "Record deleted successfully.", Alert.AlertType.INFORMATION);
            doNew(null);
            fetchPaperNames();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void doNew(ActionEvent event) {
        comboPaper.getEditor().clear();
        comboPaper.setValue(null);
        txtLanguage.clear();
        txtPrice.clear();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}