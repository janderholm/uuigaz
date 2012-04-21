package com.github.uuigaz.server;

import com.github.uuigaz.messages.BoatProtos.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/*
 private BufferedReader in;
 private OutputStreamWriter out;
 private Participant parent;

 public Sentry(Participant parent, Socket connection) throws IOException {
 this.parent = parent;
 this.in = new BufferedReader(new InputStreamReader(
 connection.getInputStream()));
 this.out = new OutputStreamWriter(connection.getOutputStream());
 }
 */

class Player implements Runnable {
	// private LinkedBlockingQueue<String> mailbox;
	private Socket connection;
	public final String name;
	private Session session;

	public Player(Init init, Socket connection) throws IOException {
		this.name = init.getName();
		this.connection = connection;
		// this.mailbox = new LinkedBlockingQueue<String>();
	}

	public void run() {
		while (true) {
			try {
				this.session = Controller.getInstance().getSession(this);
			} catch (InterruptedException e) {
				// TODO: Here we were interrupted when waiting for someone to
				// connect. We probably want to break now.
				e.printStackTrace();
				break;
			}
		}
	}
}

class Session {
	// private ArrayList<String> names;
	final Player players[];

	public Session(Player player1, Player player2) {
		players = new Player[2];
		players[0] = player1;
		players[1] = player2;
	}

	boolean checkName(String name) {
		return players[0].name.equals(name) || players[1].name.equals(name);
	}

}

class Controller {

	private static Controller instance;
	private ArrayList<Player> participants;
	private ArrayList<Session> sessions;

	private Controller() {
		this.participants = new ArrayList<Player>();
	}

	public synchronized Session getSession(Player p)
			throws InterruptedException {
		Session session = null;
		while (session == null) {
			for (Session s : sessions) {
				if (s.checkName(p.name)) {
					session = s;
					return session;
				}
			}

			if (participants.isEmpty()) {
				participants.add(p);
				wait();
			} else {
				// TODO: Maybe add return here.
				sessions.add(new Session(p, participants.remove(0)));
				notifyAll();
			}
		}

		return session;
	}

	public static synchronized Controller getInstance() {
		if (instance == null) {
			instance = new Controller();
		}
		return instance;
	}
}

public class Server {

	public static void main(String[] args) {
		
		int port = 30000;
		
		if (args.length > 0) {
			try {			
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.out.println("Argument not a proper port");
				System.exit(1);
			}
		}
		
		ServerSocket listen = null;
		try {
			listen = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}

		while (true) {
			Socket socket;
			try {
				socket = listen.accept();
				System.out.println("Connection from" + socket.getInetAddress());
				Init init = Init.parseFrom(socket.getInputStream());
				new Thread(new Player(init, socket)).start();
			} catch (IOException e) {
				// TODO: A connection failed. Not sure what to do here but
				//       just leave it with a stack trace for now.       
				e.printStackTrace();
			}
		}

	}

}
