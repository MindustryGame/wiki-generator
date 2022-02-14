package wikigen.generators;

import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.ctype.*;
import mindustry.type.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.production.*;
import mindustry.world.consumers.*;
import wikigen.*;

@Generates(ContentType.item)
public class ItemGenerator extends FileGenerator<Item>{

    @Override
    public ObjectMap<String, Object> vars(Item item){
        boolean drillable = Vars.content.blocks().contains(b -> b instanceof Floor f && f.itemDrop == item);

        return ObjectMap.of(
            "cost", (int)(item.cost * 100) + "%",
            "hardness", item.hardness,
            "drillable", drillable,
            "color", item.color.toString().substring(0, 6),
            "produced", links(Vars.content.blocks().select(b -> b.minfo.mod == null && ((drillable && b instanceof Drill d && d.tier >= item.hardness) || (b instanceof GenericCrafter g && g.outputItem != null && g.outputItem.item == item)))),
            "used", links(Vars.content.blocks().select(b -> b.minfo.mod == null && b.requirements != null && Structs.contains(b.requirements, i -> i.item == item))),
            "crafting", links(Vars.content.blocks().select(b -> b.minfo.mod == null && b.consumes.all() != null && Structs.contains(b.consumes.all(), c -> (c instanceof ConsumeItemFilter i && i.filter.get(item))
                                                                                || (c instanceof ConsumeItems h && Structs.contains(h.items, s -> s.item == item)))))
        );
    }

    @Override
    public String linkImage(Item content){
        return "item-" + content.name;
    }
}
