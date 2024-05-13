package pro.cloudnode.smp.smpcore.exception;

import org.jetbrains.annotations.NotNull;

public final class NationNotFoundException extends Exception {
    /**
     * @see pro.cloudnode.smp.smpcore.Nation#id
     */
    public final @NotNull String nation;

    /**
     * @param id See {@link pro.cloudnode.smp.smpcore.Nation#id}
     */
    public NationNotFoundException(final @NotNull String id) {
        super("Nation not found: " + id);
        this.nation = id;
    }
}
