package wikigen;

import arc.struct.ObjectMap;
import arc.files.Fi;
import arc.util.Log;

public class Splicer{
    private static final ObjectMap<String, Fi> targets = new ObjectMap<>();
    private static final String comment = "[comment]: # (WARNING: Do not modify the text above. It is automatically generated every release.)";

    /** Begins recursively splicing generated files with base, edited files.*/
    public static void splice(){
        Log.info("Splicing files from @ into @...", Config.tmpDirectory.path(), Config.fileOutDirectory.path());
        Config.fileOutDirectory.walk(f -> targets.put(f.name(), f));
        Config.tmpDirectory.walk(file -> {
            //only look at markdown files.
            if(file.extension().equals("md")){
                //splice if target is found
                if(targets.containsKey(file.name())){
                    Fi target = targets.get(file.name());
                    String sourceString = target.readString();
                    String genString = file.readString();
                    int idx = sourceString.indexOf(comment);
                    if(idx == -1){
                        Log.err("File '@' has no generated comment! Has a user removed it? Check this file manually.", target.path());
                    }else{
                        //everything after the comment is kept, everything before it is replaced
                        String result = genString + "\n" + comment + sourceString.substring(idx + comment.length()).replaceAll("[\r\n]+$", "");;
                        target.writeString(result);
                        Log.info("> Spliced file @", target.path());
                    }
                }else{
                    //else create a new file in the file directory
                    Fi dest = Config.fileOutDirectory.child(file.path().substring(Config.tmpDirectory.path().length()));
                    file.copyTo(dest);
                    //append comment to end of file so it can be used later
                    dest.writeString("\n" + comment + "\n", true);
                    Log.info("> Created new file @", dest.path());
                }
            }
        });

        //copy images
        //TODO later, just unpack them in this directory to begin with to save time
        for(Fi file : Config.imageDirectory.list()){
            file.copyTo(Config.fileOutDirectory.child("images").child(file.name()));
        }
    }
}
