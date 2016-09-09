package com.app.utility;

/**
 * Created by VIJAY on 01-09-2016.
 */
public class ServiceCode {

    public static final int LOGIN = 1;
    public static final int GET_SITES = 2;
    public static final int START_BUTTON = 3;
    public static final int STOP_BUTTON = 6;
    public static final int SEND_LOCATION = 4;
    public static final int LOGOUT = 5;

    //TODO Success code
    //Standard response for successful HTTP requests. The actual response will depend on the request method used
    public static final int OK = 200;

    //The server successfully processed the request and is not returning any content
    public static final int NO_CONTENT = 204 ;

    //TODO Re-direction code
    //This and all future requests should be directed to the given
    public static final int MOVED_PERMANENTLY = 301;

    //TODO Client error
    //The server cannot or will not process the request due to an apparent client error
    public static final int BAD_REQUEST = 400;

    //Similar to 403 Forbidden, but specifically for use when authentication is required and has failed or has not yet been provided
    public static final int UNAUTHORIZED = 401;

    //The request was a valid request, but the server is refusing to respond to it. The user might be logged in but does not have the necessary permissions for the resource.
    public static final int FORBIDDEN = 403;

    //The requested resource could not be found but may be available in the future. Subsequent requests by the client are permissible
    public static final int NOT_FOUND = 404;

    //A request method is not supported for the requested resource
    public static final int METHOD_NOT_ALLOWED = 405;

    //The server timed out waiting for the request. According to HTTP specifications
    public static final int REQUEST_TIMEOUT = 408;

    //TODO Server error
    //A generic error message, given when an unexpected condition was encountered and no more specific message is suitable
    public static final int INTERNAL_SERVER = 500;

    //A generic error message, given when an unexpected condition was encountered and no more specific message is suitable
    public static final int NOT_IMPLEMENTED = 501;

    //The server was acting as a gateway or proxy and received an invalid response from the upstream server
    public static final int BAD_GATEWAY = 502;

    //The server is currently unavailable (because it is overloaded or down for maintenance). Generally, this is a temporary state
    public static final int SERVICE_UNAVAILABLE = 503;
}
