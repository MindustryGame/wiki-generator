package wikigen.generators;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.gen.*;
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
    public boolean enabled(Planet content){
        return content.sectors.size > 0;
    }

    @Override
    public ObjectMap<String, Object> vars(Planet planet){
        Fi folder = Config.outDirectory.child(planet.localizedName);
        var planetDrops = new ObjectSet<UnlockableContent>();

        //may or may not fix a crash
        world.tiles = new Tiles(1, 1);
        world.tiles.set(0, 0, new Tile(0, 0));

        Events.fire(new WorldLoadEvent());

        for(Sector sector : planet.sectors){
            Fi file = folder.child((sector.preset == null ? sector.id + "" : sector.preset.localizedName) + ".md");

            Log.info("| | Sector: @/@", sector.id, planet.sectors.size);

            Vars.logic.reset();
            Vars.world.loadSector(sector);

            var pix = new Pixmap(world.width(), world.height());
            world.tiles.eachTile(t -> pix.set(t.x, world.height() - 1 - t.y, colorFor(t)));

            for(Tile spawn : spawner.getSpawns()){
                pix.fillCircle(spawn.x, pix.height - 1 - spawn.y, 3, Team.crux.color.rgba());
                pix.drawCircle(spawn.x, pix.height - 1 - spawn.y, 5, Team.crux.color.rgba());
            }
            Building core = Team.sharded.core();
            pix.drawCircle(core.tileX(), pix.height - 1 - core.tileY(), 6, Team.sharded.color.rgba());

            Fi imgFile = Config.imageDirectory.child("sector-" + planet.name + "-" + sector.id + ".png"),
            smallImg = Config.imageDirectory.child("sector-" + planet.name + "-" + sector.id + "-small.png");
            var small = new Pixmap(50, 50);
            small.draw(pix, 0, 0, small.width, small.height, true);

            imgFile.writePng(pix);
            smallImg.writePng(small);

            var drops = new ObjectSet<UnlockableContent>();

            for(Tile tile : Vars.world.tiles){
                if(!tile.block().solid){
                    if(tile.floor().liquidDrop != null){
                        drops.add(tile.floor().liquidDrop);
                    }
                    if(tile.floor().itemDrop != null){
                        drops.add(tile.floor().itemDrop);
                    }
                    if(tile.overlay().itemDrop != null){
                        drops.add(tile.overlay().itemDrop);
                    }
                }
            }

            planetDrops.addAll(drops);

            var waves = new StringBuilder();
            int waveCount = 50;

            for(int i = 0; i < waveCount; i++){
                int[] counts = new int[content.units().size];
                boolean[] boss = new boolean[content.units().size];
                for(var entry : state.rules.spawns){
                    counts[entry.type.id] += entry.getSpawned(i) * (entry.type.flying ? spawner.countFlyerSpawns() : spawner.countGroundSpawns());
                    boss[entry.type.id] |= entry.effect == StatusEffects.boss;
                }

                waves.append("|").append(i + 1).append("|");

                boolean any = false;
                for(var type : content.units()){
                    if(counts[type.id] > 0){
                        any = true;
                        waves.append(Generator.str(type));

                        if(boss[type.id]){
                            waves.append("** (Guardian) **");
                        }

                        waves.append("x").append(counts[type.id]).append(" ");
                    }
                }

                if(!any){
                    waves.append("*(none)*");
                }

                waves.append("|\n");
            }

            var u = drops.asArray().sort(Structs.comparing(Content::getContentType).thenComparing(c -> c.id));

            var template = rootDirectory.child("templates").child("sector.md").readString();
            var vars = ObjectMap.<String, Object>of(
            "localizedName", sector.preset == null ? "Sector " + sector.id : sector.name() + " (" + sector.id + ")",
            "resources", links(u),
            "planet", planet.name,
            "id", sector.id,
            "mode", state.rules.mode(),
            "difficulty", Strings.stripColors(sector.displayThreat()),
            "waves", waves.toString()
            );

            if(state.rules.winWave > 0 && !state.rules.attackMode){
                vars.put("captureWaves", state.rules.winWave);
            }

            if(sector.preset != null && sector.preset.description != null){
                vars.put("description", Strings.stripColors(sector.preset.description));
            }

            file.writeString(format(template, vars));
        }

        var pd = planetDrops.asArray().sort(Structs.comparing(Content::getContentType).thenComparing(c -> c.id));

        return ObjectMap.of(
            "sectors", planet.sectors.size,
            "resources", links(pd)
        );
    }
}
