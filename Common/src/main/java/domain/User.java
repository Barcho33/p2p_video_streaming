package domain;

import java.io.Serial;
import java.io.Serializable;

public class User
implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String userId;
    private String username;


    public User() {
    }

    public User(String userId, String username) {
        this.userId = userId;
        this.username = username;

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
