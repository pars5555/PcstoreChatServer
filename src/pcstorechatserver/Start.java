/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pcstorechatserver;

/**
 *
 * @author default
 */
public class Start {

    private static WebSocketServerFactory webSocketServerFactory;

    public static void main(String args[]) {
        System.out.println("Starting WebSocket...");
        System.out.println("Listening to 8579 port");
        webSocketServerFactory = new WebSocketServerFactory(8579);
    }
}
