package lk.ijse.dep10.app.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import lk.ijse.dep10.app.Util.Gender;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class MainSceneController {

    public Label lblTime;
    public CheckBox chkMale;
    public CheckBox chkFemale;
    public RadioButton rdoMale;
    public RadioButton radoFemale;
    public ToggleGroup toggleGroup;
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

    public void initialize() {

        KeyFrame key1 = new KeyFrame(Duration.seconds(1), event -> {
            lblTime.setText("Time: " + DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalTime.now()));
        });

        Timeline timeline = new Timeline(key1);
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.playFromStart();

        generateId();
        loadData();
        btnClear.setDisable(true);
        btnDelete.setDisable(true);
        txtId.setDisable(true);


        tblStudent.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblStudent.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("imageView"));
        tblStudent.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblStudent.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("dateTime"));

        tblStudent.getSelectionModel().selectedItemProperty().addListener((value, previous, current) -> {
            btnDelete.setDisable(current == null);
            btnSave.setDisable(current == null);

            try {
                if (current == null) return;
                txtId.setText(current.getId());
                txtName.setText(current.getName());
                if(current.getStatus()==Gender.MALE){
                    rdoMale.setSelected(true);
                }
                else radoFemale.setSelected(true);
                imgPro.setImage(new Image(current.getImage().getBinaryStream()));
                btnClear.setDisable(false);
            } catch (SQLException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR,"Fail to load Image").show();
            }

        });
        txtSearch.textProperty().addListener((value, previous, current) -> {
            try {
                if (txtSearch.getText().strip().isEmpty()) {
                    tblStudent.getItems().clear();
                    loadData();
                    return;
                }
                Connection connection = DBConnector.getInstance().getConnection();
                PreparedStatement preStm = connection.prepareStatement("Select * from student where name Like ? or entrance like ? or id like ?");
                preStm.setString(1, "%" + current + "%");
                preStm.setString(2, "%" + current + "%");
                preStm.setString(3, "%" + current + "%");
                ResultSet resultSet = preStm.executeQuery();
                updateTable(resultSet, connection);

            } catch (IOException e) {
                e.printStackTrace();

            } catch (SQLException e) {
               e.printStackTrace();
               new Alert(Alert.AlertType.ERROR,"Fail to connect with database").show();
            }

        });
    }

    @FXML
    void btnBrowseOnAction(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image File", "*.png", "*.jpg", "*.jpeg"));
            fileChooser.setTitle("SELECT IMAGE FILE");
            File file = fileChooser.showOpenDialog(btnBrowse.getScene().getWindow());
            if (file == null) return;
            imgPro.setImage(new Image(file.toURI().toURL().toString()));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,"Fail to load profile picture").show();
        }


    }

    @FXML
    void btnClearOnAction(ActionEvent event) {
        Image image = new Image("/image/man-dummy.jpg");
        imgPro.setImage(image);

    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        Connection connection = DBConnector.getInstance().getConnection();
        Student std = tblStudent.getSelectionModel().getSelectedItem();

        try {
            connection.setAutoCommit(false);
            PreparedStatement prestmStd = connection.prepareStatement("Delete from student where id =?");
            PreparedStatement prestmPic = connection.prepareStatement("Delete from picture where student_id=?");

            prestmPic.setString(1, std.getId());

            prestmStd.setString(1, std.getId());
            prestmPic.execute();
            prestmStd.execute();
            connection.commit();
            tblStudent.getItems().remove(std);
            btnNew.fire();

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR,"Fail to update database").show();
            }
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,"Fail to update database").show();
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR,"Problem encountered when setAutoCommit to the default").show();
            }
        }
    }

    @FXML
    void btnNewOnAction(ActionEvent event) {
        generateId();
        txtName.clear();
        btnSave.setDisable(false);
        btnDelete.setDisable(true);
        btnClear.fire();

    }

    @FXML
    void btnSavaOnAction(ActionEvent event) {
        ObservableList<Student> stdList = tblStudent.getItems();
        if (!validateData()) return;
        Connection connection = DBConnector.getInstance().getConnection();
        try {
            String id = txtId.getText();
            String name = txtName.getText();
            Image image = imgPro.getImage();
            System.out.println(image.getUrl());
            Image imageDefault = new Image("/image/man-dummy.jpg");
            LocalDateTime dataTime = LocalDateTime.now();
            Gender gender = rdoMale.isSelected() ? Gender.valueOf("MALE") : Gender.valueOf("FEMALE");


            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", bos);
            byte[] bytes = bos.toByteArray();
            Blob pic = new SerialBlob(bytes);


            connection.setAutoCommit(false);

            for (Student student : stdList) {
                if (student.getId().equals(id)) {

                    PreparedStatement preStmStd = connection.prepareStatement("update student set name=?,gender=? where id=?");
                    preStmStd.setString(1, name);
                    preStmStd.setString(2, gender.name());
                    preStmStd.setString(3, id);
                    preStmStd.execute();
                    //TODO setup image changing
                    connection.commit();

                    loadData();
                    btnNew.fire();
                    return;
                }
            }
            PreparedStatement preStmPic = connection.prepareStatement("insert into picture values(?,?)");
            PreparedStatement preStmStd = connection.prepareStatement("insert into student (id,name,gender,entrance) values(?,?,?,?)");
            preStmStd.setString(1, id);
            preStmStd.setString(2, name);
            preStmStd.setString(3, gender.name());
            preStmStd.setTimestamp(4, Timestamp.valueOf(dataTime));
            preStmStd.execute();
            if (!image.getUrl().equals(imageDefault.getUrl())) {

                preStmPic.setString(1, id);
                preStmPic.setBlob(2, pic);
                preStmPic.execute();

            }
            connection.commit();
            Student student = new Student(id, name, pic, dataTime, gender);
            tblStudent.getItems().add(student);
            btnNew.fire();

        } catch (Throwable e) {
            e.printStackTrace();
            try {
                DBConnector.getInstance().getConnection().rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Fail to update database").show();
            }
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Something went wrong").show();

        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR,"Problem encountered when setAutoCommit to the default").show();
            }
        }
    }

    public boolean validateData() {
        boolean validate = true;
        if (!txtName.getText().matches("[A-z ]{3,}")) {
            txtName.requestFocus();
            txtName.selectAll();
            validate = false;
        }
        if (toggleGroup.getSelectedToggle() == null) {
            rdoMale.requestFocus();
            validate = false;
        }
        return validate;
    }

    public void loadData() {
        Connection connection = DBConnector.getInstance().getConnection();
        try {
            Statement statement = connection.createStatement();
            String sql = "Select * from student";
            ResultSet resultSet = statement.executeQuery(sql);
            updateTable(resultSet, connection);
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,"Fail to load date form database").show();
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,"Fail to load image").show();
        }

    }

    public void generateId() {
        try {
            Connection connection = DBConnector.getInstance().getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("Select * from student");
            ArrayList<String> ids = new ArrayList<>();
            while (resultSet.next()) {
                ids.add(resultSet.getString("id"));

            }
            if (ids.size() == 0) {
                txtId.setText("DEP-10/S-001");
            } else {
                txtId.setText(String.format("DEP-10/S-".concat("%03d"), (Integer.parseInt(ids.get(ids.size() - 1).substring(9)) + 1)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Fail to connect with database").show();

        }

    }

    private void updateTable(ResultSet resultSet, Connection connection) throws SQLException, IOException {
        tblStudent.getItems().clear();
        PreparedStatement prestmPic = connection.prepareStatement("Select * from picture where Student_id  =?");
        while (resultSet.next()) {
            String id = resultSet.getString("id");
            String name = resultSet.getString("name");
            LocalDateTime dateTime = resultSet.getTimestamp("entrance").toLocalDateTime();
            Gender gender = Gender.valueOf(resultSet.getString("gender"));

            Image image = new Image("/image/man-dummy.jpg");
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", bos);
            byte[] bytes = bos.toByteArray();
            Blob pic = new SerialBlob(bytes);
            ;


            prestmPic.setString(1, id);
            ResultSet resPic = prestmPic.executeQuery();
            while (resPic.next()) {
                pic = resPic.getBlob("picture");

            }

            Student student = new Student(id, name, pic, dateTime, gender);
            tblStudent.getItems().add(student);

        }


    }


    public void tblStudentOnKeyReleased(KeyEvent keyEvent) {
        if(keyEvent.getCode()== KeyCode.DELETE) btnDelete.fire();
    }
}
