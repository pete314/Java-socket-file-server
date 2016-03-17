package ie.gmit.sw.fileserver;

/**
 *
 * @author Peter Nagy
 * @since 2015 December
 * @description Holding the user Entity
 */
public class User {
    private String username;
    private char[] password;//for security reasons as string is pooled
    private String id;
    private String sessionID;
    private String defaultDirectory;
    private String currentDirectory = "";
    
    public User(){};

    //Getters setters from here
    public String getUsername() {
        return username;
    }

    public User setUsername(String username) {
        this.username = username;
        return this;
    }

    public char[] getPassword() {
        return password;
    }

    public User setPassword(char[] password) {
        this.password = password;
        return this;
    }
    
    public String getId() {
        return id;
    }

    public User setId(String id) {
        this.id = id;
        return this;
    }

    public String getSessionID() {
        return sessionID;
    }

    public User setSessionID(String sessionID) {
        this.sessionID = sessionID;
        return this;
    }

    public String getDefaultDirectory() {
        return defaultDirectory;
    }

    public User setDefaultDirectory(String defaultDirectory) {
        this.defaultDirectory = defaultDirectory;
        return this;
    }

    public String getCurrentDirectory() {
        return currentDirectory;
    }

    public void setCurrentDirectory(String currentDirectory) {
        this.currentDirectory = currentDirectory;
    }
}
