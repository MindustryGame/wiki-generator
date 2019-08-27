package io.anuke.wikigen;

import io.anuke.arc.*;
import io.anuke.arc.backends.headless.*;
import io.anuke.mindustry.*;
import io.anuke.mindustry.core.*;
import io.anuke.mindustry.game.*;
import io.anuke.wikigen.image.*;

public class MainLauncher{

    public static void main(String[] args){
        new HeadlessApplication(new ApplicationListener(){
            @Override
            public void update(){
                try{
                    Thread.sleep(Long.MAX_VALUE);
                }catch(InterruptedException ignored){}
            }

            @Override
            public void init(){
                //generate locale file manually
                if(!Core.files.local("locales").exists()){
                    Core.files.local("locales").writeString("en");
                }

                Version.enabled = false;
                Vars.headless = true;
                Vars.loadSettings();
                Vars.init();
                Vars.content.createContent();
                Vars.world = new World();
                Vars.logic = new Logic();
                Vars.content.init();
                MockScene.init();
                Vars.content.load();
                CoreGenerator.generate();
                CoreSplicer.splice();
                System.exit(0);
            }
        });
    }
}
