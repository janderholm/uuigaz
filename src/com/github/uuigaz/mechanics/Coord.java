package com.github.uuigaz.mechanics;

import com.github.uuigaz.messages.BoatProtos;

public class Coord {
	private BoatProtos.Coordinate co; 
	
	private Coord(BoatProtos.Coordinate co){
		this.co = co;
	}
	
	public int getX() {
		return co.getX();
	}
	
	public int getY() {
		return co.getY();
	}
	
	public BoatProtos.Coordinate getMsg() {
		return co;
	}
	
	@Override
	public boolean equals(Object o){
		if (o == this) {
			return true;
		} else if (o instanceof Coord) {
			Coord other = (Coord) o;
			return (other.co.getX() == this.co.getX() &&
					other.co.getY() == this.co.getY());
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		// TODO: Proper hashcode.
		return 0;
	}
	
	public static Coord build(BoatProtos.Coordinate co) {
		return new Coord(co);
	}
	
	public static Coord build(int x, int y) {
		BoatProtos.Coordinate.Builder co = BoatProtos.Coordinate.newBuilder();
		co.setX(x);
		co.setY(y);
		return new Coord(co.build());
	}
}
