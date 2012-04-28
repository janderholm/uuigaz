package com.github.uuigaz.mechanics;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.github.uuigaz.messages.BoatProtos;
import com.github.uuigaz.messages.BoatProtos.Board.Boat;
import com.github.uuigaz.messages.BoatProtos.Board.Boat.BoatType;
import com.github.uuigaz.messages.BoatProtos.Board.Boat.Direction;
import com.github.uuigaz.messages.BoatProtos.Coordinate;

public class Board {
	private final Boat.Builder board[][];
	private final BoatProtos.Board.Builder boardmsg;

	private Board() {
		this.board = new Boat.Builder[10][10];
		this.boardmsg = BoatProtos.Board.newBuilder();
	}

	public BoatProtos.Board getMsg() {
		return boardmsg.build();
	}
	
	public void setBoat(int x, int y, BoatType type, Direction direction) {
		Boat.Builder b = Boat.newBuilder();
		
		switch (direction) {
		case DOWN: {
			for (int yn = y; yn < y + type.getNumber(); ++yn)
				if (board[x][yn] != null)
					throw new RuntimeException("Boat cannot share placement with other boat.");
			for (int yn = y; yn < y + type.getNumber(); ++yn)
				board[x][yn] = b;
			break;
		}
		case RIGHT: {
			for (int xn = x; xn < x + type.getNumber(); ++xn)
				if (board[xn][y] != null)
					throw new RuntimeException("Boat cannot share placement with other boat.");
			for (int xn = x; xn < x + type.getNumber(); ++xn)
				board[xn][y] = b;
		}
		case UP: {
			for (int yn = y; yn > y - type.getNumber(); --yn)
				if (board[x][yn] != null)
					throw new RuntimeException("Boat cannot share placement with other boat.");
			for (int yn = y; yn > y - type.getNumber(); --yn)
				board[x][yn] = b;
		}
		case LEFT: {
			for (int xn = x; xn > x - type.getNumber(); --xn)
				if (board[xn][y] != null)
					throw new RuntimeException("Boat cannot share placement with other boat.");
			for (int xn = x; xn > x - type.getNumber(); --xn)
				board[xn][y] = b;
		}
		}
		
		boardmsg.addBoats(b);
	}
	
	/**
	 * Fire at Coordinate co, and update board.
	 * Return StatusReport containing hit = true|false
	 */
	public BoatProtos.StatusReport fire(BoatProtos.Coordinate co) {
		Boat.Builder b = board[co.getX()][co.getY()];
		boolean status = false;
		if (b != null) {
			if (!b.hasHits()) {
				b.setHits(0);
			}		
			
			BoatProtos.Coordinate cob = b.getCo();
			int hitIn;
			if (co.getX() == cob.getX() && co.getY() == cob.getX()) {
				hitIn = 0;
			} else if (co.getX() == cob.getX()) {
				hitIn = Math.abs(co.getY() - cob.getY());
			} else {
				hitIn = Math.abs(co.getX() - cob.getX());
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
			Coordinate co = b.getCo();
			board.setBoat(co.getX(), co.getY(), b.getType(), b.getDirection());
		}
		return board;
	}
}
