package wikigen;

import arc.files.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;

import java.lang.reflect.*;

/** Generates and replaces variables in markdown files. */
public class VarGenerator{

    public ObjectMap<String, Object> makeVariables(){
        var out = new ObjectMap<String, Object>();

        out.put("sounds", Seq.with(Sounds.class.getFields()).toString(", ", Field::getName));

        return out;
    }

    public void generate(){
        var values = makeVariables();

        Config.docsOutDirectory.deleteDirectory();

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
