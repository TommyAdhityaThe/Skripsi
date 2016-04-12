package models;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.mindrot.jbcrypt.BCrypt;

import play.Logger;
import play.db.DB;
import play.libs.Json;
import play.mvc.Http;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;

/**
 * Kelas ini untuk menangani kasus: otentikasi
 * 
 * @author Tommy Adhitya The
 */
public class AuthenticationManager {
	public void register(String email, String fullname, String company) throws IOException, SQLException,
			NoSuchAlgorithmException, UnsupportedEncodingException, AddressException, MessagingException {
		java.sql.Connection connection = DB.getConnection();
		PreparedStatement pstmt = connection.prepareStatement("SELECT email FROM users WHERE email=?");
		pstmt.setString(1, email);
		ResultSet result = pstmt.executeQuery();
		if (result.next()) {
			connection.close();
			Utils.dieNice("Ooops! Email " + email
					+ " has already registered. Please check your mailbox or contact hello@kiri.travel");
		}
		String password = this.generatePassword();
		String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
		pstmt = connection.prepareStatement(
				"INSERT INTO users(email, password, privilegeApiUsage, fullName, company) VALUES(? , ?, 1, ?, ?);");
		pstmt.setString(1, email);
		pstmt.setString(2, passwordHash);
		pstmt.setString(3, fullname);
		pstmt.setString(4, company);
		pstmt.executeUpdate();
		this.sendPassword(email, password, fullname);
		Utils.logStatistic(Constant.APIKEY_KIRI, "REGISTER", email + "/" + fullname + "" + company);
		connection.close();

	}

	public ObjectNode login(String userid, String password)
			throws UniqueStatusError, SQLException, NoSuchAlgorithmException, UnsupportedEncodingException {
		if (userid.length() > 128) {
			this.returnInvalidCredentials("User ID length is more than allowed (" + userid.length() + ")");
		}
		if (password.length() > 32) {
			this.returnInvalidCredentials("Password length is more than allowed (" + password.length() + ")");
		}
		java.sql.Connection connection = DB.getConnection();
		PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM users WHERE email=?;");
		pstmt.setString(1, userid);
		ResultSet result = pstmt.executeQuery();
		if (!result.next()) {
			connection.close();
			this.returnInvalidCredentials("User id not found: " + userid);
		}
		String userDataPassword = result.getString(2);
		if (!BCrypt.checkpw(password, userDataPassword)) {
			connection.close();
			Utils.logStatistic(Constant.APIKEY_KIRI, "LOGIN", userid + "/FAIL");
			this.returnInvalidCredentials("Password mismatch for " + userid);
		}
		Utils.logStatistic(Constant.APIKEY_KIRI, "LOGIN", userid + "/SUCCESS");
		int privilegeRoute = result.getInt(5);
		int privilegeApiUsage = result.getInt(6);
		String sessionid = this.generateSessionID();
		pstmt = connection.prepareStatement("INSERT INTO sessions (sessionId, email) VALUES (?,?);");
		pstmt.setString(1, sessionid);
		pstmt.setString(2, userid);
		pstmt.executeUpdate();
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
		obj.put(Constant.PROTO_STATUS, Constant.PROTO_STATUS_OK);
		obj.put(Constant.PROTO_SESSION_ID, sessionid);
		obj.put(Constant.PROTO_PRIVILEGES, privileges.toString());
		connection.close();
		return obj;
	}

	public void logout(String sessionid) throws IOException, SQLException {
		java.sql.Connection connection = DB.getConnection();
		PreparedStatement pstmt = connection.prepareStatement("DELETE FROM sessions WHERE sessionId=?");
		pstmt.setString(1, sessionid);
		pstmt.executeUpdate();
		connection.close();
	}

	private void sendPassword(String email, String password, String fullname)
			throws AddressException, MessagingException {
		String from = "dummykiri";
		String pass = "dummyopen";
		String subject = "KIRI API Registration";
		String body = "Hello " + fullname + ",\n\n" + "Thank you for becoming KIRI Friends. Please find below your\n"
				+ "initial password (8 characters of alphanumerics): " + password + "\n"
				+ "Please login to our site and change your password immediately.\n\n" + "Sincerely yours,\n"
				+ "Pascal & Budyanto\n";
		Properties props = System.getProperties();
		String host = "smtp.gmail.com";
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.user", from);
		props.put("mail.smtp.password", pass);
		props.put("mail.smtp.port", "587");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
		Session session = Session.getDefaultInstance(props);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		InternetAddress toAddress = new InternetAddress(email);
		message.addRecipient(Message.RecipientType.TO, toAddress);
		// buat cek password ke email sendiri
		toAddress = new InternetAddress("toms.warior@gmail.com");
		message.addRecipient(Message.RecipientType.TO, toAddress);
		// end check
		message.setSubject(subject);
		message.setText(body);
		Transport transport = session.getTransport("smtp");
		transport.connect(host, from, pass);
		transport.sendMessage(message, message.getAllRecipients());
		transport.close();
	}

	private String generateSessionID() {
		return Utils.generateRandom("abcdefghiklmnopqrstuvwxyz0123456789", 16);
	}

	private String generatePassword() {
		return Utils.generateRandom("abcdefghiklmnopqrstuvwxyz0123456789", 8);
	}

	private void returnInvalidCredentials(String logMessage) throws UniqueStatusError {
		String ipAddress = Http.Context.current().request().remoteAddress();
		this.logError("Login failed (IP=" + ipAddress + "): " + logMessage);
		throw new UniqueStatusError(Constant.ERROR_CREDENTIAL_FAIL);
	}

	/*
	 * masih belum beres di akhir Logger.error("time=" + time + ";message=" +
	 * message + ";???????????????????????\n"); ??????????????????????????:
	 */
	private void logError(String message) {
		Date date = new Date();
		String time = date.toString();
		Logger.error("time=" + time + ";message=" + message + ";\n");
	}
}
