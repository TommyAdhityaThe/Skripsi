package models;

import java.util.Random;
import play.libs.Json;
import play.db.DB;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.io.IOException;

/**
 * Kelas ini untuk membangun: metode-metode pendukung aplikasi KIRI Dashboard
 * 
 * @author Tommy Adhitya The
 */
public class Utils {
	public static String generateRandom(String chars, int length) {
		int chars_size = chars.length();
		Random random = new Random();
		String string = "";
		for (int i = 0; i < length; i++) {
			string += chars.charAt(random.nextInt(chars_size));
		}
		return string;
	}

	public static ObjectNode wellDone() {
		ObjectNode obj = Json.newObject();
		obj.put(Constant.PROTO_STATUS, Constant.PROTO_STATUS_OK);
		return obj;
	}

	public static void dieNice(String message) throws IOException {
		throw new IOException(message);
	}

	public static void logStatistic(String verifier, String type, String additional_info) throws SQLException {
		java.sql.Connection connection = DB.getConnection();
		PreparedStatement pstmt = connection
				.prepareStatement("INSERT INTO statistics (verifier, type, additionalInfo) VALUES (?,?,?)");
		pstmt.setString(1, verifier);
		pstmt.setString(2, type);
		pstmt.setString(3, additional_info);
		connection.close();
	}
}