package io.anuke.wikigen;

import io.anuke.arc.util.Log;
import io.anuke.mindustry.Vars;

import java.util.Arrays;

public class PageGenerator{

    public static void generate(){
        Log.info("Content: {0}", Arrays.toString(Vars.content.getContentMap()));
    }
}
