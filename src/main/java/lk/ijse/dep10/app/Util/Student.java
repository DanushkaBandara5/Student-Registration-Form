package lk.ijse.dep10.app.Util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Student {
    private String id;
    private String name;
    private Blob image;
    private LocalDateTime dateTime;
    private Gender gender;

    public Student(String id, String name, Blob image) {
        this.id = id;
        this.name = name;
        this.image = image;

    }

    public Student(String id, String name, Blob image, LocalDateTime dataTime, Gender gender) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.dateTime = dataTime;
        this.gender = gender;
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

    public LocalDateTime getDateTime() {
        return this.dateTime ;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Gender getStatus() {
        return gender;
    }

    public void setStatus(Gender status) {
        this.gender = status;
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
    public String getAttendence() {
        return DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss").format(dateTime);
    }
}
