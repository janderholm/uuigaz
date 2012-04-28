package com.github.uuigaz.server;

import com.github.uuigaz.mechanics.Board;
import com.github.uuigaz.mechanics.Ident;
import com.github.uuigaz.messages.BoatProtos;
import com.github.uuigaz.messages.BoatProtos.BaseMessage;
import com.github.uuigaz.messages.BoatProtos.Fire;
import com.github.uuigaz.messages.BoatProtos.Init;

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
	public final Ident ident;
	private Session session;

	public Player(Ident ident, Socket connection) throws IOException {
		this.ident = ident;
		this.connection = connection;
		this.is = connection.getInputStream();
		this.os = connection.getOutputStream();
	}
	
	public void sendMessage(BoatProtos.BaseMessage msg) throws IOException {
		msg.writeDelimitedTo(os);
	}

	public void run() {
		System.out.println("Connected: " + ident);
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
			Init.Builder init;
			Init iinit;
			
			if (session.isInitialized(this)) {
				// If session already running replay any messages.
				init = Init.newBuilder();
				init.setBoard(session.getBoardMsg(this));
				init.build().writeDelimitedTo(os);				
			} else {
				// Ask client to create a board.
				init = Init.newBuilder();
				init.setNewGame(true);
				init.build().writeDelimitedTo(os);
				iinit = Init.parseDelimitedFrom(is);
				
				if (iinit.hasBoard()) {
					session.initialize(this, iinit.getBoard());
					// TODO: Figure out who's to start.
				} else {
					System.err.println("Client did not respond a new game with a board.");
				}
			}

			
			BaseMessage m;
			BaseMessage.Builder send;
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
					BoatProtos.StatusReport hit = session.fire(this, m.getFire());
					send.setReport(hit);
				}
				
				BaseMessage s = send.build();
				s.writeDelimitedTo(os);
			}
		} catch (IOException e) {

		} finally {
			System.out.println("Disconnected: " + ident);
		}
	}
}

class Session {
	private final Player player[];
	private final Board board[];

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

	public boolean belongsTo(Ident ident) {
		return player[0].ident.equals(ident) || player[1].ident.equals(ident);
	}
	
	public BoatProtos.Board getBoardMsg(Player p) {
		return p.ident.equals(player[0]) ? board[0].getMsg() : board[1].getMsg(); 
	}

	/**
	 * Fire at opponent and return status report.
	 * @param sender 
	 * @param co
	 * @return StatusReport with getHit set.
	 * @throws IOException 
	 */
	public BoatProtos.StatusReport fire(Player sender, Fire fire) throws IOException {
		int other = player[0].ident.equals(sender) ? 1 : 0;
		
		BoatProtos.BaseMessage.Builder msg = BoatProtos.BaseMessage.newBuilder();
		msg.setFire(fire);
		
		// TODO: Send to other player.
		player[other].sendMessage(msg.build());
		
		return board[other].fire(fire); 
	}	
}

class Controller {

	private static Controller instance;
	private LinkedList<Player> participants;
	private LinkedList<Session> sessions;

	private Controller() {
		this.participants = new LinkedList<Player>();
		this.sessions = new LinkedList<Session>();
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
		if (instance == null) {
			instance = new Controller();
		}
		return instance;
	}
}

public class Server {

	public static void main(String[] args) {

		Controller.getInstance();
		
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
				new Thread(new Player(Ident.build(ident), socket)).start();
			} catch (IOException e) {
				// TODO:
				// A connection failed. Not sure what to do here but just leave
				// it with a stack trace for now.
				e.printStackTrace();
			}
		}

	}
}
