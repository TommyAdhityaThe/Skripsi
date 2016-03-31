package models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringEscapeUtils;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.db.DB;
import play.libs.Json;

/**
 * Kelas ini untuk menangani kasus: pengelolaan rute angkutan umum KIRI
 * 
 * @author Tommy Adhitya The
 */
public class TracksManager {

	public void addTrack(User user, String trackID, String trackName, String trackType, String penalty,
			String internalInfo) throws IOException, SQLException {
		this.checkPrivilege(user.isPrivilegeRoute());
		java.sql.Connection connection = DB.getConnection();
		Statement statement = connection.createStatement();
		ResultSet result = statement.executeQuery("SELECT trackId FROM tracks WHERE trackId='" + trackID + "'");
		if (result.next()) {
			connection.close();
			Method.dieNice("The trackId '" + trackID + "' already existed.");
		}
		statement.executeUpdate("INSERT INTO tracks (trackId, trackTypeId, trackName, penalty, internalInfo) VALUES ('"
				+ trackID + "','" + trackType + "','" + trackName + "','" + penalty + "','" + internalInfo + "')");
		this.updateTrackVersion();
		connection.close();
	}

	public ObjectNode getListOfTracks(User user) throws SQLException, IOException {
		this.checkPrivilege(user.isPrivilegeRoute());
		java.sql.Connection connection = DB.getConnection();
		Statement statement = connection.createStatement();
		ResultSet result = statement
				.executeQuery("SELECT trackTypeId, trackId, trackName FROM tracks ORDER BY trackTypeId, trackId");
		ArrayNode trackList = Json.newArray();
		while (result.next()) {
			ArrayNode track = Json.newArray();
			track.add(result.getString(2));
			track.add(StringEscapeUtils.escapeHtml4(result.getString(1) + "/" + result.getString(3)));
			trackList.add(track);
		}
		result = statement.executeQuery("SELECT trackTypeId, name FROM tracktypes ORDER BY trackTypeId");
		ArrayNode trackTypeList = Json.newArray();
		while (result.next()) {
			ArrayNode trackType = Json.newArray();
			trackType.add(result.getString(1));
			trackType.add(StringEscapeUtils.escapeHtml4(result.getString(2)));
			trackTypeList.add(trackType);
		}
		ObjectNode obj = Json.newObject();
		obj.put("status", "ok");
		obj.putArray("trackslist").addAll(trackList);
		obj.putArray("tracktypeslist").addAll(trackTypeList);
		connection.close();
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
			connection.close();
			Method.dieNice("Can't find track information for '" + trackID + "'");
		}
		ArrayNode geoData = this.lineStringToLatLngArray(result.getString(4));
		ObjectNode obj = Json.newObject();
		obj.put("status", "ok");
		obj.put("trackid", trackID);
		obj.put("tracktype", result.getString(1));
		obj.put("trackname", result.getString(2));
		obj.put("internalinfo", result.getString(3));
		obj.putArray("geodata").addAll(geoData); //tidak bisa menggassign nilai ke null
		obj.put("loop", (result.getInt(5) > 0 ? true : false));
		obj.put("penalty", result.getDouble(6));
		ArrayNode transferNodes = Json.newArray();
		if (result.getString(7) == null) {
			transferNodes.add("0-" + (geoData.size() - 1));
		} else {
			String[] temp = result.getString(7).split(",");
			for (int i = 0; i < temp.length; i++) {
				transferNodes.add(temp[i]);
			}
		}
		obj.putArray("transfernodes").addAll(transferNodes);
		connection.close();
		return obj;
	}

	public void updateTrack(User user, String trackID, String newTrackID, String trackType, String trackName,
			String internalInfo, String loop, String penalty, String transferNodes) throws SQLException, IOException {
		this.checkPrivilege(user.isPrivilegeRoute());
		int pathLoop = 0;
		if (loop.equals("true")) {
			pathLoop = 1;
		}
		java.sql.Connection connection = DB.getConnection();
		Statement statement = connection.createStatement();
		ResultSet result;
		if (!newTrackID.equals(trackID)) {
			result = statement.executeQuery("SELECT trackId FROM tracks WHERE trackId='" + newTrackID + "'");
			if (result.next()) {
				connection.close();
				Method.dieNice("The new trackId '" + newTrackID + "' already existed.");
			}
		}
		statement.executeUpdate("UPDATE tracks SET trackTypeId='" + trackType + "', trackId='" + newTrackID
				+ "', trackName='" + trackName + "', internalInfo='" + internalInfo + "', pathloop='" + pathLoop
				+ "', penalty='" + penalty + "' WHERE trackId='" + trackID + "'");
		if (!transferNodes.equals("")) {
			statement.executeUpdate(
					"UPDATE tracks SET transferNodes='" + transferNodes + "' WHERE trackId='" + trackID + "'");
		}
		this.updateTrackVersion();
		connection.close();
	}

	public void deleteTrack(User user, String trackID) throws IOException, SQLException {
		this.checkPrivilege(user.isPrivilegeRoute());
		java.sql.Connection connection = DB.getConnection();
		Statement statement = connection.createStatement();
		if (statement.executeUpdate("DELETE FROM tracks WHERE trackId='" + trackID + "'") == 0) {
			connection.close();
			Method.dieNice("The track " + trackID + " was not found in the database");
		}
		this.updateTrackVersion();
		connection.close();
	}

	public void clearGeoData(User user, String trackID) throws IOException, SQLException {
		this.checkPrivilege(user.isPrivilegeRoute());
		java.sql.Connection connection = DB.getConnection();
		Statement statement = connection.createStatement();
		statement.executeUpdate("UPDATE tracks SET geodata=NULL, transferNodes=NULL WHERE trackId='" + trackID + "'");
		connection.close();
	}

	// belum beress
	public void importKML(User user, String trackID, File dataKML) throws IOException, SQLException {
		this.checkPrivilege(user.isPrivilegeRoute());
		if (dataKML.length() > Constant.MAX_FILE_SIZE) {
			Method.dieNice("Uploaded file size is greater than maximum size allowed (" + Constant.MAX_FILE_SIZE + ")");
		}
		BufferedReader br = null;
		br = new BufferedReader(new FileReader(dataKML));
		String line;
		StringBuilder hayStack = new StringBuilder();
		while ((line = br.readLine()) != null) {
			hayStack.append(line.trim());
		}
		br.close();
		Pattern patt = Pattern.compile("(?i)<LineString>.*?<coordinates>(.*?)</coordinates>.*?</LineString>");
		Matcher match = patt.matcher(hayStack.toString());
		int numMatches = 0;
		while (match.find()) {
			numMatches++;
		}
		if (numMatches != 1) {
			Method.dieNice(
					"The KML file must contain exactly one <coordinate> tag inside one <LineString> tag. But I found "
							+ numMatches + " occurences");
		}
		match.reset();
		match.find();
		StringBuilder output = new StringBuilder("LINESTRING(");
		String[] points = match.group(1).split("\\s+");
		int i = 0;
		for (String string : points) {
			String splitter[] = string.split(",");
			if (i != 0) {
				output.append(",");
			}
			output.append(splitter[0]);
			if (!splitter[0].equals("")) {
				output.append(" " + splitter[1]);
				i++;
			}
		}
		output.append(")");
		java.sql.Connection connection = DB.getConnection();
		Statement statement = connection.createStatement();
		statement.executeUpdate("UPDATE tracks SET geodata=GeomFromText('" + output.toString()
				+ "'), transferNodes=NULL WHERE trackId='" + trackID + "'");
		connection.close();
		this.updateTrackVersion();
	}

	private void updateTrackVersion() throws SQLException {
		java.sql.Connection connection = DB.getConnection();
		Statement statement = connection.createStatement();
		statement
				.executeUpdate("UPDATE properties SET propertyvalue=propertyvalue+1 WHERE propertyname='trackversion'");
		connection.close();
	}

	private void checkPrivilege(boolean privilegeRoute) throws IOException {
		if (!privilegeRoute) {
			Method.dieNice("User doesn't have enough privilege to perform the action.");
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
