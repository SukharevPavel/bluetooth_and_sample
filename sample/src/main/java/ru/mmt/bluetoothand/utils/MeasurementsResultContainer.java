package ru.mmt.bluetoothand.utils;

import java.util.ArrayList;
import java.util.List;

public final class MeasurementsResultContainer {
    private List<ArterialPressure> arterialPressures = new ArrayList<>();
    private List<Listener> observers = new ArrayList<>();

    private static final MeasurementsResultContainer instance = new MeasurementsResultContainer();

    private MeasurementsResultContainer(){
        //fill with sample data
        arterialPressures.add(new ArterialPressure(120,60,60, "2018-07-30 15:30:35"));

        arterialPressures.add(new ArterialPressure(110,58,65, "2018-08-10 12:13:55"));

        arterialPressures.add(new ArterialPressure(125,70,80, "2018-08-12 11:50:23"));

        arterialPressures.add(new ArterialPressure(100,55,73, "2018-08-18 18:12:11"));
    }

    public static MeasurementsResultContainer getInstance(){
        return instance;
    }

    public void attachObserver(Listener listener){
        observers.add(listener);
    }

    public void removeObserver(Listener listener){
        observers.remove(listener);
    }

    public void addPressure(ArterialPressure pressure){
        arterialPressures.add(pressure);
        for (Listener listener : observers) {
            listener.onDataChanged();
        }
    }

    public List<ArterialPressure> getArterialPressures(){
        return arterialPressures;
    }

    public interface Listener {
        void onDataChanged();
    }

}
