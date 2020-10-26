package wikigen;

import arc.*;
import arc.files.*;
import arc.func.*;
import arc.graphics.g2d.TextureAtlas.*;
import arc.struct.*;
import arc.util.*;
import mindustry.ctype.*;
import mindustry.ui.*;

import java.io.*;

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

    public ContentType type(){
        return otype != null ? otype : getClass().getAnnotation(Generates.class).value();
    }

    /** Returns a markdown file with this name in the output directory with this generator's type name.*/
    public Fi file(String name){
        Config.tmpDirectory.mkdirs();
        return Config.tmpDirectory.child(displayType(type())).child(name + ".md");
    }

    /** Creates a writer for writing to this {@link wikigen.FileGenerator#file}.*/
    public void write(String name, Cons<PrintWriter> consumer){
        Fi file = file(name);
        PrintWriter writer = new PrintWriter(file.writer(false));
        consumer.get(writer);
        writer.close();
    }

    /** @see #template(String, ObjectMap) */
    public void template(String name, Object... values){
        template(name, ObjectMap.of(values));
    }

    /** Loads a template using this generator's type name,
     * then uses the provided map of values to write the result to the provided {@link wikigen.FileGenerator#file} by name.
     * Strings in the template are replaced in the format $key -> value.
     * If a line contains a template string (e.g. a string with $ in it), and no key is provided, that line is automatically removed. */
    public void template(String name, ObjectMap<String, Object> values){
        StringBuilder template = new StringBuilder(Core.files.internal("../../../Mindustry-Wiki-Generator/templates/" + type().name() + ".md").readString());
        values.each((key, val) -> {
            if(!Generator.str(val).isEmpty()){
                Strings.replace(template, "$" + key, Generator.str(val));
            }
        });
        file(name).writeString( Strings.join("\n", Seq.with(template.toString().split("\n")).select(s -> !s.contains("$"))));
    }

    /** @return an image link for this content with a correct icon and path. */
    public final String makeLink(T content){
        return Strings.format("<a href=\"/wiki/@\"><img id=\"@\" src=\"/wiki/images/@.png\"/></a>", displayType(content.getContentType()) + "/" + linkPath(content), imageStyle(), linkImage(content));
    }

    public String imageStyle(){
        return "spr";
    }

    /** @return the name of the image this content should use in links without an extension or additional paths.*/
    public String linkImage(T content){
        return ((AtlasRegion)content.icon(Cicon.medium)).name;
    }

    /** @return the file name of this content in its folder, without the `type/` prefix or extension.*/
    public String linkPath(T content){
        return ((UnlockableContent)content).name;
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

    public String displayType(ContentType cat){
        return cat.name() + "s";
    }
}
