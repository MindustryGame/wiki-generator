package io.anuke.wikigen.generators;

import io.anuke.arc.util.Strings;
import io.anuke.arc.util.Structs;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.type.*;
import io.anuke.mindustry.world.blocks.Floor;
import io.anuke.mindustry.world.blocks.production.Drill;
import io.anuke.mindustry.world.blocks.production.GenericCrafter;
import io.anuke.mindustry.world.consumers.ConsumeItemFilter;
import io.anuke.mindustry.world.consumers.ConsumeItems;
import io.anuke.wikigen.FileGenerator;
import io.anuke.wikigen.Generates;

@Generates(ContentType.item)
public class ItemGenerator extends FileGenerator<Item>{

    @Override
    public void generate(Item item){
        boolean drillable = Vars.content.blocks().contains(b -> b instanceof Floor && ((Floor)b).itemDrop == item);

        template(item.name,
            "name", item.localizedName,
            "internalname", item.name,
            "description", item.description,
            "type", Strings.capitalize(item.type.toString()),
            "flammability", (int)(item.flammability * 100) + "%",
            "explosiveness", (int)(item.explosiveness * 100) + "%",
            "radioactivity", (int)(item.radioactivity * 100) + "%",
            "cost", item.type == ItemType.material ? (int)(item.cost * 100) + "%" : "",
            "hardness", item.hardness,
            "drillable", drillable,
            "color", item.color.toString().substring(0, 6),
            "produced", links(Vars.content.blocks().select(b -> (drillable && b instanceof Drill && ((Drill)b).tier() >= item.hardness) || (b instanceof GenericCrafter && ((GenericCrafter)b).outputItem() == item))),
            "used", links(Vars.content.blocks().select(b -> b.requirements != null && Structs.contains(b.requirements, i -> i.item == item))),
            "crafting", links(Vars.content.blocks().select(b -> b.consumes.all() != null && Structs.contains(b.consumes.all(), c -> (c instanceof ConsumeItemFilter && ((ConsumeItemFilter)c).filter.test(item))
                                                                                || (c instanceof ConsumeItems && Structs.contains(((ConsumeItems)c).items, s -> s.item == item)))))
        );
    }

    @Override
    protected String linkImage(Item content){
        return "item-" + content.name + "-xlarge";
    }
}
