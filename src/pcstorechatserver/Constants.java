/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pcstorechatserver;

/**
 *
 * @author Pars
 */
abstract public class Constants {

    public static final String CUSTOMER_TYPE_ADMIN = "admin";
    public static final String CUSTOMER_TYPE_USER = "user";
    public static final String CUSTOMER_TYPE_COMPANY = "company";
    public static final String CUSTOMER_TYPE_SERVICE_COMPANY = "service_company";
    public static final String CUSTOMER_TYPE_GUEST = "guest";

    //Receiving Actions
    public static final String REGISTER_ACTION = "register";
    public static final String SEND_MESSAGE_ACTION = "send_message";
    public static final String SESSION_END_ACTION = "end_session";

    //Sending Actions
    public static final String CONNECTION_ERROR_ACTION = "error_connection";
    public static final String CONNECTION_OVER_ACTION = "connection_over";
    
}
