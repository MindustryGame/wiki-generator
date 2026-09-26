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
    /** Where per-content nav icons (see {@link wikigen.FileGenerator#iconSvg}) are written.*/
    public static final Fi iconsOutDirectory = Core.files.local("../../../" + repo + "/overrides/.icons/custom/");
}
