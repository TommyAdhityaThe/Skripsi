package models;

import java.util.Random;
import play.libs.Json;
import play.mvc.Http;
import java.io.*;   
import java.sql.*;
import play.db.*;
import play.Logger;
import java.util.Date;
import com.fasterxml.jackson.databind.node.ObjectNode;

//for hashing SHA-256
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//for sending email
import java.util.Date;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

public class Method{
    public static String generate_sessionid(){
        return generate_random("abcdefghiklmnopqrstuvwxyz0123456789", 16);
    }

	public static String generate_password(){
        return generate_random("abcdefghiklmnopqrstuvwxyz0123456789", 8);
    }
    
    private static String generate_random(String chars, int length){
        int chars_size = chars.length();
        Random random=new Random();
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
        String ipAddress=Http.Context.current().request().remoteAddress();
        log_error("Login failed (IP="+ipAddress+"): " + logMessage);
        throw new UniqueStatusError("credentialfail");
    }

    public static void die_nice(String message) throws IOException {
        throw new IOException(message);
    }

    private static void log_error(String message){
        Date date = new Date();
        String time = date.toString();
        Logger.error("time="+time+";message="+message+";\n");
    }

    public static void log_statistic(String verifier, String type, String additional_info) throws SQLException{
        java.sql.Connection connection = DB.getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate("INSERT INTO statistics (verifier, type, additionalInfo) VALUES ('"+verifier+"','"+type+"','"+additional_info+"')");
    }

    public static String hashingPassword(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException{
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(password.getBytes("UTF-8")); // Change this to "UTF-16" if needed
        byte[] digest = md.digest();
        return String.format("%064x", new java.math.BigInteger(1, digest));
    }

    public static void sendPassword(String email, String password, String fullname){
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        Session session = Session.getInstance(props, null);

        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom("toms.warior@gmail.com");
            msg.setRecipients(Message.RecipientType.TO,
                    "7312031@student.unpar.ac.id");
            msg.setSubject("JavaMail hello world example");
            msg.setSentDate(new Date());
            msg.setText("Hello, world!\n");
            Transport.send(msg, "toms.warior@gmail.com", "");
        } catch (MessagingException mex) {
            System.out.println("send failed, exception: " + mex);
        }

    }
}