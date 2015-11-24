package controllers;

//default import
import play.*;
import play.mvc.*;
import views.html.*;

//tambahan import
import play.data.*;
import org.json.simple.JSONObject;
import play.db.*;
import java.io.*;
import java.sql.*;

public class Application extends Controller {
	DynamicForm requestData;

	public Result index(){
		return ok(index.render("APLIKASI SIAP!!"));
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

    public Result handle() {
    	this.requestData = Form.form().bindFromRequest();
    	String mode = this.requestData.get("mode");
        if(mode.equals("login")){
        	return this.login();
        }
        if(mode.equals("register")){
        	return this.register();
        }
        else{
        	return badRequest("failed");
        }
    }

    private Result login() {
    	JSONObject obj;
    	String userid = this.requestData.get("userid");
    	String password = this.requestData.get("password");
    	//masih salah belum convert ke JSON
    	if (userid.length() > 128) {
			return badRequest("User ID length is more than allowed (" + userid.length() + ")");
		}
		//masih salah belum convert ke JSON
		if (password.length() > 32) {
			return badRequest("Password length is more than allowed ("+ password.length() + ")");
		}

		//generate dummy langsung berhasil
		obj = new JSONObject();
		obj.put("status", "ok");
        obj.put("sessionid", "e27wy7s3f08fmu13");
        obj.put("privileges", "route,apiusage");
        return ok(obj.toString());
    }
    
    private Result register() {
        return ok("rede");
    }


}
