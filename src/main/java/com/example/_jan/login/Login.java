package com.example._jan.login;

import java.net.URL; // Yeh zaroori hai URL ke liye
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.example._jan.db_connection.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class Login {

    @FXML private Button btnLogin;
    @FXML private PasswordField txtPass;
    @FXML private TextField txtUser;

    @FXML
    void doLogin(ActionEvent event) {
        String user = txtUser.getText();
        String pass = txtPass.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            showAlert("Error", " Username aur Password dono bharo!");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            // MySQL mein table ka naam 'admin' aur columns 'userid', 'password'
            PreparedStatement pst = con.prepareStatement("SELECT * FROM admin WHERE userid=? AND password=?");
            pst.setString(1, user);
            pst.setString(2, pass);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                // Login Success!
                showDashboard();

                // Login window band karne ke liye
                Stage stage = (Stage) btnLogin.getScene().getWindow();
                stage.close();
            } else {
                showAlert("Access Denied", "Username ya Password galat hai!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Database Error", "DB Connection check : " + e.getMessage());
        }
    }

    void showDashboard() {
        try {

            URL url = getClass().getResource("/com/example/_jan/Dashboard.fxml");
            if (url == null) {
                System.out.println("Path check ! Dashboard.fxml nahi mili.");
                return;
            }

            Parent root = FXMLLoader.load(url);
            Stage stage = new Stage();
            stage.setTitle("Newspaper Agency - Admin Dashboard");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();

        } catch (Exception e) {
            System.out.println("Dashboard load  error!");
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}