package ru.mmt.bluetoothand.utils;

public class ArterialPressure {

    public ArterialPressure(int upper, int lower, int pulse, String date){
        this.upperPressure = upper;
        this.lowerPressure = lower;
        this.pulse = pulse;
        this.updatedAt = date;
    }

    public Integer id;

    public Integer upperPressure;

    public Integer lowerPressure;

    public Integer pulse;

    public String updatedAt;

}
