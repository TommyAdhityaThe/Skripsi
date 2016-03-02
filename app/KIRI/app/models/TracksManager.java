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

	public ObjectNode getListOfTracks(User user) throws SQLException, IOException {
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

	public ObjectNode getDetailsTrack(User user, String trackID) throws IOException, SQLException {
		this.checkPrivilege(user.isPrivilegeRoute());
		java.sql.Connection connection = DB.getConnection();
		Statement statement = connection.createStatement();
		ResultSet result = statement.executeQuery(
				"SELECT trackTypeId, trackName, internalInfo, AsText(geodata), pathloop, penalty, transferNodes FROM tracks WHERE trackId='"
						+ trackID + "'");
		if (!result.next()) {
			Method.die_nice("Can't find track information for '" + trackID + "'");
		}
		ArrayNode geoData = this.lineStringToLatLngArray(result.getString("AsText(geodata)"));
		ObjectNode obj = Json.newObject();
		obj.put("status", "ok");
		obj.put("trackid", trackID);
		obj.put("tracktype", result.getString("trackTypeId"));
		obj.put("trackname", result.getString("trackName"));
		obj.put("internalinfo", result.getString("internalInfo"));
		obj.putArray("geodata").addAll(geoData);
		obj.put("loop", (result.getInt("pathloop") > 0 ? true : false));
		obj.put("penalty", result.getDouble("penalty"));
		ArrayNode transferNodes = Json.newArray();
		if (result.getString("transferNodes") == null) {
			transferNodes.add("0-" + (geoData.size() - 1));
		} else {
			String[] temp = result.getString("transferNodes").split(",");
			for (int i = 0; i < temp.length; i++) {
				transferNodes.add(temp[i]);
			}
		}
		obj.putArray("transfernodes").addAll(transferNodes);
		return obj;
	}

	private void checkPrivilege(boolean privilegeRoute) throws IOException {
		if (!privilegeRoute) {
			Method.die_nice("User doesn't have enough privilege to perform the action.");
		}
	}

	private ArrayNode lineStringToLatLngArray(String lineString) {
		if (lineString == null) {
			return Json.newArray();
		}
		lineString = lineString.replaceAll("LINESTRING\\(([^)]+)\\)", "$1");
		String[] lnglatArray = lineString.split(",");
		ArrayNode returnValue = Json.newArray();
		for (String lnglat : lnglatArray) {
			String[] temp = lnglat.split(" ");
			returnValue.add(String.format("%.6f,%.6f", Double.parseDouble(temp[1]), Double.parseDouble(temp[0])));
		}
		return returnValue;
	}

}
