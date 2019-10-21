package hu.bme.aut.fox.robotvacuum.virtual.components;
import hu.bme.aut.fox.robotvacuum.hal.Radar;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class VirtualRadar implements Radar {
	private static final Object observableLock = new Object();
	private static final int pingInterval = 500;
	private static final double angle = 2.0 / 3.0 * Math.PI;
	private static final double dPhi = 0.2;

	private List<RadarListener> listeners;
	private VirtualWorld world;
	private boolean isRunning = false;
	private Thread radarThread;

	public VirtualRadar(VirtualWorld world) {
		this.world = world;
		this.listeners = new LinkedList<>();
		radarThread = null;
	}

	private void notifyRadarListeners(List<RadarData> data) {
		synchronized (observableLock) {
			for(RadarListener listener : listeners)
				listener.newRadarData(data);
		}
	}
	@Override
	public void addRadarListener(RadarListener listener) {
		synchronized (observableLock) {
			this.listeners.add(listener);
		}
	}

	@Override
	public void start() {
		if (isRunning) return;
		isRunning = true;
		radarThread = new Thread(() -> {
			while (isRunning) {
				try {
					this.measure();
					Thread.sleep(pingInterval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		radarThread.start();
	}

	@Override
	public void stop() {
		isRunning = false;
		try {
			radarThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void measure() {
		final Position position = world.getRobotVacuumPosition();
		final List<RadarData> data = new ArrayList<>();
		for (double phi = angle / 2; phi >= -angle / 2; phi -= dPhi)
			data.add(new RadarData(position.direction, this.getRayLength(phi, position)));
		notifyRadarListeners(data);
	}

	private double getRayLength (double phi, Position position) {
		final double fieldSize = VirtualWorld.WORLD_FIELD_SIZE;
		int x = (int) Math.round(position.x / fieldSize), y = (int) Math.round(position.y / fieldSize);
		final double sinPhi = Math.sin(phi + position.direction), cosPhi = Math.cos(phi + position.direction);
		int dx, dy;
		dx = sinPhi > 0 ? 1 : -1;
		dy = cosPhi > 0 ? 1 : -1;
		while (true) {
			Pair<Double, Double> intersect = findFirstIntersection(
				(x + dx) * fieldSize, y * fieldSize,
				sinPhi ,cosPhi, position.x, position.y);
			if (intersect != null)
				if (world.isFieldEmpty(x + dx, y))
					return getLength(intersect, position);
				else {
					x += dx;
					continue;
				}

			intersect = findFirstIntersection(
				x * fieldSize, (y + dy) * fieldSize,
				sinPhi ,cosPhi, position.x, position.y);
			if (intersect == null) throw new NullPointerException("Na ez látod nagy baj");

			if (world.isFieldEmpty(x, y + dy))
				return getLength(intersect, position);
			y += dy;
		}
	}

	private double getLength(Pair<Double, Double> intersection, Position position) {
		final double a2 = intersection.getKey() - position.x;
		final double b2 = intersection.getValue() - position.y;
		return Math.sqrt(a2 * a2 + b2 * b2);
	}

	private Pair<Double, Double> findFirstIntersection(
		final double fieldX, final double fieldY,
		final double sinPhi, final double cosPhi,
		final double pX, final double pY) {

		final double fieldSize = VirtualWorld.WORLD_FIELD_SIZE;
		Pair<Double, Double> intersectSouth = intersectLineWithHorizontalSegment(
			sinPhi, cosPhi, pX, pY, fieldY + fieldSize, fieldX, fieldX + fieldSize);
		Pair<Double, Double> intersectNorth = intersectLineWithHorizontalSegment(
			sinPhi, cosPhi, pX, pY, fieldY, fieldX, fieldX + fieldSize);
		Pair<Double, Double> intersectEast = intersectLineWithVerticalSegment(
			sinPhi, cosPhi, pX, pY, fieldX + fieldSize, fieldY, fieldY + fieldSize);
		Pair<Double, Double> intersectWest = intersectLineWithVerticalSegment(
			sinPhi, cosPhi, pX, pY, fieldX, fieldY, fieldY + fieldSize);

		if (intersectSouth == null && intersectEast == null && intersectNorth == null && intersectWest == null) return null;
		if (sinPhi > 0) {
			if (cosPhi > 0) return intersectSouth == null ? intersectWest : intersectSouth;
			else return intersectSouth == null ? intersectEast : intersectSouth;

		}
		else {
			if (cosPhi > 0) return intersectNorth == null ? intersectWest : intersectNorth;
			else return intersectNorth == null ? intersectEast: intersectNorth;
		}
	}

	private Pair<Double, Double> intersectLineWithHorizontalSegment(
		final double sinPhi, final double cosPhi,
		final double pX, final double pY,
		final double y, final double x0, final double x1) {
		final double lambda = (y - pY) / sinPhi;
		final double x = pX + lambda * cosPhi;
		if (x >= x0 && x <= x1) return new Pair<>(x, y);
		return null;
	}

	private Pair<Double, Double> intersectLineWithVerticalSegment(
		final double sinPhi, final double cosPhi,
		final double pX, final double pY,
		final double x, final double y0, final double y1) {
		return intersectLineWithHorizontalSegment(cosPhi, sinPhi, pY, pX, x, y0, y1);
	}
}
