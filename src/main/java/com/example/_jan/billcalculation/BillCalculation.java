package com.example._jan.billcalculation;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import com.example._jan.db_connection.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class BillCalculation {

    @FXML private DatePicker dateEnd, dateStart;
    @FXML private TextField txtBillAmount, txtLessDays, txtMobile, txtName, txtTotalPrice;

    /**
     * Fetches customer details and subscription pricing based on the mobile number.
     * Combines data from 'customers' and 'subscriptions' tables.
     */
    @FXML
    void doFetch(ActionEvent event) {
        String mobile = txtMobile.getText();
        if (mobile.isEmpty()) {
            showAlert("Validation Error", "Please enter a mobile number.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            // Retrieving basic profile and service start date
            PreparedStatement pst1 = con.prepareStatement("SELECT name, start_date FROM customers WHERE mobile=?");
            pst1.setString(1, mobile);
            ResultSet rs1 = pst1.executeQuery();

            if (rs1.next()) {
                txtName.setText(rs1.getString("name"));
                dateStart.setValue(rs1.getDate("start_date").toLocalDate());

                // Aggregating total monthly price from all active subscriptions
                PreparedStatement pst2 = con.prepareStatement("SELECT SUM(CAST(price AS FLOAT)) as total FROM subscriptions WHERE mobile=?");
                pst2.setString(1, mobile);
                ResultSet rs2 = pst2.executeQuery();
                if (rs2.next()) {
                    txtTotalPrice.setText(String.valueOf(rs2.getFloat("total")));
                }
            } else {
                showAlert("Search Result", "No record found for this mobile number.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Logic to calculate the bill based on duration and daily average rate.
     * Subtracts 'Less Days' (holidays/breaks) from the total duration.
     */
    @FXML
    void doGenerateBill(ActionEvent event) {
        try {
            LocalDate start = dateStart.getValue();
            LocalDate end = dateEnd.getValue();

            if (start == null || end == null) {
                showAlert("Date Error", "Please specify the billing period (Start & End dates).");
                return;
            }

            float monthlyPrice = Float.parseFloat(txtTotalPrice.getText());
            int offDays = txtLessDays.getText().isEmpty() ? 0 : Integer.parseInt(txtLessDays.getText());

            // Calculating temporal difference using ChronoUnit
            long totalDays = ChronoUnit.DAYS.between(start, end);
            long effectiveDays = totalDays - offDays;

            if (effectiveDays < 0) {
                showAlert("Logical Error", "Less days cannot exceed the total billing duration.");
                return;
            }

            // Billing Formula: (Monthly Rate / 30 Standard Days) * Effective Consumption Days
            float finalBill = (monthlyPrice / 30) * effectiveDays;
            txtBillAmount.setText(String.format("%.2f", finalBill));

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please ensure numerical fields are filled correctly.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Commits the calculated bill to the 'bills' table for payment tracking.
     * Status 0 indicates 'Pending' or 'Unpaid'.
     */
    @FXML
    void doSaveBill(ActionEvent event) {
        if (txtBillAmount.getText().isEmpty()) {
            showAlert("Action Required", "Please generate the bill amount before saving.");
            return;
        }

        String query = "INSERT INTO bills (mobile, date_to, bill_amount, status) VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, txtMobile.getText());
            pst.setDate(2, Date.valueOf(dateEnd.getValue()));
            pst.setFloat(3, Float.parseFloat(txtBillAmount.getText()));
            pst.setInt(4, 0); // 0 representing unpaid/pending status

            pst.executeUpdate();
            showAlert("Transaction Success", "Bill has been recorded successfully.");

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                showAlert("Duplicate Record", "A bill for this period already exists for this client.");
            } else {
                showAlert("System Error", "Database communication failure: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    /**
     * Resets the form to its initial state for a new transaction.
     */
    @FXML
    void doRefresh(ActionEvent event) {
        txtMobile.clear(); txtName.clear(); txtTotalPrice.clear();
        txtLessDays.clear(); txtBillAmount.clear();
        dateStart.setValue(null); dateEnd.setValue(null);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Billing System: " + title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}