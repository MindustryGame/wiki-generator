package io.anuke.wikigen;

import io.anuke.arc.Core;
import io.anuke.arc.collection.Array;
import io.anuke.arc.files.FileHandle;
import io.anuke.arc.function.Consumer;
import io.anuke.arc.util.Log;
import io.anuke.mindustry.game.Content;
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

    /** Loads a template using this generator's type name,
     * then uses the provided map of values to write the result to the provided {@link io.anuke.wikigen.FileGenerator#file} by name.
     * Strings in the template are replaced in the format $key -> value.*/
    public void template(String name, Object... values){
        String template = Core.files.internal("../../../Mindustry-Wiki-Generator/templates/" + type().name() + ".md").readString();
        for(int i = 0; i < values.length; i+= 2){
            template = template.replace("$" + values[i], String.valueOf(values[i+1]));
        }
        file(name).writeString(template);
    }

    public void generate(Array<T> array){
        array.each(content -> {
            Log.info("| Generating file for '{0}'...", content);
            generate(content);
        });
    }

    public abstract void generate(T content);
}
