package mechanics
public class Coord{
	public final short X;
	public final char Y;

	public Coord(short x,char y){
		X = x;
		Y = y;
	}
	
	public boolean equals(Object o){
		if (o instanceof Coord){
			return (o.X == this.X && o.Y == this.Y)
		}
		return false;
	}
	
}
