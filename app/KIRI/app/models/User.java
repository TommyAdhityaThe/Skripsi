package models;

import play.db.DB;
import play.libs.Json;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;
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
		PreparedStatement pstmt = connection
				.prepareStatement("DELETE FROM sessions WHERE lastSeen < (NOW() - INTERVAL 6 HOUR)");
		pstmt.executeUpdate();
		pstmt = connection.prepareStatement(
				"SELECT users.email, users.privilegeRoute, users.privilegeApiUsage FROM users LEFT JOIN sessions ON users.email = sessions.email WHERE sessions.sessionId =?");
		pstmt.setString(1, this.sessionID);
		ResultSet result = pstmt.executeQuery();
		if (!result.next()) {
			connection.close();
			throw new UniqueStatusError(Constant.ERROR_SESSION_EXPIRED);
		}
		this.activeUserID = result.getString(1);
		this.privilegeRoute = result.getInt(2) != 0;
		this.privilegeApiUsage = result.getInt(3) != 0;
		connection.close();
	}

	public ObjectNode getProfile() throws IOException, SQLException {
		java.sql.Connection connection = DB.getConnection();
		PreparedStatement pstmt = connection.prepareStatement("SELECT fullName, company FROM users WHERE email=?");
		pstmt.setString(1, this.activeUserID);
		ResultSet result = pstmt.executeQuery();
		if (!result.next()) {
			connection.close();
			Utils.dieNice("User " + this.activeUserID + " not found in database.");
		}
		String fullName = result.getString(1);
		String company = result.getString(2);
		ObjectNode obj = Json.newObject();
		obj.put(Constant.PROTO_STATUS, Constant.PROTO_STATUS_OK);
		obj.put(Constant.PROTO_FULL_NAME, fullName);
		obj.put(Constant.PROTO_COMPANY, company);
		connection.close();
		return obj;
	}

	public void updateProfile(String newPassword, String newFullName, String newCompany)
			throws NoSuchAlgorithmException, SQLException, UnsupportedEncodingException {
		java.sql.Connection connection = DB.getConnection();
		PreparedStatement pstmt;
		if (!newPassword.equals("")) {
			String passwordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
			pstmt = connection.prepareStatement("UPDATE users SET password=? WHERE email=?");
			pstmt.setString(1, passwordHash);
			pstmt.setString(2, this.activeUserID);
			pstmt.executeUpdate();
		}
		pstmt = connection.prepareStatement("UPDATE users SET fullName=?, company=? WHERE email=?");
		pstmt.setString(1, newFullName);
		pstmt.setString(2, newCompany);
		pstmt.setString(3, this.activeUserID);
		pstmt.executeUpdate();
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