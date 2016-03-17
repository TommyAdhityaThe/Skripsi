package models;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import play.db.DB;
import play.libs.Json;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Kelas ini untuk menangani kasus: otentikasi
 * 
 * @author Tommy Adhitya The
 */
public class AuthenticationManager {
	public ObjectNode register(String email, String fullname, String company) throws IOException, SQLException,
			NoSuchAlgorithmException, UnsupportedEncodingException, AddressException, MessagingException {

		java.sql.Connection connection = DB.getConnection();
		Statement statement = connection.createStatement();
		ResultSet result = statement.executeQuery("SELECT email FROM users WHERE email='" + email + "'");
		if(result.next()) {
			connection.close();
			Method.dieNice("Ooops! Email " + email
					+ " has already registered. Please check your mailbox or contact hello@kiri.travel");
		}
		String password = Method.generatePassword();
		String passwordHash = Method.hashingPassword(password);
		statement.executeUpdate("INSERT INTO users(email, password, privilegeApiUsage, fullName, company) VALUES('"
				+ email + "', '" + passwordHash + "', 1, '" + fullname + "', '" + company + "');");
		Method.sendPassword(email, password, fullname); 
		Method.logStatistic(Constant.APIKEY_KIRI, "REGISTER", email + "/" + fullname + "" + company);
		connection.close();
		return Method.wellDone(null);
	}

	public ObjectNode login(String userid, String password)
			throws UniqueStatusError, SQLException, NoSuchAlgorithmException, UnsupportedEncodingException {
		if (userid.length() > 128) {
			Method.returnInvalidCredentials("User ID length is more than allowed (" + userid.length() + ")");
		}
		if (password.length() > 32) {
			Method.returnInvalidCredentials("Password length is more than allowed (" + password.length() + ")");
		}
		java.sql.Connection connection = DB.getConnection();
		Statement statement = connection.createStatement();
		ResultSet result = statement.executeQuery("SELECT * FROM users WHERE email='" + userid + "'");
		if (!result.next()) {
			connection.close();
			Method.returnInvalidCredentials("User id not found: " + userid);
		}
		String userDataPassword = result.getString(2);
		String passwordHash = Method.hashingPassword(password);
		if (!passwordHash.equals(userDataPassword)) {
			connection.close();
			Method.logStatistic(Constant.APIKEY_KIRI, "LOGIN", userid + "/FAIL");
			Method.returnInvalidCredentials("Password mismatch for " + userid);
		}
		Method.logStatistic(Constant.APIKEY_KIRI, "LOGIN", userid + "/SUCCESS");
		int privilegeRoute = result.getInt(5);
		int privilegeApiUsage = result.getInt(6);
		String sessionid = Method.generateSessionID();
		statement.executeUpdate(
				"INSERT INTO sessions (sessionId, email) VALUES ('" + sessionid + "', '" + userid + "')");
		StringBuilder privileges = new StringBuilder();
		if (privilegeRoute != 0) {
			privileges.append(",route");
		}
		if (privilegeApiUsage != 0) {
			privileges.append(",apiusage");
		}
		if (privileges.length() > 0) {
			privileges = new StringBuilder(privileges.substring(1));
		}
		ObjectNode obj = Json.newObject();
		obj.put("status", "ok");
		obj.put("sessionid", sessionid);
		obj.put("privileges", privileges.toString());
		connection.close();
		return obj;
	}

	public void logout(String sessionid) throws IOException, SQLException {
		java.sql.Connection connection = DB.getConnection();
		Statement statement = connection.createStatement();
		statement.executeUpdate("DELETE FROM sessions WHERE sessionId='" + sessionid + "'");
		connection.close();
	}
}
