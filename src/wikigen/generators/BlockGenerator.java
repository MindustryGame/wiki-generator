package wikigen.generators;

import arc.files.*;
import arc.struct.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.legacy.*;
import wikigen.*;

@Generates(ContentType.block)
public class BlockGenerator extends FileGenerator<Block>{

    @Override
    public ObjectMap<String, Object> vars(Block block){
        return ObjectMap.of(
        "itemDrop", block.itemDrop,
        "liquidDrop", block instanceof Floor f ? f.liquidDrop : null,
        "speedMultiplier", block instanceof Floor f && f.speedMultiplier != 1f ? (int)(f.speedMultiplier * 100) + "%" : null,
        "dragMultiplier", block instanceof Floor f && f.dragMultiplier != 1f ? (int)(f.dragMultiplier * 100) + "%" : null,
        "status", block instanceof Floor f && f.status != StatusEffects.none ? f.status : null
        );
    }

    @Override
    public String linkImage(Block content){
        return "block-" + content.name + "-ui";
    }

    @Override
    public boolean enabled(Block b){
        return b != Blocks.air && b.uiIcon.found() && b.inEditor && !(b instanceof ConstructBlock) && !(b instanceof LegacyBlock);
    }

    @Override
    public Fi file(Block b){
        return Config.outDirectory
        .child(b.synthetic() ? "blocks" : "Environment Blocks")
        .child(linkPath(b) + ".md");
    }

    @Override
    public String plural(Block b){
        return b.synthetic() ? "blocks" : "Environment Blocks";
    }

}
