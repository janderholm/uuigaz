package com.github.uuigaz.mechanics;

import com.github.uuigaz.messages.BoatProtos;
import com.github.uuigaz.messages.BoatProtos.Board.Boat;
import com.github.uuigaz.messages.BoatProtos.Board.Boat.BoatType;
import com.github.uuigaz.messages.BoatProtos.Board.Boat.Direction;

public class Board {
	
	private final BoatWrapper board[][];
	private final BoatProtos.Board.Builder boardmsg;

	private Board() {
		this.board = new BoatWrapper[10][10];
		this.boardmsg = BoatProtos.Board.newBuilder();
	}

	public BoatProtos.Board getMsg() {
		return boardmsg.build();
	}
	
	public void setBoat(int x, int y, BoatType type, Direction direction) {
		Boat.Builder b = Boat.newBuilder();
		
		b.setDirection(direction);
		b.setType(type);
		
		b.setX(x);
		b.setY(y);
		
		switch (direction) {
		case DOWN: {
			for (int yn = y; yn <= y + type.getNumber(); ++yn)
				if (board[x][yn] != null)
					throw new RuntimeException("Boat cannot share placement with other boat.");
			for (int yn = y; yn < y + type.getNumber(); ++yn)
				board[x][yn] = new BoatWrapper(b);
			break;
		}
		case RIGHT: {
			for (int xn = x; xn <= x + type.getNumber(); ++xn)
				if (board[xn][y] != null) {
					System.out.println(board[xn][y]);
					throw new RuntimeException("Boat cannot share placement with other boat.");
				}
			for (int xn = x; xn < x + type.getNumber(); ++xn)
				board[xn][y] = new BoatWrapper(b);
			break;
		}
		case UP: {
			for (int yn = y; yn >= y - type.getNumber(); --yn)
				if (board[x][yn] != null) 
					throw new RuntimeException("Boat cannot share placement with other boat.");
			for (int yn = y; yn > y - type.getNumber(); --yn)
				board[x][yn] = new BoatWrapper(b);
			break;
		}
		case LEFT: {
			for (int xn = x; xn >= x - type.getNumber(); --xn)
				if (board[xn][y] != null)
					throw new RuntimeException("Boat cannot share placement with other boat.");
			for (int xn = x; xn > x - type.getNumber(); --xn)
				board[xn][y] = new BoatWrapper(b);
			break;
		}
		}
		
		boardmsg.addBoats(b);
	}
	
	/**
	 * Fire at Coordinate co, and update board.
	 * Return StatusReport containing hit = true|false
	 */
	public BoatProtos.StatusReport fire(BoatProtos.Fire f) {
		Boat.Builder b = board[f.getX()][f.getY()].b;
		boolean status = false;
		if (b != null) {
			if (!b.hasHits()) {
				b.setHits(0);
			}		
			
			int x = b.getX();
			int y = b.getY();
			int hitIn;
			if (f.getX() == x && f.getY() == y) {
				hitIn = 0;
			} else if (f.getX() == x) {
				hitIn = Math.abs(f.getY() - y);
			} else {
				hitIn = Math.abs(f.getX() - x);
			}
			
			int bits = b.getHits();
			bits = bits ^ (1 << hitIn);
			
			b.setHits(bits);		
			status = true;
		}
		
		BoatProtos.StatusReport.Builder msg = BoatProtos.StatusReport.newBuilder();
		msg.setHit(status);
		return msg.build();
	}

	public static Board build() {
		return new Board();
	}
	
	public static Board build(BoatProtos.Board boardmsg) {
		
		Board board = new Board();
		
		for (Boat b : boardmsg.getBoatsList()) {
			board.setBoat(b.getX(), b.getY(), b.getType(), b.getDirection());
		}
		return board;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append('┏');
		for (int i = 0; i < board.length - 1; i++) {
			sb.append('━');
			sb.append('┳');
		}
		sb.append('━');
		sb.append('┓');
		sb.append('\n');
		
		for (int i = 0; i < board.length; ++i) {
			for (int j = 0; j < board.length; ++j) {
				sb.append('┃');
				if (board[i][j] == null) {
				sb.append(' ');
				} else if (board[i][j].hit) {
					sb.append('░');
				} else {
					sb.append('█');
				}
			}
			sb.append('┃');
			sb.append('\n');
			
			if (i == board.length - 1)
				break;
			sb.append('┣');
			for (int j = 0; j < board.length - 1; ++j) {
				sb.append('━');
				sb.append('╋');
			}
			sb.append('━');
			sb.append('┫');
			sb.append('\n');
			
		}
		
		sb.append('┗');
		for (int i = 0; i < board.length - 1; i++) {
			sb.append('━');
			sb.append('┻');
		}
		sb.append('━');
		sb.append('┛');
		sb.append('\n');
		return sb.toString();
	}
	
	private class BoatWrapper {
	public boolean hit;
	public Boat.Builder b;
	
	public BoatWrapper(Boat.Builder b) {
		this.hit = false;
		this.b = b;
	}
		
		
	}

}
