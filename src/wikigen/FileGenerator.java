package wikigen;

import arc.files.*;
import arc.graphics.g2d.TextureAtlas.*;
import arc.struct.*;
import arc.util.*;
import mindustry.ctype.*;

import static wikigen.Config.*;

/** Represents a generator for a type of content.
 * Each subclass should be placed in generators/ and annotated with {@link Generates} to indicate its content type.*/
public class FileGenerator<T extends UnlockableContent>{
    private ContentType otype;

    public FileGenerator(ContentType otype){
        this.otype = otype;
    }

    public FileGenerator(){
    }

    public ObjectMap<String, Object> vars(T content){
        return ObjectMap.of();
    }

    /** Generate additional pages here. */
    public void onGenerate(T content){

    }

    /** @return whether to generate a page for a specific piece of content. */
    public boolean enabled(T content){
        return !content.isHidden();
    }

    public ContentType type(){
        return otype != null ? otype : getClass().getAnnotation(Generates.class).value();
    }

    public String format(String temp, ObjectMap<String, Object> vars){
        StringBuilder template = new StringBuilder(temp);
        vars.put("repo", repo);
        //sort keys by length so longer variables get replaced with invalid symbols first
        Seq<String> keys = vars.keys().toSeq().sort(s -> -s.length());
        keys.each(key -> {
            var val = vars.get(key);
            var str = Generator.str(val);
            Strings.replace(template, "$" + key, str.isEmpty() ? "$INVALID!" : str);
        });

        return Strings.join("\n", Seq.with(template.toString().split("\n")).select(s -> !s.contains("$")));
    }

    /** Returns a markdown file with this name in the output directory with this generator's type name.*/
    public Fi file(T t){
        return Config.outDirectory.child(folder(t)).child(linkPath(t) + ".md");
    }

    public String folder(T content){
        return type().name().endsWith("s") ? type().name() + "es" : type().name() + "s";
    }

    /** @return an image link for this content with a correct icon and path. */
    public final String makeLink(T content){
        return Strings.format("<a href=\"/@/@\"><img id=\"@\" src=\"/@/images/@.png\"/></a>", Config.repo, folder(content) + "/" + linkPath(content), imageStyle(), Config.repo, linkImage(content));
    }

    public final String makeImageLink(String imageFolderPath){
        return Strings.format("<img src=\"/@/images/@.png\"/>", Config.repo, imageFolderPath);
    }

    public String imageStyle(){
        return "spr";
    }

    /** @return the name of the image this content should use in links without an extension or additional paths.*/
    public String linkImage(T content){
        return ((AtlasRegion)content.uiIcon).name;
    }

    /** @return the file name of this content in its folder, without the `type/` prefix or extension.*/
    public String linkPath(T content){
        return content.id + "-" + content.name;
    }

    /** @return a markdown image link to this content.*/
    @SuppressWarnings("unchecked")
    public final String link(UnlockableContent content){
        return Generator.get(content.getContentType()).makeLink(content);
    }

    /** @return a string representing a list of links to related content. */
    public String links(Iterable<? extends UnlockableContent> list){
        StringBuilder build = new StringBuilder();
        for(var c : list){
            if(c == null) continue;
            build.append(link(c).replace("\"spr\"", "\"sprlist\"")).append(" ");
        }
        return build.toString();
    }
}
