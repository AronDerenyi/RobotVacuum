package hu.bme.aut.fox.robotvacuum.virtual.components;

import java.util.LinkedList;
import java.util.List;

public class VirtualWorld {
	private WorldObject[] objects;
	private double width;
	private double height;
	private Position robotVacuum;
	private List<ContinuousWorldListener> listeners;

	public VirtualWorld(double width, double height, WorldObject[] objects, Position robotVacuum) {
		this.height = height;
		this.width = width;
		this.objects = objects;
		this.robotVacuum = robotVacuum;
		listeners = new LinkedList<>();
	}

	public double getHeight() {
		return height;
	}

	public double getWidth() {
		return width;
	}

	public WorldObject[] getObjects() {
		return objects;
	}

	public Position getRobotVacuum() {
		return robotVacuum;
	}

	public void setRobotVacuum(Position robotVacuum) {
		this.robotVacuum = robotVacuum;
		this.normalizeRobotVacuumPosition();
		notifyListeners();
	}


	public void addListener(ContinuousWorldListener listener) {
		listeners.add(listener);
	}

	private void notifyListeners() {
		listeners.forEach(listener -> listener.positionChanged(robotVacuum));
	}

	private void normalizeRobotVacuumPosition() {
		double direction = robotVacuum.direction;

		direction = direction % (2 * Math.PI);
		if (direction < 0) direction += 2 * Math.PI;

		robotVacuum.direction = direction;
	}

	public interface ContinuousWorldListener {
		void positionChanged(Position position);
	}

	public static class WorldObject {
		private Coordinate[] vertices;

		public WorldObject(Coordinate...vertices) {
			this.vertices = vertices;
		}

		public Coordinate[] getVertices() {
			return vertices;
		}
	}

	public static class Coordinate extends Vec2 {
		public Coordinate(double x, double y) {
			super(x, y);
		}
	}
}
