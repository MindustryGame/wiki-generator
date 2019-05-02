package io.anuke.wikigen;

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

    public FileHandle file(String name){
        Config.outputDirectory.mkdirs();
        return Config.outputDirectory.child(type().name()).child(name + ".md");
    }

    public void write(String name, Consumer<PrintWriter> consumer){
        FileHandle file = file(name);
        PrintWriter writer = new PrintWriter(file.writer(false));
        consumer.accept(writer);
        writer.close();
    }

    public void generate(Array<T> array){
        array.each(content -> {
            Log.info("Generating file for '{0}'...", content);
            generate(content);
        });
    }

    public abstract void generate(T content);
}
