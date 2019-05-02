package io.anuke.wikigen.generators;

import io.anuke.mindustry.type.ContentType;
import io.anuke.mindustry.type.Item;
import io.anuke.wikigen.FileGenerator;
import io.anuke.wikigen.Generates;

@Generates(ContentType.item)
public class ItemGenerator extends FileGenerator<Item>{

    @Override
    public void generate(Item content){
        write(content.name, writer -> {
            writer.println("Name: **" + content.name + "**");
            writer.println();
            writer.println("Description: \"*" + content.description + "*\"");
        });
    }
}
