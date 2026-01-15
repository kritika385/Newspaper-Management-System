package com.example._jan.hawker;

import java.io.File;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import com.example._jan.db_connection.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

public class HawkerConsole {

    @FXML private ResourceBundle resources;
    @FXML private URL location;

    @FXML private ComboBox<String> comboAreas;
    @FXML private ComboBox<String> comboId;
    @FXML private DatePicker dateJoining;
    @FXML private ImageView imgUser;
    @FXML private ListView<String> listSelectedAreas;
    @FXML private TextField txtAddress, txtAdhaar, txtContact, txtName;

    private String selectedImagePath = ""; // Stores path for DB but not shown on UI

    @FXML
    void initialize() {
        // 1. Load All Areas from DB to ComboBox
        fillAreas();

        // 2. Real-time ID Generation Logic
        txtName.textProperty().addListener((obs, oldVal, newVal) -> generateHawkerId());
        txtContact.textProperty().addListener((obs, oldVal, newVal) -> generateHawkerId());

        // 3. Load Existing Hawker IDs for Fetching
        fillHawkerIds();

        // 4. FIX: Selection logic for Editable ComboBox
        comboId.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                comboId.getEditor().setText(newVal); // Dropdown se select karne par editor mein text set karega
            }
        });
    }

    // --- ID GENERATION: 5 of Name + 5 of Phone ---
    private void generateHawkerId() {
        String name = txtName.getText().trim().replace(" ", "");
        String phone = txtContact.getText().trim();
        String part1 = name.length() >= 5 ? name.substring(0, 5) : name;
        String part2 = phone.length() >= 5 ? phone.substring(phone.length() - 5) : phone;

        if (!part1.isEmpty() || !part2.isEmpty()) {
            comboId.getEditor().setText((part1 + part2).toUpperCase());
        }
    }

    @FXML
    void doBrowse(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedImagePath = file.getAbsolutePath();
            imgUser.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    void doAddArea(ActionEvent event) {
        String area = comboAreas.getSelectionModel().getSelectedItem();
        if (area != null && !listSelectedAreas.getItems().contains(area)) {
            listSelectedAreas.getItems().add(area);
        }
    }

    @FXML
    void doRecruit(ActionEvent event) {
        String hId = comboId.getEditor().getText();
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            // Save Basic Details
            PreparedStatement pst = con.prepareStatement("INSERT INTO hawkers VALUES(?,?,?,?,?,?,?)");
            pst.setString(1, hId);
            pst.setString(2, txtName.getText());
            pst.setString(3, txtContact.getText());
            pst.setString(4, txtAddress.getText());
            pst.setString(5, txtAdhaar.getText());
            pst.setDate(6, Date.valueOf(dateJoining.getValue()));
            pst.setString(7, selectedImagePath);
            pst.executeUpdate();

            // Save Areas
            PreparedStatement pstArea = con.prepareStatement("INSERT INTO hawker_areas VALUES(?,?)");
            for (String area : listSelectedAreas.getItems()) {
                pstArea.setString(1, hId);
                pstArea.setString(2, area);
                pstArea.executeUpdate();
            }

            con.commit();
            showAlert("Success", "Hawker Recruited Successfully!", Alert.AlertType.INFORMATION);
            fillHawkerIds();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Registration failed. ID might already exist.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    void doFetch(ActionEvent event) {
        String hId = comboId.getEditor().getText();
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM hawkers WHERE hawker_id=?");
            pst.setString(1, hId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                txtName.setText(rs.getString("name"));
                txtContact.setText(rs.getString("contact"));
                txtAddress.setText(rs.getString("address"));
                txtAdhaar.setText(rs.getString("adhar_no"));
                dateJoining.setValue(rs.getDate("joining_date").toLocalDate());
                selectedImagePath = rs.getString("pic_path");
                if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                    imgUser.setImage(new Image(new File(selectedImagePath).toURI().toString()));
                }

                // Fetch Areas
                listSelectedAreas.getItems().clear();
                PreparedStatement pstA = con.prepareStatement("SELECT area_name FROM hawker_areas WHERE hawker_id=?");
                pstA.setString(1, hId);
                ResultSet rsA = pstA.executeQuery();
                while (rsA.next()) listSelectedAreas.getItems().add(rsA.getString("area_name"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    void doUpdate(ActionEvent event) {
        String hId = comboId.getEditor().getText();
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            PreparedStatement pst = con.prepareStatement("UPDATE hawkers SET name=?, contact=?, address=?, adhar_no=?, joining_date=?, pic_path=? WHERE hawker_id=?");
            pst.setString(1, txtName.getText());
            pst.setString(2, txtContact.getText());
            pst.setString(3, txtAddress.getText());
            pst.setString(4, txtAdhaar.getText());
            pst.setDate(5, Date.valueOf(dateJoining.getValue()));
            pst.setString(6, selectedImagePath);
            pst.setString(7, hId);
            pst.executeUpdate();

            // Refresh Areas (Delete old and Insert current list)
            PreparedStatement delArea = con.prepareStatement("DELETE FROM hawker_areas WHERE hawker_id=?");
            delArea.setString(1, hId);
            delArea.executeUpdate();

            PreparedStatement insArea = con.prepareStatement("INSERT INTO hawker_areas VALUES(?,?)");
            for (String area : listSelectedAreas.getItems()) {
                insArea.setString(1, hId);
                insArea.setString(2, area);
                insArea.executeUpdate();
            }

            con.commit();
            showAlert("Success", "Hawker Data Updated!", Alert.AlertType.INFORMATION);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    void doDelete(ActionEvent event) {
        String hId = comboId.getEditor().getText();
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement("DELETE FROM hawkers WHERE hawker_id=?");
            pst.setString(1, hId);
            pst.executeUpdate();
            showAlert("Success", "Record Deleted.", Alert.AlertType.INFORMATION);
            doNew(null);
            fillHawkerIds();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    void doNew(ActionEvent event) {
        // 1. Pehle listeners ko bypass karne ke liye fields clear karo
        txtName.clear(); //
        txtContact.clear(); //

        // 2. Ab ComboBox ka Editor aur Selection dono uda do
        comboId.getSelectionModel().clearSelection();
        comboId.setValue(null);
        comboId.getEditor().clear(); //

        // 3. Baki saari fields reset karo
        txtAddress.clear(); //
        txtAdhaar.clear(); //
        dateJoining.setValue(null); //
        listSelectedAreas.getItems().clear(); //

        // 4. Image aur Path reset karo
        imgUser.setImage(null); //
        selectedImagePath = "";
    }
    private void fillAreas() {
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.prepareStatement("SELECT area_name FROM areas").executeQuery();
            while (rs.next()) comboAreas.getItems().add(rs.getString("area_name"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void fillHawkerIds() {
        comboId.getItems().clear();
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.prepareStatement("SELECT hawker_id FROM hawkers").executeQuery();
            while (rs.next()) comboId.getItems().add(rs.getString("hawker_id"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}