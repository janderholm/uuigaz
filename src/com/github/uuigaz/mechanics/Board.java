package com.github.uuigaz.mechanics;

import java.util.List;


import com.github.uuigaz.messages.BoatProtos;
import com.github.uuigaz.messages.BoatProtos.Board.Boat;


public class Board {
	private final Boat board[][];
	private final List<Boat> boats;
	
	private Board(List<Boat> boats) {
		this.board = new Boat[10][10];
		
		this.boats = boats;
		
		for (Boat b : this.boats) {
			switch (b.getType()) {
			case BATTLESHIP:
				break;
			case CARRIER:
				break;
			case CRUISER:
				break;
			case DESTROYER:
				break;
			case SUBMARINE:
				break;
			}
		}
	}
	
	public BoatProtos.Board getMsg() {
		BoatProtos.Board.Builder m = BoatProtos.Board.newBuilder();
		m.addAllBoats(this.boats);
		return m.build();
	}
	
	public boolean isHit(BoatProtos.Coordinate co) {
		return board[co.getX()][co.getY()] != null;
	}
	
	public static Board build(BoatProtos.Board board) {
		return new Board(board.getBoatsList());
	}
}
