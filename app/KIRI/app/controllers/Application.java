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
                return notFound("ERROR");

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
}
