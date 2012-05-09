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
		for (int i = 0; i < board.length; ++i) {
			for (int j = 0; j < board.length; ++j) {
				board[i][j] = new BoatWrapper();
			}
		}
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
			for (int yn = y; yn < y + type.getNumber(); ++yn) {
				if (board[yn][x].occupied)
					throw new RuntimeException(
							"Boat cannot share placement with other boat.");
			}
			for (int yn = y - 1; yn <= y + type.getNumber(); ++yn) {
				for (int xn = x - 1; xn <= x + 1; ++xn) {
					if (yn < 0 || xn < 0 || yn >= board.length || xn >= board.length)
						continue;
					board[yn][xn].occupied = true;
				}
			}
			for (int yn = y; yn < y + type.getNumber(); ++yn) {
				board[yn][x].b = b;			
			}
			break;
		}
		case RIGHT: {
			for (int xn = x; xn < x + type.getNumber(); ++xn)
				if (board[y][xn].occupied)
					throw new RuntimeException(
							"Boat cannot share placement with other boat.");

			for (int xn = x - 1; xn <= x + type.getNumber(); ++xn) {
				for (int yn = y - 1; yn <= y + 1; ++yn) {
					if (yn < 0 || xn < 0 || yn >= board.length || xn >= board.length)
						continue;
					board[yn][xn].occupied = true;
				}
			}
			for (int xn = x; xn < x + type.getNumber(); ++xn) {
				board[y][xn].b = b;
			}
			break;
		}
		case UP: {
			for (int yn = y; yn > y - type.getNumber(); --yn)
				if (board[yn][x].occupied)
					throw new RuntimeException(
							"Boat cannot share placement with other boat.");
			for (int yn = y + 1; yn >= y - type.getNumber(); --yn) {
				for (int xn = x - 1; xn <= x + 1; ++xn) {
					if (yn < 0 || xn < 0 || yn >= board.length || xn >= board.length)
						continue;
					board[yn][xn].occupied = true;
				}
			}
			for (int yn = y; yn > y - type.getNumber(); --yn) {
				board[yn][x].b = b;
			}
			break;
		}
		case LEFT: {
			for (int xn = x; xn > x - type.getNumber(); --xn)
				if (board[y][xn].occupied)
					throw new RuntimeException(
							"Boat cannot share placement with other boat.");

			for (int xn = x + 1; xn >= x - type.getNumber(); --xn) {
				for (int yn = y - 1; yn <= y + 1; ++yn) {
					if (yn < 0 || xn < 0 || yn >= board.length || xn >= board.length)
						continue;
					board[yn][xn].occupied = true;
				}
			}
			for (int xn = x; xn > x - type.getNumber(); --xn) {
				board[y][xn].b = b;
			}
			break;
		}
		}

		boardmsg.addBoats(b);
	}

	public int isHit(BoatProtos.Fire f) {
		BoatWrapper bw = board[f.getX()][f.getY()];
		Boat.Builder b = bw.b;

		bw.hit = true;

		if (b != null) {
			if (!b.hasHits()) {
				b.setHits(0);
			}

			int x = b.getX();
			int y = b.getY();
			int hitIn = (f.getX() - x) + (f.getY() - y);

			int bits = b.getHits();
			bits |= (1 << hitIn);
		
			b.setHits(bits);
			
			int sunk = 0;
			for (int i = 0; i < b.getType().getNumber(); ++i) {
				sunk |= (1 << i);
			}

			if (sunk == bits) {
				return 2;
			} else {
				return 1;
			}
		}
		return 0;
	}

	/**
	 * Fire at Coordinate co, and update board. Return StatusReport containing
	 * hit = true|false
	 */
	public BoatProtos.StatusReport fire(BoatProtos.Fire f) {
		BoatProtos.StatusReport.Builder msg = BoatProtos.StatusReport
				.newBuilder();
		int status = isHit(f);
		msg.setHit(status != 0);
		if (status == 2) {
			msg.setSunk(true);
		}
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
				BoatWrapper bw = board[i][j];
				sb.append('┃');
				if (bw.b == null && !bw.hit) {
					// Empty space.
					sb.append(' ');
				} else if (bw.b == null && bw.hit) {
					// Hit in water.
					sb.append('░');
				} else if (bw.b != null && bw.hit) {
					// hit in boat.
					sb.append('▒');
				} else if (bw.b != null && !bw.hit) {
					// no hit in boat.
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
		public boolean occupied;
		public Boat.Builder b;

	}

}
