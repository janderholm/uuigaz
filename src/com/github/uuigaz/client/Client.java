package com.github.uuigaz.client;

import com.github.uuigaz.mechanics.Ident;
import com.github.uuigaz.messages.BoatProtos.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public static void main(String[] args) throws UnknownHostException, IOException {
	
		if (args.length != 1) {
			System.out.println("Need to supply a host");
			System.exit(1);
		}
		
		String parts[] = args[0].split(":", 2);
		
		String host = parts[0];
		int port = 30000;

		if (parts.length > 1) {
			port = Integer.parseInt(parts[1]);
		}
		
		Ident ident = Ident.build("SimpleClient");
		
		Socket socket = new Socket(host, port);
		
				
		OutputStream os = socket.getOutputStream();
		InputStream is = socket.getInputStream();
		
		ident.getMsg().writeDelimitedTo(os);
	}

}
