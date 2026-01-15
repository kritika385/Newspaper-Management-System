package com.example._jan.customerboard;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import com.example._jan.db_connection.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.scene.layout.VBox;

public class CustomerBoard {

    @FXML private ComboBox<String> comboArea;
    @FXML private ComboBox<String> comboHawker;
    @FXML private ComboBox<String> comboPaper;
    @FXML private TableView<CustomerBean> tableCustomers;

    @FXML
    void initialize() {
        tableCustomers.setPlaceholder(new Label("No data found. Select filters and click 'Show Data'."));
        setupTable();
        fillFilters();
    }

    // 1. Table Columns Setup with Text Wrapping
    void setupTable() {
        TableColumn<CustomerBean, String> mobileCol = new TableColumn<>("Mobile");
        mobileCol.setCellValueFactory(new PropertyValueFactory<>("mobile"));
        mobileCol.setMinWidth(100);

        TableColumn<CustomerBean, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setMinWidth(150);

        TableColumn<CustomerBean, String> areaCol = new TableColumn<>("Area");
        areaCol.setCellValueFactory(new PropertyValueFactory<>("area"));
        areaCol.setMinWidth(120);

        TableColumn<CustomerBean, String> hawkerCol = new TableColumn<>("Hawker ID");
        hawkerCol.setCellValueFactory(new PropertyValueFactory<>("hawker"));
        hawkerCol.setMinWidth(100);

        // Newspaper Column - Isme text wrapping daali hai taaki naam na kate
        TableColumn<CustomerBean, String> paperCol = new TableColumn<>("Selected Newspapers");
        paperCol.setCellValueFactory(new PropertyValueFactory<>("paper"));
        paperCol.setMinWidth(300);

        // Magic Code for Wrapping: Text ko agli line mein bhejne ke liye
        paperCol.setCellFactory(tc -> {
            TableCell<CustomerBean, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(paperCol.widthProperty().subtract(10));
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });

        tableCustomers.getColumns().setAll(mobileCol, nameCol, areaCol, hawkerCol, paperCol);
        tableCustomers.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    void fillFilters() {
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.prepareStatement("SELECT DISTINCT area_name FROM areas").executeQuery();
            while (rs.next()) comboArea.getItems().add(rs.getString("area_name"));

            rs = con.prepareStatement("SELECT hawker_id FROM hawkers").executeQuery();
            while (rs.next()) comboHawker.getItems().add(rs.getString("hawker_id"));

            rs = con.prepareStatement("SELECT paper_name FROM papers").executeQuery();
            while (rs.next()) comboPaper.getItems().add(rs.getString("paper_name"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void doFetchData(ActionEvent event) {
        ObservableList<CustomerBean> list = FXCollections.observableArrayList();
        String selArea = comboArea.getSelectionModel().getSelectedItem();
        String selHawker = comboHawker.getSelectionModel().getSelectedItem();
        String selPaper = comboPaper.getSelectionModel().getSelectedItem();

        // GROUP_CONCAT saare papers ko comma ke sath jod dega
        StringBuilder query = new StringBuilder(
                "SELECT c.mobile, c.name, c.area, c.hawker_id, GROUP_CONCAT(s.paper_name SEPARATOR ', ') as all_papers " +
                        "FROM customers c INNER JOIN subscriptions s ON c.mobile = s.mobile WHERE 1=1"
        );

        if (selArea != null && !selArea.isEmpty()) query.append(" AND c.area = ?");
        if (selHawker != null && !selHawker.isEmpty()) query.append(" AND c.hawker_id = ?");
        if (selPaper != null && !selPaper.isEmpty()) query.append(" AND s.paper_name = ?");

        query.append(" GROUP BY c.mobile");

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement(query.toString());

            int paramIndex = 1;
            if (selArea != null && !selArea.isEmpty()) pst.setString(paramIndex++, selArea);
            if (selHawker != null && !selHawker.isEmpty()) pst.setString(paramIndex++, selHawker);
            if (selPaper != null && !selPaper.isEmpty()) pst.setString(paramIndex++, selPaper);

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(new CustomerBean(
                        rs.getString("mobile"),
                        rs.getString("name"),
                        rs.getString("area"),
                        rs.getString("hawker_id"),
                        rs.getString("all_papers")
                ));
            }
            tableCustomers.setItems(list);
            tableCustomers.refresh(); // Table ko refresh karega naye layout ke liye

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}