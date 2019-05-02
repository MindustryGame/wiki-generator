package io.anuke.wikigen;

import io.anuke.arc.Core;
import io.anuke.arc.files.FileHandle;

public class Config{
    public static final FileHandle outputDirectory = Core.files.external("wiki-output/");
}
