module com.example._jan {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;


    opens com.example._jan to javafx.fxml;
    exports com.example._jan;

    opens com.example._jan.area to javafx.fxml;
    exports com.example._jan.area;

    opens com.example._jan.paper to javafx.fxml;
    exports com.example._jan.paper;

    opens com.example._jan.hawker to javafx.fxml;
    exports com.example._jan.hawker;

    opens com.example._jan.customer to javafx.fxml;
    exports com.example._jan.customer;

    opens com.example._jan.billcalculation to javafx.fxml;
    exports com.example._jan.billcalculation;

    opens com.example._jan.billpayment to javafx.fxml;
    exports com.example._jan.billpayment;

    opens com.example._jan.customerboard to javafx.fxml;
    exports com.example._jan.customerboard;

    opens com.example._jan.billingboard to javafx.fxml;
    exports com.example._jan.billingboard;

    opens com.example._jan.analyser to javafx.fxml;
    exports com.example._jan.analyser;

    opens com.example._jan.login to javafx.fxml;
    exports com.example._jan.login;

    opens com.example._jan.dashboard to javafx.fxml;
    exports com.example._jan.dashboard;
}