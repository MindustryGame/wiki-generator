package wikigen;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.serialization.JsonWriter.*;
import arc.util.serialization.*;
import arc.util.serialization.Jval.*;
import com.github.javaparser.*;
import com.github.javaparser.ast.body.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.net.*;
import mindustry.server.*;
import mindustry.type.*;
import mindustry.world.blocks.legacy.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import org.reflections.*;
import wikigen.Generator.*;

import java.util.*;

import static arc.util.Log.*;

/** Generates and replaces variables in markdown files. */
@SuppressWarnings("unchecked")
public class VarGenerator{

    public ObjectMap<String, Object> makeVariables() throws Exception{
        var out = new ObjectMap<String, Object>();

        out.put("sounds", Seq.with(Sounds.class.getFields()).toString(" ", f -> "`" + f.getName() + "`"));
        out.put("contentTypes", Seq.with(ContentType.all).select(c -> !c.name().contains("UNUSED")).toString(" ", c -> "`" + c.name() + "`"));
        out.put("bundles", Seq.with(Core.files.local("locales").readString().split("\n")).toString(" ", c -> "`" + c + "`"));
        out.put("blockGroups", Seq.with(BlockGroup.values()).toString("\n", g -> "- `" + g.name() + "`"));
        out.put("buildVisibilities", Seq.with(BuildVisibility.class.getFields()).toString("\n", g -> "- `" + g.getName() + "`"));

        //create dummy server to scrape its commands
        var cont = new ServerControl(null){
           @Override
           public void setup(String[] args){
               registerCommands();
           }
        };

        out.put("serverCommands", cont.handler.getCommandList().toString("\n", command -> "- `" + command.text + (command.paramText.isEmpty() ? "" : " ") + command.paramText + "`: *" + command.description + "*"));
        out.put("serverConfigs", Seq.with(Administration.Config.all).toString("\n", conf -> "- `" + conf.name + "`: *" + conf.description + "*"));

        Http.get("https://api.github.com/repos/Anuken/Mindustry/releases").header("Accept", "application/vnd.github.v3+json").block(response -> {
            Jval json = Jval.read(response.getResultAsString());
            String latestRelease = json.asArray().first().getString("tag_name").substring(1);
            out.put("latestRelease", latestRelease);
            String latestReleaseLink = json.asArray().first().getString("html_url");
            out.put("latestReleaseLink", latestReleaseLink);
        });

        //TODO wrong
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
        var allClasses = fetchTypes("mindustry", MappableContent.class);
        allClasses.addAll(fetchTypes("mindustry.entities.effect", Effect.class));
        allClasses.addAll(fetchTypes("mindustry.entities.abilities", Ability.class));
        allClasses.addAll(fetchTypes("mindustry.entities.bullet", BulletType.class));
        allClasses.addAll(fetchTypes("mindustry.type.weapons", Weapon.class));
        allClasses.addAll(fetchTypes("mindustry.type.weather", Weather.class));
        allClasses.addAll(fetchTypes("mindustry.world.draw", DrawBlock.class));
        allClasses.add(Weapon.class);
        allClasses.add(SectorPreset.class);

        var parser = new JavaParser();
        var builtIns = StringMap.of(
        "effect", fetchFields(Fx.class),
        "bullet", fetchFields(Bullets.class),
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
        var allContent = Seq.with(Vars.content.getContentMap()).<Content>flatten().select(o -> o.minfo.mod != null);

        for(var c : allClasses){
            if(c.isAnonymousClass() || c.isAnnotationPresent(Deprecated.class) || LegacyBlock.class.isAssignableFrom(c)) continue;

            Object instance = null;

            if(c == BulletType.class) instance = new BulletType();
            if(c == Ability.class) instance = new Ability(){};
            if(c == SectorPreset.class) instance = new SectorPreset("sectorName", "groundZero", Planets.serpulo, 5);

            if(instance == null){
                //skip non-string constructors
                try{
                    instance = c.getConstructor(String.class).newInstance(Strings.capitalize(c.getSimpleName()) + " Name");
                }catch(Exception ignored){
                    try{
                        var cons = c.getDeclaredConstructor();
                        cons.setAccessible(true);
                        instance = cons.newInstance();
                    }catch(Exception ignored2){
                        continue;
                    }
                }
            }

            //TODO garbage code?? need to list subclasses as well...
            String type = instance instanceof Content cont ? cont.getContentType().toString() : instance instanceof Effect ? "effect" : "zzz_other";
            counts.increment(type);

            refs.add(new Ref(c, type, instance));
        }

        refs.sort(((Comparator<Ref>)((a, b) -> -Boolean.compare(a.c.isAssignableFrom(b.c), b.c.isAssignableFrom(a.c)))).thenComparing(r -> r.type).thenComparing(f -> f.c.getSimpleName()));


        for(var ref : refs){
            var out = new StringBuilder();

            if(builtIns.containsKey(ref.type)){
                out.append("Built-in constants:  \n\n").append(builtIns.get(ref.type)).append("  \n  ");
            }

            out.append("\n");

            var c = ref.c;
            var path = c.getCanonicalName().replace('.', '/') + ".java";
            var supclass = c.getSuperclass().getSimpleName();

            //pick JSON objects of approximately average length; long files are not used, as those tend to have too many long particle effects.
            //TODO better selection criteria
            float complexity = 0.5f;

            Object example = allContent.select(cont -> cont.getClass() == c && cont.minfo.sourceFile != null && cont.minfo.sourceFile.length() < 1024 * 5).sort(cont -> cont.minfo.mod.file.length()).getFrac(complexity);
            if(example == null){
                example = Generator.parsed.select(p -> p.object != null && p.object.getClass() == c).sort(p -> p.json.toJson(OutputType.json).length()).getFrac(complexity);
            }

            String exampleJson = example == null ? null : example instanceof Content cont ? cont.minfo.sourceFile.readString() : ((ParseRecord)example).json.toJson(OutputType.json);

            info("Parsing @@", path, example == null ? "" : " &lb(found example)&fr");

            out.append("## ").append(c.getSimpleName()).append("\n\n");

            //TODO do not link non-existent stuff
            out.append("*extends ").append("[").append(supclass).append("](").append(supclass).append(".md)*\n\n");

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

                            if(value instanceof Seq){
                                initValue = value + "";
                            }

                            //assign to last, making sure it's not a number
                            if(initValue != null && initValue.contains(".") && !(baseField.getType().isArray())){
                                var split = initValue.split("\\.");
                                initValue = split[split.length - 1];
                            }

                            if(initValue != null && value instanceof Object[] o){
                                initValue = Arrays.toString(o);
                            }

                            if(initValue != null && value instanceof float[] f){
                                initValue = Arrays.toString(f);
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
                            .append("|").append(determineJavadoc(field,variable)).append("|\n");
                        }
                    }
                }
            }

            if(example != null){
                var read = Jval.read(exampleJson);

                //a single string is a terrible example.
                if(read.isObject()){
                    String json = Jval.read(exampleJson).toString(Jformat.hjson);

                    if(!json.trim().isEmpty()){
                        outf.append("\n#### Example");
                        if(example instanceof UnlockableContent cont){
                            Log.info(cont.minfo.sourceFile.path());
                            String realPath = "https://github.com/BlueWolf3682/Exotic-Mod/tree/master" + cont.minfo.sourceFile.path().replace("Exotic-Mod-master", "");
                            outf.append(" ").append(" [(\"").append(cont.localizedName).append("\")](").append(realPath).append(")");
                        }
                        outf.append("\n");
                        outf.append("```\n");

                        outf.append(json);

                        outf.append("```\n");
                    }
                }
            }

            if(anyFields){
                out.append(outf);
            }

            out.append("\n\n");

            Config.outDirectory.child("Modding Classes").child(c.getSimpleName() + ".md").writeString(out.toString());
        }

        return ""; //TODO remove
    }

    private String determineJavadoc(FieldDeclaration field, VariableDeclaration variable){
        if(variable.getComment().isPresent()){
            return variable.getComment().get().getContent().replace("\n", " ");
        }else if(field.getJavadoc().isPresent()){
            return field.getJavadoc().get().toText().replace("\n", " ");
        }else{
            return " ";
        }
        
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
