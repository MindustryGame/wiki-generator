package io.anuke.wikigen;

import io.anuke.arc.Core;
import io.anuke.arc.files.FileHandle;

/** General configuration. */
public class Config{
    public static final FileHandle outputDirectory = Core.files.local("../../../Mindustry-Wiki-Generator/output/");
    public static final FileHandle imageDirectory = outputDirectory.child("images");
    public static final FileHandle fileDirectory = Core.files.local("../../../Mindustry-Wiki-Generator/wiki-files/");
}
