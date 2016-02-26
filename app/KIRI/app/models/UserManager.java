/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import play.data.*;
import play.db.*;
import java.io.*;
import java.sql.*;
import play.libs.Json;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author Tommy Adhitya The
 */
public class UserManager{
   	public ObjectNode getProfile(User user) throws IOException, SQLException{
   		java.sql.Connection connection = DB.getConnection();
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery("SELECT fullName, company FROM users WHERE email='"+user.getActiveUserID()+"'");
        if (!result.next()) {
            Method.die_nice("User "+user.getActiveUserID()+" not found in database.");
        }
        String fullName=result.getString("fullname");
        String company=result.getString("company");
        ObjectNode obj = Json.newObject();
        obj.put("status", "ok");
        obj.put("fullname", fullName);
        obj.put("company", company);
        return obj;
    }

    public void updateProfile(User user, String newPassword, String newFullName, String newCompany) throws NoSuchAlgorithmException,SQLException,UnsupportedEncodingException{
        java.sql.Connection connection = DB.getConnection();
        Statement statement = connection.createStatement();
        if(!newPassword.equals("")){
            String passwordHash=Method.hashingPassword(newPassword); 
            statement.executeUpdate("UPDATE users SET password='"+passwordHash+"' WHERE email='"+user.getActiveUserID()+"'");    
        }
        statement.executeUpdate("UPDATE users SET fullName='"+newFullName+"', company='"+newCompany+"' WHERE email='"+user.getActiveUserID()+"'");
    }
}
