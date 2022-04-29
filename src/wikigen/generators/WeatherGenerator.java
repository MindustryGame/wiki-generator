package wikigen.generators;

import arc.struct.*;
import mindustry.ctype.*;
import mindustry.type.*;
import mindustry.world.meta.*;
import wikigen.*;

//TODO implement better support
@Generates(ContentType.weather)
public class WeatherGenerator extends FileGenerator<Weather>{

    @Override
    public ObjectMap<String, Object> vars(Weather weather){
        return ObjectMap.of(
            "attrs", getAttributes(weather)
        );
    }

    public String getAttributes(Weather weather){
        StringBuilder s = new StringBuilder();
        for(Attribute a : Attribute.all){
            if(weather.attrs.get(a) != 0){
                if(!s.isEmpty()) s.append("\n");
                s.append(a.name).append(": ").append((int)(weather.attrs.get(a) * 100)).append("%");
            }
        }
        if(s.isEmpty()) return null;
        return s.toString();
    }

    @Override
    public String linkImage(Weather content){
        return "weather-" + content.name + "-ui";
    }
}
