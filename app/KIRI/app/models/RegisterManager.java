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
/**
 *
 * @author Tommy Adhitya The
 */
public class RegisterManager extends Method{
    public Message doRegister(String email, String fullname, String company) throws IOException, SQLException{
        java.sql.Connection connection = DB.getConnection();
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery("SELECT email FROM users WHERE email='"+email+"'");
        while (result.next()) {
            return this.well_done("sudah ada usernya");
        }
        // Generate password tanpa fitur hash and send password (belum dilakukan)
        String password = this.generate_password();
        statement.executeUpdate("INSERT INTO users(email, password, privilegeApiUsage, fullName, company) VALUES('" + email + "', '" + password +"', 1, '" + fullname +"', '" + company +"');");
        return this.well_done(null);
    }
}
