package ru.deadsoftware.cavedroid.misc;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;


public class Assets {


    public static final JsonReader jsonReader = new JsonReader();


    public static int getIntFromJson(JsonValue json, String name, int defaultValue) {
        return json.has(name) ? json.getInt(name) : defaultValue;
    }


    public static String getStringFromJson(JsonValue json, String name, String defaultValue) {
        return json.has(name) ? json.getString(name) : defaultValue;
    }

    public static boolean getBooleanFromJson(JsonValue json, String name, boolean defaultValue) {
        return json.has(name) ? json.getBoolean(name) : defaultValue;
    }

}
