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
import mindustry.ctype.*;
import mindustry.gen.*;
import mindustry.net.*;
import mindustry.server.*;
import mindustry.world.*;
import org.reflections.*;

import java.lang.reflect.*;

/** Generates and replaces variables in markdown files. */
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

        out.put("blockTypes", genTypes());

        return out;
    }

    public String genTypes() throws Exception{
        var out = new StringBuilder();
        var reflections = new Reflections("mindustry.world.blocks");
        var allClasses = reflections.getSubTypesOf(Block.class);
        allClasses.add(Block.class);
        var parser = new JavaParser();

        var sorted = Seq.with(allClasses).sortComparing(Class::getSimpleName);

        for(var c : allClasses){
            if(Modifier.isAbstract(c.getModifiers())) continue;

            var path = c.getCanonicalName().replace('.', '/') + ".java";
            Log.info("Parse @", path);

            out.append("## ").append(c.getSimpleName()).append("\n\n");

            out.append("*extends ").append(c.getSuperclass().getSimpleName()).append("*\n\n");

            var result = parser.parse(Config.srcDirectory.child(path).file());

            if(result.getProblems().size() > 0){
                Log.info(result.getProblems().toString());
            }

            var cu = result.getResult().orElseThrow();
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

            //skip non-string constructors
            try{
                c.getConstructor(String.class);
            }catch(Exception ignored){
                continue;
            }

            var instance = c.getConstructor(String.class).newInstance("__typeof" + c.getSimpleName());

            var members = typeDec.getMembers();
            if(members != null){
                for(var member : members){
                    if(member instanceof FieldDeclaration field){
                        if(field.isStatic() || !field.isPublic()) continue;

                        for(var variable : field.getVariables()){
                            var baseField = c.getField(variable.getNameAsString());
                            var value = baseField.get(instance);
                            var initValue = variable.getInitializer().isEmpty() ? null : variable.getInitializer().get().toString();

                            //special overrides
                            if(value instanceof Color || value instanceof Vec2 || value instanceof Number){
                                initValue = String.valueOf(value);
                            }

                            //remove f suffix
                            if(variable.getTypeAsString().equals("float") && initValue != null && initValue.endsWith("f")){
                                initValue = initValue.substring(0, initValue.length() - 1);
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
