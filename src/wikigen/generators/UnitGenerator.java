package wikigen.generators;

import arc.struct.*;
import mindustry.*;
import mindustry.ctype.*;
import mindustry.type.*;
import mindustry.world.blocks.units.*;
import wikigen.*;

@Generates(ContentType.unit)
public class UnitGenerator extends FileGenerator<UnitType>{

    @Override
    public ObjectMap<String, Object> vars(UnitType content){
        return ObjectMap.of(
            "created", links(Vars.content.blocks().select(b -> b.minfo.mod == null && b instanceof UnitFactory u && u.plans.contains(p -> p.unit == content)))
        );
    }
}
