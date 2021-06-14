package licenta.dto;

import java.time.LocalDateTime;

public class ReviewDTO {

    private int id;
    private int rating;
    private String text;
    private LocalDateTime dateTime;
    private UserDTO receiver;
    private UserDTO sender;
    private String travelRole;

    public ReviewDTO() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public UserDTO getReceiver() {
        return receiver;
    }

    public void setReceiver(UserDTO receiver) {
        this.receiver = receiver;
    }

    public UserDTO getSender() {
        return sender;
    }

    public void setSender(UserDTO sender) {
        this.sender = sender;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getTravelRole() {
        return travelRole;
    }

    public void setTravelRole(String travelRole) {
        this.travelRole = travelRole;
    }

}
