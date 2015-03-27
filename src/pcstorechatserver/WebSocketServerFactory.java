/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pcstorechatserver;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author Pars
 */
public class WebSocketServerFactory extends WebSocketServer {

    private static Map<Integer, WebSocket> adminWebSokets;
    private static Map<String, Pair<WebSocket, Integer>> customerWebSocketsPair;

    //private final Config conf;
    public WebSocketServerFactory(int port) {
        super(new InetSocketAddress(port));
        adminWebSokets = new HashMap<>();
        customerWebSocketsPair = new HashMap<>();
        //this.conf = Config.getInstance();
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                removeClosedConnections();
            }
        }, 1000, 1000);
    }

    public static void removeClosedConnections() {
        for (Map.Entry<Integer, WebSocket> adminEntrySet : adminWebSokets.entrySet()) {
            Integer adminId = adminEntrySet.getKey();
            WebSocket adminConnection = adminEntrySet.getValue();
            if (adminConnection.isClosed()) {
                sendConnectionErrorToCustomersWhoIsConnectedToAdmin(adminId);
            }
        }
        for (Map.Entry<String, Pair<WebSocket, Integer>> customerEntrySet : customerWebSocketsPair.entrySet()) {
            String customerKey = customerEntrySet.getKey();
            Pair<WebSocket, Integer> customerConnectionAndAdminIdPair = customerEntrySet.getValue();
            WebSocket customerConnection = customerConnectionAndAdminIdPair.getFirst();
            if (customerConnection.isClosed()) {
                sendConnectionErrorToAdminWhoIsConnectedToCustomer(customerKey);
            }
        }
    }

    private static void sendConnectionErrorToCustomersWhoIsConnectedToAdmin(int searchAdminId) {
        for (Map.Entry<String, Pair<WebSocket, Integer>> customerEntrySet : customerWebSocketsPair.entrySet()) {
            String key = customerEntrySet.getKey();
            Pair<WebSocket, Integer> customerConnectionAndAdminIdPair = customerEntrySet.getValue();
            WebSocket customerConnection = customerConnectionAndAdminIdPair.getFirst();
            int adminId = customerConnectionAndAdminIdPair.getSecond();
            if (searchAdminId == adminId) {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("action", Constants.CONNECTION_ERROR_ACTION);
                customerConnection.send(jsonObj.toJSONString());
                customerConnection.close(0);
            }
        }
    }

    private static void sendConnectionErrorToAdminWhoIsConnectedToCustomer(String customerKey) {
        Pair<WebSocket, Integer> connAdminIdPair = customerWebSocketsPair.get(customerKey);
        Integer adminId = connAdminIdPair.getSecond();
        WebSocket adminConnection = adminWebSokets.get(adminId);
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("action", Constants.CONNECTION_ERROR_ACTION);
        String customerType = customerKey.substring(0, customerKey.lastIndexOf("_"));
        Integer customerId = Integer.parseInt(customerKey.substring(customerKey.lastIndexOf("_") + 1));
        jsonObj.put("customer_type", customerType);
        jsonObj.put("customer_id", customerId);
        adminConnection.send(jsonObj.toJSONString());
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        //Handle new connection here
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonMessage = (JSONObject) parser.parse(message);
            String fromCustomerType = (String) jsonMessage.get("customer_type");
            Integer fromCustomerId = (Integer) jsonMessage.get("id");
            String action = (String) jsonMessage.get("action");
            switch (action) {
                case Constants.REGISTER_ACTION:
                    if (Constants.CUSTOMER_TYPE_ADMIN.equals(fromCustomerType)) {
                        registerAdmin(fromCustomerId, conn);
                    } else {
                        registerCustomer(fromCustomerType, fromCustomerId, conn);
                    }
                    break;
                case Constants.SEND_MESSAGE_ACTION:
                    if (Constants.CUSTOMER_TYPE_ADMIN.equals(fromCustomerType)) {
                        String toCustomerKey = (String) jsonMessage.get("to_customer_key");
                        String messageBody = (String) jsonMessage.get("message");
                        sendMessageToCustomer(toCustomerKey, messageBody);
                    } else {
                        String messageBody = (String) jsonMessage.get("message");
                        sendMessageToAdmin(fromCustomerType, fromCustomerId, messageBody);
                    }
                    break;
                case Constants.SESSION_END_ACTION:
                    if (Constants.CUSTOMER_TYPE_ADMIN.equals(fromCustomerType)) {
                        String toCustomerKey = (String) jsonMessage.get("to_customer_key");
                        endSessionToCustomer(toCustomerKey);
                    } else {
                        endSessionToAdmin(fromCustomerType, fromCustomerId);
                    }
                    break;
            }
        } catch (Exception ex) {
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
    }

    @Override
    public void onError(WebSocket conn, Exception exc) {
    }

    private String generateCustomerKey(String customerType, Integer customerId) {
        return customerType + "_" + customerId;
    }

    private void sendMessageToAdmin(String fromCustomerType, Integer fromCustomerId, String messageBody) {
        String customerKey = generateCustomerKey(fromCustomerType, fromCustomerId);
        Pair<WebSocket, Integer> customerConnectionAndAdminIdPair = customerWebSocketsPair.get(customerKey);
        Integer adminId = customerConnectionAndAdminIdPair.getSecond();
        WebSocket adminConnection = adminWebSokets.get(adminId);
        adminConnection.send(messageBody);
    }

    private void sendMessageToCustomer(String customerKey, String message) {
        Pair<WebSocket, Integer> connectionAdminIdPaid = customerWebSocketsPair.get(customerKey);
        WebSocket customerConnection = connectionAdminIdPaid.getFirst();
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("action", Constants.SEND_MESSAGE_ACTION);
        jsonObj.put("message", message);
        customerConnection.send(jsonObj.toJSONString());
    }

    private void endSessionToAdmin(String fromCustomerType, Integer fromCustomerId) {
        String customerKey = generateCustomerKey(fromCustomerType, fromCustomerId);
        Pair<WebSocket, Integer> customerConnectionAndAdminIdPair = customerWebSocketsPair.get(customerKey);
        WebSocket adminConnection = customerConnectionAndAdminIdPair.getFirst();
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("action", Constants.SESSION_END_ACTION);
        adminConnection.send(jsonObj.toJSONString());
    }

    private void endSessionToCustomer(String customerKey) {
        Pair<WebSocket, Integer> customerConnectionAndAdminIdPair = customerWebSocketsPair.get(customerKey);
        WebSocket customerConnection = customerConnectionAndAdminIdPair.getFirst();
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("action", Constants.SESSION_END_ACTION);
        customerConnection.send(jsonObj.toJSONString());
        customerConnection.close(0);
    }

    private boolean registerAdmin(Integer id, WebSocket conn) {
        if (adminWebSokets.containsKey(id)) {
            return false;
        }
        adminWebSokets.put(id, conn);
        return true;
    }

    private boolean registerCustomer(String customerType, Integer id, WebSocket conn) {
        if (adminWebSokets.isEmpty()) {
            return false;
        }
        Map.Entry<Integer, WebSocket> adminIdConnectionEntry = adminWebSokets.entrySet().iterator().next();
        Integer adminId = adminIdConnectionEntry.getKey();
        String customerKey = generateCustomerKey(customerType, id);
        customerWebSocketsPair.put(customerKey, new Pair(conn, adminId));
        return true;
    }

}
