package io.anuke.wikigen;

import io.anuke.arc.ApplicationListener;
import io.anuke.arc.backends.headless.HeadlessApplication;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.core.Logic;
import io.anuke.mindustry.core.World;
import io.anuke.mindustry.game.Content;
import io.anuke.mindustry.game.Version;
import io.anuke.mindustry.io.BundleLoader;
import io.anuke.wikigen.image.MockScene;

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
                Version.enabled = false;
                Vars.init();
                BundleLoader.load();
                Vars.headless = true;
                Vars.content.verbose(false);
                Vars.content.load();
                Vars.world = new World();
                Vars.logic = new Logic();
                Vars.content.initialize(Content::init);
                MockScene.init();
                Vars.content.initialize(Content::load);
                PageGenerator.generate();
                System.exit(0);
            }
        });
    }
}
