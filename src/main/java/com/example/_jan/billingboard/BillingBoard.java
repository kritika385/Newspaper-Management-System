package com.example._jan.billingboard;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;
import com.example._jan.db_connection.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class BillingBoard {

    @FXML private ComboBox<String> comboStatus;
    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;
    @FXML private TableView<BillingBean> tableBilling;
    @FXML private TextField txtAmount;

    @FXML
    void initialize() {
        comboStatus.getItems().addAll("Pending", "Paid", "All");
        setupTable();
    }

    void setupTable() {
        // TableView mapping strictly with BillingBean getters
        TableColumn<BillingBean, String> mobileCol = new TableColumn<>("Mobile Number");
        mobileCol.setCellValueFactory(new PropertyValueFactory<>("mobile"));

        TableColumn<BillingBean, String> endCol = new TableColumn<>("Bill Date");
        endCol.setCellValueFactory(new PropertyValueFactory<>("dateTo"));

        TableColumn<BillingBean, Float> amtCol = new TableColumn<>("Amount");
        amtCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<BillingBean, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        tableBilling.getColumns().setAll(mobileCol, endCol, amtCol, statusCol);
    }

    @FXML
    void doFindNow(ActionEvent event) {
        ObservableList<BillingBean> list = FXCollections.observableArrayList();
        float totalSum = 0;

        String selStatus = comboStatus.getSelectionModel().getSelectedItem();
        LocalDate from = dateFrom.getValue();
        LocalDate to = dateTo.getValue();

        StringBuilder query = new StringBuilder("SELECT * FROM bills WHERE 1=1");
        if (from != null && to != null) query.append(" AND date_to BETWEEN ? AND ?");
        if (selStatus != null && !selStatus.equals("All")) query.append(" AND status = ?");

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement(query.toString());
            int pIndex = 1;

            if (from != null && to != null) {
                pst.setDate(pIndex++, Date.valueOf(from));
                pst.setDate(pIndex++, Date.valueOf(to));
            }
            if (selStatus != null && !selStatus.equals("All")) {
                pst.setInt(pIndex++, selStatus.equals("Paid") ? 1 : 0);
            }

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                float amt = rs.getFloat("bill_amount");
                String stText = (rs.getInt("status") == 1) ? "Paid" : "Pending";

                // FIX: BillingBean ke constructor mein ab sirf 4 cheezein bhejni hain
                list.add(new BillingBean(
                        rs.getString("mobile"),
                        rs.getString("date_to"),
                        amt,
                        stText
                ));
                totalSum += amt;
            }
            tableBilling.setItems(list);
            txtAmount.setText(String.format("%.2f", totalSum));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}