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
import models.AuthenticationManager;


public class Application extends Controller {
	
    
    

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
        ObjectNode response=Json.newObject();
        try {
            DynamicForm requestData=Form.form().bindFromRequest();
            String mode = requestData.get("mode");
            if(!mode.equals("login") && !mode.equals("register") && !mode.equals("logout")){
                
            }
            switch(mode){
                case "login":
                    response=this.login(requestData.get("userid"),requestData.get("password"));
                    break;
                case "register":
                    response=this.register(requestData.get("userid"), requestData.get("fullname"),requestData.get("company"));
                    break;
                case "logout":
                    response=this.logout(requestData.get("sessionid"));
                    break;
                case "listtracks": 
                    break;
                case "listapikeys":
                    break;
                default:
                    throw new IOException("Mode Not Found");
            }
            return ok(response);
        } catch (Exception e) {
            response.put("status",e.getMessage());
            return badRequest(response);
        }
        
       
    }

    private ObjectNode login(String userid, String password) throws IOException, SQLException{
        AuthenticationManager manager = new AuthenticationManager();
        return manager.login(userid,password);
    }

    private ObjectNode register(String email, String fullname, String company) throws IOException, SQLException{
        AuthenticationManager manager = new AuthenticationManager();
        return manager.register(email,fullname,company);
    }

    private ObjectNode logout(String sessionid) throws IOException, SQLException{
        AuthenticationManager manager = new AuthenticationManager();
        return manager.logout(sessionid);
    }
}
