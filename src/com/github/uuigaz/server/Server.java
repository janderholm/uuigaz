package com.github.uuigaz.server;

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
	//private LinkedBlockingQueue<String> mailbox;
	private Socket connection;
	public final String name;
	private Session session;
	
	public Player(String name, Socket connection) throws IOException, InterruptedException {
		this.name = name;
		this.connection = connection;
		//this.mailbox = new LinkedBlockingQueue<String>();
		this.session = Controller.getInstance().getSession(this);
	}

	public void run() {
		// TODO Auto-generated method stub
		
	}
}

class Session {
	//private ArrayList<String> names;
	final Player players[];
	public Session(Player player1, Player player2) {
		players = new Player[2];
		players[0] = player1;
		players[1] = player2;
	}
		
	boolean checkName(String name){
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

	public synchronized Session getSession(Player p) throws InterruptedException {
		Session session = null;
		while (session == null) {
			for(Session s : sessions){
				if(s.checkName(p.name)) {
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

	public static void main(String[] args) throws IOException, InterruptedException {
		ServerSocket listen = new ServerSocket(Integer.parseInt(args[0]));
		while (true) {
			Socket socket = listen.accept();
			System.out.println("Connection from" + socket.getInetAddress());
			String name = "hej"; //get name of particpant
			new Thread(new Player(name,socket)).start();
		}

	}

}
