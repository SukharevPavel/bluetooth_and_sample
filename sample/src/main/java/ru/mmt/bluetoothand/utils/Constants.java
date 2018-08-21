package ru.mmt.bluetoothand.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Constants {
    public final static String EMPTY_STRING = "";
    public final static DateFormat DATE_FORMAT = new SimpleDateFormat("d MMMM", new Locale("ru", "RU"));
    public final static SimpleDateFormat DATE_FORMAT_FULL = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("ru", "RU"));

}
