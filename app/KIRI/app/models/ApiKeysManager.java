package models;

import play.db.DB;
import play.libs.Json;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
		PreparedStatement pstmt = connection.prepareStatement(
				"SELECT verifier, domainFilter, description FROM apikeys WHERE email=? ORDER BY verifier");
		pstmt.setString(1, user.getActiveUserID());
		ResultSet result = pstmt.executeQuery();
		ArrayNode listApiKeys = Json.newArray();
		while (result.next()) {
			ArrayNode apiKeyValue = Json.newArray();
			apiKeyValue.add(result.getString(1));
			apiKeyValue.add(result.getString(2));
			apiKeyValue.add(result.getString(3));
			listApiKeys.add(apiKeyValue);
		}
		ObjectNode obj = Json.newObject();
		obj.put(Constant.PROTO_STATUS, Constant.PROTO_STATUS_OK);
		obj.putArray(Constant.PROTO_API_KEYS_LIST).addAll(listApiKeys);
		connection.close();
		return obj;
	}

	public ObjectNode addApiKey(User user, String domainFilter, String description) throws SQLException, IOException {
		this.checkPrivilege(user.isPrivilegeApiUsage());
		String apiKey = this.generateApiKey();
		java.sql.Connection connection = DB.getConnection();
		PreparedStatement pstmt = connection
				.prepareStatement("INSERT INTO apikeys(verifier, email, domainFilter, description) VALUES(?,?,?,?)");
		pstmt.setString(1, apiKey);
		pstmt.setString(2, user.getActiveUserID());
		pstmt.setString(3, domainFilter);
		pstmt.setString(4, description);
		pstmt.executeUpdate();
		Utils.logStatistic(Constant.APIKEY_KIRI, "ADDAPIKEY", user.getActiveUserID() + apiKey);
		ObjectNode obj = Json.newObject();
		obj.put(Constant.PROTO_STATUS, Constant.PROTO_STATUS_OK);
		obj.put(Constant.PROTO_VERIFIER, apiKey);
		connection.close();
		return obj;
	}

	public void updateApiKey(User user, String apiKey, String domainFilter, String description)
			throws IOException, SQLException {
		this.checkPrivilege(user.isPrivilegeApiUsage());
		java.sql.Connection connection = DB.getConnection();
		PreparedStatement pstmt = connection.prepareStatement("SELECT email FROM apikeys WHERE verifier=?");
		pstmt.setString(1, apiKey);
		ResultSet result = pstmt.executeQuery();
		while (result.next()) {
			if (!result.getString(1).equals(user.getActiveUserID())) {
				connection.close();
				Utils.dieNice(
						"User " + user.getActiveUserID() + " does not have privilege to update API Key " + apiKey + "");
			}
		}
		pstmt = connection.prepareStatement("UPDATE apikeys SET domainFilter=?, description=? WHERE verifier=?");
		pstmt.setString(1, domainFilter);
		pstmt.setString(2, description);
		pstmt.setString(3, apiKey);
		pstmt.executeUpdate();
		connection.close();
	}

	private void checkPrivilege(boolean privilegeApiUsage) throws IOException {
		if (!privilegeApiUsage) {
			Utils.dieNice("User doesn't have enough privilege to perform the action.");
		}
	}

	private String generateApiKey() {
		return Utils.generateRandom("01234456789ABCDEF", 16);
	}
}