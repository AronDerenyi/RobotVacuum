package hu.bme.aut.fox.robotvacuum.virtual.app.main;

import hu.bme.aut.fox.robotvacuum.virtual.app.App;
import hu.bme.aut.fox.robotvacuum.virtual.app.Simulation;

import hu.bme.aut.fox.robotvacuum.virtual.app.combined.CombinedScreen;

import javax.swing.*;
import java.awt.*;

public class MainScreen extends App.Screen {

	public MainScreen() {
		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(layout);

		JButton combinedButton = new JButton("Start");
		combinedButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		combinedButton.addActionListener(
				(event) -> {
					Simulation simulation = new Simulation();
					navigate(new CombinedScreen(
							simulation.getWorld(),
							simulation.getRadar(),
							simulation.getMotor(),
							simulation.getRobotVacuum()
					));
					simulation.start();
				}
		);

		add(Box.createVerticalGlue());
		add(combinedButton);
		add(Box.createVerticalGlue());
	}
}
