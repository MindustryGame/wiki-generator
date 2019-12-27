package wikigen.generators;

import arc.util.Structs;
import mindustry.Vars;
import mindustry.ctype.ContentType;
import mindustry.type.Liquid;
import mindustry.world.blocks.Floor;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.production.Pump;
import mindustry.world.consumers.ConsumeLiquid;
import mindustry.world.consumers.ConsumeLiquidFilter;
import wikigen.FileGenerator;
import wikigen.Generates;

@Generates(ContentType.liquid)
public class LiquidGenerator extends FileGenerator<Liquid>{

    @Override
    public void generate(Liquid liquid){
        boolean pumpable = Vars.content.blocks().contains(b -> b instanceof Floor && ((Floor)b).liquidDrop == liquid);

        template(liquid.name,
            "name", liquid.localizedName,
            "internalname", liquid.name,
            "description", liquid.description,
            "flammability", (int)(liquid.flammability * 100) + "%",
            "explosiveness", (int)(liquid.explosiveness * 100) + "%",
            "heatcapacity", (int)(liquid.heatCapacity * 100) + "%",
            "produced", links(Vars.content.blocks().select(b -> (pumpable && b instanceof Pump) || (b instanceof GenericCrafter && ((GenericCrafter)b).outputLiquid() == liquid))),
            "crafting", links(Vars.content.blocks().select(b -> b.consumes.all() != null && Structs.contains(b.consumes.all(), c -> (c instanceof ConsumeLiquidFilter && ((ConsumeLiquidFilter)c).filter.get(liquid))
                                                                                || (c instanceof ConsumeLiquid && ((ConsumeLiquid)c).liquid == liquid))))
        );
    }

    @Override
    protected String linkImage(Liquid content){
        return "liquid-" + content.name;
    }
}
