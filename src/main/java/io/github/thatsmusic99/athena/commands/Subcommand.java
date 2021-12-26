package io.github.thatsmusic99.athena.commands;

import java.util.Locale;

public enum Subcommand {

    LISTEN(new ListenCommand()),
    LISTENERS(new ListenersCommand()),
    STOP(new StopCommand()),
    LOOKUP(new LookupCommand());

    private final IAthenaCommand command;

    Subcommand(IAthenaCommand iAthenaCommand) {
        this.command = iAthenaCommand;
    }

    public IAthenaCommand getExecutor() {
        return command;
    }

    public static Subcommand match(String string) {
        try {
            return Subcommand.valueOf(string.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
