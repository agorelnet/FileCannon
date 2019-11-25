#!/bin/bash
javac filecannon/Main.java
jar cmf META-INF/MANIFEST.MF FileCannon.jar filecannon/*.class filecannon/server/*.class filecannon/client/*.class
