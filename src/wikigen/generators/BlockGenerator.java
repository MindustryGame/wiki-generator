package wikigen.generators;

import arc.Core;
import arc.struct.Array;
import arc.struct.ObjectMap;
import arc.graphics.g2d.TextureAtlas.AtlasRegion;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import mindustry.ctype.ContentType;
import mindustry.ui.ItemDisplay;
import mindustry.ui.LiquidDisplay;
import mindustry.world.Block;
import mindustry.world.meta.StatCategory;
import mindustry.world.meta.StatValue;
import wikigen.FileGenerator;
import wikigen.Generates;

@Generates(ContentType.block)
public class BlockGenerator extends FileGenerator<Block>{

    @Override
    public void generate(Block block){
        //skip hidden blocks
        if(!block.isBuildable()){
            return;
        }

        ObjectMap<String, Object> values = ObjectMap.of(
            "name", block.localizedName,
            "internalname", block.name,
            "requirements", block.requirements == null ? "" : Array.with(block.requirements).reduce("", (stack, r) -> r + link(stack.item) + "x" + stack.amount + " ")
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
        template(linkPath(block), values);
    }

    @Override
    protected String linkImage(Block content){
        return "block-" + content.name + "-small";
    }

    @Override
    protected String linkPath(Block content){
        return content.category + "/" + content.name;
    }

    String strStat(StatValue value){
        Table dummy = new Table();
        value.display(dummy);
        StringBuilder result = new StringBuilder();
        display(dummy, result);
        return result.toString();
    }

    void display(Element e, StringBuilder result){
        if(e instanceof Label){
            String text = ((Label)e).getText().toString();
            if(text.startsWith("$")){
                text = Core.bundle.get(text.substring(1));
            }
            boolean stat = text.contains("[stat]") || text.contains("[lightgray]");
            if(text.contains("[stat]") && !text.contains("[lightgray]")){
                text += "**";
            }
            text = text.replace("[stat]", "**").replace("[lightgray]", "**");
            if(stat){
                result.append("<br> • ");
            }
            result.append(text).append(" ");
        }else if(e instanceof ItemDisplay){
            ItemDisplay d = (ItemDisplay)e;
            result.append(link(d.item));
            if(d.amount > 0){
                result.append("x");
                result.append(d.amount);
            }
            result.append(" ");
        }else if(e instanceof LiquidDisplay){
            LiquidDisplay d = (LiquidDisplay)e;
            result.append(link(d.liquid));
            if(d.amount > 0){
                if(d.perSecond){
                    result.append(Strings.autoFixed(d.amount, 1));
                    result.append("/sec");
                }else{
                    result.append("x");
                    result.append(Strings.autoFixed(d.amount, 1));
                }
            }
            result.append(" ");
        }else if(e instanceof Image){
            AtlasRegion region = (AtlasRegion)((TextureRegionDrawable)((Image)e).getDrawable()).getRegion();
            result.append(Strings.format("![{0}](/wiki/images/{0}.png)", region.name));
            result.append(" ");
        }else if(e instanceof Table){
            for(Cell cell : ((Table)e).getCells()){
                display(cell.get(), result);
                if(cell.isEndRow() && e.getParent() == null){
                    result.append("<br>");
                }
            }
        }else if(e instanceof Group){
            for(Element child : ((Group)e).getChildren()){
                display(child, result);
            }
        }
    }
}
