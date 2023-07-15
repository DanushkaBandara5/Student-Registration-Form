package lk.ijse.dep10.app.controller;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseDragEvent;
import javafx.stage.FileChooser;
import lk.ijse.dep10.app.Util.Status;
import lk.ijse.dep10.app.Util.Student;
import lk.ijse.dep10.app.db.DBConnector;

import javax.imageio.ImageIO;
import javax.sql.rowset.serial.SerialBlob;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
    public void initialize(){
        generateId();
        loadData();
        btnClear.setDisable(true);
        btnDelete.setDisable(true);
        txtId.setDisable(true);


        tblStudent.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblStudent.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("imageView"));
        tblStudent.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("name"));

        tblStudent.getSelectionModel().selectedItemProperty().addListener((value,previous,current)->{
            btnDelete.setDisable(current==null);
            btnSave.setDisable(current!=null);
            try {
                if(current==null) return;
                txtId.setText(current.getId());
                txtName.setText(current.getName());
                imgPro.setImage(new Image(current.getImage().getBinaryStream()));

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        });
        txtSearch.textProperty().addListener((value,prevoius,current)->{
            try {
                if(txtSearch.getText().strip().isEmpty()){
                    tblStudent.getItems().clear();
                    loadData();
                    return;
                }
                Connection connection = DBConnector.getInstance().getConnection();
                PreparedStatement preStm = connection.prepareStatement("Select * from student where name Like ? ");
                preStm.setString(1,"%"+current+"%");
                ResultSet resultSet=preStm.executeQuery();
                updateTable(resultSet,connection);

            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        });
    }

    @FXML
    void btnBrowseOnAction(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image File","*.png","*.jpg","*.jpeg"));
            fileChooser.setTitle("SELECT IMAGE FILE");
            File file = fileChooser.showOpenDialog(btnBrowse.getScene().getWindow());
            if(file==null)return;
            imgPro.setImage(new Image(file.toURI().toURL().toString()));
            btnClear.setDisable(false);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }



    }

    @FXML
    void btnClearOnAction(ActionEvent event) {
        Image image= new Image("/image/man-dummy.jpg");
        imgPro.setImage(image);
        btnClear.setDisable(true);

    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {

    }

    @FXML
    void btnNewOnAction(ActionEvent event) {
        generateId();
        txtName.clear();
        btnSave.setDisable(false);
        btnDelete.setDisable(true);
        Image image= new Image("/image/man-dummy.jpg");
        imgPro.setImage(image);

    }

    @FXML
    void btnSavaOnAction(ActionEvent event) {
        if(!validateData()) return;

        try {
            String id=txtId.getText();
            String name = txtName.getText();
            Image image =imgPro.getImage();
            Image imageDefault =new Image("/image/man-dummy.jpg");

            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
            ByteArrayOutputStream bos =new ByteArrayOutputStream();
            ImageIO.write(bufferedImage,"png",bos);
            byte[] bytes = bos.toByteArray();
            Blob pic = new SerialBlob(bytes);
            ;

            Connection connection =DBConnector.getInstance().getConnection();
            connection.setAutoCommit(false);
            PreparedStatement preStmStd = connection.prepareStatement("insert into student values(?,?)") ;
            PreparedStatement preStmPic = connection.prepareStatement("insert into picture values(?,?)") ;
            preStmStd.setString(1,id);
            preStmStd.setString(2,name);
            preStmStd.execute();

            if(!image.getUrl().equals(imageDefault.getUrl())) {

                preStmPic.setString(1, id);
                preStmPic.setBlob(2, pic);
                preStmPic.execute();
            }

            connection.commit();

            Student student = new Student(id, name, pic);
            tblStudent.getItems().add(student);
            btnNew.fire();

        } catch (Throwable e) {
            e.printStackTrace();
            try {
                DBConnector.getInstance().getConnection().rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }

            new RuntimeException(e);

        }finally {
            try {
                DBConnector.getInstance().getConnection().setAutoCommit(true);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

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
            ResultSet resultSet = statement.executeQuery(sql);
            updateTable(resultSet,connection);
            }catch (SQLException e) {
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
    private void updateTable(ResultSet resultSet,Connection connection) throws SQLException, IOException {
        tblStudent.getItems().clear();
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


    }
}
