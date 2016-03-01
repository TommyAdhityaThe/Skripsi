/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import play.db.*;
import java.io.*;
import java.sql.*;
import play.libs.Json;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author Tommy Adhitya The
 */
public class AuthenticationManager {
	public ObjectNode register(String email, String fullname, String company)
			throws IOException, SQLException, NoSuchAlgorithmException, UnsupportedEncodingException {

		java.sql.Connection connection = DB.getConnection();
		Statement statement = connection.createStatement();
		ResultSet result = statement.executeQuery("SELECT email FROM users WHERE email='" + email + "'");
		while (result.next()) {
			return Method.well_done("Ooops! Email " + email
					+ " has already registered. Please check your mailbox or contact hello@kiri.travel");
		}
		// Generate password tanpa fitur hash and send password (belum
		// dilakukan)
		String password = Method.generate_password();
		System.out.println("password: " + password);
		String passwordHash = Method.hashingPassword(password);
		statement.executeUpdate("INSERT INTO users(email, password, privilegeApiUsage, fullName, company) VALUES('"
				+ email + "', '" + passwordHash + "', 1, '" + fullname + "', '" + company + "');");
		Method.sendPassword(email, password, fullname);
		Method.log_statistic("E5D9904F0A8B4F99", "REGISTER", email + "/" + fullname + "" + company);
		return Method.well_done(null);
	}

	public ObjectNode login(String userid, String password)
			throws UniqueStatusError, SQLException, NoSuchAlgorithmException, UnsupportedEncodingException {
		if (userid.length() > 128) {
			Method.return_invalid_credentials("User ID length is more than allowed (" + userid.length() + ")");
		}
		if (password.length() > 32) {
			Method.return_invalid_credentials("Password length is more than allowed (" + password.length() + ")");
		}

		// Retrieve the user information
		java.sql.Connection connection = DB.getConnection();
		Statement statement = connection.createStatement();
		ResultSet result = statement.executeQuery("SELECT * FROM users WHERE email='" + userid + "'");
		if (!result.next()) {
			Method.return_invalid_credentials("User id not found: " + userid);
		}

		String userDataPassword = result.getString("password");
		String passwordHash = Method.hashingPassword(password);

		if (!passwordHash.equals(userDataPassword)) {
			Method.log_statistic("E5D9904F0A8B4F99", "LOGIN", userid + "/FAIL");
			Method.return_invalid_credentials("Password mismatch for " + userid);
		}

		Method.log_statistic("E5D9904F0A8B4F99", "LOGIN", userid + "/SUCCESS");
		int privilegeRoute = result.getInt("privilegeRoute");
		int privilegeApiUsage = result.getInt("privilegeApiUsage");

		String sessionid = Method.generate_sessionid();
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
		return obj;
	}

	public void logout(String sessionid) throws IOException, SQLException {
		java.sql.Connection connection = DB.getConnection();
		Statement statement = connection.createStatement();
		statement.executeUpdate("DELETE FROM sessions WHERE sessionId='" + sessionid + "'");
	}
}
