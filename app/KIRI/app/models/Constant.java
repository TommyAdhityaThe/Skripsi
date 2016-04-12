package models;

public class Constant {
	public final static String APIKEY_KIRI = "E5D9904F0A8B4F99";

	public final static String PROTO_MODE = "mode";
	public final static String PROTO_MODE_LOGIN = "login";
	public final static String PROTO_MODE_REGISTER="register";
	public final static String PROTO_MODE_LOGOUT="logout";
	public final static String PROTO_MODE_GET_PROFILE="getprofile";
	public final static String PROTO_MODE_UPDATE_PROFILE="updateprofile";
	public final static String PROTO_MODE_LIST_API_KEYS="listapikeys";
	public final static String PROTO_MODE_ADD_API_KEY="addapikey";
	public final static String PROTO_MODE_UPDATE_API_KEY="updateapikey";
	public final static String PROTO_MODE_LIST_TRACKS="listtracks";
	public final static String PROTO_MODE_GET_DETAILS_TRACK="getdetailstrack";
	public final static String PROTO_MODE_DELETE_TRACK="deletetrack";
	public final static String PROTO_MODE_ADD_TRACK="addtrack";
	public final static String PROTO_MODE_UPDATE_TRACK="updatetrack";
	public final static String PROTO_MODE_CLEAR_GEODATA="cleargeodata";
	public final static String PROTO_MODE_IMPORT_KML="importkml";
	
	public final static String PROTO_STATUS = "status";
	public final static String PROTO_STATUS_OK = "ok";
	public final static String PROTO_FULL_NAME = "fullname";
	public final static String PROTO_COMPANY = "company";
	public final static String PROTO_MESSAGE = "message";
	public final static String PROTO_API_KEYS_LIST = "apikeyslist";
	public final static String PROTO_SESSION_ID = "sessionid";
	public final static String PROTO_PRIVILEGES = "privileges";
	public final static String PROTO_VERIFIER = "verifier";
	public final static String PROTO_TRACK_ID = "trackid";
	public final static String PROTO_TRACKS_LIST = "trackslist";
	public final static String PROTO_TRACK_TYPE = "tracktype";
	public final static String PROTO_TRACK_NAME = "trackname";
	public final static String PROTO_TRACK_TYPES_LIST = "tracktypeslist";
	public final static String PROTO_INTERNAL_INFO = "internalinfo";
	public final static String PROTO_GEO_DATA = "geodata";
	public final static String PROTO_PATH_LOOP = "loop";
	public final static String PROTO_PENALTY = "penalty";
	public final static String PROTO_TRANSFER_NODES = "transfernodes";
	public final static String PROTO_USER_ID="userid";
	public final static String PROTO_PASSWORD="password";
	public final static String PROTO_DOMAIN_FILTER="domainfilter";
	public final static String PROTO_DESCRIPTION="description";
	public final static String PROTO_NEW_TRACK_ID="newtrackid";
	public final static String PROTO_UPLOADED_FILE="uploadedfile";
	
	public final static String ERROR = "error";
	public final static String ERROR_CREDENTIAL_FAIL = "credentialfail";
	public final static String ERROR_SESSION_EXPIRED = "sessionexpired";
	public final static String ERROR_MODE_NOT_FOUND = "Mode Not Found";

	public final static int MAX_FILE_SIZE = 100 * 1024;
}
