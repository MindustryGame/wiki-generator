package io.anuke.wikigen.generators;

import io.anuke.mindustry.*;
import io.anuke.mindustry.type.*;
import io.anuke.mindustry.world.blocks.units.*;
import io.anuke.wikigen.*;

@Generates(ContentType.unit)
public class UnitGenerator extends FileGenerator<UnitType>{

    @Override
    public void generate(UnitType content){
        template(content.name,
        "icon", linkImage(content),
        "description", content.description,
        "name", content.localizedName(),
        "internalname", content.name,
        "health", content.health,
        "flying", content.isFlying,
        "speed", content.speed,
        "mass", content.mass,
        "maxvelocity", content.maxVelocity,
        "created", links(Vars.content.blocks().select(b -> b instanceof UnitFactory && getPrivate(b, UnitFactory.class, "type") == content))
        );
    }

    @Override
    protected String linkImage(UnitType content){
        String baseName = "unit-icon-" + content.name;
        return Config.imageDirectory.child(baseName+ ".png").exists() ? baseName : content.name;
    }
}
