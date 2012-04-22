package com.github.uuigaz.server;

import com.github.uuigaz.mechanics.Board;
import com.github.uuigaz.messages.BoatProtos;
import com.github.uuigaz.messages.BoatProtos.BaseMessage;
import com.github.uuigaz.messages.BoatProtos.Coordinate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

class Player implements Runnable {
	private Socket connection;
	private InputStream is;
	private OutputStream os;
	public final BoatProtos.Ident ident;
	private Session session;

	public Player(BoatProtos.Ident ident, Socket connection) throws IOException {
		this.ident = ident;
		this.connection = connection;
		this.is = connection.getInputStream();
		this.os = connection.getOutputStream();
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
		// previous messages if there are any, or initialize a new game if
		// there weren't.

		try {
			BaseMessage m;
			BaseMessage.Builder send;

			if (session.isInitialized(this)) {
				send = BaseMessage.newBuilder();
				send.setBoard(session.getBoardMsg(this));
				send.build().writeDelimitedTo(os);
				
				// TODO: Replay packages.
			} else {
				send = BaseMessage.newBuilder();
				send.setNewGame(true);
				send.build().writeDelimitedTo(os);
				m = BaseMessage.parseDelimitedFrom(is);
				
				if (m.hasBoard()) {
					session.initialize(this, m.getBoard());
					
					// TODO: Figure out who's to start.
				} else {
					System.err.println("Client did not respond a new game with a board.");
				}
			}

			while (true) {
				// TODO:
				// Probably no need for a listening sentry. When a game is
				// properly initialized any packages should just be relayed
				// through the session. Which could be made a thread.

				m = BaseMessage.parseDelimitedFrom(is);

				// Clean the basemessagebuilder.
				send = BaseMessage.newBuilder();
				
				if (m.hasFire()) {
					// TODO: A shot was fired. Respond with StatusReport
					BoatProtos.StatusReport hit = session.fire(this, m.getFire().getCo());
					send.setReport(hit);
				}
				
				send.build().writeDelimitedTo(os);
			}
		} catch (IOException e) {

		}
	}
}

class Session {
	private final Player player[];
	private Board board[];

	public Session(Player player1, Player player2) {
		player = new Player[2];
		board = new Board[2];
		
		player[0] = player1;
		player[1] = player2;
	}

	public void initialize(Player p, BoatProtos.Board boardmsg) {
		int i = p.ident.equals(player[0]) ? 0 : 1;
		board[i] = Board.build(boardmsg);
	}

	public boolean isInitialized(Player p) {
		return p.ident.equals(player[0]) ? board[0] != null : board[1] != null; 
	}

	public boolean belongsTo(BoatProtos.Ident ident) {
		return player[0].ident.equals(ident) || player[1].ident.equals(ident);
	}
	
	public BoatProtos.Board getBoardMsg(Player p) {
		return p.ident.equals(player[0]) ? board[0].getMsg() : board[1].getMsg(); 
	}

	public BoatProtos.StatusReport fire(Player sender, Coordinate co) {
		Board b = player[0].ident.equals(sender) ? board[1] : board[0];
		
		// TODO: Send to other player.
		
		BoatProtos.StatusReport.Builder msg = BoatProtos.StatusReport.newBuilder();
		
		msg.setHit(b.isHit(co));
		return msg.build();
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
				if (s.belongsTo(p.ident)) {
					session = s;
					return session;
				}
			}

			if (participants.isEmpty()) {
				// No session and no one to play against means we must
				// wait for someone.
				participants.add(p);
				wait();
			} else {
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
				BoatProtos.Ident ident = BoatProtos.Ident.parseDelimitedFrom(socket.getInputStream());
				new Thread(new Player(ident, socket)).start();
			} catch (IOException e) {
				// TODO:
				// A connection failed. Not sure what to do here but just leave
				// it with a stack trace for now.
				e.printStackTrace();
			}
		}

	}
}
