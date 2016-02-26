package models;

import play.db.*;
import java.sql.*;
import java.io.*;

public class User {

    private String sessionID;
    private String activeUserID;
    private boolean privilegeRoute;
    private boolean privilegeApiUsage;

    public User(String sessionID) throws UniqueStatusError, SQLException {
        this.sessionID = sessionID;
        java.sql.Connection connection = DB.getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate("DELETE FROM sessions WHERE lastSeen < (NOW() - INTERVAL 6 HOUR)");
        ResultSet result = statement.executeQuery("SELECT users.email, users.privilegeRoute, users.privilegeApiUsage FROM users LEFT JOIN sessions ON users.email = sessions.email WHERE sessions.sessionId = '" + this.sessionID + "'");
        if (!result.next()) {
            throw new UniqueStatusError("sessionexpired");
        }
        this.activeUserID = result.getString("users.email");
        this.privilegeRoute = result.getInt("users.privilegeRoute") != 0;
        this.privilegeApiUsage = result.getInt("users.privilegeApiUsage") != 0;
    }

    public String getSessionID() {
        return sessionID;
    }

    public String getActiveUserID() {
        return activeUserID;
    }

    public boolean isPrivilegeRoute() {
        return privilegeRoute;
    }

    public boolean isPrivilegeApiUsage() {
        return privilegeApiUsage;
    }
}