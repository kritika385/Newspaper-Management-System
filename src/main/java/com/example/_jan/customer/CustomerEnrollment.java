package com.example._jan.customer;

import java.sql.*;
import java.net.URL;
import java.time.LocalDate;
import com.example._jan.db_connection.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

public class CustomerEnrollment {

    @FXML private ComboBox<String> comboArea;
    @FXML private ComboBox<String> comboHawker;
    @FXML private DatePicker dateStart;
    @FXML private ListView<String> listPapers;
    @FXML private ListView<String> listPrices;
    @FXML private ListView<String> listSelPapers;
    @FXML private ListView<String> listSelPrices;
    @FXML private TextField txtAddress, txtEmail, txtMobile, txtName;

    @FXML
    void initialize() {
        fillAreas();
        fillPapers();
    }

    // 1. Fetch Areas
    private void fillAreas() {
        comboArea.getItems().clear();
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.prepareStatement("SELECT DISTINCT area_name FROM areas").executeQuery();
            while (rs.next()) {
                comboArea.getItems().add(rs.getString("area_name"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 2. Fetch Papers & Prices
    private void fillPapers() {
        listPapers.getItems().clear();
        listPrices.getItems().clear();
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.prepareStatement("SELECT paper_name, price FROM papers").executeQuery();
            while (rs.next()) {
                listPapers.getItems().add(rs.getString("paper_name"));
                listPrices.getItems().add(rs.getString("price"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 3. Fetch Hawkers based on Area
    @FXML
    void doFetchHawkers(ActionEvent event) {
        comboHawker.getItems().clear();
        String area = comboArea.getSelectionModel().getSelectedItem();
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement("SELECT hawker_id FROM hawker_areas WHERE area_name=?");
            pst.setString(1, area);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                comboHawker.getItems().add(rs.getString("hawker_id"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 4. Selection Logic (Double Click)
    @FXML
    void doSelectOne(MouseEvent event) {
        if (event.getClickCount() == 2) {
            int index = listPapers.getSelectionModel().getSelectedIndex();
            if (index >= 0) {
                String paper = listPapers.getItems().get(index);
                String price = listPrices.getItems().get(index);
                if (!listSelPapers.getItems().contains(paper)) {
                    listSelPapers.getItems().add(paper);
                    listSelPrices.getItems().add(price);
                }
            }
        }
    }

    @FXML
    void doDeSelectOne(MouseEvent event) {
        if (event.getClickCount() == 2) {
            int index = listSelPapers.getSelectionModel().getSelectedIndex();
            if (index >= 0) {
                listSelPapers.getItems().remove(index);
                listSelPrices.getItems().remove(index);
            }
        }
    }

    // 5. Enroll (Save)
    @FXML
    void doEnroll(ActionEvent event) {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            PreparedStatement pstCust = con.prepareStatement("INSERT INTO customers VALUES(?,?,?,?,?,?,?)");
            pstCust.setString(1, txtMobile.getText());
            pstCust.setString(2, txtName.getText());
            pstCust.setString(3, txtEmail.getText());
            pstCust.setString(4, txtAddress.getText());
            pstCust.setString(5, comboArea.getSelectionModel().getSelectedItem());
            pstCust.setString(6, comboHawker.getSelectionModel().getSelectedItem());
            pstCust.setDate(7, Date.valueOf(dateStart.getValue()));
            pstCust.executeUpdate();

            PreparedStatement pstSub = con.prepareStatement("INSERT INTO subscriptions VALUES(?,?,?)");
            for (int i = 0; i < listSelPapers.getItems().size(); i++) {
                pstSub.setString(1, txtMobile.getText());
                pstSub.setString(2, listSelPapers.getItems().get(i));
                pstSub.setString(3, listSelPrices.getItems().get(i));
                pstSub.executeUpdate();
            }
            con.commit();
            showAlert("Success", "Customer Enrolled!");
            doNew(null);
        } catch (Exception e) { showAlert("Error", e.getMessage()); }
    }

    // 6. Find Customer
    @FXML
    void doFind(ActionEvent event) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM customers WHERE mobile=?");
            pst.setString(1, txtMobile.getText());
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                txtName.setText(rs.getString("name"));
                txtEmail.setText(rs.getString("email"));
                txtAddress.setText(rs.getString("address"));
                comboArea.setValue(rs.getString("area"));
                comboHawker.setValue(rs.getString("hawker_id"));
                dateStart.setValue(rs.getDate("start_date").toLocalDate());

                listSelPapers.getItems().clear();
                listSelPrices.getItems().clear();
                PreparedStatement pstSub = con.prepareStatement("SELECT * FROM subscriptions WHERE mobile=?");
                pstSub.setString(1, txtMobile.getText());
                ResultSet rsSub = pstSub.executeQuery();
                while (rsSub.next()) {
                    listSelPapers.getItems().add(rsSub.getString("paper_name"));
                    listSelPrices.getItems().add(rsSub.getString("price"));
                }
            } else { showAlert("Info", "Record Not Found!"); }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 7. Update Logic
    @FXML
    void doUpdate(ActionEvent event) {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            // Update Customer Table
            PreparedStatement pst = con.prepareStatement("UPDATE customers SET name=?, email=?, address=?, area=?, hawker_id=?, start_date=? WHERE mobile=?");
            pst.setString(1, txtName.getText());
            pst.setString(2, txtEmail.getText());
            pst.setString(3, txtAddress.getText());
            pst.setString(4, comboArea.getSelectionModel().getSelectedItem());
            pst.setString(5, comboHawker.getSelectionModel().getSelectedItem());
            pst.setDate(6, Date.valueOf(dateStart.getValue()));
            pst.setString(7, txtMobile.getText());
            pst.executeUpdate();

            // Delete old subscriptions and add new ones
            PreparedStatement del = con.prepareStatement("DELETE FROM subscriptions WHERE mobile=?");
            del.setString(1, txtMobile.getText());
            del.executeUpdate();

            PreparedStatement pstSub = con.prepareStatement("INSERT INTO subscriptions VALUES(?,?,?)");
            for (int i = 0; i < listSelPapers.getItems().size(); i++) {
                pstSub.setString(1, txtMobile.getText());
                pstSub.setString(2, listSelPapers.getItems().get(i));
                pstSub.setString(3, listSelPrices.getItems().get(i));
                pstSub.executeUpdate();
            }
            con.commit();
            showAlert("Success", "Record Updated!");
        } catch (Exception e) { showAlert("Error", e.getMessage()); }
    }

    // 8. Unsubscribe (Delete) Logic
    @FXML
    void doUnsubscribe(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete Customer?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            try (Connection con = DBConnection.getConnection()) {
                // Suna bhai, ON DELETE CASCADE ki wajah se subscriptions khud delete ho jayengi
                PreparedStatement pst = con.prepareStatement("DELETE FROM customers WHERE mobile=?");
                pst.setString(1, txtMobile.getText());
                pst.executeUpdate();
                showAlert("Deleted", "Customer Removed!");
                doNew(null);
            } catch (Exception e) { showAlert("Error", e.getMessage()); }
        }
    }

    @FXML
    void doNew(ActionEvent event) {
        txtMobile.clear(); txtName.clear(); txtEmail.clear(); txtAddress.clear();
        listSelPapers.getItems().clear(); listSelPrices.getItems().clear();
        dateStart.setValue(null); comboArea.setValue(null); comboHawker.setValue(null);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }
}