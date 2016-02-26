package controllers;

//default import
import play.*;
import play.mvc.*;
import views.html.*;

//models
import models.Method;
import models.User;
import models.UserManager;
import models.AuthenticationManager;
import models.UniqueStatusError;

//tambahan import
import play.data.*;
import play.db.*;
import java.io.*;
import java.sql.*;
import java.util.Random;

//untuk json
import play.libs.Json;
import com.fasterxml.jackson.databind.node.ObjectNode;

//exception
import java.security.NoSuchAlgorithmException;
import java.io.UnsupportedEncodingException;


public class Application extends Controller {
	public Result index(){
		return redirect("/bukitjarian/");
	}

    public Result pagenotfound(String other){
        return notFound("<h1>"+other+" not found</h1>").as("text/html");
    }

    public Result handle(){
        ObjectNode response;
        try {
            User user=null;
            DynamicForm requestData=Form.form().bindFromRequest();
            String mode = requestData.get("mode");
            if(!mode.equals("login") && !mode.equals("register") && !mode.equals("logout")){
                user = new User(requestData.get("sessionid"));
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
                case "getprofile":
                    response=this.getProfile(user);
                    break;
                case "updateprofile":
                    response=this.updateProfile(user,requestData.get("password"),requestData.get("fullname"),requestData.get("company"));
                    break;
                default:
                    throw new IOException("Mode Not Found");
            }
            return ok(response);
        } 
        catch (UniqueStatusError e){
            response=Json.newObject();
            response.put("status",e.getStatus());
            return badRequest(response);
        }
        catch (Exception e) {
            response=Json.newObject();
            response.put("status","error");
            response.put("message",e.getMessage());
            return badRequest(response);
        }
    }

    private ObjectNode login(String userid, String password) throws UniqueStatusError,IOException, SQLException,NoSuchAlgorithmException, UnsupportedEncodingException{
        AuthenticationManager manager = new AuthenticationManager();
        return manager.login(userid,password);
    }

    private ObjectNode register(String email, String fullname, String company) throws IOException, SQLException,NoSuchAlgorithmException, UnsupportedEncodingException{
        AuthenticationManager manager = new AuthenticationManager();
        return manager.register(email,fullname,company);
    }

    private ObjectNode logout(String sessionid) throws IOException, SQLException{
        AuthenticationManager manager = new AuthenticationManager();
        manager.logout(sessionid);
        return Method.well_done(null);
    }

    private ObjectNode getProfile(User user) throws IOException, SQLException{
        UserManager manager = new UserManager();
        return manager.getProfile(user);
    }

    private ObjectNode updateProfile(User user,String newPassword, String newFullName, String newCompany) throws NoSuchAlgorithmException, SQLException, UnsupportedEncodingException{
        UserManager manager = new UserManager();
        manager.updateProfile(user,newPassword,newFullName,newFullName);
        return Method.well_done(null);
    }
}
