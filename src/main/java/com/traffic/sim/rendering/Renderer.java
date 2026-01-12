package com.traffic.sim.rendering;

import com.traffic.sim.simulation.SimulationManager;
import com.traffic.sim.simulation.entities.Pedestrian;
import com.traffic.sim.simulation.entities.TrafficLight;
import com.traffic.sim.simulation.entities.Vehicle;
import com.traffic.sim.simulation.map.Map;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 * Handles rendering of the simulation state to the JavaFX Pane.
 */
public class Renderer {
    private Pane canvas;
    private SpriteLoader spriteLoader;

    private final int TILE_SIZE = 20; // pixels per grid tile

    public Renderer(Pane canvas) {
        this.canvas = canvas;
        this.spriteLoader = new SpriteLoader();
    }

    private SimulationManager currentSimulationManager;

    public static boolean IS_DEBUG_MODE = false;

    public void toggleGrid() {
        IS_DEBUG_MODE = !IS_DEBUG_MODE;
    }

    public void render(SimulationManager simulationManager) {
        this.currentSimulationManager = simulationManager;

        canvas.getChildren().clear();

        // Draw Map
        drawMap(simulationManager.getMapSystem().getMap());

        // Draw Grid if enabled (Debug Mode)
        if (IS_DEBUG_MODE) {
            drawDebugOverlay(simulationManager.getMapSystem().getMap());
        }

        // Draw Vehicles
        for (Vehicle v : simulationManager.getVehicles()) {
            drawVehicle(v);
        }

        // Draw Pedestrians
        for (Pedestrian p : simulationManager.getPedestrians()) {
            drawPedestrian(p);
        }

        // Draw Traffic Lights
        for (TrafficLight tl : simulationManager.getTrafficLights()) {
            drawTrafficLight(tl);
        }
    }

    private void drawMap(Map map) {
        javafx.scene.image.Image mapImage = spriteLoader.getSprite("/assets/map/map.png");
        if (mapImage != null) {
            javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(mapImage);
            imageView.setFitWidth(1000);
            imageView.setFitHeight(720);
            canvas.getChildren().add(imageView);
        } else {
            Text errorText = new Text("Error loading map");
            errorText.setFill(Color.RED);
            errorText.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
            errorText.setLayoutX(400);
            errorText.setLayoutY(360);

            canvas.getChildren().add(errorText);
        }
    }

    private void drawDebugOverlay(Map map) {
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                double pixelX = x * TILE_SIZE;
                double pixelY = y * TILE_SIZE;

                Rectangle rect = new Rectangle(pixelX, pixelY, TILE_SIZE, TILE_SIZE);
                rect.setFill(null);
                rect.setStroke(Color.CYAN);
                rect.setStrokeWidth(0.5);

                int type = map.getTileType(x, y);
                // Highlight generic blocked areas vs roads
                if (type == Map.BLOCKED) {
                    rect.setStroke(Color.RED);
                }

                // Draw tile type number
                Text text = new Text(String.valueOf(type));
                text.setX(pixelX + 5);
                text.setY(pixelY + 15);
                text.setFill(Color.YELLOW);
                text.setStyle("-fx-font-size: 8px;");

                canvas.getChildren().addAll(rect, text);
            }
        }
    }

    private void drawVehicle(Vehicle v) {
        javafx.scene.image.Image carImage = spriteLoader.getSprite(v.getSpritePath());

        if (carImage != null) {
            javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(carImage);

            double widthPx = v.getWidth() * TILE_SIZE;
            double heightPx = v.getHeight() * TILE_SIZE;

            imageView.setFitWidth(widthPx);
            imageView.setFitHeight(heightPx);

            double pixelX = v.getX() * TILE_SIZE;
            double pixelY = v.getY() * TILE_SIZE;

            imageView.setX(pixelX - widthPx / 2.0);
            imageView.setY(pixelY - heightPx / 2.0);

            imageView.setRotate(v.getRotation());

            canvas.getChildren().add(imageView);
        }
    }

    private void drawPedestrian(Pedestrian p) {
        javafx.scene.image.Image personImage = spriteLoader.getSprite(p.getSpritePath());

        if (personImage != null) {
            javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(personImage);
            imageView.setFitWidth(p.getWidth());
            imageView.setFitHeight(p.getHeight());

            double pixelX = p.getX() * TILE_SIZE;
            double pixelY = p.getY() * TILE_SIZE;

            imageView.setX(pixelX - p.getWidth() / 2.0 + TILE_SIZE / 2.0);
            imageView.setY(pixelY - p.getHeight() / 2.0 + TILE_SIZE / 2.0);

            imageView.setRotate(p.getRotation());

            canvas.getChildren().add(imageView);
        }
    }

    private void drawTrafficLight(TrafficLight tl) {
        String id = tl.getId();

        // Vehicle Lights Configuration
        if (id.equals("TL_V_EW_1")) { // West - Vertical
            drawTrafficLightBox(244, 340, 19, 68, true, tl.getCurrentState(), true);
        } else if (id.equals("TL_V_EW_2")) { // East - Vertical
            drawTrafficLightBox(731, 312, 19, 68, true, tl.getCurrentState(), true);
        } else if (id.equals("TL_V_NS_1")) { // North - Horizontal
            drawTrafficLightBox(450, 105, 68, 19, false, tl.getCurrentState(), true);
        } else if (id.equals("TL_V_NS_2")) { // South - Horizontal
            drawTrafficLightBox(480, 595, 68, 19, false, tl.getCurrentState(), true);
        }
        // Pedestrian Lights Configuration
        else if (id.equals("TL_P_EW_1")) { // West Pedestrian - Horizontal
            drawTrafficLightBox(238, 528, 43, 22, false, tl.getCurrentState(), false);
        } else if (id.equals("TL_P_EW_2")) { // East Pedestrian - Horizontal
            drawTrafficLightBox(720, 167, 43, 22, false, tl.getCurrentState(), false);
        } else if (id.equals("TL_P_NS_1")) { // North Pedestrian - Vertical
            drawTrafficLightBox(308, 105, 22, 43, true, tl.getCurrentState(), false);
        } else if (id.equals("TL_P_NS_2")) { // South Pedestrian - Vertical
            drawTrafficLightBox(670, 595, 22, 43, true, tl.getCurrentState(), false);
        }
    }

    private void drawTrafficLightBox(double x, double y, double width, double height,
            boolean isVertical, TrafficLight.State state, boolean isVehicle) {
        Rectangle box = new Rectangle(x, y, width, height);
        box.setFill(Color.web("#3B3B3B"));
        double arcSize = Math.min(width, height);
        box.setArcWidth(arcSize);
        box.setArcHeight(arcSize);
        canvas.getChildren().add(box);

        double lightSize = 15;
        double spacing = isVehicle ? (isVertical ? (height - 3 * lightSize) / 4 : (width - 3 * lightSize) / 4)
                : (isVertical ? (height - 2 * lightSize) / 3 : (width - 2 * lightSize) / 3);

        if (isVehicle) {
            if (isVertical) {
                // Top to Bottom: Red -> Yellow -> Green
                drawLight(x + width / 2, y + spacing + lightSize / 2, lightSize, Color.RED,
                        state == TrafficLight.State.RED);
                drawLight(x + width / 2, y + 2 * spacing + 1.5 * lightSize, lightSize, Color.YELLOW,
                        state == TrafficLight.State.YELLOW);
                drawLight(x + width / 2, y + 3 * spacing + 2.5 * lightSize, lightSize, Color.web("#34C759"),
                        state == TrafficLight.State.GREEN);
            } else {
                // Left to Right: Red -> Yellow -> Green
                drawLight(x + spacing + lightSize / 2, y + height / 2, lightSize, Color.RED,
                        state == TrafficLight.State.RED);
                drawLight(x + 2 * spacing + 1.5 * lightSize, y + height / 2, lightSize, Color.YELLOW,
                        state == TrafficLight.State.YELLOW);
                drawLight(x + 3 * spacing + 2.5 * lightSize, y + height / 2, lightSize, Color.web("#34C759"),
                        state == TrafficLight.State.GREEN);
            }
        } else {
            if (isVertical) {
                drawLight(x + width / 2, y + spacing + lightSize / 2, lightSize, Color.RED,
                        state == TrafficLight.State.RED);
                drawLight(x + width / 2, y + 2 * spacing + 1.5 * lightSize, lightSize, Color.web("#34C759"),
                        state == TrafficLight.State.GREEN);
            } else {
                drawLight(x + spacing + lightSize / 2, y + height / 2, lightSize, Color.RED,
                        state == TrafficLight.State.RED);
                drawLight(x + 2 * spacing + 1.5 * lightSize, y + height / 2, lightSize, Color.web("#34C759"),
                        state == TrafficLight.State.GREEN);
            }
        }
    }

    private void drawLight(double centerX, double centerY, double size, Color color, boolean active) {
        Circle light = new Circle(centerX, centerY, size / 2);
        light.setFill(color);
        light.setOpacity(active ? 1.0 : 0.3);
        canvas.getChildren().add(light);
    }

    public void clear() {
        canvas.getChildren().clear();
    }
}
