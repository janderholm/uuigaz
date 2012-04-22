package com.github.uuigaz.mechanics;

import java.util.ArrayList;
import java.util.List;

import com.github.uuigaz.messages.BoatProtos;
import com.github.uuigaz.messages.BoatProtos.Board.Boat;
import com.github.uuigaz.messages.BoatProtos.Coordinate;

public class Board {
	private final Boat.Builder board[][];
	private final List<Boat.Builder> boats;

	private Board(List<Boat.Builder> boats) {
		this.board = new Boat.Builder[10][10];

		this.boats = boats;

		for (Boat.Builder b : this.boats) {

			Coordinate co = b.getCo();

			int length = b.getType().getNumber();

			switch (b.getDirection()) {
			case DOWN: {
				int x = co.getX();
				for (int y = co.getY(); y < co.getY() + length; ++y)
					board[x][y] = b;
				break;
			}
			case RIGHT: {
				int y = co.getY();
				for (int x = co.getX(); x < co.getX() + length; ++x)
					board[x][y] = b;
			}
			case UP: {
				int x = co.getX();
				for (int y = co.getY(); y > co.getY() - length; --y)
					board[x][y] = b;
			}
			case LEFT: {
				int y = co.getY();
				for (int x = co.getX(); x > co.getX() - length; --x)
					board[x][y] = b;
			}
			}
		}
	}

	public BoatProtos.Board getMsg() {
		BoatProtos.Board.Builder m = BoatProtos.Board.newBuilder();
		for (Boat.Builder b : this.boats) {
			m.addBoats(b);
		}
		return m.build();
	}

	public boolean isHit(BoatProtos.Coordinate co) {
		Boat.Builder b = board[co.getX()][co.getY()];
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
			return true;
		}
		return false;
	}

	public static Board build(BoatProtos.Board board) {
		List<Boat.Builder> l = new ArrayList<Boat.Builder>();
		
		for (Boat b : board.getBoatsList()) {
			l.add(b.toBuilder());
		}
		return new Board(l);
	}
}
