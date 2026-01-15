package com.example._jan.analyser;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import com.example._jan.db_connection.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;

public class Analyser {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private ComboBox<String> comboArea;
    @FXML private PieChart pieChartAnalysis;

    /**
     * Standard JavaFX initialize method to set up UI components.
     */
    @FXML
    void initialize() {
        fillAreas();
        assert comboArea != null : "fx:id=\"comboArea\" was not injected: check your FXML file.";
        assert pieChartAnalysis != null : "fx:id=\"pieChartAnalysis\" was not injected: check your FXML file.";
    }

    /**
     * Fetches distinct area names from the database to populate the ComboBox.
     */
    void fillAreas() {
        String query = "SELECT DISTINCT area_name FROM areas";
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.prepareStatement(query).executeQuery();
            while (rs.next()) {
                comboArea.getItems().add(rs.getString("area_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates a PieChart analysis showing newspaper distribution per area.
     * Uses SQL aggregation to count subscribers for each newspaper brand.
     */
    @FXML
    void doGetStatistics(ActionEvent event) {
        ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList();
        String selectedArea = comboArea.getSelectionModel().getSelectedItem();

        if (selectedArea == null || selectedArea.isEmpty()) {
            return;
        }

        // SQL Query to fetch paper distribution for the selected area
        String query = "SELECT s.paper_name, COUNT(s.mobile) as total " +
                "FROM subscriptions s INNER JOIN customers c ON s.mobile = c.mobile " +
                "WHERE c.area = ? GROUP BY s.paper_name";

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, selectedArea);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                // Mapping database results to PieChart Data objects
                chartData.add(new PieChart.Data(rs.getString("paper_name"), rs.getInt("total")));
            }

            // Updating UI component with processed data
            pieChartAnalysis.setData(chartData);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}