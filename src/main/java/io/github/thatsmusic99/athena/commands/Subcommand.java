package io.github.thatsmusic99.athena.commands;

import java.util.Locale;

public enum Subcommand {

    LISTEN(new ListenCommand()),
    LISTENERS(new ListenersCommand()),
    STOP(new StopCommand()),
    LOOKUP(new LookupCommand());

    private final IAthenaCommand iAthenaCommand;
    Subcommand(IAthenaCommand iAthenaCommand) {
        this.iAthenaCommand = iAthenaCommand;
    }

    public IAthenaCommand getExecutor() {
        return iAthenaCommand;
    }

    public static Subcommand match(String string) {
        try {
            return Subcommand.valueOf(string.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
