package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import models.Method;
import models.TracksManager;
import models.User;
import models.ApiKeysManager;
import models.AuthenticationManager;
import models.UniqueStatusError;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

/**
 * Kelas ini untuk menangani permintaan-permintaan dari bagian View
 * 
 * @author Tommy Adhitya The
 */
public class Application extends Controller {
	public Result index() {
		return redirect("/bukitjarian/");
	}

	public Result pagenotfound(String other) {
		return notFound("<h1>" + other + " not found</h1>").as("text/html");
	}

	public Result handle() {
		ObjectNode response;
		try {
			User user = null;
			DynamicForm requestData = Form.form().bindFromRequest();
			String mode = requestData.get("mode");
			if (!mode.equals("login") && !mode.equals("register") && !mode.equals("logout")) {
				user = new User(requestData.get("sessionid"));
			}
			switch (mode) {
			case "login":
				response = this.login(requestData.get("userid"), requestData.get("password"));
				break;
			case "register":
				response = this.register(requestData.get("userid"), requestData.get("fullname"),
						requestData.get("company"));
				break;
			case "logout":
				response = this.logout(requestData.get("sessionid"));
				break;
			case "getprofile":
				response = this.getProfile(user);
				break;
			case "updateprofile":
				response = this.updateProfile(user, requestData.get("password"), requestData.get("fullname"),
						requestData.get("company"));
				break;
			case "listapikeys":
				response = this.getListOfApiKeys(user);
				break;
			case "addapikey":
				response = this.addApiKey(user, requestData.get("domainfilter"), requestData.get("description"));
				break;
			case "updateapikey":
				response = this.updateApiKey(user, requestData.get("verifier"), requestData.get("domainfilter"),
						requestData.get("description"));
				break;
			case "listtracks":
				response = this.getListOfTracks(user);
				break;
			case "getdetailstrack":
				response = this.getDetailsTrack(user, requestData.get("trackid"));
				break;
			// case "updatetrack":
			// response =
			// this.updateTrack(user,requestData.get("trackid"),requestData.get("newtrackid"),requestData.get("tracktype"),requestData.get("trackname"),requestData.get("internalinfo"),requestData.get("loop"),requestData.get("penalty"),requestData.get("transfernodes"));
			// break;
			default:
				throw new IOException("Mode Not Found");
			}
			return ok(response);
		} catch (UniqueStatusError e) {
			response = Json.newObject();
			response.put("status", e.getStatus());
			return badRequest(response);
		} catch (Exception e) {
			response = Json.newObject();
			response.put("status", "error");
			response.put("message", e.getMessage());
			return badRequest(response);
		}
	}

	private ObjectNode login(String userid, String password) throws UniqueStatusError, IOException, SQLException,
			NoSuchAlgorithmException, UnsupportedEncodingException {
		AuthenticationManager manager = new AuthenticationManager();
		return manager.login(userid, password);
	}

	private ObjectNode register(String email, String fullname, String company) throws IOException, SQLException,
			NoSuchAlgorithmException, UnsupportedEncodingException, AddressException, MessagingException {
		AuthenticationManager manager = new AuthenticationManager();
		return manager.register(email, fullname, company);
	}

	private ObjectNode logout(String sessionid) throws IOException, SQLException {
		AuthenticationManager manager = new AuthenticationManager();
		manager.logout(sessionid);
		return Method.well_done(null);
	}

	private ObjectNode getProfile(User user) throws IOException, SQLException {
		return user.getProfile();
	}

	private ObjectNode updateProfile(User user, String newPassword, String newFullName, String newCompany)
			throws NoSuchAlgorithmException, SQLException, UnsupportedEncodingException {
		user.updateProfile(newPassword, newFullName, newFullName);
		return Method.well_done(null);
	}

	private ObjectNode getListOfApiKeys(User user) throws SQLException, IOException {
		ApiKeysManager manager = new ApiKeysManager();
		return manager.getListOfApiKeys(user);
	}

	private ObjectNode addApiKey(User user, String domainFilter, String description) throws SQLException, IOException {
		ApiKeysManager manager = new ApiKeysManager();
		return manager.addApiKey(user, domainFilter, description);
	}

	private ObjectNode updateApiKey(User user, String apiKey, String domainFilter, String description)
			throws IOException, SQLException {
		ApiKeysManager manager = new ApiKeysManager();
		manager.updateApiKey(user, apiKey, domainFilter, description);
		return Method.well_done(null);
	}

	private ObjectNode getListOfTracks(User user) throws SQLException, IOException {
		TracksManager manager = new TracksManager();
		return manager.getListOfTracks(user);
	}

	private ObjectNode getDetailsTrack(User user, String trackID) throws IOException, SQLException {
		TracksManager manager = new TracksManager();
		return manager.getDetailsTrack(user, trackID);
	}
}
