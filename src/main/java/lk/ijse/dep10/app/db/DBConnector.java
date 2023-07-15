package lk.ijse.dep10.app.db;

import javafx.scene.control.Alert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnector {
    private static DBConnector dbController;
    private final Connection connection;
    private DBConnector(){
        Properties properties = new Properties();
        try {
            FileReader fr = new FileReader(new File("application.properties"));
            properties.load(fr);
            fr.close();

            String host=properties.getProperty("dep10.host");
            String port=properties.getProperty("dep10.port");
            String database=properties.getProperty("dep10.database");
            String user=properties.getProperty("dep10.user");
            String password=properties.getProperty("dep10.password");

            StringBuilder sb = new StringBuilder("jdbc:mysql://").append(host).append(":").append(port).append("/").
                    append(database).append("?createDatabaseIfNotExist=true&allowMultiQueries=true");

            connection = DriverManager.getConnection(sb.toString(),user,password);
        } catch (FileNotFoundException e) {
            new Alert(Alert.AlertType.ERROR,"Configuration File Doesn't Exists").showAndWait();
            throw new RuntimeException(e);
        }catch (IOException e) {
            new Alert(Alert.AlertType.ERROR,"Fail to Read Configurations").showAndWait();
            throw new RuntimeException(e);
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR,"Fail to Establish the Data Base Connection").showAndWait();
            throw new RuntimeException(e);
        }


    }
    public static DBConnector getInstance(){
        return (dbController==null)? dbController= new DBConnector():dbController;
    }
    public Connection getConnection(){
        return connection;
    }

}
