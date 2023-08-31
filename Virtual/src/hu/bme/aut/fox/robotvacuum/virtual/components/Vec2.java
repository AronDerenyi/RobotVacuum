package hu.bme.aut.fox.robotvacuum.virtual.components;

public class Vec2 {
	private final double x;
	private final double y;
	public Vec2(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public Vec2 minus(Vec2 b) {
		return new Vec2(getX() - b.getX(), getY() - b.getY());
	}

	public Vec2 plus(Vec2 b) {
		return new Vec2(getX() + b.getX(), getY() + b.getY());
	}

	public double getTangent() {
		return x / y;
	}

	public double getLength() {
		return Math.sqrt(getLengthSquared());
	}

	public double getLengthSquared() {
		return x * x + y * y;
	}

	public Vec2 scale(double phi) {
		return new Vec2(x * phi, y * phi);
	}

	public Vec2 normalize() {
		return this.scale(1 / getLength());
	}

	public double scalarProduct(Vec2 b) {
		return x * b.x + y * b.y;
	}

	public Vec2 normal() {
		return new Vec2(-y, x);
	}

	@Override
	public String toString() {
		return "("+ x +", "+ y +" )";
	}
}
