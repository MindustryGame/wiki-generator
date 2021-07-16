package wikigen.generators;

import arc.files.*;
import arc.graphics.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.ctype.*;
import mindustry.io.*;
import mindustry.type.*;
import mindustry.world.*;
import wikigen.*;

import static mindustry.Vars.*;
import static wikigen.Config.*;

@Generates(ContentType.planet)
public class PlanetGenerator extends FileGenerator<Planet>{

    private int colorFor(Tile tile){
        if(tile == null) return 0;
        int bc = tile.block().minimapColor(tile);
        Color color = Tmp.c1.set(bc == 0 ? MapIO.colorFor(tile.block(), tile.floor(), tile.overlay(), tile.team()) : bc);
        color.mul(1f - Mathf.clamp(world.getDarkness(tile.x, tile.y) / 4f));

        return color.rgba();
    }

    @Override
    public ObjectMap<String, Object> vars(Planet planet){
        Fi folder = Config.outDirectory.child(planet.localizedName);
        var planetDrops = new ObjectSet<UnlockableContent>();

        for(Sector sector : planet.sectors){
            Fi file = folder.child(sector.preset == null ? sector.id + "" : sector.preset.localizedName);

            Log.info("| | Sector: @/@", sector.id, planet.sectors.size);

            Vars.logic.reset();
            Vars.world.loadSector(sector);

            var pix = new Pixmap(world.width(), world.height());
            world.tiles.eachTile(t -> {
                pix.set(t.x, world.height() - 1 - t.y, colorFor(t));
            });

            Fi imgFile = Config.imageDirectory.child("sector-" + planet.name + "-" + sector.id + ".png"),
            smallImg = Config.imageDirectory.child("sector-" + planet.name + "-" + sector.id + "-small.png");
            var small = new Pixmap(64, 64);
            small.draw(pix, 0, 0, small.width, small.height, true);

            imgFile.writePng(pix);
            smallImg.writePng(small);

            var drops = new ObjectSet<UnlockableContent>();

            planetDrops.addAll(drops);

            for(Tile tile : Vars.world.tiles){
                if(!tile.block().solid){
                    if(tile.floor().liquidDrop != null){
                        drops.add(tile.floor().liquidDrop);
                    }
                    if(tile.floor().itemDrop != null){
                        drops.add(tile.floor().itemDrop);
                    }
                }
            }

            var u = drops.asArray().sort(Structs.comparing(Content::getContentType).thenComparing(c -> c.id));

            var template = rootDirectory.child("templates").child("sector.md").readString();
            var vars = ObjectMap.<String, Object>of(
            "localizedName", sector.name() + (sector.preset == null ? "" : " (" + sector.id + ")"),
            "resources", links(u),
            "planet", planet.name,
            "id", sector.id,
            "mode", state.rules.mode(),
            "image", makeImageLink(imgFile.nameWithoutExtension()),
            "difficulty", Strings.stripColors(sector.displayThreat())
            );

            if(state.rules.waves){
                vars.put("captureWaves", state.rules.winWave);
            }

            file.writeString(format(template, vars));
        }

        return ObjectMap.of(
            "sectors", planet.sectors.size,
            "resources", links(planetDrops)
        );
    }
}
