package DataModels;

import java.io.Serializable;
import java.util.ArrayList;

public class Trip implements Serializable {
    public String title,creatorName;
    public double lat, longi;
    public String tripPhotoUrl;
    public ArrayList<String> userList;
    public String creatorEmail;
}
