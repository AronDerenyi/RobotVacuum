package hu.bme.aut.fox.robotvacuum.hardware;

public interface Motor {

	double move(double distance);
	double rotate(double angle);

	void start();
	void stop();
}
