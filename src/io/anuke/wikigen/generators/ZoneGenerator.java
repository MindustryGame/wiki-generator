package io.anuke.wikigen.generators;

import io.anuke.arc.collection.*;
import io.anuke.arc.util.*;
import io.anuke.mindustry.*;
import io.anuke.mindustry.type.*;
import io.anuke.wikigen.*;

@Generates(ContentType.zone)
public class ZoneGenerator extends FileGenerator<Zone>{

    @Override
    protected String linkImage(Zone content){
        return "zone-" + content.name;
    }

    @Override
    public String imageStyle(){
        return "zonespr";
    }

    @Override
    public void generate(Zone zone){
        template(zone.name,
        "name", zone.localizedName(),
        "internalname", zone.name,
        "description", zone.description.replace("[lightgray]", "").replace("[accent]", ""),
        "launchperiod", zone.launchPeriod,
        "mode", !zone.getRules().attackMode ? "Survival" : "Attack",
        "launchwave", zone.conditionWave == Integer.MAX_VALUE ? "none" : zone.conditionWave,
        "loadout", Array.with(zone.getStartingItems()).reduce("", (stack, r) -> r + link(stack.item) + "x" + stack.amount + " "),
        "required", links(Vars.content.zones().select(z -> Structs.contains(z.zoneRequirements, r -> r.zone == zone))),
        "preceded", links(Array.with(zone.zoneRequirements).map(z -> z.zone)),
        "resources", links(Array.with(zone.resources))
        );
    }
}
