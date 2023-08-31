package hu.bme.aut.fox.robotvacuum.virtual.components;

import hu.bme.aut.fox.robotvacuum.virtual.app.Simulation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VirtualRadarTest {
	@Test
	void testIntersectLines() {
		Simulation simulation = new Simulation();
		VirtualRadar radar = simulation.getRadar();
		Vec2 point = radar.intersectWithLine(new Vec2(1, 2), new Vec2(3.1, 6.2), new Vec2(0, 3), 3 * Math.PI / 4);
		assertTrue(Math.abs(3 - point.getX()) < 0.00001, "x is " + point.getX() + " expected: " + 3);
		assertTrue(Math.abs(6 - point.getY()) < 0.00001, "y is " + point.getY() + " expected: " + 6);
	}

	@Test
	void testRadarSpeed() {
		Simulation simulation = new Simulation();
		VirtualRadar radar = simulation.getRadar();
		long t1 = System.currentTimeMillis();
		radar.getRadarData();
		long t2 = System.currentTimeMillis();
		assertTrue(t2 - t1 < 20, "Processing data took " + (t2 - t1) / 1000 + " seconds");
	}
}