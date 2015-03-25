/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pcstorechatserver;

import java.net.InetSocketAddress;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author Pars
 */
public class WebSocketServerFactory extends WebSocketServer implements Runnable {

    //private final Config conf;
    public WebSocketServerFactory(int port) {
        super(new InetSocketAddress(port));
        //this.conf = Config.getInstance();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        //Handle new connection here
    }

    @Override
    public void onMessage(WebSocket conn, String message) {

        JSONObject res = new JSONObject();
        String command = null;
        JSONObject jsonObj = null;
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(message);
            jsonObj = (JSONObject) obj;
        } catch (Exception ex) {
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
    }

    @Override
    public void onError(WebSocket conn, Exception exc) {
    }

}
