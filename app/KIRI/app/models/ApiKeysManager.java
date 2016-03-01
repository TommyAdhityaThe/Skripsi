package models;

import play.db.*;
import java.sql.*;
import java.io.*;
import play.libs.Json;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class ApiKeysManager {

	// blom beres
	public ObjectNode getListApiKeys(User user) throws SQLException, IOException {
		this.checkPrivilege(user.isPrivilegeApiUsage());
		java.sql.Connection connection = DB.getConnection();
		Statement statement = connection.createStatement();
		ResultSet result = statement
				.executeQuery("SELECT verifier, domainFilter, description FROM apikeys WHERE email='"
						+ user.getActiveUserID() + "' ORDER BY verifier");
		ArrayNode listApiKeys=Json.newArray();
		while (result.next()) {
			ArrayNode apiKeyValue=Json.newArray();
			apiKeyValue.add(result.getString("verifier"));
			apiKeyValue.add(result.getString("domainFilter"));
			apiKeyValue.add(result.getString("description"));
			listApiKeys.add(apiKeyValue);
		}
		ObjectNode obj = Json.newObject();
		obj.put("status", "ok");
		obj.putArray("apikeyslist").addAll(listApiKeys);
		return obj;
	}

	public ObjectNode addApiKey(User user, String domainFilter, String description) throws SQLException, IOException {
		this.checkPrivilege(user.isPrivilegeApiUsage());
		String apiKey = Method.generateApiKey();
		java.sql.Connection connection = DB.getConnection();
		Statement statement = connection.createStatement();
		statement.executeUpdate("INSERT INTO apikeys(verifier, email, domainFilter, description) VALUES('" + apiKey
				+ "', '" + user.getActiveUserID() + "', '" + domainFilter + "', '" + description + "')");
		Method.log_statistic("E5D9904F0A8B4F99", "ADDAPIKEY", user.getActiveUserID() + apiKey);
		ObjectNode obj = Json.newObject();
		obj.put("status", "ok");
		obj.put("verifier", apiKey);
		return obj;
	}

	public void updateApiKey(User user, String apiKey, String domainFilter, String description)
			throws IOException, SQLException {
		this.checkPrivilege(user.isPrivilegeApiUsage());
		java.sql.Connection connection = DB.getConnection();
		Statement statement = connection.createStatement();
		ResultSet result = statement.executeQuery("SELECT email FROM apikeys WHERE verifier='" + apiKey + "'");
		while (result.next()) {
			if (!result.getString("email").equals(user.getActiveUserID())) {
				Method.die_nice(
						"User " + user.getActiveUserID() + " does not have privilege to update API Key " + apiKey + "");
			}
		}
		statement.executeUpdate("UPDATE apikeys SET domainFilter='"+domainFilter+"', description='"+description+"' WHERE verifier='"+apiKey+"'");
	}

	private void checkPrivilege(boolean privilegeApiUsage) throws IOException {
		if (!privilegeApiUsage) {
			Method.die_nice("User doesn't have enough privilege to perform the action.");
		}
	}

}