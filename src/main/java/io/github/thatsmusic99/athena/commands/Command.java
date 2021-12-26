package io.github.thatsmusic99.athena.commands;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

    String name();

    String permission();

    String description();

    String usage();
}
