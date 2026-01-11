#!/bin/bash

# 1. Clean and build with Maven
echo "Building with Maven..."
mvn clean javafx:jlink

# 2. Check if build was successful
if [ ! -d "target/traffic-sim-image" ]; then
    echo "Maven build/jlink failed. Check errors above."
    exit 1
fi

# 3. Use jpackage to create .app (macOS)
# Note: --module-path points to the jlink image created by Maven
# We use the module name defined in module-info.java (com.traffic.sim) and the main class
echo "Packaging for macOS (.app)..."

# Determine version
VERSION="1.0.0"

jpackage \
  --name "TrafficSimulation" \
  --type app-image \
  --runtime-image "target/traffic-sim-image" \
  --module com.traffic.sim/com.traffic.sim.app.App \
  --app-version "$VERSION" \
  --vendor "Group1" \
  --dest "target/dist"

# Note: If you want a DMG installer, change --type app-image to --type dmg
# You might need an .icns icon file for proper Mac appearance. 
# I added a placeholder icon path, remove it if you don't have one yet.

echo "Done! check target/dist/TrafficSimulation.app"
