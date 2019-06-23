package io.anuke.wikigen;

import io.anuke.arc.collection.ObjectMap;
import io.anuke.arc.files.FileHandle;
import io.anuke.arc.util.Log;

public class CoreSplicer{
    private static final ObjectMap<String, FileHandle> targets = new ObjectMap<>();
    private static final String comment = "[comment]: # (WARNING: Do not modify the text above. It is automatically generated every release.)";

    /** Begins recursively splicing generated files with base, edited files.*/
    public static void splice(){
        Log.info("Splicing files from {0} into {1}...", Config.tmpDirectory.path(), Config.fileOutDirectory.path());
        Config.fileOutDirectory.walk(f -> targets.put(f.name(), f));
        Config.tmpDirectory.walk(file -> {
            //only look at markdown files.
            if(file.extension().equals("md")){
                //splice if target is found
                if(targets.containsKey(file.name())){
                    FileHandle target = targets.get(file.name());
                    String sourceString = target.readString();
                    String genString = file.readString();
                    int idx = sourceString.indexOf(comment);
                    if(idx == -1){
                        Log.err("File '{0}' has no generated comment! Has a user removed it? Check this file manually.", target.path());
                    }else{
                        //everything after the comment is kept, everything before it is replaced
                        String result = genString + "\n" + comment + sourceString.substring(idx + comment.length()).replaceAll("[\r\n]+$", "");;
                        target.writeString(result);
                        Log.info("> Spliced file {0}", target.path());
                    }
                }else{
                    //else create a new file in the file directory
                    FileHandle dest = Config.fileOutDirectory.child(file.path().substring(Config.tmpDirectory.path().length()));
                    file.copyTo(dest);
                    //append comment to end of file so it can be used later
                    dest.writeString("\n" + comment + "\n", true);
                    Log.info("> Created new file {0}", dest.path());
                }
            }
        });

        //copy images
        //TODO later, just unpack them in this directory to begin with to save time
        for(FileHandle file : Config.imageDirectory.list()){
            file.copyTo(Config.fileOutDirectory.child("images").child(file.name()));
        }
    }
}
