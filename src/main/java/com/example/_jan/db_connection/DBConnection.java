//package com.example._jan.db_connection;
//
//public class DBConnection {
//}
package com.example._jan.db_connection;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    public static Connection getConnection() {
        Connection con = null;
        try {
            // "newspaper_agency" database ka naam hai jo humne workbench mein banaya tha
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/newspaper_agency", "root", "K#10rjnp@bti");
            System.out.println("Connected to Database!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Connection Failed! Password ya URL check karo.");
        }
        return con;
    }
}