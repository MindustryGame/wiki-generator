package io.anuke.wikigen.generators;

import io.anuke.arc.util.Strings;
import io.anuke.mindustry.type.ContentType;
import io.anuke.mindustry.type.Item;
import io.anuke.wikigen.FileGenerator;
import io.anuke.wikigen.Generates;

@Generates(ContentType.item)
public class ItemGenerator extends FileGenerator<Item>{

    @Override
    public void generate(Item item){
        template(item.name,
            "name", item.localizedName,
            "internalname", item.name,
            "description", item.description,
            "type", Strings.capitalize(item.type.toString()),
            "flammability", (int)(item.flammability * 100) + "%",
            "explosiveness", (int)(item.explosiveness * 100) + "%",
            "radioactivity", (int)(item.radioactivity * 100) + "%",
            "hardness", item.hardness,
            "cost", (int)(item.cost * 100) + "%"
        );
    }
}
