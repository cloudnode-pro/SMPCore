package pro.cloudnode.smp.smpcore.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.smpcore.Member;
import pro.cloudnode.smp.smpcore.Nation;
import pro.cloudnode.smp.smpcore.SMPCore;

import java.util.Optional;

public final class PlayerJoinListener implements Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    public void putPlayerInNationTeam(final @NotNull PlayerJoinEvent event) {
        SMPCore.runAsync(() -> {
            final @NotNull Player player = event.getPlayer();
            final @NotNull Optional<@NotNull Member> member = Member.get(player);
            final @NotNull Optional<@NotNull Team> team = Optional.ofNullable(player.getScoreboard()
                    .getPlayerTeam(player));
            final Team staffTeam = Member.getStaffTeam();

            final @NotNull Optional<@NotNull Nation> nationFromTeam = team.flatMap(Nation::get);
            if (member.isEmpty()) {
                // no longer a member, but in a nation's team?
                nationFromTeam.ifPresent(ignored -> team.get().removePlayer(player));
                staffTeam.removePlayer(player);
                return;
            }

            if (member.get().staff) {
                if (team.isPresent() && !team.get().equals(staffTeam))
                    team.get().removePlayer(player);

                staffTeam.addPlayer(player);

                return;
            }
            staffTeam.removePlayer(player);

            final @NotNull Optional<@NotNull Nation> nation = member.get().nation();
            if (nation.isEmpty() && team.isPresent()) {
                // no longer in a nation, but in a nation's team?
                nationFromTeam.ifPresent(ignored -> team.get().removePlayer(player));
                return;
            }
            if (nation.isPresent()) {
                // in a nation and team, but not the nation's team?
                if (team.isPresent() && !team.get().getName().equals(nation.get().getTeam().getName()))
                    team.get().removePlayer(player);
                nation.get().getTeam().addPlayer(player);
            }
        });
    }
}
