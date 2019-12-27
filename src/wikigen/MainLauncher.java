package wikigen;

import arc.*;
import arc.backend.headless.*;
import mindustry.*;
import mindustry.core.*;
import wikigen.image.*;

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
                Vars.content.createBaseContent();
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
