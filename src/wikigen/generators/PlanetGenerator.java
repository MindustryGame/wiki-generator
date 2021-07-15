package wikigen.generators;

import arc.struct.*;
import mindustry.ctype.*;
import mindustry.type.*;
import wikigen.*;

//TODO implement
@Generates(ContentType.planet)
public class PlanetGenerator extends FileGenerator<Planet>{

    @Override
    public boolean enabled(){
        return false;
    }

    @Override
    public void onGenerate(Planet planet){

    }

    @Override
    public ObjectMap<String, Object> vars(Planet content){
        return ObjectMap.of();
    }
}
