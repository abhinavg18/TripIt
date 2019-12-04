package DataModels;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class ChatModel {
    public String message,creatorEmail, creatorName, msgImgUrl,id;
    @ServerTimestamp
    public Date timestamp;

    public Date getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
