package lk.ijse.dep10.app.Util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.sql.Blob;
import java.sql.SQLException;

public class Student {
    private String id;
    private String name;
    private Blob image;
    private int attendanceId;
    private Status status;

    public Student(String id, String name, Blob image) {
        this.id = id;
        this.name = name;
        this.image = image;

    }

    public Student(String id, String name, Blob image, int attendanceId, Status status) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.attendanceId = attendanceId;
        this.status = status;
    }

    public Student() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Blob getImage() {
        return image;
    }

    public void setImage(Blob image) {
        this.image = image;
    }

    public int getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(int attendanceId) {
        this.attendanceId = attendanceId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
    public ImageView getImageView(){
        try {
            Image image = new Image(getImage().getBinaryStream());
            ImageView imageView= new ImageView(image);
            imageView.setFitHeight(50);
            imageView.setFitWidth(50);
            return imageView;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
