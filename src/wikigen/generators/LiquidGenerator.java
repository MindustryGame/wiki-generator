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

@Generates(ContentType.liquid)
public class LiquidGenerator extends FileGenerator<Liquid>{

    @Override
    public ObjectMap<String, Object> vars(Liquid liquid){
        boolean pumpable = Vars.content.blocks().contains(b -> b instanceof Floor f && f.liquidDrop == liquid);

        return ObjectMap.of(
            "produced", links(Vars.content.blocks().select(b -> b.minfo.mod == null && (pumpable && b instanceof Pump && !(b instanceof SolidPump sp && sp.result != liquid))
                || (b instanceof GenericCrafter g && g.outputLiquid != null && g.outputLiquid.liquid == liquid))),
            "crafting", links(Vars.content.blocks().select(b -> b.minfo.mod == null && b.consumes.all() != null && Structs.contains(b.consumes.all(), c -> (c instanceof ConsumeLiquidFilter f && f.filter.get(liquid))
                || (c instanceof ConsumeLiquid l && l.liquid == liquid))))
        );
    }

    @Override
    public String linkImage(Liquid content){
        return "liquid-" + content.name;
    }
}
