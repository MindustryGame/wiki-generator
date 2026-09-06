package wikigen;

import arc.Core;
import arc.files.Fi;

/** General configuration. */
public class Config{
    public static final String repo = "wiki";
    public static final Fi outDirectory = Core.files.local("../../../Mindustry-Wiki-Generator/output/");
    public static final Fi rootDirectory = Core.files.local("../../../Mindustry-Wiki-Generator/");
    public static final Fi srcDirectory = Core.files.local("../../../Mindustry/core/src");
    public static final Fi docsDirectory = Core.files.local("../../../" + repo + "/docs");
    public static final Fi docsOutDirectory = Core.files.local("../../../" + repo + "/docs_out");
    public static final Fi imageDirectory = outDirectory.child("images");
    public static final Fi fileOutDirectory = docsOutDirectory;

    /** Where generated nav-icon SVGs are written during generation, mirroring {@link #imageDirectory}. */
    public static final Fi iconDirectory = outDirectory.child("icons");
    /** mkdocs-material looks up page-nav icons relative to the theme's `custom_dir` (see mkdocs.yml), not docs_out. */
    public static final Fi customIconOutDirectory = Core.files.local("../../../" + repo + "/overrides/.icons/custom/");
}
