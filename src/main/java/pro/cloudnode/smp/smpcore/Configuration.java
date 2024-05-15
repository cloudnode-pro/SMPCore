package pro.cloudnode.smp.smpcore;

public final class Configuration extends BaseConfig {
    public Configuration() {
        super("config.yml");
    }

    /**
     * REST API HTTP server port
     */
    public int apiPort() {
        return config.getInt("api.port");
    }

    /**
     * Number of days since last seen after which player is considered inactive
     */
    public int membersInactiveDays() {
        return config.getInt("members.inactive-days");
    }

    /**
     * Maximum number of alts you can have
     */
    public int altsMax() {
        return config.getInt("alts.max");
    }
}
