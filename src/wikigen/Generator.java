package wikigen;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.g2d.TextureAtlas.*;
import arc.mock.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.ctype.*;
import mindustry.graphics.*;
import mindustry.net.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import org.reflections.*;
import wikigen.image.*;

import java.lang.reflect.*;

import static wikigen.Config.*;

@SuppressWarnings("unchecked")
public class Generator{
    private static final ObjectMap<ContentType, FileGenerator<?>> generators = new ObjectMap<>();
    private static final ObjectMap<String, UnlockableContent> regionToContent = new ObjectMap<>();

    public static void main(String[] args){
        Core.settings = new MockSettings();
        Core.app = new MockApplication();
        Core.files = new MockFiles();
        Core.audio = new MockAudio();
        Core.graphics = new MockGraphics();
        Core.input = new MockInput();

        //generate locale file manually
        if(!Core.files.local("locales").exists()){
            Core.files.local("locales").writeString("en");
        }

        Config.outDirectory.deleteDirectory();
        //copy over generated sprites.
        Core.files.local("../assets-raw/sprites_out").walk(file -> {
            file.copyTo(imageDirectory.child(file.name()));
        });

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
        Vars.net = new Net(null);
        Colors.put("accent", Pal.accent);
        Colors.put("stat", Pal.accent);
        Colors.put("health", Pal.health);
        MockScene.init();
        Vars.content.load();
        Vars.content.loadColors();
        Generator.generate();
        Splicer.splice();
    }

    /** Generates all the pages, loads the classes. */
    public static void generate(){
        try{
            for(var t : ContentType.all){
                for(var c : Vars.content.getBy(t)){
                    if(c instanceof UnlockableContent u){
                        if(u.uiIcon == null) continue;
                        regionToContent.put(((AtlasRegion)u.uiIcon).name, u);
                        regionToContent.put(((AtlasRegion)u.fullIcon).name, u);
                    }
                }
            }

            Reflections reflections = new Reflections("wikigen.generators");
            reflections.getTypesAnnotatedWith(Generates.class).forEach(type -> {
                try{
                    ContentType content = type.getAnnotation(Generates.class).value();
                    var generator = (FileGenerator<?>)type.getDeclaredConstructor().newInstance();
                    generators.put(content, generator);
                }catch(Exception e){
                    throw new RuntimeException(e);
                }
            });

            for(ContentType type : ContentType.all){
                //ignore sectors
                if(type == ContentType.sector) continue;

                var list = Vars.content.getBy(type);
                if(list.any() && list.first() instanceof UnlockableContent){
                    Fi templatef = rootDirectory.child("templates").child(type.name() + ".md");
                    if(templatef.exists()){
                        Log.info("Generating content of type '@'...", type);
                        var generator = get(type);

                        if(!generator.enabled()) continue;

                        for(var content : list.<UnlockableContent>as()){
                            if(!(content instanceof Planet p && p.sectors != null && p.sectors.size > 0) && content.isHidden() && !(content instanceof Block b && b.buildVisibility != BuildVisibility.hidden)){
                                continue;
                            }

                            var values = new ObjectMap<String, Object>();

                            for(Field field : content.getClass().getFields()){
                                values.put("" + field.getName(), field.get(content));
                            }

                            values.putAll(generator.vars(content));
                            values.put("stats", MockScene.scrapeStats(content));

                            Log.info("| Generating file for '@'...", content.name);

                            generator.file(content).writeString(generator.format(templatef.readString(), values));
                            generator.onGenerate(content);
                        }
                    }
                }
            }

            new VarGenerator().generate();

        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public static @Nullable UnlockableContent getByRegion(TextureRegion region){
        if(region instanceof AtlasRegion a){
            return regionToContent.get(a.name);
        }
        return null;
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
