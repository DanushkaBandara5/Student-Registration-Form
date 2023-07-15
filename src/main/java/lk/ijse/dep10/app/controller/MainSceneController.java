package lk.ijse.dep10.app.controller;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseDragEvent;
import lk.ijse.dep10.app.Util.Status;
import lk.ijse.dep10.app.Util.Student;
import lk.ijse.dep10.app.db.DBConnector;

import javax.imageio.ImageIO;
import javax.sql.rowset.serial.SerialBlob;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.*;
import java.util.ArrayList;

public class MainSceneController {

    @FXML
    private Button btnBrowse;

    @FXML
    private Button btnClear;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnNew;

    @FXML
    private Button btnSave;

    @FXML
    private ImageView imgPro;

    @FXML
    private TableView<Student> tblStudent;

    @FXML
    private TextField txtId;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtSearch;

    @FXML
    void btnBrowseOnAction(ActionEvent event) {

    }

    @FXML
    void btnClearOnAction(ActionEvent event) {

    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {

    }

    @FXML
    void btnNewOnAction(ActionEvent event) {

    }

    @FXML
    void btnSavaOnAction(ActionEvent event) {

    }

    @FXML
    void tblStudentOnKEyRelaesed(MouseDragEvent event) {

    }
    public boolean validateData(){
        boolean validate = true;
        if(!txtName.getText().matches("[A-z ]{3,}")){
            txtName.requestFocus();
            txtName.selectAll();
            validate=false;
        }return validate;
    }
    public void loadData(){
        Connection connection = DBConnector.getInstance().getConnection();
        try {
            Statement statement =connection.createStatement();
            String sql ="Select * from student";
            ResultSet resultSet=statement.executeQuery(sql);
            PreparedStatement prestmPic = connection.prepareStatement("Select * from picture where Student_id  =?");
            PreparedStatement prestmAtn = connection.prepareStatement("Select * from attendence where student_id=?");
            while (resultSet.next()){
                String id = resultSet.getString("id");
                String name =resultSet.getString("name");

                Image image =new Image("/image/man-dummy.jpg");
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                ByteArrayOutputStream bos =new ByteArrayOutputStream();
                ImageIO.write(bufferedImage,"png",bos);
                byte[] bytes = bos.toByteArray();
                Blob pic = new SerialBlob(bytes); ;


                prestmPic.setString(1,id);
                ResultSet resPic = prestmPic.executeQuery();
                while (resPic.next()){
                    pic =resPic.getBlob("picture");

                }
                int atnId;
                Timestamp timeStamp;
                Status status;
                prestmAtn.setString(1,id);
                ResultSet resAtn= prestmAtn.executeQuery();
                while(resAtn.next()){
                    atnId =resAtn.getInt("id");
                    timeStamp = resAtn.getTimestamp("stamp");
                    status = Status.valueOf(resAtn.getString(2));
                }
                Student student = new Student(id, name, pic);
                tblStudent.getItems().add(student);



            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public void generateId(){
        try {
            Connection connection= DBConnector.getInstance().getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet=statement.executeQuery("Select * from student");
            ArrayList<String> ids = new ArrayList<>();
            while(resultSet.next()){
                ids.add(resultSet.getString("id"));

            }
            if(ids.size()==0){
                txtId.setText("DEP-10/S-001");
            }else{
                txtId.setText(String.format("DEP-10/S-".concat("%03d"),(Integer.parseInt(ids.get(ids.size()-1).substring(9))+1)));
            }
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR,"Fail to generate student id from database ").showAndWait();
            throw new RuntimeException(e);
        }

    }

}
