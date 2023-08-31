package hu.bme.aut.fox.robotvacuum.virtual.viewmodel;

import hu.bme.aut.fox.robotvacuum.virtual.components.*;
import io.reactivex.subjects.BehaviorSubject;


public class VirtualWorldViewModel {

    public final BehaviorSubject<Position> robotVacuum = BehaviorSubject.create();
    public final BehaviorSubject<VirtualRadar.RadarData[]> radarData = BehaviorSubject.create();

    private VirtualWorld virtualWorld;
    private VirtualRadar virtualRadar;
    private VirtualMotor virtualMotor;

    public VirtualWorldViewModel(
        VirtualWorld virtualWorld,
        VirtualRadar radar,
        VirtualMotor virtualMotor
    ){
        this.virtualWorld = virtualWorld;
        this.virtualMotor = virtualMotor;
        this.virtualRadar = radar;

        virtualWorld.addListener(this::positionChanged);
        virtualRadar.addRadarListener(this::onUpdate);
    }

    private void positionChanged(Position position) {
        robotVacuum.onNext(position);
    }

    private void onUpdate(VirtualRadar.RadarData[] data) {
        radarData.onNext(data.clone());
    }

    public VirtualWorld getVirtualWorld() {
        return virtualWorld;
    }
}