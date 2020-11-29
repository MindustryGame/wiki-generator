package wikigen;

import arc.*;
import arc.Net.*;
import arc.files.*;
import arc.graphics.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;
import com.github.javaparser.*;
import com.github.javaparser.ast.body.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.net.*;
import mindustry.server.*;
import mindustry.type.*;
import org.reflections.*;

import java.lang.reflect.*;
import java.util.*;

/** Generates and replaces variables in markdown files. */
@SuppressWarnings("unchecked")
public class VarGenerator{
    private static final NetJavaImpl net = new NetJavaImpl();

    static{
        net.setBlock(true);
    }

    public ObjectMap<String, Object> makeVariables() throws Exception{
        var out = new ObjectMap<String, Object>();

        out.put("sounds", Seq.with(Sounds.class.getFields()).toString(" ", f -> "`" + f.getName() + "`"));
        out.put("contentTypes", Seq.with(ContentType.all).select(c -> !c.name().contains("UNUSED")).toString(" ", c -> "`" + c.name() + "`"));
        out.put("bundles", Seq.with(Core.files.local("locales").readString().split("\n")).toString(" ", c -> "`" + c + "`"));

        //create dummy server to scrape its commands
        var cont = new ServerControl(null){
           @Override
           public void setup(String[] args){
               registerCommands();
           }
        };

        out.put("serverCommands", cont.handler.getCommandList().toString("\n", command -> "- `" + command.text + (command.paramText.isEmpty() ? "" : " ") + command.paramText + "`: *" + command.description + "*"));
        out.put("serverConfigs", Seq.with(Administration.Config.all).toString("\n", conf -> "- `" + conf.name() + "`: *" + conf.description + "*"));

        net.http(new HttpRequest().method(HttpMethod.GET).url("https://api.github.com/repos/Anuken/Mindustry/releases").header("Accept", "application/vnd.github.v3+json"), response -> {
            if(response.getStatus() == HttpStatus.OK){
                Jval json = Jval.read(response.getResultAsString());
                String latestRelease = json.asArray().first().getString("tag_name").substring(1);
                out.put("latestRelease", latestRelease);
            }
        }, Log::err);

        out.put("allTypes", genTypes());

        return out;
    }

    Set<Class> fetchTypes(String pack, Class sub){
        var reflections = new Reflections(pack);
        var allClasses = reflections.getSubTypesOf(sub);
        allClasses.add(sub);
        return allClasses;
    }

    String fetchFields(Class type){
        return Seq.with(type.getFields()).toString(" ", f -> "`" + f.getName() + "`");
    }

    public String genTypes() throws Exception{
        var out = new StringBuilder();
        var allClasses = fetchTypes("mindustry", MappableContent.class);
        allClasses.addAll(fetchTypes("mindustry.entities.effect", Effect.class));
        allClasses.add(Weapon.class);

        var parser = new JavaParser();
        var builtIns = StringMap.of(
        "effect", fetchFields(Fx.class),
        "bullet", fetchFields(Bullet.class),
        "status", fetchFields(StatusEffects.class)
        );

        class Ref{
            Class c;
            String type;
            Object instance;

            Ref(Class c, String type, Object instance){
                this.c = c;
                this.type = type;
                this.instance = instance;
            }
        }

        var refs = new Seq<Ref>();
        var counts = new ObjectIntMap<String>();

        for(var c : allClasses){
            if(Modifier.isAbstract(c.getModifiers()) || c.isAnonymousClass()) continue;

            Constructor cons;
            Object instance;

            //skip non-string constructors
            try{
                cons = c.getConstructor(String.class);
                instance = cons.newInstance("__typeof" + c.getSimpleName());
            }catch(Exception ignored){
                try{
                    cons = c.getConstructor();
                    instance = cons.newInstance();
                }catch(Exception ignored2){
                    continue;
                }
            }

            String type = instance instanceof Content cont ? cont.getContentType().toString() : instance instanceof Effect ? "effect" : "zzz_other";
            counts.increment(type);

            refs.add(new Ref(c, type, instance));
        }

        refs.sort(((Comparator<Ref>)((a, b) -> -Boolean.compare(a.c.isAssignableFrom(b.c), b.c.isAssignableFrom(a.c)))).thenComparing(r -> r.type).thenComparing(f -> f.c.getSimpleName()));

        String lastType = null;

        for(var ref : refs){
            if(!ref.type.equals(lastType)){
                out.append("\n# ").append(Strings.capitalize(ref.type.replace("zzz_", ""))).append("\n");
                lastType = ref.type;

                if(builtIns.containsKey(ref.type)){
                    out.append("Built-in constants:  \n\n").append(builtIns.get(ref.type)).append("  \n  ");
                }

                out.append("\n");
            }

            var c = ref.c;
            var path = c.getCanonicalName().replace('.', '/') + ".java";
            var supclass = c.getSuperclass().getSimpleName();

            Log.info("Parsing @", path);

            if(counts.get(ref.type) > 1 || ref.type.contains("zzz_")){
                out.append("## ").append(c.getSimpleName()).append("\n\n");
            }

            out.append("*extends ").append("[").append(supclass).append("](#").append(supclass.toLowerCase()).append(")*\n\n");

            var cu = parser.parse(Config.srcDirectory.child(path).file()).getResult().orElseThrow();
            var typeDec = cu.getTypes().getFirst().orElseThrow();

            if(typeDec.getJavadoc().isPresent()){
                out.append(typeDec.getJavadoc().get().toText()).append("\n");
            }

            boolean anyFields = false;

            var outf = new StringBuilder();

            outf.append("""
            |field|type|default|notes|
            |---|---|---|---|
            """);

            var members = typeDec.getMembers();
            if(members != null){
                for(var member : members){
                    if(member instanceof FieldDeclaration field){
                        if(field.isStatic() || !field.isPublic()) continue;

                        for(var variable : field.getVariables()){
                            var baseField = c.getField(variable.getNameAsString());
                            var value = baseField.get(ref.instance);
                            var initValue = variable.getInitializer().isEmpty() ? null : variable.getInitializer().get().toString();

                            //array init
                            if(initValue != null && initValue.equals("{}")){
                                initValue = "[]";
                            }

                            //special array init
                            if(initValue != null && initValue.contains("new") && initValue.contains("[") && initValue.contains("]")){
                                initValue = "[]";
                            }

                            //assign to last, making sure it's not a number
                            if(initValue != null && initValue.contains(".")){
                                var split = initValue.split("\\.");
                                initValue = split[split.length - 1];
                            }

                            //special overrides
                            if(value instanceof Color || value instanceof Vec2 || value instanceof Number){
                                initValue = String.valueOf(value);
                            }

                            //remove f suffix
                            if(variable.getTypeAsString().equals("float") && initValue != null && initValue.endsWith("f")){
                                initValue = initValue.substring(0, initValue.length() - 1);
                            }

                            //remove lambdas
                            if(initValue != null && initValue.contains("->")){
                                initValue = "{code}";
                            }

                            anyFields = true;
                            outf
                            .append("|").append(variable.getName())
                            .append("|").append(variable.getTypeAsString())
                            .append("|").append(initValue == null ? value : initValue)
                            .append("|").append(field.getJavadoc().isPresent() ? field.getJavadoc().get().toText().replace("\n", " ") : " ").append("|\n");
                        }
                    }
                }
            }

            if(anyFields){
                out.append(outf);
            }

            out.append("\n\n");
        }

        return out.toString();
    }

    public void generate() throws Exception{
        var values = makeVariables();

        Config.docsOutDirectory.deleteDirectory();
        Config.docsOutDirectory.delete();
        Config.docsOutDirectory.mkdirs();

        for(Fi file : Config.docsDirectory.list()){
            file.copyTo(Config.docsOutDirectory);
        }

        Config.docsOutDirectory.walk(f -> {
            if(f.extEquals("md")){
                StringBuilder template = new StringBuilder(f.readString());
                values.each((key, val) -> {
                    if(!Generator.str(val).isEmpty()){
                        Strings.replace(template, "$" + key, Generator.str(val));
                    }
                });
                f.writeString(Strings.join("\n", Seq.with(template.toString().split("\n")).select(s -> !s.contains("$"))));
            }
        });
    }
}
