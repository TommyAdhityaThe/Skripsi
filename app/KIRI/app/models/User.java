package models;

import play.db.DB;
import play.libs.Json;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Kelas ini untuk menangani kasus: pemeriksaan login, update profil dan
 * permintaan data profil
 * 
 * @author Tommy Adhitya The
 */
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
		ResultSet result = statement.executeQuery(
				"SELECT users.email, users.privilegeRoute, users.privilegeApiUsage FROM users LEFT JOIN sessions ON users.email = sessions.email WHERE sessions.sessionId = '"
						+ this.sessionID + "'");
		if (!result.next()) {
			connection.close();
			throw new UniqueStatusError("sessionexpired");
		}
		this.activeUserID = result.getString(1);
		this.privilegeRoute = result.getInt(2) != 0;
		this.privilegeApiUsage = result.getInt(3) != 0;
		connection.close();
	}

	public ObjectNode getProfile() throws IOException, SQLException {
		java.sql.Connection connection = DB.getConnection();
		Statement statement = connection.createStatement();
		ResultSet result = statement
				.executeQuery("SELECT fullName, company FROM users WHERE email='" + this.activeUserID + "'");
		if (!result.next()) {
			connection.close();
			Method.dieNice("User " + this.activeUserID + " not found in database.");
		}
		String fullName = result.getString(1);
		String company = result.getString(2);
		ObjectNode obj = Json.newObject();
		obj.put("status", "ok");
		obj.put("fullname", fullName);
		obj.put("company", company);
		connection.close();
		return obj;
	}

	public void updateProfile(String newPassword, String newFullName, String newCompany)
			throws NoSuchAlgorithmException, SQLException, UnsupportedEncodingException {
		java.sql.Connection connection = DB.getConnection();
		Statement statement = connection.createStatement();
		if (!newPassword.equals("")) {
			String passwordHash = Method.hashingPassword(newPassword);
			statement.executeUpdate(
					"UPDATE users SET password='" + passwordHash + "' WHERE email='" + this.activeUserID + "'");
		}
		statement.executeUpdate("UPDATE users SET fullName='" + newFullName + "', company='" + newCompany
				+ "' WHERE email='" + this.activeUserID + "'");
		connection.close();
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