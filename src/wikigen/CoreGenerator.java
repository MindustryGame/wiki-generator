package wikigen;

import arc.Core;
import arc.struct.ObjectMap;
import arc.util.Log;
import mindustry.Vars;
import mindustry.ctype.ContentType;
import wikigen.image.*;
import org.reflections.Reflections;

public class CoreGenerator{
    private static ObjectMap<ContentType, FileGenerator<?>> generators = new ObjectMap<>();

    /** Generates all the pages, loads the classes. */
    public static void generate(){
        Config.tmpDirectory.deleteDirectory();
        new TextureUnpacker().split(Core.files.local("sprites/sprites.atlas"), Config.imageDirectory);

        Reflections reflections = new Reflections("wikigen.generators");
        reflections.getTypesAnnotatedWith(Generates.class).forEach(type -> {
            try{
                ContentType content = type.getAnnotation(Generates.class).value();
                FileGenerator<?> generator = (FileGenerator)type.newInstance();
                generators.put(content, generator);
            }catch(Exception e){
                throw new RuntimeException(e);
            }
        });

        generators.each((type, generator) -> {
            Log.info("Generating content of type '{0}'...", type);
            generator.generate(Vars.content.getBy(type));
        });
    }

    /** Returns a generator instance by type.*/
    public static FileGenerator typeGenerator(ContentType type){
        if(!generators.containsKey(type)) throw new IllegalArgumentException("Content type '" + type + "' does not have any generators registered.");
        return generators.get(type);
    }
}
