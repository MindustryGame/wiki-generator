package io.anuke.wikigen;

import io.anuke.mindustry.type.ContentType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Generates{
    ContentType value();
}
