package hu.bme.aut.fox.robotvacuum.virtual.viewmodel;


import hu.bme.aut.fox.robotvacuum.RobotVacuum;
import hu.bme.aut.fox.robotvacuum.navigation.Navigator;
import hu.bme.aut.fox.robotvacuum.world.Field;
import io.reactivex.subjects.BehaviorSubject;

public class WorldViewModel {

    public BehaviorSubject<Field[][]> world = BehaviorSubject.create();
    private Thread timerThread;
    private RobotVacuum robotVacuum;
    private Field[][] fields;
    private int scalingFactor = 40;
    private double gridScale;

    public WorldViewModel(RobotVacuum rv){

        robotVacuum = rv;
        gridScale = robotVacuum.getWorld().getGridScale();
        timerThread = new Thread(() -> {
            while(true) {
                this.updateWorld();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        timerThread.start();
    }

    private void updateWorld() {
        fields = new Field[scalingFactor][scalingFactor];
        int min = 0 - scalingFactor/2;
        int max = scalingFactor/2;
        gridScale = robotVacuum.getWorld().getGridScale();
        for(int i = min; i < max; ++i)
            for(int j = min; j < max; ++j)
                fields[i - min][j - min] = robotVacuum.getWorld().getGridField(j, i);
        world.onNext(fields);
    }

    public Field[][] getFields(){
        return fields;
    }

    public Navigator.Target[] getTargets() {
        return robotVacuum.getTargets().toArray(new Navigator.Target[0]);
    }

    public int getScalingFactor() {
        return scalingFactor;
    }

    public double getGridScale() {
        return gridScale;
    }

    public void setScalingFactor(int scalingFactor) {
        this.scalingFactor = scalingFactor;
    }

}
