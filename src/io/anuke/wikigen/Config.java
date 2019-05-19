package io.anuke.wikigen;

import io.anuke.arc.Core;
import io.anuke.arc.files.FileHandle;

/** General configuration. */
public class Config{
    public static final FileHandle tmpDirectory = Core.files.local("../../../Mindustry-Wiki-Generator/output/");
    public static final FileHandle imageDirectory = tmpDirectory.child("images");
    public static final FileHandle fileOutDirectory = Core.files.local("../../../wiki/docs/");
}
