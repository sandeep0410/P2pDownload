
//This class has all the responses that are to be sent to the client by the credential server.
//These constants are used to determine the response for the client.
public class Constants {
 
	public static int RESPONSE_SUCCESS = 200;
	public static int RESPONSE_INVALID_COMMANDS = 400;
	public static int RESPONSE_USERNAME_EXISTS = 401;
	public static int RESONSE_PASSWORD_MISMATCH = 402;
	public static int RESPONSE_INCORRECT_CREDENTIALS = 403;
	public static int REPONSE_FILE_NOT_FOUND = 404;
	public static int RESPONSE_INVALID_ARGUMENTS = 405;
	public static int RESPONSE_NOT_LOGGED_IN = 406;
	public static int RESPONSE_ALREADY_LOGGED_IN = 409;
	public static int CLIENT_NOT_FOUND = 410;
	
}
