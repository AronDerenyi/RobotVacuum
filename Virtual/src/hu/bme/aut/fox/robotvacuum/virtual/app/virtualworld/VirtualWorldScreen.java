package hu.bme.aut.fox.robotvacuum.virtual.app.virtualworld;

import hu.bme.aut.fox.robotvacuum.virtual.components.*;
import hu.bme.aut.fox.robotvacuum.virtual.app.App.Screen;
import hu.bme.aut.fox.robotvacuum.virtual.viewmodel.VirtualWorldViewModel;


import javax.swing.*;
import java.awt.*;

public class VirtualWorldScreen extends Screen {
    private VirtualWorldPanel virtualWorldPanel;

    public VirtualWorldScreen(VirtualWorld virtualWorld, VirtualRadar radar, VirtualMotor virtualMotor){
        virtualWorldPanel = new VirtualWorldPanel(virtualWorld, radar, virtualMotor);
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        add(virtualWorldPanel);
    }

    public static class VirtualWorldPanel extends JPanel {

        private VirtualWorldViewModel viewModel;
        private final int baseX = 0;
        private final int baseY = 0;
        private final int fieldSize = 20;

        private double actualPosX = 0;
        private double actualPosY = 0;

        private VirtualRadar.RadarData[] radarData = new VirtualRadar.RadarData[0];

        private JPanel myCanvas;

        public VirtualWorldPanel(
            VirtualWorld virtualWorld,
            VirtualRadar radar,
            VirtualMotor virtualMotor
        ) {
            radarData = radar.getRadarData();
            viewModel = new VirtualWorldViewModel(
                virtualWorld,
                radar,
                virtualMotor
            );

            setPreferredSize(new Dimension(700,700));
            BoxLayout layout = new BoxLayout(this, BoxLayout.X_AXIS);
            setLayout(layout);

            myCanvas = new FullWorldCanvas();
            add(myCanvas);
        }

        public class FullWorldCanvas extends JPanel {

            //matrix adatok
            private int rows = 0;
            private int columns = 0;
            VirtualWorld.WorldObject[] objects;

            public FullWorldCanvas() {

                actualPosX = (int) viewModel.getVirtualWorld().getRobotVacuum().x;
                actualPosY = (int) viewModel.getVirtualWorld().getRobotVacuum().y;

                objects = viewModel.getVirtualWorld().getObjects();
                int max = 0;
                int len = objects.length;
                for (int i = 0; i < len; i++) {
                    if (objects[i].getVertices().length > max)
                        max = objects[i].getVertices().length;
                }

                columns = (int) viewModel.getVirtualWorld().getWidth();
                rows = (int) viewModel.getVirtualWorld().getHeight();
            }

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                for (int i = 0; i < objects.length; i++) {
                    VirtualWorld.Coordinate[] coos = objects[i].getVertices();
                    int[] xCoos = new int[coos.length];
                    int[] yCoos = new int[coos.length];
                    for(int j = 0; j < coos.length; j++) {
                        xCoos[j] = (int)(coos[j].getX() * fieldSize);
                        yCoos[j] = (int)(coos[j].getY() * fieldSize);
                    }
                    g.setColor(Color.BLACK);
                    g.fillPolygon(new Polygon(xCoos, yCoos, coos.length));
                }


                g.setColor(Color.RED);
                g.fillOval((int) (baseX + (actualPosX - 0.5) * fieldSize),
                        (int) (baseY + (actualPosY- 0.5) * fieldSize),
                        fieldSize, fieldSize);


                g.setColor(Color.BLUE);
                if(radarData != null) {
                    for (VirtualRadar.RadarData data : radarData) {
                        double startX = baseX + actualPosX * fieldSize;
                        double startY = baseY + actualPosY * fieldSize;
                        double rayLength = data.getDistance() * fieldSize;
                        g.drawLine(
                                (int) startX,
                                (int) startY,
                                (int) (startX + Math.cos(data.getDirection()) * rayLength),
                                (int) (startY + Math.sin(data.getDirection()) * rayLength)
                        );
                    }
                }

            }
        }

        public void setRobotVacuumPos(Position pos) {
            actualPosX = pos.x;
            actualPosY = pos.y;

            myCanvas.repaint();
        }

        public void setRadarData(VirtualRadar.RadarData[] data) {
            radarData = data;
            myCanvas.repaint();
        }
    }

    @Override
    public void onAttach() {
        super.onAttach();
        subscribe(virtualWorldPanel.viewModel.robotVacuum, virtualWorldPanel::setRobotVacuumPos);
        subscribe(virtualWorldPanel.viewModel.radarData, virtualWorldPanel::setRadarData);
    }


}