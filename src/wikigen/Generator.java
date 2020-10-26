package wikigen;

import arc.*;
import arc.backend.headless.*;
import arc.files.*;
import arc.graphics.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.ctype.*;
import org.reflections.*;
import wikigen.image.*;

import java.lang.reflect.*;

import static wikigen.Config.*;

@SuppressWarnings("unchecked")
public class Generator{
    private static ObjectMap<ContentType, FileGenerator<?>> generators = new ObjectMap<>();

    public static void main(String[] args){
        new HeadlessApplication(new ApplicationListener(){}){
            @Override
            protected void initialize(){
                //generate locale file manually
                if(!Core.files.local("locales").exists()){
                    Core.files.local("locales").writeString("en");
                }

                ArcNativesLoader.load();

                Version.enabled = false;
                Vars.headless = true;
                Vars.loadSettings();
                Vars.init();
                Vars.content.createBaseContent();
                Vars.world = new World();
                Vars.logic = new Logic();
                Vars.content.init();
                Vars.state = new GameState();
                MockScene.init();
                Vars.content.load();
                Generator.generate();
                Splicer.splice();
            }
        };
    }

    /** Generates all the pages, loads the classes. */
    public static void generate(){
        try{

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

            for(ContentType type : ContentType.all){
                var list = Vars.content.getBy(type);
                if(list.any() && list.first() instanceof UnlockableContent){
                    Fi templatef = rootDirectory.child("templates").child(type.name() + ".md");
                    if(templatef.exists()){
                        Log.info("Generating content of type '@'...", type);
                        FileGenerator generator = get(type);

                        for(var content : list.<UnlockableContent>as()){
                            if(content.isHidden()){
                                continue;
                            }

                            var values = generator.vars(content);
                            values.put("stats", MockScene.scrapeStats(content));

                            for(Field field : generator.getClass().getDeclaredFields()){
                                if(field.isAccessible()){
                                    values.put("" + field.getName(), field.get(content));
                                }
                            }

                            StringBuilder template = new StringBuilder(templatef.readString());
                            values.each((key, val) -> {
                                if(!str(val).isEmpty()){
                                    Strings.replace(template, "$" + key, str(val));
                                }
                            });
                            generator.file(content.name).writeString( Strings.join("\n", Seq.with(template.toString().split("\n")).select(s -> !s.contains("$"))));

                            Log.info("| Generating file for '@'...", content.name);
                        }
                    }
                }
            }

        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    /** Stringifies an object for display.*/
    public static String str(Object obj){
        if(obj instanceof Boolean b){
            return b ? "Yes" : "No";
        }else if(obj instanceof Float f){
            return Strings.autoFixed(f, 3);
        }else if(obj instanceof Color c){
            return c.toString();
        }else if(obj == null){
            return "Unknown...";
        }
        return obj + "";
    }

    /** Returns a generator instance by type.*/
    public static FileGenerator get(ContentType type){
        return generators.get(type, () -> new FileGenerator<>(type));
    }
}
