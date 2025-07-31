package net.supervision.superviseurapp.service;

import java.net.InetSocketAddress;
import java.net.Socket;

public class PortChecker {

    public static boolean isPortOpen(String ip, int port, int timeoutMillis) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), timeoutMillis);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
