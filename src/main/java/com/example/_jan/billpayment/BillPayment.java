package com.example._jan.billpayment;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;
import com.example._jan.billcalculation.BillBean;
import com.example._jan.db_connection.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class BillPayment {

    @FXML private ResourceBundle resources;
    @FXML private URL location;

    // Yahan <?, ?> ki jagah sahi Types daal di hain
    @FXML private TableColumn<BillBean, Float> colAmount;
    @FXML private TableColumn<BillBean, String> colDate;
    @FXML private TableColumn<BillBean, String> colMobile;
    @FXML private TableView<BillBean> tableBills;

    @FXML private TextField txtMobile;
    @FXML private TextField txtTotalPending;

    ObservableList<BillBean> list;

    @FXML
    void doShowBills(ActionEvent event) {
        list = FXCollections.observableArrayList();
        float total = 0;
        String mobile = txtMobile.getText().trim();

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst;
            if (mobile.isEmpty()) {
                pst = con.prepareStatement("SELECT * FROM bills WHERE status=0");
            } else {
                pst = con.prepareStatement("SELECT * FROM bills WHERE mobile=? AND status=0");
                pst.setString(1, mobile);
            }

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                // Database columns: mobile, date_to, bill_amount
                list.add(new BillBean(
                        rs.getString("mobile"),
                        rs.getString("date_to"),
                        rs.getFloat("bill_amount")
                ));
                total += rs.getFloat("bill_amount");
            }

            tableBills.setItems(list);
            txtTotalPending.setText(String.format("%.2f", total));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void doMarkAsPaid(ActionEvent event) {
        String mobile = txtMobile.getText().trim();
        if (mobile.isEmpty()) {
            showAlert("Error", "Pehle mobile number enter karein.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement("UPDATE bills SET status=1 WHERE mobile=? AND status=0");
            pst.setString(1, mobile);
            int count = pst.executeUpdate();

            if (count > 0) {
                tableBills.getItems().clear();
                txtTotalPending.setText("0.00");
                showAlert("Success", "Bill paid successfully!");
            } else {
                showAlert("Info", "Is number ka koi pending bill nahi hai.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void initialize() {
        // Exact mapping with Bean getters
        colMobile.setCellValueFactory(new PropertyValueFactory<BillBean, String>("mobile"));
        colDate.setCellValueFactory(new PropertyValueFactory<BillBean, String>("dateTo"));
        colAmount.setCellValueFactory(new PropertyValueFactory<BillBean, Float>("amount"));

        tableBills.setPlaceholder(new Label("No records found in Database."));
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}