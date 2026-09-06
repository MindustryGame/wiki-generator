package wikigen.generators;

import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.ctype.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.units.*;
import mindustry.world.consumers.*;
import wikigen.*;

@Generates(ContentType.liquid)
public class LiquidGenerator extends FileGenerator<Liquid>{

    @Override
    public ObjectMap<String, Object> vars(Liquid liquid){
        boolean pumpable = Vars.content.blocks().contains(b -> b instanceof Floor f && f.liquidDrop == liquid);

        return ObjectMap.of(
            "produced", links(Vars.content.blocks().select(b -> b.minfo.mod == null && (
                (pumpable && b instanceof Pump && !(b instanceof SolidPump sp && sp.result != liquid))
                || outputsLiquid(b, liquid)
            ))),
            "crafting", links(Vars.content.blocks().select(b -> b.minfo.mod == null && consumesLiquid(b, liquid)))
        );
    }

    private boolean outputsLiquid(Block block, Liquid liquid){
        if(block instanceof SolidPump pump && pump.result == liquid) return true;

        if(block instanceof GenericCrafter crafter){
            if(crafter.outputLiquid != null && crafter.outputLiquid.liquid == liquid) return true;
            if(crafter.outputLiquids != null && Structs.contains(crafter.outputLiquids, stack -> stack.liquid == liquid)) return true;
        }

        if(block instanceof ConsumeGenerator generator && generator.outputLiquid != null && generator.outputLiquid.liquid == liquid) return true;
        if(block instanceof ThermalGenerator generator && generator.outputLiquid != null && generator.outputLiquid.liquid == liquid) return true;

        return false;
    }

    private boolean consumesLiquid(Block block, Liquid liquid){
        if(block.consumers != null && Structs.contains(block.consumers, c ->
            (c instanceof ConsumeLiquidBase base && base.consumes(liquid)) ||
            (c instanceof ConsumeLiquids liquids && Structs.contains(liquids.liquids, stack -> stack.liquid == liquid))
        )) return true;

        if(block instanceof UnitAssembler assembler && assembler.plans.contains(plan -> plan.liquidReq != null && Structs.contains(plan.liquidReq, stack -> stack.liquid == liquid))) return true;

        return false;
    }

    @Override
    public String linkImage(Liquid content){
        return "liquid-" + content.name;
    }
}
