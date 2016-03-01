package models;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.lang3.StringEscapeUtils;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.db.DB;
import play.libs.Json;

public class TracksManager {

	public ObjectNode getListTracks(User user) throws SQLException, IOException {
		this.checkPrivilege(user.isPrivilegeRoute());
		java.sql.Connection connection = DB.getConnection();
		Statement statement = connection.createStatement();
		ResultSet result = statement
				.executeQuery("SELECT trackTypeId, trackId, trackName FROM tracks ORDER BY trackTypeId, trackId");
		ArrayNode trackList = Json.newArray();
		while (result.next()) {
			ArrayNode track = Json.newArray();
			track.add(result.getString("trackId"));
			track.add(StringEscapeUtils
					.escapeHtml4(result.getString("trackTypeId") + "/" + result.getString("trackName")));
			trackList.add(track);
		}
		result = statement.executeQuery("SELECT trackTypeId, name FROM tracktypes ORDER BY trackTypeId");
		ArrayNode trackTypeList = Json.newArray();
		while (result.next()) {
			ArrayNode trackType = Json.newArray();
			trackType.add(result.getString("trackTypeId"));
			trackType.add(StringEscapeUtils.escapeHtml4(result.getString("name")));
			trackTypeList.add(trackType);
		}
		ObjectNode obj = Json.newObject();
		obj.put("status", "ok");
		obj.putArray("trackslist").addAll(trackList);
		obj.putArray("tracktypeslist").addAll(trackTypeList);
		return obj;
	}

	private void checkPrivilege(boolean privilegeRoute) throws IOException {
		if (!privilegeRoute) {
			Method.die_nice("User doesn't have enough privilege to perform the action.");
		}
	}

}
