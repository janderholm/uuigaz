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

class Pair
{
    int fst;
    int snd;
    Pair(int fst, int snd)
    {
        this.fst = fst;
        this.snd = snd;
    }
}

/**
 * ServerTest is a simple and automatic random client usable both
 * for testing of servers and clients and as a worthless bot.
 * Randomly picks from a list of all possible moves until someone
 * has won.
 */
public class ServerTest
{

    /**
     * @param args
     * @throws IOException
     * @throws UnknownHostException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException
    {

        String arg  = "localhost:30000";
        String name = "TestClient-" + ManagementFactory.getRuntimeMXBean().getName();

        if (args.length != 2) {
            System.out.println("Defaulting to localhost:30000 with name " + name);
        } else {
            arg = args[0];
            name = args[1];
        }

        String parts[] = arg.split(":", 2);

        String host = parts[0];
        int port = 30000;

        if (parts.length > 1) {
            port = Integer.parseInt(parts[1]);
        } else {
            System.out.println("Defaulting to port 30000");
        }


        /* Ident phase */

        System.out.println("Client: " + name + " initializing.");

        Ident ident = Ident.build(name);

        System.out.println("Connecting to: " + host + ":" + port);

        Socket socket = new Socket(host, port);


        OutputStream os = socket.getOutputStream();
        InputStream is = socket.getInputStream();

        ident.getMsg().writeDelimitedTo(os);

        /* Init phase */

        Init init = Init.parseDelimitedFrom(is);
        Init.Builder initresponse = Init.newBuilder();

        Board board = null;
        Board theirBoard = null;
        Random rand = new Random();

        int hits = 0;
        int taken = 0;


        if (init.hasNewGame() && init.getNewGame()) {
            // The server asks us to create a new game.
            board = Board.build();
            theirBoard = Board.build();

            List<BoatType> boats = Arrays.asList(
                                       BoatType.BATTLESHIP,
                                       BoatType.CARRIER,
                                       BoatType.CRUISER,
                                       BoatType.DESTROYER,
                                       BoatType.SUBMARINE);

            List<Direction> dirs = Arrays.asList(
                                       Direction.RIGHT,
                                       Direction.DOWN
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
            initresponse.setBoard(board.getMsg()).build().writeDelimitedTo(os);
        } else if (init.hasBoard()) {
            // A session was already establised.
            board = Board.build(init.getBoard());
            for (Coordinate co : init.getBoard().getCosList()) {
                if (co.getHit()) {
                    taken += 1;
                }
            }
            theirBoard = Board.build(init.getOther());
            for (Coordinate co : init.getOther().getCosList()) {
                if (co.getHit()) {
                    hits += 1;
                }
            }
        } else {
            System.out.println("Server did not respond with a proper init message.");
            System.exit(1);
        }

        // Generate and shuffle all possible moves.
        LinkedList<Pair> moves = new LinkedList<Pair>();
        for (int i = 0; i < 10; ++i) {
            for (int j = 0; j < 10; ++j) {
                moves.add(new Pair(i, j));
            }
        }
        java.util.Collections.shuffle(moves);


        /* Game phase */

        BaseMessage msg;
        BaseMessage.Builder send;

        String status = "";


        boolean run = true;
        while (run) {
            msg = BaseMessage.parseDelimitedFrom(is);
            send = BaseMessage.newBuilder();

            Thread.sleep(50);

            if (msg.hasFire()) {
                Fire f = msg.getFire();
                if (board.isHit(f) != 0) {
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

            status = "\n";

            if (hits == 15) {
                System.out.println("WIN!");
                send.setEndGame(true);
                run = false;
            } else if (taken == 15) {
                System.out.println("LOSE!");
                send.setEndGame(true);
                run = false;
            }

            if (msg.hasYourTurn() && msg.getYourTurn()) {
                Fire.Builder fb = Fire.newBuilder();
                Pair move = moves.poll();
                fb.setX(move.fst);
                fb.setY(move.snd);
                status += "Fire at: (" + move.fst + "," + move.snd + ")\n";
                Fire f = fb.build();
                theirBoard.fire(f);
                send.setFire(f);
            }

            if (rand.nextInt(20) == 1) {
                // Randomly end game
                //send.setEndGame(true);
                //run = false;
            }

            if(msg.hasEndGame()) {
                run = false;
            }

            // Don't send empty messages!
            if (msg.hasFire() || msg.hasReport() || msg.hasYourTurn() || msg.hasEndGame()) {
                send.build().writeDelimitedTo(os);
                os.flush();
            }
        }
    }

}
