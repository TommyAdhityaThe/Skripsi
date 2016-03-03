package models;

import java.util.Random;
import play.libs.Json;
import play.mvc.Http;
import play.Logger;
import play.db.DB;
import java.util.Date;
import java.util.Properties;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Kelas ini untuk membangun: metode-metode pendukung aplikasi KIRI Dashboard
 * 
 * @author Tommy Adhitya The
 */
public class Method {
	public static String generate_sessionid() {
		return generate_random("abcdefghiklmnopqrstuvwxyz0123456789", 16);
	}

	public static String generate_password() {
		return generate_random("abcdefghiklmnopqrstuvwxyz0123456789", 8);
	}

	public static String generateApiKey() {
		return generate_random("01234456789ABCDEF", 16);
	}

	private static String generate_random(String chars, int length) {
		int chars_size = chars.length();
		Random random = new Random();
		String string = "";
		for (int i = 0; i < length; i++) {
			string += chars.charAt(random.nextInt(chars_size));
		}
		return string;
	}

	public static ObjectNode well_done(String message) {
		ObjectNode obj = Json.newObject();
		obj.put("status", "ok");
		if (message != null) {
			obj.put("status", message);
		}
		return obj;
	}

	public static void return_invalid_credentials(String logMessage) throws UniqueStatusError {
		String ipAddress = Http.Context.current().request().remoteAddress();
		log_error("Login failed (IP=" + ipAddress + "): " + logMessage);
		throw new UniqueStatusError("credentialfail");
	}

	public static void die_nice(String message) throws IOException {
		throw new IOException(message);
	}

	private static void log_error(String message) {
		Date date = new Date();
		String time = date.toString();
		Logger.error("time=" + time + ";message=" + message + ";\n");
	}

	public static void log_statistic(String verifier, String type, String additional_info) throws SQLException {
		java.sql.Connection connection = DB.getConnection();
		Statement statement = connection.createStatement();
		statement.executeUpdate("INSERT INTO statistics (verifier, type, additionalInfo) VALUES ('" + verifier + "','"
				+ type + "','" + additional_info + "')");
	}

	public static String hashingPassword(String password)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(password.getBytes("UTF-8"));
		byte[] digest = md.digest();
		return String.format("%064x", new java.math.BigInteger(1, digest));
	}

	public static void sendPassword(String email, String password, String fullname)
			throws AddressException, MessagingException {
		String from = "********";
		String pass = "********";
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
		// buat cek kelengkapan
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
}