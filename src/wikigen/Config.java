package wikigen;

import arc.Core;
import arc.files.Fi;

/** General configuration. */
public class Config{
    public static final String repo = "wiki-testing";
    public static final Fi outDirectory = Core.files.local("../../../Mindustry-Wiki-Generator/output/");
    public static final Fi rootDirectory = Core.files.local("../../../Mindustry-Wiki-Generator/");
    public static final Fi docsDirectory = Core.files.local("../../../" + repo + "/docs");
    public static final Fi docsOutDirectory = Core.files.local("../../../" + repo + "/docs_out");
    public static final Fi imageDirectory = outDirectory.child("images");
    public static final Fi fileOutDirectory = docsOutDirectory;
}
