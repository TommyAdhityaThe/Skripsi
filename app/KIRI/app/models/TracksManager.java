package models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
		PreparedStatement pstmt = connection.prepareStatement("SELECT trackId FROM tracks WHERE trackId=?");
		pstmt.setString(1, trackID);
		ResultSet result = pstmt.executeQuery();
		if (result.next()) {
			connection.close();
			Utils.dieNice("The trackId '" + trackID + "' already existed.");
		}
		pstmt = connection.prepareStatement(
				"INSERT INTO tracks (trackId, trackTypeId, trackName, penalty, internalInfo) VALUES (?,?,?,?,?)");
		pstmt.setString(1, trackID);
		pstmt.setString(2, trackType);
		pstmt.setString(3, trackName);
		pstmt.setString(4, penalty);
		pstmt.setString(5, internalInfo);
		pstmt.executeUpdate();
		this.updateTrackVersion();
		connection.close();
	}

	public ObjectNode getListOfTracks(User user) throws SQLException, IOException {
		this.checkPrivilege(user.isPrivilegeRoute());
		java.sql.Connection connection = DB.getConnection();
		PreparedStatement pstmt = connection
				.prepareStatement("SELECT trackTypeId, trackId, trackName FROM tracks ORDER BY trackTypeId, trackId");
		ResultSet result = pstmt.executeQuery();
		ArrayNode trackList = Json.newArray();
		while (result.next()) {
			ArrayNode track = Json.newArray();
			track.add(result.getString(2));
			track.add(result.getString(1) + "/" + result.getString(3));
			trackList.add(track);
		}
		pstmt = connection.prepareStatement("SELECT trackTypeId, name FROM tracktypes ORDER BY trackTypeId");
		result = pstmt.executeQuery();
		ArrayNode trackTypeList = Json.newArray();
		while (result.next()) {
			ArrayNode trackType = Json.newArray();
			trackType.add(result.getString(1));
			trackType.add(result.getString(2));
			trackTypeList.add(trackType);
		}
		ObjectNode obj = Json.newObject();
		obj.put(Constant.PROTO_STATUS, Constant.PROTO_STATUS_OK);
		obj.putArray(Constant.PROTO_TRACKS_LIST).addAll(trackList);
		obj.putArray(Constant.PROTO_TRACK_TYPES_LIST).addAll(trackTypeList);
		connection.close();
		return obj;
	}

	public ObjectNode getDetailsTrack(User user, String trackID) throws IOException, SQLException {
		this.checkPrivilege(user.isPrivilegeRoute());
		java.sql.Connection connection = DB.getConnection();
		PreparedStatement pstmt = connection.prepareStatement(
				"SELECT trackTypeId, trackName, internalInfo, AsText(geodata), pathloop, penalty, transferNodes FROM tracks WHERE trackId=?");
		pstmt.setString(1, trackID);
		ResultSet result = pstmt.executeQuery();
		if (!result.next()) {
			connection.close();
			Utils.dieNice("Can't find track information for '" + trackID + "'");
		}
		ArrayNode geoData = this.lineStringToLatLngArray(result.getString(4));
		ObjectNode obj = Json.newObject();
		obj.put(Constant.PROTO_STATUS, Constant.PROTO_STATUS_OK);
		obj.put(Constant.PROTO_TRACK_ID, trackID);
		obj.put(Constant.PROTO_TRACK_TYPE, result.getString(1));
		obj.put(Constant.PROTO_TRACK_NAME, result.getString(2));
		obj.put(Constant.PROTO_INTERNAL_INFO, result.getString(3));
		if (result.getString(4) == null) {
			obj.putNull(Constant.PROTO_GEO_DATA);
		} else {
			obj.putArray(Constant.PROTO_GEO_DATA).addAll(geoData);
		}
		obj.put(Constant.PROTO_PATH_LOOP, (result.getInt(5) > 0 ? true : false));
		obj.put(Constant.PROTO_PENALTY, result.getDouble(6));
		ArrayNode transferNodes = Json.newArray();
		if (result.getString(7) == null) {
			transferNodes.add("0-" + (geoData.size() - 1));
		} else {
			String[] temp = result.getString(7).split(",");
			for (int i = 0; i < temp.length; i++) {
				transferNodes.add(temp[i]);
			}
		}
		obj.putArray(Constant.PROTO_TRANSFER_NODES).addAll(transferNodes);
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
		PreparedStatement pstmt;
		ResultSet result;
		if (!newTrackID.equals(trackID)) {
			pstmt = connection.prepareStatement("SELECT trackId FROM tracks WHERE trackId=?");
			pstmt.setString(1, newTrackID);
			result = pstmt.executeQuery();
			if (result.next()) {
				connection.close();
				Utils.dieNice("The new trackId '" + newTrackID + "' already existed.");
			}
		}
		pstmt = connection.prepareStatement(
				"UPDATE tracks SET trackTypeId=?, trackId=?, trackName=?, internalInfo=?, pathloop=?, penalty=? WHERE trackId=?");
		pstmt.setString(1, trackType);
		pstmt.setString(2, newTrackID);
		pstmt.setString(3, trackName);
		pstmt.setString(4, internalInfo);
		pstmt.setInt(5, pathLoop);
		pstmt.setString(6, penalty);
		pstmt.setString(7, trackID);
		pstmt.executeUpdate();
		if (!transferNodes.equals("")) {
			pstmt = connection.prepareStatement("UPDATE tracks SET transferNodes=? WHERE trackId=?");
			pstmt.setString(1, transferNodes);
			pstmt.setString(2, trackID);
			pstmt.executeUpdate();
		}
		this.updateTrackVersion();
		connection.close();
	}

	public void deleteTrack(User user, String trackID) throws IOException, SQLException {
		this.checkPrivilege(user.isPrivilegeRoute());
		java.sql.Connection connection = DB.getConnection();
		PreparedStatement pstmt = connection.prepareStatement("DELETE FROM tracks WHERE trackId=?");
		pstmt.setString(1, trackID);
		if (pstmt.executeUpdate() == 0) {
			connection.close();
			Utils.dieNice("The track " + trackID + " was not found in the database");
		}
		this.updateTrackVersion();
		connection.close();
	}

	public void clearGeoData(User user, String trackID) throws IOException, SQLException {
		this.checkPrivilege(user.isPrivilegeRoute());
		java.sql.Connection connection = DB.getConnection();

		PreparedStatement pstmt = connection
				.prepareStatement("UPDATE tracks SET geodata=NULL, transferNodes=NULL WHERE trackId=?");
		pstmt.setString(1, trackID);
		pstmt.executeUpdate();
		connection.close();
	}

	// belum beress
	public void importKML(User user, String trackID, File dataKML) throws IOException, SQLException {
		this.checkPrivilege(user.isPrivilegeRoute());
		if (dataKML.length() > Constant.MAX_FILE_SIZE) {
			Utils.dieNice("Uploaded file size is greater than maximum size allowed (" + Constant.MAX_FILE_SIZE + ")");
		}
		BufferedReader br = null;
		br = new BufferedReader(new FileReader(dataKML));
		String line;
		StringBuilder hayStack = new StringBuilder();
		while ((line = br.readLine()) != null) {
			hayStack.append(line.trim());
		}
		br.close();
		Pattern patt = Pattern.compile(
			"(?i)<LineString>.*?<coordinates>(.*?)</coordinates>.*?</LineString>");
		Matcher match = patt.matcher(hayStack.toString());
		int numMatches = 0;
		while (match.find()) {
			numMatches++;
		}
		if (numMatches != 1) {
			Utils.dieNice(
					"The KML file must contain exactly one <coordinate> 
					tag inside one <LineString> tag. But I found "
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
		PreparedStatement pstmt = connection
				.prepareStatement("UPDATE tracks SET geodata=GeomFromText(?), transferNodes=NULL WHERE trackId=?");
		pstmt.setString(1, output.toString());
		pstmt.setString(2, trackID);
		pstmt.executeUpdate();
		connection.close();
		this.updateTrackVersion();
	}

	private void updateTrackVersion() throws SQLException {
		java.sql.Connection connection = DB.getConnection();
		PreparedStatement pstmt = connection.prepareStatement(
				"UPDATE properties SET propertyvalue=propertyvalue+1 WHERE propertyname='trackversion'");
		pstmt.executeUpdate();
		connection.close();
	}

	private void checkPrivilege(boolean privilegeRoute) throws IOException {
		if (!privilegeRoute) {
			Utils.dieNice("User doesn't have enough privilege to perform the action.");
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
