package wikigen.generators;

import arc.struct.*;
import mindustry.*;
import mindustry.ctype.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.units.*;
import wikigen.*;

@Generates(ContentType.unit)
public class UnitGenerator extends FileGenerator<UnitType>{

    @Override
    public ObjectMap<String, Object> vars(UnitType content){
        return ObjectMap.of(
        "created", links(Vars.content.blocks().select(b -> b.minfo.mod == null && createsUnit(b, content)))
        );
    }

    private boolean createsUnit(Block block, UnitType unit){
        if(block instanceof UnitFactory factory && factory.plans.contains(plan -> plan.unit == unit)) return true;
        if(block instanceof Reconstructor reconstructor && reconstructor.upgrades.contains(upgrade -> upgrade.length > 1 && upgrade[1] == unit)) return true;
        if(block instanceof UnitAssembler assembler && assembler.plans.contains(plan -> plan.unit == unit)) return true;
        return false;
    }
}
