package wikigen.generators;

import arc.struct.*;
import mindustry.ctype.*;
import mindustry.world.*;
import wikigen.*;

@Generates(ContentType.block)
public class BlockGenerator extends FileGenerator<Block>{

    @Override
    public ObjectMap<String, Object> vars(Block block){
        return ObjectMap.of();
    }

    @Override
    public String linkImage(Block content){
        return "block-" + content.name + "-ui";
    }
}
