package io.anuke.wikigen.generators;

import io.anuke.arc.collection.ObjectMap;
import io.anuke.arc.graphics.g2d.TextureAtlas.AtlasRegion;
import io.anuke.arc.scene.Element;
import io.anuke.arc.scene.Group;
import io.anuke.arc.scene.style.TextureRegionDrawable;
import io.anuke.arc.scene.ui.Image;
import io.anuke.arc.scene.ui.Label;
import io.anuke.arc.scene.ui.layout.Table;
import io.anuke.arc.util.Strings;
import io.anuke.mindustry.type.ContentType;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.meta.StatCategory;
import io.anuke.mindustry.world.meta.StatValue;
import io.anuke.wikigen.FileGenerator;
import io.anuke.wikigen.Generates;

@Generates(ContentType.block)
public class BlockGenerator extends FileGenerator<Block>{

    @Override
    public void generate(Block block){
        //skip hidden blocks
        if(block.isHidden() || !block.isVisible()){
            return;
        }

        ObjectMap<String, Object> values = ObjectMap.of(
            "name", block.localizedName,
            "internalname", block.name
        );

        if(block.description != null){
            values.put("description", block.description);
        }

        StringBuilder stats = new StringBuilder();

        //add all in-game stats to block info
        block.stats.toMap().each((category, map) -> {
            if(map.isEmpty()) return;
            stats.append("\n|").append(category.localized()).append("||\n| --- | --- |\n");
            //general category has additional info
            if(category == StatCategory.general){
                stats.append("|Internal Name|`").append(block.name).append("`|\n");
                stats.append("|Solid|").append(str(block.solid)).append("|\n");
            }

            map.each((stat, statValues) -> {
                stats.append("|").append(stat.localized()).append("|");
                for(StatValue value : statValues){
                    stats.append(strStat(value));
                    stats.append(" ");
                }
                stats.append("|\n");
            });
        });

        values.put("stats", stats.toString());
        template(block.buildCategory.name() + "/" + block.name, values);
    }

    @Override
    protected String linkImage(Block content){
        return content.name + "-icon-small";
    }

    @Override
    protected String linkPath(Block content){
        return content.buildCategory + "/" + content.name;
    }

    String strStat(StatValue value){
        Table dummy = new Table();
        value.display(dummy);
        StringBuilder result = new StringBuilder();

        for(Element e : dummy.getChildren()){
            display(e, result);
        }
        return result.toString();
    }

    void display(Element e, StringBuilder result){
        if(e instanceof Label){
            result.append(((Label)e).getText());
            result.append(" ");
        }else if(e instanceof Image){
            AtlasRegion region = (AtlasRegion)((TextureRegionDrawable)((Image)e).getDrawable()).getRegion();
            result.append(Strings.format("![{0}](../../images/{0}.png)", region.name));
            result.append(" ");
        }else if(e instanceof Group){
            for(Element child : ((Group)e).getChildren()){
                display(child, result);
            }
        }
    }
}
