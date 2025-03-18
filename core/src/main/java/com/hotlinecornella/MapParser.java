package com.hotlinecornella;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

public class MapParser {
    public static GameMap loadMap(String filePath) {
        FileHandle file = Gdx.files.internal(filePath);
        String json = file.readString();
        Json jsonParser = new Json();
        return jsonParser.fromJson(GameMap.class, json);
    }
}
