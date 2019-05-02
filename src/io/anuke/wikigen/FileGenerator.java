package io.anuke.wikigen;

import io.anuke.arc.Core;
import io.anuke.arc.collection.Array;
import io.anuke.arc.collection.ObjectMap;
import io.anuke.arc.files.FileHandle;
import io.anuke.arc.function.Consumer;
import io.anuke.arc.util.Log;
import io.anuke.arc.util.Strings;
import io.anuke.mindustry.game.Content;
import io.anuke.mindustry.game.UnlockableContent;
import io.anuke.mindustry.type.ContentType;

import java.io.PrintWriter;

public abstract class FileGenerator<T extends Content>{

    public ContentType type(){
        return getClass().getAnnotation(Generates.class).value();
    }

    /** Returns a markdown file with this name in the output directory with this generator's type name.*/
    public FileHandle file(String name){
        Config.outputDirectory.mkdirs();
        return Config.outputDirectory.child(type().name()).child(name + ".md");
    }

    /** Creates a writer for writing to this {@link io.anuke.wikigen.FileGenerator#file}.*/
    public void write(String name, Consumer<PrintWriter> consumer){
        FileHandle file = file(name);
        PrintWriter writer = new PrintWriter(file.writer(false));
        consumer.accept(writer);
        writer.close();
    }

    /** @see #template(String, ObjectMap) */
    public void template(String name, Object... values){
        template(name, ObjectMap.of(values));
    }

    /** Loads a template using this generator's type name,
     * then uses the provided map of values to write the result to the provided {@link io.anuke.wikigen.FileGenerator#file} by name.
     * Strings in the template are replaced in the format $key -> value.*/
    public void template(String name, ObjectMap<String, Object> values){
        StringBuilder template = new StringBuilder(Core.files.internal("../../../Mindustry-Wiki-Generator/templates/" + type().name() + ".md").readString());
        values.each((key, val) -> {
            if(!str(val).isEmpty()){
                Strings.replace(template, "$" + key, str(val));
            }
        });
        file(name).writeString( Strings.join("\n", Array.with(template.toString().split("\n")).select(s -> !s.contains("$"))));
    }

    /** @return an image link for this content with a correct icon and path. */
    public final String makeLink(T content){
        String name = content instanceof UnlockableContent ? ((UnlockableContent)content).localizedName : "";
        return Strings.format("[![{0}]({5}/{1}.png)]({3}/{4}/{2}.md)", name, linkImage(content), linkPath(content), Config.outputDirectory.path(), content.getContentType().name(), Config.imageDirectory.path());
    }

    protected abstract String linkImage(T content);

    protected  String linkPath(T content){
        return ((UnlockableContent)content).name;
    }

    /** Returns a markdown image link to this content.*/
    @SuppressWarnings("unchecked")
    public final String link(Content content){
        return PageGenerator.typeGenerator(content.getContentType()).makeLink(content);
    }

    public String links(Iterable<? extends Content> list){
        StringBuilder build = new StringBuilder();
        for(Content c : list){
            build.append(link(c)).append(" ");
        }
        return build.toString();
    }

    /** Stringifies an object for display.*/
    public String str(Object obj){
        if(obj instanceof Boolean){
            return (Boolean)obj ? "Yes" : "No";
        }
        return obj + "";
    }

    public void generate(Array<T> array){
        array.each(content -> {
            Log.info("| Generating file for '{0}'...", content);
            generate(content);
        });
    }

    public abstract void generate(T content);
}
