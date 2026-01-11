@echo off

echo Building with Maven...
call mvn clean javafx:jlink

if not exist target\traffic-sim-image (
    echo Maven build/jlink failed.
    exit /b 1
)

echo Packaging for Windows (.exe)...

set VERSION=1.0.0

jpackage ^
  --name "TrafficSimulation" ^
  --type app-image ^
  --runtime-image "target/traffic-sim-image" ^
  --module com.traffic.sim/com.traffic.sim.app.App ^
  --app-version "%VERSION%" ^
  --vendor "Group1" ^
  --dest "target/dist" ^
  --win-console

echo Done! Check target\dist\TrafficSimulation
