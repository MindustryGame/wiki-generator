package wikigen.generators;

import mindustry.ctype.*;
import mindustry.type.*;
import wikigen.*;

//TODO implement better support
@Generates(ContentType.status)
public class StatusGenerator extends FileGenerator<StatusEffect>{

    @Override
    public String linkImage(StatusEffect content){
        return "status-" + content.name + "-ui";
    }
}
