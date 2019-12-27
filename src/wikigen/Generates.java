package wikigen;

import mindustry.ctype.ContentType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Generates{
    ContentType value();
}
