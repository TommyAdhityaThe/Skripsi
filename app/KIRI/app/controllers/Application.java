package controllers;

//default import
import play.*;
import play.mvc.*;
import views.html.*;

//tambahan import
import play.data.*;
import play.db.*;
import java.io.*;
import java.sql.*;
import java.util.Random;

//untuk json
import play.libs.Json;
import com.fasterxml.jackson.databind.node.ObjectNode;

//model yang dipake
import models.Message;
import models.RegisterManager;
import models.LoginManager;

public class Application extends Controller {
	DynamicForm requestData;
    Message message;
    

	public Result index(){
		return redirect("/bukitjarian/");
	}

    public Result pagenotfound(String other){
        return notFound("<h1>"+other+" not found</h1>").as("text/html");
    }

    public Result testingDB() throws IOException, SQLException{
    	java.sql.Connection connection = DB.getConnection();
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery("select * from users;");
        StringBuilder sb = new StringBuilder();
        sb.append("LIST USERs Tirtayasa"+"\n");
        while (result.next()) {
            sb.append("userid: "+ result.getString("email") + "\npassword: " + result.getString("password")+"\n");
        }
        return ok(sb.toString());
    }


    public Result handle() throws IOException, SQLException{
    	this.requestData = Form.form().bindFromRequest();
    	String mode = this.requestData.get("mode");
        if(!mode.equals("login") && !mode.equals("register") && !mode.equals("logout")){
            this.checkLogin();
        }
        switch(mode){
            case "login":
                message = this.login();
                break;
            case "register":
                message = this.register();
                break;
            case "listtracks":
                
                break;
            case "listapikeys":
                
                break;
            default:
                ObjectNode obj = Json.newObject();
                obj.put("status", "mode not found");
                this.message=new Message("badrequest", obj);
                break;
       }
       switch(message.getHttpcode()){
            case "ok":
                return ok(message.getAlert());
            case "badrequest":
                return badRequest(message.getAlert());
            default:
                return badRequest("ERROR");

       }
    }

    private Message login() throws IOException, SQLException{
        String userid = this.requestData.get("userid");
        String password = this.requestData.get("password");
        LoginManager manager = new LoginManager();
        return manager.doLogin(userid,password);
    }

    private Message register() throws IOException, SQLException{
        String email = this.requestData.get("userid");
        String fullname = this.requestData.get("fullname");
        String company = this.requestData.get("company");
        RegisterManager manager = new RegisterManager();
        return manager.doRegister(email,fullname,company);
    }

    private Result checkLogin(){
        return ok("To be Created");

    }

    private Result getListTracks(){
        return ok("To be Created");

    }

    private Result getListApiKeys(){
        return ok("To be Created");

    }

    /**
    private Result cobaLogin() throws IOException, SQLException{
        PengelolaLogin pengelola= new PengelolaLogin();
        String userid = this.requestData.get("userid");
        String password = this.requestData.get("password");
        return pengelola.lakukanLogin(userid,password);
    }
    */

    

    private Result oldLogin() throws IOException, SQLException{
    	String userid = this.requestData.get("userid");
    	String password = this.requestData.get("password");
    	if (userid.length() > 128) {
			return badRequest(this.return_invalid_credentials("User ID length is more than allowed (" + userid.length() + ")"));
		}
		if (password.length() > 32) {
			return badRequest(this.return_invalid_credentials("Password length is more than allowed ("+ password.length() + ")"));
		}

        // Retrieve the user information
        java.sql.Connection connection = DB.getConnection();
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM users WHERE email='"+userid+"'");
        if(!result.next()){
            System.out.println("userid tidak ditemukan");
            return badRequest(this.return_invalid_credentials(null));
        }

        String hasher=result.getString("password");
        if(!hasher.equals(password)){
            System.out.println("password salah");
            return badRequest(this.return_invalid_credentials(null));
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
        System.out.println(obj);
        return ok(obj);
    }
    
    private Result oldRegister() throws IOException, SQLException{
        String email = this.requestData.get("userid");
        String fullname = this.requestData.get("fullname");
        String company = this.requestData.get("company");

        // Check if the email has already been registered.
        java.sql.Connection connection = DB.getConnection();
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery("SELECT email FROM users WHERE email='"+email+"'");
        while (result.next()) {
            return badRequest("udah ada usernya");
        }

        // Generate password tanpa fitur hash and send password (belum dilakukan)
        String password = this.generate_password();
        statement.executeUpdate("INSERT INTO users(email, password, privilegeApiUsage, fullName, company) VALUES('" + email + "', '" + password +"', 1, '" + fullname +"', '" + company +"');");
        

        return ok(this.well_done(null));
    }


    private String generate_password(){
        return this.generate_random("abcdefghiklmnopqrstuvwxyz0123456789", 8);
    }
    
    private String generate_random(String chars, int length){
        int chars_size = chars.length();
        Random random=new Random();
        String string = "";
        for (int i = 0; i < length; i++) {
            string += chars.charAt(random.nextInt(chars_size));
        }
        return string;
    }

    private ObjectNode well_done(String message) {
        ObjectNode obj = Json.newObject();
        obj.put("status", "ok");
        if (message != null) {
            obj.put("status", message);
        }
        return obj;
    }

    public ObjectNode return_invalid_credentials(String logmessage) {
        ObjectNode obj = Json.newObject();
        obj.put("status", "credentialfail");
        return obj;
    }
}
