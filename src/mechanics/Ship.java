package mechanics;

public abstract class Ship{
	private short length;
	private Coord point;
	
	public Ship(short l, Coord p){
		length = l;
		point = p;
	}
}
