package com.traffic.sim.rendering;

import javafx.scene.image.Image;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SpriteLoader {
    private Map<String, Image> cache;

    public SpriteLoader() {
        this.cache = new HashMap<>();
    }

    public Image getSprite(String path) {
        if (cache.containsKey(path)) {
            return cache.get(path);
        }

        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is != null) {
                Image image = new Image(is);
                cache.put(path, image);
                return image;
            } else {
                // Return null or placeholder
                // System.err.println("Could not load image: " + path);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
