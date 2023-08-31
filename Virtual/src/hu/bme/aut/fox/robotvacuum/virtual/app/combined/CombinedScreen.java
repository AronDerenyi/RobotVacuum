package hu.bme.aut.fox.robotvacuum.virtual.app.combined;

import hu.bme.aut.fox.robotvacuum.RobotVacuum;
import hu.bme.aut.fox.robotvacuum.navigation.Navigator;
import hu.bme.aut.fox.robotvacuum.virtual.app.App;
import hu.bme.aut.fox.robotvacuum.virtual.components.*;
import hu.bme.aut.fox.robotvacuum.virtual.viewmodel.VirtualWorldViewModel;
import hu.bme.aut.fox.robotvacuum.virtual.viewmodel.WorldViewModel;
import hu.bme.aut.fox.robotvacuum.world.Field;
import javax.swing.*;
import java.awt.*;

public class CombinedScreen extends App.Screen {
    private VirtualWorldViewModel virtualWorldViewModel;
    private WorldViewModel worldViewModel;
    private JPanel combinedCanvas;

    //porszivo adatai
    private int robotVacuumPosX;
    private int robotVacuumPosY;

    private double actualPosX;
    private double actualPosY;
    private double recentPosX;
    private double recentPosY;
    private VirtualRadar.RadarData[] radarData = new VirtualRadar.RadarData[0];
    //kép adatai
    private int fieldSize;
    private double worldScale;
    private final int baseX = 0;
    private final int baseY = 0;

    //virtualis vilag adatai
    private int vwBaseX;
    private int vwBaseY;

    //vilag adatai
    private int wBaseX;
    private int wBaseY;


    public CombinedScreen(VirtualWorld virtualWorld, VirtualRadar virtualRadar, VirtualMotor virtualMotor, RobotVacuum robotVacuum){
        worldViewModel = new WorldViewModel(robotVacuum);
        virtualWorldViewModel = new VirtualWorldViewModel(
                virtualWorld,
                virtualRadar,
                virtualMotor
        );
        BoxLayout layout = new BoxLayout(this, BoxLayout.X_AXIS);
        setLayout(layout);
        combinedCanvas = new CombinedCanvas();
        add(combinedCanvas);
    }


    public class CombinedCanvas extends JPanel{
        private Field[][] fields;
        private VirtualWorld.WorldObject[] objects;
        private int rows = 0;
        private int columns = 0;
        private int SF;

        public CombinedCanvas(){
            actualPosX = (int) virtualWorldViewModel.getVirtualWorld().getRobotVacuum().x;
            actualPosY = (int) virtualWorldViewModel.getVirtualWorld().getRobotVacuum().y;
            recentPosX = actualPosX;
            recentPosY = actualPosY;

            objects = virtualWorldViewModel.getVirtualWorld().getObjects();
            int max = 0;
            int len = objects.length;
            for (int i = 0; i < len; i++) {
                if (objects[i].getVertices().length > max)
                    max = objects[i].getVertices().length;
            }
            columns = (int)virtualWorldViewModel.getVirtualWorld().getWidth();
            rows = (int)virtualWorldViewModel.getVirtualWorld().getHeight();

            int rp, rn, cp, cn;
            rp = rows-(int)actualPosY;
            rn = (int)actualPosY;
            cp = columns-(int)actualPosX;
            cn = (int)actualPosX;
            int r = (rp > rn)? rp : rn;
            int c = (cn > cp)? cn : cp;
            worldViewModel.setScalingFactor((int)(2 * ((r > c)? r : c) / worldViewModel.getGridScale()));
            SF = worldViewModel.getScalingFactor();

            fieldSize = 30;
            vwBaseX = (SF/2 - (int)actualPosX) * fieldSize;
            vwBaseY = (SF/2 - (int)actualPosY) * fieldSize;
            wBaseX = baseX;
            wBaseY = baseY;
        }

       @Override
        public void paint(Graphics graphics) {
            super.paint(graphics);
            fields = worldViewModel.getFields();
            worldScale = worldViewModel.getGridScale();

            //Az alap
           for (int i = 0; i < objects.length; i++) {
               VirtualWorld.Coordinate[] coos = objects[i].getVertices();
               int[] xCoos = new int[coos.length];
               int[] yCoos = new int[coos.length];
               for (int j = 0; j < coos.length; j++) {
                   xCoos[j] = (int) (coos[j].getX() * fieldSize);
                   yCoos[j] = (int) (coos[j].getY() * fieldSize);
               }
               graphics.setColor(Color.BLACK);
               graphics.fillPolygon(new Polygon(xCoos, yCoos, coos.length));
           }

            //Robot kepe
            for (int i = 0; i < fields.length; i++) {
                for (int j = 0; j < fields.length; j++) {
                    if (fields[j][i] != null) {
                        if (fields[j][i].isObstacle()) {
                            graphics.setColor(new Color(127, 0, 0, 100));
                        } else if (!fields[j][i].isReachable()) {
                            graphics.setColor(new Color(255, 0, 0, 100));
                        } else {
                            if (fields[j][i].isCleaned()) {
                                graphics.setColor(new Color(0, 0, 255, 100));
                            } else {
                                graphics.setColor(new Color(0, 0, 127, 100));
                            }
                        }
                        graphics.fillRect(
                                baseX + (int) ((i - SF / 2) * worldViewModel.getGridScale() * fieldSize) + 1 * fieldSize,
                                baseY + (int) ((j - SF / 2) * worldViewModel.getGridScale() * fieldSize) + 1 * fieldSize,
                                (int)(fieldSize * worldViewModel.getGridScale()),
                                (int)(fieldSize * worldViewModel.getGridScale())
                        );
                    } else {
                    }
                }
            }

            //A megcelzott utvonal
            int prevX = 0;
            int prevY = 0;
            boolean first = true;
            graphics.setColor(Color.GREEN);
            for (Navigator.Target target : worldViewModel.getTargets()) {
                int x = (int) ((target.getX()) * fieldSize) + 1 * fieldSize;
                int y = (int) ((target.getY()) * fieldSize) + 1 * fieldSize;

                if (!first) {
                    graphics.drawLine(x, y, prevX, prevY);
                }

                graphics.drawOval(
                        x - fieldSize / 2,
                        y - fieldSize / 2,
                        fieldSize,
                        fieldSize
                );

                prevX = x;
                prevY = y;
                first = false;
            }

            //A porszivo
           graphics.setColor(Color.RED);
           graphics.fillOval((int) (baseX + (actualPosX - 0.5) * fieldSize),
                   (int) (baseY + (actualPosY- 0.5) * fieldSize),
                   (int)(fieldSize),(int)(fieldSize));

            //A radar
           graphics.setColor(Color.BLUE);
           if(radarData != null) {
               for (VirtualRadar.RadarData data : radarData) {
                   double startX = baseX + actualPosX * fieldSize;
                   double startY = baseY + actualPosY * fieldSize;
                   double rayLength = data.getDistance() * fieldSize;
                   graphics.drawLine(
                           (int) startX,
                           (int) startY,
                           (int) (startX + Math.cos(data.getDirection()) * rayLength),
                           (int) (startY + Math.sin(data.getDirection()) * rayLength)
                   );
               }
           }

            recentPosX = actualPosX;
            recentPosY = actualPosY;
        }
    }

    public void setRobotVacuumPos(Position pos) {
        actualPosX = pos.x;
        actualPosY = pos.y;
        combinedCanvas.repaint();
    }

    public void setRadarData(VirtualRadar.RadarData[] data) {
        radarData = data;
        combinedCanvas.repaint();
    }

    public void drawWorld(Field[][] fields) {
        combinedCanvas.repaint();
    }

    @Override
    public void onAttach() {
        super.onAttach();
        subscribe(virtualWorldViewModel.robotVacuum, this::setRobotVacuumPos);
        subscribe(virtualWorldViewModel.radarData, this::setRadarData);
        subscribe(worldViewModel.world, (matrix) -> drawWorld(matrix));
    }

}
