package hu.bme.aut.fox.robotvacuum.world;

public class Field {

	private final int x;
	private final int y;

	private final boolean obstacle;
	private final boolean reachable;
	private final boolean cleaned;

	public Field(int x, int y, boolean obstacle, boolean reachable, boolean cleaned) {
		this.x = x;
		this.y = y;
		this.obstacle = obstacle;
		this.reachable = reachable;
		this.cleaned = cleaned;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public boolean isObstacle() {
		return obstacle;
	}

	public boolean isReachable() {
		return reachable;
	}

	public boolean isCleaned() {
		return cleaned;
	}
}
