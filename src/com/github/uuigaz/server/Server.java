package com.github.uuigaz.server;

import com.github.uuigaz.messages.BoatProtos.*;
import com.google.protobuf.MessageLite;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

class Player implements Runnable {
	private Socket connection;
	public final String name;
	private Session session;

	public Player(Init init, Socket connection) throws IOException {
		this.name = init.getName();
		this.connection = connection;
	}

	public void run() {
		try {
			this.session = Controller.getInstance().getSession(this);
		} catch (InterruptedException e) {
			// TODO:
			// Here we were interrupted when waiting for someone to connect.
			// We probably want to kill ourselves now.
			e.printStackTrace();
			return;
		}

		// TODO:
		// Session was just started here. This means we need to replay all
		// previous messages if there are any, or initalize a new game if
		// there weren't.

		while (true) {
			// TODO:
			// Probably no need for a listening sentry. When a game is properly
			// initialized any packages should just be relayed through the
			// session. Which could be made a thread.

			break;
		}
	}
}

class Session {
	Player player[];

	public Session(Player player1, Player player2) {
		player = new Player[2];
		player[0] = player1;
		player[1] = player2;
	}

	boolean checkName(String name) {
		return player[0].name.equals(name) || player[1].name.equals(name);
	}

	void sendMessage(Player sender, MessageLite msg) {
		Player to = player[0].equals(sender) ? player[1] : player[2];

		// TODO: Send msg to player indicated by "to"
	}

}

class Controller {

	private static Controller instance;
	private LinkedList<Player> participants;
	private LinkedList<Session> sessions;

	private Controller() {
		this.participants = new LinkedList<Player>();
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
				session = new Session(p, participants.removeFirst());
				sessions.addFirst(session);

				// TODO:
				// Change this to notify() if we don't lock on anything else.
				notifyAll();
			}
		}

		return session;
	}

	public static Controller getInstance() {
		synchronized (instance) {
			if (instance == null) {
				instance = new Controller();
			}
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
				System.out.println("Port argument not a number");
				System.exit(1);
			}
		}

		ServerSocket listen = null;
		try {
			listen = new ServerSocket(port);
			System.out.println("Listening on port: " + port);
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
				// TODO:
				// A connection failed. Not sure what to do here but just leave
				// it with a stack trace for now.
				e.printStackTrace();
			}
		}

	}

}
