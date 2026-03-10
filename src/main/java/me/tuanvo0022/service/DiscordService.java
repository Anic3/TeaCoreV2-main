package me.tuanvo0022.service;

import me.tuanvo0022.Main;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.types.InheritanceNode;

import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DiscordService {
    private final Main plugin;
    private final LuckPerms luckPerms;
    
    public DiscordService(Main plugin) {
        this.plugin = plugin;
        this.luckPerms = plugin.getLuckPerms();
    }

    /**
     * Syncs the user to the discord server if the ROLE_SYNC_MC_TO_DISCORD
     *  configuration option is not enabled.
     *
     * Retrieves the user's unique identifier using {@link OfflinePlayer#getUniqueId()}.
     * Retrieves the user's information from the database using {@link plugin.getDatabaseManager()}.getAccountAsync(UUID)}.
     * If the user is null, the method returns.
     * Retrieves the guild that the user is in using {@link plugin.getDiscordBotManager()}.getJDA()}.
     * If the guild is null, the method returns.
     * Retrieves the user's groups using {@link plugin.getLuckPerms()}.getUserManager().getUser(UUID)}.
     * If the groups map is empty, the method returns.
     * Sets the groups that the user is in to the given groups map.
     * If the groups map is not empty, the method returns the guild that the user is in.
     */
    public void syncMcToDiscord(OfflinePlayer player) {
        if (!plugin.config().ROLE_SYNC_MC_TO_DISCORD_ENABLED) return;

        UUID uuid = player.getUniqueId();

        plugin.getDatabaseManager()
            .getAccountAsync(uuid)
            .thenAccept(acc -> {
                if (acc == null) return;

                Guild guild = plugin.getDiscordBotManager().getJda()
                        .getGuildById(plugin.config().DISCORD_GUILD_ID);
                if (guild == null) return;

                long discordId = acc.getDiscordId();

                User lpUser = plugin.getLuckPerms().getUserManager().getUser(uuid);
                if (lpUser == null) return;

                Set<String> playerGroups = getPlayerGroups(uuid);
                if (playerGroups.isEmpty()) return;

                Map<String, String> groupMap = plugin.config().ROLE_SYNC_MC_TO_DISCORD_GROUPS;
                
                guild.retrieveMemberById(discordId).queue(member -> {
                    syncMcToDiscordRoles(member, playerGroups, groupMap, guild);
                });
            });
    }
    
    /**
     * Syncs the user to the Mc server if the ROLE_SYNC_DISCORD_TO_MC configuration option is not enabled.
     *
     * Retrieves the user's unique identifier using {@link OfflinePlayer#getUniqueId()}.
     * Retrieves the user's information from the database using {@link plugin.getDatabaseManager()}.getAccountAsync(UUID)}.
     * If the user is null, the method returns.
     * Retrieves the user's groups using {@link plugin.getLuckPerms()}.getUserManager().getUser(UUID)}.
     * If the groups map is empty, the method returns.
     * Sets the groups that the user is in to the given groups map.
     * If the groups map is not empty, the method returns the guild that the user is in.
     */
    public void syncDiscordToMc(long discordId) {
        if (!plugin.config().ROLE_SYNC_DISCORD_TO_MC_ENABLED) return;

        Guild guild = plugin.getDiscordBotManager()
                .getJda()
                .getGuildById(plugin.config().DISCORD_GUILD_ID);
        if (guild == null) return;

        guild.retrieveMemberById(discordId).queue(member -> {
            plugin.getDatabaseManager()
                .getAccountAsync(discordId)
                .thenAccept(acc -> {
                    if (acc == null) return;

                    UUID playerId = acc.getPlayerId();

                    User lpUser = luckPerms.getUserManager().getUser(playerId);
                    if (lpUser == null) return;

                    Set<String> playerGroups = getPlayerGroups(playerId);

                    Map<String, String> roleMap =
                            plugin.config().ROLE_SYNC_DISCORD_TO_MC_ROLES;

                    syncGroupsFromDiscord(member, lpUser, roleMap, playerGroups);
                });
        });
    }

    /**
     * Sync groups from a Discord member to a Mc user.
     * @param member the Discord member to sync groups from
     * @param user the Mc user to sync groups to
     * @param roleMap the map of roles to groups
     * @param playerGroups the set of groups to sync
     */
    private void syncGroupsFromDiscord(Member member, User user, Map<String, String> roleMap, Set<String> playerGroups) {
        Set<String> discordRoleIds = member.getRoles()
                .stream()
                .map(Role::getId)
                .collect(Collectors.toSet());

        roleMap.forEach((roleId, group) -> {

            boolean hasDiscordRole = discordRoleIds.contains(roleId);
            boolean hasMcGroup = playerGroups.contains(group);

            if (hasDiscordRole && !hasMcGroup) {
                user.data().add(
                    InheritanceNode.builder(group).build()
                );
            }

            if (!hasDiscordRole && hasMcGroup) {
                user.data().remove(
                    InheritanceNode.builder(group).build()
                );
            }
        });

        luckPerms.getUserManager().saveUser(user);
    }

    /**
     * Syncs Minecraft (LuckPerms) groups to Discord roles.
     * @param member the member to update
     * @param playerGroups the groups the player is in
     * @param groupMap the map of roles to groups
     * @param guild the guild to update
     */
    private void syncMcToDiscordRoles(Member member, Set<String> playerGroups, Map<String, String> groupMap, Guild guild) {
        Map<String, String> roleToGroup = groupMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getValue,
                        Map.Entry::getKey
                ));

        Set<String> managedRoleIds = roleToGroup.keySet();

        for (String group : playerGroups) {
            String roleId = groupMap.get(group);
            if (roleId == null) continue;

            Role role = guild.getRoleById(roleId);
            if (role == null) continue;

            if (!member.getRoles().contains(role)) {
                guild.addRoleToMember(member, role).queue(sc -> {}, er -> {});
            }
        }

        for (Role role : member.getRoles()) {
            String group = roleToGroup.get(role.getId());
            if (group == null) continue;

            if (!playerGroups.contains(group)) {
                guild.removeRoleFromMember(member, role).queue(sc -> {}, er -> {});
            }
        }
    }

    /**
     * A set of groups that a user is in. This is used for permission checks.
     * @param uuid the uuid of the user
     * @return a set of group names
     */
    private Set<String> getPlayerGroups(UUID uuid) {
        User lpUser = luckPerms.getUserManager().getUser(uuid);
        if (lpUser == null) return Set.of();

        return lpUser.getInheritedGroups(
                QueryOptions.defaultContextualOptions()
        ).stream().map(Group::getName).collect(Collectors.toSet());
    }

    /**
     * Resets a user's nickname on the Discord server to null.
     * @param discordId the Discord ID of the user to reset the nickname for
     */
    public void resetDiscordNickname(long discordId) {
        Guild guild = plugin.getDiscordBotManager()
                .getJda()
                .getGuildById(plugin.config().DISCORD_GUILD_ID);

        if (guild == null) return;

        guild.retrieveMemberById(discordId).queue(
            member -> {
                member.modifyNickname(null).queue(
                    success -> {},
                    error -> plugin.getLogger().warning(
                        "[Discord] Failed to reset nickname: " + error.getMessage()
                    )
                );
            },
            error -> plugin.getLogger().warning(
                "[Discord] Member not found for nickname reset"
            )
        );
    }

    public void forceRemoveMcRanksOnUnlink(UUID playerId) {
        if (!plugin.config().ROLE_SYNC_DISCORD_TO_MC_ENABLED) return;

        User user = luckPerms.getUserManager().getUser(playerId);
        if (user == null) return;

        Set<String> playerGroups = getPlayerGroups(playerId);
        if (playerGroups.isEmpty()) return;

        Map<String, String> roleMap =
                plugin.config().ROLE_SYNC_DISCORD_TO_MC_ROLES;

        boolean changed = false;

        for (String group : roleMap.values()) {
            if (playerGroups.contains(group)) {
                user.data().remove(
                        InheritanceNode.builder(group).build()
                );
                changed = true;
            }
        }

        if (changed) {
            luckPerms.getUserManager().saveUser(user);
        }
    }

    public boolean isAllowedRole(SlashCommandInteractionEvent event, List<String> allowedRoles) {
        if (allowedRoles == null || allowedRoles.isEmpty()) return true;

        Member member = event.getMember();
        if (member == null) return false;

        return member.getRoles().stream()
                .anyMatch(role -> allowedRoles.contains(role.getId()));
    }

    public boolean isAllowedChannel(SlashCommandInteractionEvent event, List<String> allowedChannels) {
        if (allowedChannels == null || allowedChannels.isEmpty()) return true;

        return allowedChannels.contains(event.getChannel().getId());
    }
}
