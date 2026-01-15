package com.example._jan.dashboard;

import java.net.URL;
import java.awt.Desktop;
import java.net.URI;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Dashboard {

    @FXML private Button btnAllBills, btnAnalyser, btnArea, btnBillCalc, btnBillCollect, btnCustData, btnCustomer, btnHawker, btnPaper;

    // --- Sahi Path Logic (Modules kholne ke liye) ---
    void openModule(String fxmlFile, String title) {
        try {
            String path = "/com/example/_jan/" + fxmlFile;
            URL url = getClass().getResource(path);

            if (url == null) {
                System.out.println("Path galat hai bhai: " + path);
                return;
            }

            Parent root = FXMLLoader.load(url);
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Button Click Actions ---
    @FXML void doAreaMaster(ActionEvent event) { openModule("AreaMasterView.fxml", "Area Master"); }
    @FXML void doPaperMaster(ActionEvent event) { openModule("PaperMasterView.fxml", "Paper Master"); }
    @FXML void doHawker(ActionEvent event) { openModule("HawkerConsoleView.fxml", "Hawker Manager"); }
    @FXML void doCustomer(ActionEvent event) { openModule("CustomerEnrollmentView.fxml", "Customer Entry"); }
    @FXML void doBillCalc(ActionEvent event) { openModule("BillingCalculationView.fxml", "Bill Calculation"); }
    @FXML void doBillCollect(ActionEvent event) { openModule("BillPaymentView.fxml", "Bill Collection"); }
    @FXML void doCustData(ActionEvent event) { openModule("CustomerBoardView.fxml", "Customer Data"); }
    @FXML void doAllBills(ActionEvent event) { openModule("BillingBoardView.fxml", "All Bills Report"); }
    @FXML void doAnalyser(ActionEvent event) { openModule("AnalyserView.fxml", "Analysis"); }

    // --- LinkedIn Profile Link ---
    @FXML
    void doOpenLinkedIn(ActionEvent event) {
        try {
            //   link yahan update kar diya hai
            Desktop.getDesktop().browse(new URI("https://www.linkedin.com/in/kritika-33312b278"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Hover Animations (Buttons move up) ---
    @FXML
    void doHoverIn(MouseEvent event) {
        Button btn = (Button) event.getSource();
        TranslateTransition tt = new TranslateTransition(Duration.millis(200), btn);
        tt.setToY(-10);
        tt.play();
    }

    // --- Hover Animations (Buttons come back) ---
    @FXML
    void doHoverOut(MouseEvent event) {
        Button btn = (Button) event.getSource();
        TranslateTransition tt = new TranslateTransition(Duration.millis(200), btn);
        tt.setToY(0);
        tt.play();
    }
}