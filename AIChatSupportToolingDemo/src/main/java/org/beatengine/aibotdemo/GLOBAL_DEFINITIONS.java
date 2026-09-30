package org.beatengine.aibotdemo;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GLOBAL_DEFINITIONS {
    public static String ROOT_ENVIRONMENT_PATH = new File(Paths.get("").toAbsolutePath().getParent().toFile(), "environmentconfigurations.env").getAbsolutePath();
}
