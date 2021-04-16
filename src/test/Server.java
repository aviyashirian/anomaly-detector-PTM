package test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Server {

	public interface ClientHandler{
		public void handle(InputStream inFromClient, OutputStream outToClient);
	}

	volatile boolean stop;
	public Server() {
		stop=false;
	}
	
	
	private void startServer(int port, ClientHandler ch) {
		try {
			ServerSocket server = new ServerSocket(port);
			while (!stop) {
				Socket client = server.accept();
				ch.handle(client.getInputStream(), client.getOutputStream());
				client.close();
			}
			server.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	// runs the server in its own thread
	public void start(int port, ClientHandler ch) {
		new Thread(()->startServer(port,ch)).start();
	}
	
	public void stop() {
		stop=true;
	}
}
