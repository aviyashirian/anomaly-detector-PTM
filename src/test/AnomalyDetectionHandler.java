package test;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Scanner;
import test.Commands.DefaultIO;
import test.Server.ClientHandler;

public class AnomalyDetectionHandler implements ClientHandler{

	public class SocketIO implements DefaultIO{
		private Scanner in;
		private PrintWriter out;

		public SocketIO(InputStream clientInput, OutputStream clientOutput) {
			this.in = new Scanner(clientInput);
			this.out = new PrintWriter(clientOutput);			
		}
		
		@Override
		public String readText() {
			return this.in.nextLine();
		}

		@Override
		public void write(String text) {
			this.out.print(text);
			this.out.flush();
		}

		@Override
		public float readVal() {
			return this.in.nextFloat();
		}

		@Override
		public void write(float val) {
			this.out.print(val);
			this.out.flush();
		}

		public void close() {
			this.in.close();
			this.out.close();
		}

	}

	@Override
	public void handle(InputStream input, OutputStream output) {
		SocketIO sio = new SocketIO(input, output);
		CLI cli = new CLI(sio);
		cli.start();
		sio.close();
	}


}
