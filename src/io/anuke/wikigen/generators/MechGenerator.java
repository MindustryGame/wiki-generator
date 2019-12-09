package io.anuke.wikigen.generators;

import io.anuke.mindustry.*;
import io.anuke.mindustry.type.*;
import io.anuke.mindustry.world.blocks.units.*;
import io.anuke.wikigen.*;

@Generates(ContentType.mech)
public class MechGenerator extends FileGenerator<Mech>{

    @Override
    public void generate(Mech content){
        template(content.name,
        "name", content.localizedName,
        "description", content.description,
        "internalname", content.name,
        "health", content.health,
        "flying", content.flying,
        "speed", content.speed,
        "mass", content.mass,
        "maxvelocity", content.maxSpeed,
        "itemcapacity", content.itemCapacity,
        "drillpower", content.drillPower,
        "minespeed", (int)(content.mineSpeed * 100) + "%",
        "buildspeed", (int)(content.buildPower * 100) + "%",
        "created", links(Vars.content.blocks().select(b -> b instanceof MechPad && getPrivate(b, MechPad.class, "mech") == content))
        );
    }

    @Override
    protected String linkImage(Mech content){
        return "mech-icon-" + content.name;
    }
}
