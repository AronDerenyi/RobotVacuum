package hu.bme.aut.fox.robotvacuum.virtual.app;

import hu.bme.aut.fox.robotvacuum.virtual.app.main.MainScreen;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.util.LinkedList;
import java.util.List;

public class App {

	public static void main(String[] args) {
		JFrame frame = new JFrame();

		// Setting up the window
		frame.setTitle("Robot Vacuum");
		frame.setSize(1400, 700);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Setting the main screen
		Container cnt = new MainScreen();
		frame.setContentPane(cnt);
		frame.setVisible(true);
	}



	public static abstract class Screen extends JPanel {

		private List<Disposable> disposables = new LinkedList<>();

		public Screen() {
			addHierarchyListener(event -> {
				if ((event.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) != 0) {
					if (event.getChangedParent() == getParent()) {

						// Checking if the window is a JFrame
						Window window = SwingUtilities.getWindowAncestor(this);
						if (window instanceof JFrame) {

							// Checking if the screen is the top level component
							JFrame frame = (JFrame) window;
							if (frame.getContentPane() != this) {
								throw new IllegalStateException("The screen is not the top level component");
							}
						} else {
							throw new IllegalStateException("The window is not a JFrame");
						}

						attach();
					} else {
						detach();
					}
				}
			});
		}

		// Attaching

		private void attach() {
			onAttach();
		}

		public void onAttach() {

		}

		// Detaching

		private void detach() {
			onDetach();
			dispose();
		}

		public void onDetach() {

		}

		// Disposing

		public void addDisposable(Disposable disposable) {
			disposables.add(disposable);
		}

		private void dispose() {
			for (Disposable disposable : disposables) {
				disposable.dispose();
			}
			disposables.clear();
		}

		/* Helper functions */

		public JFrame getFrame() {
			Window window = SwingUtilities.getWindowAncestor(this);
			if (window instanceof JFrame) {
				return (JFrame) window;
			} else {
				throw new IllegalStateException("The window is not a JFrame");
			}
		}

		public void navigate(Container container) {
			JFrame frame = getFrame();
			if (frame != null) {
				frame.setContentPane(container);
				frame.setVisible(true);
			} else {
				throw new IllegalStateException("The screen is not attached to a window.");
			}
		}

		public <T> void subscribe(Observable<T> observable, Consumer<? super T> onNext) {
			Disposable disposable = observable.subscribe((value) -> SwingUtilities.invokeLater(() -> {
				try {
					onNext.accept(value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}));
			addDisposable(disposable);
		}
	}


}
