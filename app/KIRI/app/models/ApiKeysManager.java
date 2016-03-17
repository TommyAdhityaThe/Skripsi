package models;

import play.db.DB;
import play.libs.Json;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Kelas ini untuk menangani kasus: pengelolaan Api Keys
 * 
 * @author Tommy Adhitya The
 */
public class ApiKeysManager {
	public ObjectNode getListOfApiKeys(User user) throws SQLException, IOException {
		this.checkPrivilege(user.isPrivilegeApiUsage());
		java.sql.Connection connection = DB.getConnection();
		Statement statement = connection.createStatement();
		ResultSet result = statement
				.executeQuery("SELECT verifier, domainFilter, description FROM apikeys WHERE email='"
						+ user.getActiveUserID() + "' ORDER BY verifier");
		ArrayNode listApiKeys = Json.newArray();
		while (result.next()) {
			ArrayNode apiKeyValue = Json.newArray();
			apiKeyValue.add(result.getString(1));
			apiKeyValue.add(result.getString(2));
			apiKeyValue.add(result.getString(3));
			listApiKeys.add(apiKeyValue);
		}
		ObjectNode obj = Json.newObject();
		obj.put("status", "ok");
		obj.putArray("apikeyslist").addAll(listApiKeys);
		connection.close();
		return obj;
	}

	public ObjectNode addApiKey(User user, String domainFilter, String description) throws SQLException, IOException {
		this.checkPrivilege(user.isPrivilegeApiUsage());
		String apiKey = Method.generateApiKey();
		java.sql.Connection connection = DB.getConnection();
		Statement statement = connection.createStatement();
		statement.executeUpdate("INSERT INTO apikeys(verifier, email, domainFilter, description) VALUES('" + apiKey
				+ "', '" + user.getActiveUserID() + "', '" + domainFilter + "', '" + description + "')");
		Method.logStatistic(Constant.APIKEY_KIRI, "ADDAPIKEY", user.getActiveUserID() + apiKey);
		ObjectNode obj = Json.newObject();
		obj.put("status", "ok");
		obj.put("verifier", apiKey);
		connection.close();
		return obj;
	}

	public void updateApiKey(User user, String apiKey, String domainFilter, String description)
			throws IOException, SQLException {
		this.checkPrivilege(user.isPrivilegeApiUsage());
		java.sql.Connection connection = DB.getConnection();
		Statement statement = connection.createStatement();
		ResultSet result = statement.executeQuery("SELECT email FROM apikeys WHERE verifier='" + apiKey + "'");
		while (result.next()) {
			if (!result.getString(1).equals(user.getActiveUserID())) {
				connection.close();
				Method.dieNice(
						"User " + user.getActiveUserID() + " does not have privilege to update API Key " + apiKey + "");
			}
		}
		statement.executeUpdate("UPDATE apikeys SET domainFilter='" + domainFilter + "', description='" + description
				+ "' WHERE verifier='" + apiKey + "'");
		connection.close();
	}

	private void checkPrivilege(boolean privilegeApiUsage) throws IOException {
		if (!privilegeApiUsage) {
			Method.dieNice("User doesn't have enough privilege to perform the action.");
		}
	}

}