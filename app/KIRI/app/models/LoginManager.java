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

/**
 *
 * @author Tommy Adhitya The
 */
public class LoginManager extends Method{
    public Message doLogin(String userid,String password) throws IOException, SQLException{
        if (userid.length() > 128) {
            return this.return_invalid_credentials("User ID length is more than allowed (" + userid.length() + ")");
        }
        if (password.length() > 32) {
            return this.return_invalid_credentials("Password length is more than allowed ("+ password.length() + ")");
        }

        // Retrieve the user information
        java.sql.Connection connection = DB.getConnection();
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM users WHERE email='"+userid+"'");
        if(!result.next()){
            System.out.println("userid tidak ditemukan");
            return this.return_invalid_credentials("userid tidak ditemukan");
        }

        String hasher=result.getString("password");
        if(!hasher.equals(password)){
            System.out.println("password salah");
            return this.return_invalid_credentials("password salah");
        }
        StringBuilder privileges= new StringBuilder();
        if (result.getInt("privilegeRoute") != 0) {
            privileges.append(",route");
        }
        if (result.getInt("privilegeApiUsage") != 0) {
            privileges.append(",apiusage");
        }
        if (privileges.length() > 0) {
            privileges=new StringBuilder(privileges.substring(1));
        }
        ObjectNode obj = Json.newObject();
        obj.put("status", "ok");
        obj.put("sessionid", "e27wy7s3f08fmu13");
        obj.put("privileges", privileges.toString());
        return new Message("ok", obj);
    }
}
