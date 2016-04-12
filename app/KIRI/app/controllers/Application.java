package controllers;

import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import models.Utils;
import models.TracksManager;
import models.User;
import models.ApiKeysManager;
import models.AuthenticationManager;
import models.Constant;
import models.UniqueStatusError;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
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
			String mode = requestData.get(Constant.PROTO_MODE);
			if (!mode.equals(Constant.PROTO_MODE_LOGIN) && !mode.equals(Constant.PROTO_MODE_REGISTER)
					&& !mode.equals(Constant.PROTO_MODE_LOGOUT)) {
				user = new User(requestData.get(Constant.PROTO_SESSION_ID));
			}
			switch (mode) {
			case Constant.PROTO_MODE_LOGIN:
				response = this.login(requestData.get(Constant.PROTO_USER_ID),
						requestData.get(Constant.PROTO_PASSWORD));
				break;
			case Constant.PROTO_MODE_REGISTER:
				response = this.register(requestData.get(Constant.PROTO_USER_ID),
						requestData.get(Constant.PROTO_FULL_NAME), requestData.get(Constant.PROTO_COMPANY));
				break;
			case Constant.PROTO_MODE_LOGOUT:
				response = this.logout(requestData.get(Constant.PROTO_SESSION_ID));
				break;
			case Constant.PROTO_MODE_GET_PROFILE:
				response = this.getProfile(user);
				break;
			case Constant.PROTO_MODE_UPDATE_PROFILE:
				response = this.updateProfile(user, requestData.get(Constant.PROTO_PASSWORD),
						requestData.get(Constant.PROTO_FULL_NAME), requestData.get(Constant.PROTO_COMPANY));
				break;
			case Constant.PROTO_MODE_LIST_API_KEYS:
				response = this.getListOfApiKeys(user);
				break;
			case Constant.PROTO_MODE_ADD_API_KEY:
				response = this.addApiKey(user, requestData.get(Constant.PROTO_DOMAIN_FILTER),
						requestData.get(Constant.PROTO_DESCRIPTION));
				break;
			case Constant.PROTO_MODE_UPDATE_API_KEY:
				response = this.updateApiKey(user, requestData.get(Constant.PROTO_VERIFIER),
						requestData.get(Constant.PROTO_DOMAIN_FILTER), requestData.get(Constant.PROTO_DESCRIPTION));
				break;
			case Constant.PROTO_MODE_LIST_TRACKS:
				response = this.getListOfTracks(user);
				break;
			case Constant.PROTO_MODE_GET_DETAILS_TRACK:
				response = this.getDetailsTrack(user, requestData.get(Constant.PROTO_TRACK_ID));
				break;
			case Constant.PROTO_MODE_DELETE_TRACK:
				response = this.deleteTrack(user, requestData.get(Constant.PROTO_TRACK_ID));
				break;
			case Constant.PROTO_MODE_ADD_TRACK:
				response = this.addTrack(user, requestData.get(Constant.PROTO_TRACK_ID),
						requestData.get(Constant.PROTO_TRACK_NAME), requestData.get(Constant.PROTO_TRACK_TYPE),
						requestData.get(Constant.PROTO_PENALTY), requestData.get(Constant.PROTO_INTERNAL_INFO));
				break;
			case Constant.PROTO_MODE_UPDATE_TRACK:
				String transfernodes = requestData.get(Constant.PROTO_TRANSFER_NODES) == null ? ""
						: requestData.get(Constant.PROTO_TRANSFER_NODES);
				String internalinfo = requestData.get(Constant.PROTO_INTERNAL_INFO) == null ? ""
						: requestData.get(Constant.PROTO_INTERNAL_INFO);
				response = this.updateTrack(user, requestData.get(Constant.PROTO_TRACK_ID),
						requestData.get(Constant.PROTO_NEW_TRACK_ID), requestData.get(Constant.PROTO_TRACK_TYPE),
						requestData.get(Constant.PROTO_TRACK_NAME), internalinfo,
						requestData.get(Constant.PROTO_PATH_LOOP), requestData.get(Constant.PROTO_PENALTY),
						transfernodes);
				break;
			case Constant.PROTO_MODE_CLEAR_GEODATA:
				response = this.clearGeoData(user, requestData.get(Constant.PROTO_TRACK_ID));
				break;
			case Constant.PROTO_MODE_IMPORT_KML:
				File dataKML = null;
				MultipartFormData body = request().body().asMultipartFormData();
				FilePart uploadedFile = body.getFile(Constant.PROTO_UPLOADED_FILE);
				if (uploadedFile != null) {
					dataKML = uploadedFile.getFile();
				} else {
					Utils.dieNice("Server script is unable to retrieve the file");
				}
				response = this.importKML(user, requestData.get("trackid"), dataKML);
				break;
			default:
				throw new IOException(Constant.ERROR_MODE_NOT_FOUND);
			}
			return ok(response);
		} catch (UniqueStatusError e) {
			e.printStackTrace();
			response = Json.newObject();
			response.put(Constant.PROTO_STATUS, e.getStatus());
			return badRequest(response);
		} catch (Exception e) {
			e.printStackTrace();
			response = Json.newObject();
			response.put(Constant.PROTO_STATUS, Constant.ERROR);
			response.put(Constant.PROTO_MESSAGE, e.getMessage());
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
		manager.register(email, fullname, company);
		return Utils.wellDone();
	}

	private ObjectNode logout(String sessionid) throws IOException, SQLException {
		AuthenticationManager manager = new AuthenticationManager();
		manager.logout(sessionid);
		return Utils.wellDone();
	}

	private ObjectNode getProfile(User user) throws IOException, SQLException {
		return user.getProfile();
	}

	private ObjectNode updateProfile(User user, String newPassword, String newFullName, String newCompany)
			throws NoSuchAlgorithmException, SQLException, UnsupportedEncodingException {
		user.updateProfile(newPassword, newFullName, newCompany);
		return Utils.wellDone();
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
		return Utils.wellDone();
	}

	private ObjectNode getListOfTracks(User user) throws SQLException, IOException {
		TracksManager manager = new TracksManager();
		return manager.getListOfTracks(user);
	}

	private ObjectNode getDetailsTrack(User user, String trackID) throws IOException, SQLException {
		TracksManager manager = new TracksManager();
		return manager.getDetailsTrack(user, trackID);
	}

	private ObjectNode deleteTrack(User user, String trackID) throws IOException, SQLException {
		TracksManager manager = new TracksManager();
		manager.deleteTrack(user, trackID);
		return Utils.wellDone();
	}

	private ObjectNode addTrack(User user, String trackID, String trackName, String trackType, String penalty,
			String internalInfo) throws IOException, SQLException {
		TracksManager manager = new TracksManager();
		manager.addTrack(user, trackID, trackName, trackType, penalty, internalInfo);
		return Utils.wellDone();
	}

	private ObjectNode updateTrack(User user, String trackID, String newTrackID, String trackType, String trackName,
			String internalInfo, String loop, String penalty, String transferNodes) throws SQLException, IOException {
		TracksManager manager = new TracksManager();
		manager.updateTrack(user, trackID, newTrackID, trackType, trackName, internalInfo, loop, penalty,
				transferNodes);
		return Utils.wellDone();
	}

	private ObjectNode clearGeoData(User user, String trackID) throws IOException, SQLException {
		TracksManager manager = new TracksManager();
		manager.clearGeoData(user, trackID);
		return Utils.wellDone();
	}

	private ObjectNode importKML(User user, String trackID, File dataKML) throws IOException, SQLException {
		TracksManager manager = new TracksManager();
		manager.importKML(user, trackID, dataKML);
		return Utils.wellDone();
	}

}
