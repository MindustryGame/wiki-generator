package wikigen;

import arc.Core;
import arc.files.Fi;

/** General configuration. */
public class Config{
    public static final String repo = "wiki-testing";
    public static final Fi outDirectory = Core.files.local("../../../Mindustry-Wiki-Generator/output/");
    public static final Fi rootDirectory = Core.files.local("../../../Mindustry-Wiki-Generator/");
    public static final Fi imageDirectory = outDirectory.child("images");
    public static final Fi fileOutDirectory = Core.files.local("../../../wiki-testing/docs/");
}
