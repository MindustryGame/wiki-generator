package wikigen.generators;

import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.ctype.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.units.*;
import mindustry.world.consumers.*;
import wikigen.*;

@Generates(ContentType.item)
public class ItemGenerator extends FileGenerator<Item>{

    @Override
    public ObjectMap<String, Object> vars(Item item){
        boolean drillable = Vars.content.blocks().contains(b -> b instanceof Floor f && !f.wallOre && f.itemDrop == item);
        boolean beamDrillable = Vars.content.blocks().contains(b ->
            (b instanceof Floor f && f.wallOre && f.itemDrop == item) ||
            (b instanceof StaticWall w && w.itemDrop == item)
        );

        return ObjectMap.of(
            "cost", (int)(item.cost * 100) + "%",
            "hardness", item.hardness,
            "drillable", drillable || beamDrillable,
            "color", item.color.toString().substring(0, 6),
            "produced", links(Vars.content.blocks().select(b -> b.minfo.mod == null && outputsItem(b, item, drillable, beamDrillable))),
            "used", links(Vars.content.blocks().select(b -> b.minfo.mod == null && b.requirements != null && Structs.contains(b.requirements, i -> i.item == item))),
            "crafting", links(Vars.content.blocks().select(b -> b.minfo.mod == null && consumesItem(b, item)))
        );
    }

    private boolean outputsItem(Block block, Item item, boolean drillable, boolean beamDrillable){
        if(drillable && block instanceof Drill drill && drill.tier >= item.hardness && canMine(drill.blockedItems, item)) return true;
        if(beamDrillable && block instanceof BeamDrill drill && drill.tier >= item.hardness && canMine(drill.blockedItems, item)) return true;
        if(block instanceof WallCrafter crafter && crafter.output == item) return true;
        if(block instanceof Separator separator && separator.results != null && Structs.contains(separator.results, stack -> stack.item == item)) return true;

        if(block instanceof GenericCrafter crafter){
            if(crafter.outputItem != null && crafter.outputItem.item == item) return true;
            if(crafter.outputItems != null && Structs.contains(crafter.outputItems, stack -> stack.item == item)) return true;
        }

        return false;
    }

    private boolean consumesItem(Block block, Item item){
        if(block.consumers != null && Structs.contains(block.consumers, c -> (c instanceof ConsumeItemFilter i && i.filter.get(item))
            || (c instanceof ConsumeItems h && Structs.contains(h.items, s -> s.item == item)))) return true;

        if(block instanceof UnitFactory factory && factory.plans.contains(plan -> Structs.contains(plan.requirements, stack -> stack.item == item))) return true;
        if(block instanceof UnitAssembler assembler && assembler.plans.contains(plan -> plan.itemReq != null && Structs.contains(plan.itemReq, stack -> stack.item == item))) return true;

        return false;
    }

    private boolean canMine(Seq<Item> blockedItems, Item item){
        return blockedItems == null || !blockedItems.contains(item);
    }

    @Override
    public String linkImage(Item content){
        return "item-" + content.name;
    }
}
