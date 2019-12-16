package io.anuke.wikigen;

import io.anuke.arc.Core;
import io.anuke.arc.files.Fi;

/** General configuration. */
public class Config{
    public static final Fi tmpDirectory = Core.files.local("../../../Mindustry-Wiki-Generator/output/");
    public static final Fi imageDirectory = tmpDirectory.child("images");
    public static final Fi fileOutDirectory = Core.files.local("../../../wiki/docs/");
}
