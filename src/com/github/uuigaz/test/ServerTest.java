package com.github.uuigaz.test;

import com.github.uuigaz.mechanics.Ident;
import com.github.uuigaz.mechanics.Board;
import com.github.uuigaz.messages.BoatProtos.Board.Boat.BoatType;
import com.github.uuigaz.messages.BoatProtos.Board.Boat.Direction;
import com.github.uuigaz.messages.BoatProtos.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.Socket;
import java.net.UnknownHostException;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

class Pair {
	int fst;
	int snd;
	Pair(int fst, int snd) {
		this.fst = fst;
		this.snd = snd;
	}
}

public class ServerTest {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
	
		String arg  = "localhost:30000";
		
		if (args.length != 1) {
			System.out.println("Defaulting to localhost:30000");
		} else {
			arg = args[0];
		}
		
		String parts[] = arg.split(":", 2);
		
		String host = parts[0];
		int port = 30000;

		if (parts.length > 1) {
			port = Integer.parseInt(parts[1]);
		} else {
			System.out.println("Defaulting to port 30000");
		}
		
		String name = "TestClient-" + ManagementFactory.getRuntimeMXBean().getName();
		
		System.out.println("Client: " + name + " initializing.");
		
		Ident ident = Ident.build(name);
		
		System.out.println("Connecting to: " + host + ":" + port);
		
		Socket socket = new Socket(host, port);
		
				
		OutputStream os = socket.getOutputStream();
		InputStream is = socket.getInputStream();
		
		ident.getMsg().writeDelimitedTo(os);
		
		Init init = Init.parseDelimitedFrom(is);
		Init.Builder initresponse = Init.newBuilder(); 
		
		Board board = null;
		Board theirBoard = Board.build();
		Random rand = new Random();
		
		if (init.hasNewGame() && init.getNewGame()) {
			// TODO: Create board.
			board = Board.build();
			List<BoatType> boats = Arrays.asList(
					BoatType.BATTLESHIP,
					BoatType.CARRIER,
					BoatType.CRUISER, 
					BoatType.DESTROYER,
					BoatType.SUBMARINE);
			
			List<Direction> dirs = Arrays.asList(
					Direction.RIGHT,
					Direction.DOWN,
					Direction.UP,
					Direction.LEFT
					);
			for (BoatType b : boats) {
				while (true) {
					try {
						board.setBoat(rand.nextInt(10), rand.nextInt(10), b, dirs.get(rand.nextInt(4)));
					} catch (RuntimeException e) {
						continue;
					}
					break;
				}
			}
			
			System.out.println(board);
			initresponse.setBoard(board.getMsg()).build().writeDelimitedTo(os);
		} else if (init.hasBoard()) {
			// TODO: Get board.
			board = Board.build(init.getBoard());
		} else {
			System.out.println("Server did not respond with a proper init message.");
			System.exit(1);
		}
		
		LinkedList<Pair> moves = new LinkedList<Pair>();
		
		for (int i = 0; i < 10; ++i) {
			for (int j = 0; j < 10; ++j) {
				moves.add(new Pair(i, j));
			}
		}
		
		java.util.Collections.shuffle(moves);
		
		BaseMessage msg;
		BaseMessage.Builder send;

		int hits = 0;
		int taken = 0;
		String status = "";
		
		while (true) {
			msg = BaseMessage.parseDelimitedFrom(is);
			send = BaseMessage.newBuilder();
			
			Thread.sleep(100);
			
			if (msg.hasFire()) {
				Fire f = msg.getFire();
				if (board.isHit(f)) {
					status += "Hit taken!\n";
					taken += 1;
				}
			}
			
			if (msg.hasReport()) {
				if (msg.getReport().getHit()) {
					status += "Last shot was a hit!\n";
					hits += 1;
				}
			}
				
			// Score printing
			String hitBoard = board.toString();
			String takenBoard = theirBoard.toString();
			
			String[] l = hitBoard.split("\\n");
			String[] r = takenBoard.split("\\n");
			String[] s = status.split("\\n");

			System.out.println("\033[2J");
			for (int i = 0; i < l.length; ++ i) {
				if (i < s.length) {
					System.out.println(l[i] + "     " + r[i] + "  " + s[i]);
				} else {
					System.out.println(l[i] + "     " + r[i]);
				}
			}
			
			status = "";
					
			if (hits == 15) {
				System.out.println("WIN!");
				break;
			} else if (taken == 15) {
				System.out.println("LOSE!");
				break;
			}

			if (msg.hasYourTurn() && msg.hasYourTurn()) {
				Fire.Builder fb = Fire.newBuilder();
				Pair move = moves.poll();
				fb.setX(move.fst);
				fb.setY(move.snd);
				status = "\nFire at: (" + move.fst + "," + move.snd + ")\n";
				Fire f = fb.build(); 
				theirBoard.fire(f);
				send.setFire(f);
			}
			
			// Don't send empty messages!
			if (msg.hasFire() || msg.hasReport() || msg.hasYourTurn()) {
				send.build().writeDelimitedTo(os);
				os.flush();
			}
		}
	}

}
