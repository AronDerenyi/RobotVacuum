package hu.bme.aut.fox.robotvacuum.virtual.components;


public class Position {
	public double x;
	public double y;
	public double direction;

	public Position(final double x, final double y, final double dir) {
		this.x = x;
		this.y = y;
		this.direction = dir;
	}
}
