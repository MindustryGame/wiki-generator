package wikigen.generators;

import mindustry.ctype.*;
import mindustry.type.*;
import wikigen.*;

//TODO implement better support
@Generates(ContentType.weather)
public class WeatherGenerator extends FileGenerator<Weather>{

    @Override
    public String linkImage(Weather content){
        return "weather-" + content.name + "-ui";
    }
}
