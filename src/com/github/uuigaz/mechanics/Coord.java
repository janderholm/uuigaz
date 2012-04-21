package com.github.uuigaz.mechanics;
public class Coord{
	public final short X;
	public final char Y;

	public Coord(short x,char y){
		X = x;
		Y = y;
	}
	
	public boolean equals(Object o){
		if (o instanceof Coord){
			Coord tmp = (Coord) o;
			return (tmp.X == this.X && tmp.Y == this.Y);
		}
		return false;
	}
	
}
