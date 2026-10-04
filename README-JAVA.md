# Fortnite PE — Performance Edition (Java)

Fortnite PE is being built as a standalone Windows Java desktop game. It does **not** use a browser, HTML, or JavaScript.

## Windows build

Requirements:
- JDK 17 or newer
- Apache Maven

From the repository folder, double-click:

**build-windows.bat**

This runs Maven and produces:

`target\fortnite-pe-1.0.0.jar`

The JAR is packaged with the Java-side LWJGL dependencies and has a main class configured, so it can be launched with:

`java -jar target\fortnite-pe-1.0.0.jar`

## Windows launcher

After building, double-click:

**run-windows.bat**

If the JAR is missing, the launcher automatically runs the Windows build script first.

## Manual commands

```bat
mvn clean package
java -jar target\fortnite-pe-1.0.0.jar
```

The first build downloads LWJGL from Maven Central.

## Controls

- WASD — move
- Mouse — look
- Left click — shoot
- Right click — build
- Shift — sprint
- F1/F2 — build mode
- Esc — quit

## Project

- Java 17
- LWJGL 3.3.3
- OpenGL
- Maven
- Windows-native LWJGL runtime dependencies

Created by **JacobProjects**.

> This is an independent project and is not affiliated with or endorsed by Epic Games.
