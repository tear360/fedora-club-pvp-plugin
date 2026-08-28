package fr.duelplugin.managers;

import fr.duelplugin.DuelPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class LanguageManager {

    private final DuelPlugin plugin;
    private final Map<Language, Map<String, String>> translations = new HashMap<>();

    public LanguageManager(DuelPlugin plugin) {
        this.plugin = plugin;
        for (Language lang : Language.values()) {
            translations.put(lang, new HashMap<>());
        }
        loadTranslations();
    }

    private Language getLang(Player player) {
        if (player == null) return Language.FR;
        return plugin.getSettingsManager().getLanguage(player.getUniqueId());
    }

    public String msg(Player player, String key, String... args) {
        Language lang = getLang(player);
        String pattern = translations.getOrDefault(lang, translations.get(Language.FR)).get(key);
        if (pattern == null) {
            pattern = translations.get(Language.FR).get(key);
        }
        if (pattern == null) {
            return plugin.getPrefix() + ChatColor.RED + "Missing: " + key;
        }
        String result = pattern;
        for (int i = 0; i < args.length - 1; i += 2) {
            result = result.replace(args[i], args[i + 1]);
        }
        return plugin.getPrefix() + ChatColor.translateAlternateColorCodes('&', result);
    }

    public String msgRaw(Player player, String key, String... args) {
        Language lang = getLang(player);
        String pattern = translations.getOrDefault(lang, translations.get(Language.FR)).get(key);
        if (pattern == null) {
            pattern = translations.get(Language.FR).get(key);
        }
        if (pattern == null) {
            return ChatColor.RED + "Missing: " + key;
        }
        String result = pattern;
        for (int i = 0; i < args.length - 1; i += 2) {
            result = result.replace(args[i], args[i + 1]);
        }
        return ChatColor.translateAlternateColorCodes('&', result);
    }

    public String msgNoPrefix(Player player, String key, String... args) {
        Language lang = getLang(player);
        String pattern = translations.getOrDefault(lang, translations.get(Language.FR)).get(key);
        if (pattern == null) {
            pattern = translations.get(Language.FR).get(key);
        }
        if (pattern == null) {
            return ChatColor.RED + "Missing: " + key;
        }
        String result = pattern;
        for (int i = 0; i < args.length - 1; i += 2) {
            result = result.replace(args[i], args[i + 1]);
        }
        return ChatColor.translateAlternateColorCodes('&', result);
    }

    private void t(Language lang, String key, String value) {
        translations.get(lang).put(key, value);
    }

    private void tBoth(String key, String fr, String en) {
        t(Language.FR, key, fr);
        t(Language.EN, key, en);
    }

    private void loadTranslations() {
        // ───────────── COMMON ─────────────
        tBoth("command_only_players",
            "&cCommande réservée aux joueurs.",
            "&cCommand only for players.");
        tBoth("no_permission",
            "&cVous n'avez pas la permission.",
            "&cYou don't have permission.");
        tBoth("player_not_found",
            "&cJoueur introuvable ou hors ligne.",
            "&cPlayer not found or offline.");

        // ───────────── FRIENDS ─────────────
        tBoth("friend_added",
            "&d%player% &aajouté(e) en ami!",
            "&d%player% &ahas been added as friend!");
        tBoth("friend_removed",
            "&d%player% &cretiré(e) de vos amis.",
            "&d%player% &cremoved from your friends.");
        tBoth("friend_already_exists",
            "&c%player% est déjà votre ami.",
            "&c%player% is already your friend.");
        tBoth("friend_cannot_self",
            "&cVous ne pouvez pas vous ajouter vous-même.",
            "&cYou cannot add yourself.");
        tBoth("friend_disabled",
            "&c%player% a désactivé les demandes d'amis.",
            "&c%player% has disabled friend requests.");
        tBoth("friend_request_sent",
            "&dDemande d'ami envoyée à &f%player%&d!",
            "&dFriend request sent to &f%player%&d!");
        tBoth("friend_request_received",
            "&d%player% &7vous a envoyé une demande d'ami!",
            "&d%player% &7sent you a friend request!");
        tBoth("friend_request_accept",
            "&a[ACCEPTER]",
            "&a[ACCEPT]");
        tBoth("friend_request_deny",
            "&c[REFUSER]",
            "&c[DECLINE]");
        tBoth("friend_request_hover_accept",
            "Accepter la demande",
            "Accept the request");
        tBoth("friend_request_hover_deny",
            "Refuser la demande",
            "Decline the request");
        tBoth("friend_accept_success",
            "&d%player% &aaccepté votre demande d'ami!",
            "&d%player% &aaccepted your friend request!");
        tBoth("friend_accept_no_requests",
            "&cAucune demande d'ami en attente.",
            "&cNo pending friend requests.");
        tBoth("friend_deny_success",
            "&d%player% &ca refusé votre demande d'ami.",
            "&d%player% &crejected your friend request.");
        tBoth("friend_deny_refused",
            "&cDemande refusée.",
            "&cRequest declined.");
        tBoth("friend_denied_by_target",
            "&d%player% &ca refusé votre demande d'ami.",
            "&d%player% &crejected your friend request.");
        tBoth("friend_not_friend",
            "&c%player% n'est pas votre ami.",
            "&c%player% is not your friend.");
        tBoth("friend_list_title",
            "&d&lVos amis &7(%count%)",
            "&d&lYour Friends &7(%count%)");
        tBoth("friend_list_empty",
            "&7Aucun ami. Utilisez &d/f add <joueur>",
            "&7No friends. Use &d/f add <player>");
        tBoth("friend_list_online",
            "&a● &f%player% &7(En ligne)",
            "&a● &f%player% &7(Online)");
        tBoth("friend_list_offline",
            "&7● &8%player% &7(Hors ligne)",
            "&7● &8%player% &7(Offline)");
        tBoth("friend_help_title",
            "&d&lCommandes Amis",
            "&d&lFriend Commands");
        tBoth("friend_help_add",
            "&d/f add <joueur> &7- Envoyer une demande d'ami",
            "&d/f add <player> &7- Send a friend request");
        tBoth("friend_help_accept",
            "&d/f accept &7- Accepter la dernière demande",
            "&d/f accept &7- Accept the last request");
        tBoth("friend_help_deny",
            "&d/f deny &7- Refuser la dernière demande",
            "&d/f deny &7- Decline the last request");
        tBoth("friend_help_remove",
            "&d/f remove <joueur> &7- Retirer un ami",
            "&d/f remove <player> &7- Remove a friend");
        tBoth("friend_help_list",
            "&d/f list &7- Voir vos amis",
            "&d/f list &7- View your friends");
        tBoth("friend_usage_add",
            "&cUsage: /f add <joueur>",
            "&cUsage: /f add <player>");
        tBoth("friend_usage_remove",
            "&cUsage: /f remove <joueur>",
            "&cUsage: /f remove <player>");

        // ───────────── PARTY ─────────────
        tBoth("party_created",
            "&aParty &dcréée! &7Invitez des joueurs avec &d/party invite <joueur>",
            "&aParty &dcreated! &7Invite players with &d/party invite <player>");
        tBoth("party_already_in",
            "&cVous êtes déjà dans une party.",
            "&cYou are already in a party.");
        tBoth("party_not_in",
            "&cVous n'êtes pas dans une party.",
            "&cYou are not in a party.");
        tBoth("party_leader_only",
            "&cSeul le leader peut faire cela.",
            "&cOnly the leader can do that.");
        tBoth("party_full",
            "&cLa party est pleine! (%max% max)",
            "&cParty is full! (%max% max)");
        tBoth("party_disbanded",
            "&cParty dissoute.",
            "&cParty disbanded.");
        tBoth("party_disbanded_by_leader",
            "&cLa party a été dissoute par le leader.",
            "&cThe party has been disbanded by the leader.");
        tBoth("party_invite_sent",
            "&dInvitation envoyée à &f%player% &d(%count%/%max%)",
            "&dInvitation sent to &f%player% &d(%count%/%max%)");
        tBoth("party_invite_received",
            "&d%player% &7vous invite dans sa party! &d/party join &7pour accepter.",
            "&d%player% &7invited you to their party! &d/party join &7to accept.");
        tBoth("party_joined",
            "&aVous avez rejoint la party de &d%player%&a! (%count%/%max%)",
            "&aYou joined &d%player%&a's party! (%count%/%max%)");
        tBoth("party_joined_broadcast",
            "&d%player% &aa rejoint la party!",
            "&d%player% &ahas joined the party!");
        tBoth("party_joined_pub_broadcast",
            "&d%player% &aa rejoint la party via la publication!",
            "&d%player% &ahas joined the party via public invite!");
        tBoth("party_left",
            "&cVous avez quitté la party.",
            "&cYou left the party.");
        tBoth("party_member_left",
            "&d%player% &7a quitté la party.",
            "&d%player% &7left the party.");
        tBoth("party_transfer",
            "&d%player% &aest maintenant le leader!",
            "&d%player% &ais now the leader!");
        tBoth("party_kicked_self",
            "&cVous avez été kick de la party.",
            "&cYou have been kicked from the party.");
        tBoth("party_kicked_target",
            "&d%player% &ckické de la party.",
            "&d%player% &ckicked from the party.");
        tBoth("party_kicked_broadcast",
            "&d%player% &7a été kick de la party.",
            "&d%player% &7has been kicked from the party.");
        tBoth("party_no_pending_invite",
            "&cAucune invitation en attente.",
            "&cNo pending invitation.");
        tBoth("party_invite_expired",
            "&cCette party n'existe plus.",
            "&cThis party no longer exists.");
        tBoth("party_cannot_self_invite",
            "&cVous ne pouvez pas vous inviter.",
            "&cYou cannot invite yourself.");
        tBoth("party_target_already_in",
            "&c%player% est déjà dans une party.",
            "&c%player% is already in a party.");
        tBoth("party_invite_disabled",
            "&cPublication de party désactivée.",
            "&cParty publication is disabled.");
        tBoth("party_pub_success",
            "&dParty publiée dans le chat!",
            "&dParty published in chat!");
        tBoth("party_pub_hover",
            "Cliquez pour rejoindre la party de %player% (%count%)",
            "Click to join %player%'s party (%count%)");
        tBoth("party_pub_chat",
            "Rejoignez la party de",
            "Join the party of");
        tBoth("party_no_pub_available",
            "&cCette party n'est plus disponible.",
            "&cThis party is no longer available.");
        tBoth("party_pub_full",
            "&cLa party est pleine ou n'est plus disponible.",
            "&cThe party is full or no longer available.");
        tBoth("party_title_list",
            "&d&lParty &7(%count% joueur%s%)",
            "&d&lParty &7(%count% player%s%)");
        tBoth("party_leader_display",
            "&6👑 &f%player% &7(Leader)",
            "&6👑 &f%player% &7(Leader)");
        tBoth("party_vip_only",
            "&cCette commande est réservée aux VIP.",
            "&cThis command is VIP only.");
        tBoth("party_disband_only_leader",
            "&cSeul le leader peut disband la party.",
            "&cOnly the leader can disband the party.");
        tBoth("party_kick_only_leader",
            "&cSeul le leader peut kick.",
            "&cOnly the leader can kick.");
        tBoth("party_kick_usage",
            "&cUsage: /party kick <joueur>",
            "&cUsage: /party kick <player>");
        tBoth("party_kick_cannot_self",
            "&cVous ne pouvez pas vous kick.",
            "&cYou cannot kick yourself.");
        tBoth("party_kick_not_member",
            "&c%player% n'est pas dans votre party.",
            "&c%player% is not in your party.");
        tBoth("party_transfer_usage",
            "&cUsage: /party transfer <joueur>",
            "&cUsage: /party transfer <player>");
        tBoth("party_transfer_to_self",
            "&cVous ne pouvez pas vous transférer.",
            "&cYou cannot transfer to yourself.");
        tBoth("party_ffa_no_arena",
            "&cAucune arène disponible pour ce mode!",
            "&cNo arena available for this mode!");
        tBoth("party_ffa_started",
            "&5&lFFA COMMENCÉ! &dMode: &f%mode%",
            "&5&lFFA STARTED! &dMode: &f%mode%");
        tBoth("party_ffa_not_enough",
            "&cIl faut au moins 3 joueurs pour lancer un FFA.",
            "&cYou need at least 3 players to start an FFA.");
        tBoth("party_help_title",
            "&d&lCommandes Party",
            "&d&lParty Commands");
        tBoth("party_help_create",
            "&d/party create &7- Créer une party",
            "&d/party create &7- Create a party");
        tBoth("party_help_invite",
            "&d/party invite <joueur> &7- Inviter un joueur",
            "&d/party invite <player> &7- Invite a player");
        tBoth("party_help_join",
            "&d/party join &7- Accepter l'invitation",
            "&d/party join &7- Accept the invitation");
        tBoth("party_help_leave",
            "&d/party leave &7- Quitter la party",
            "&d/party leave &7- Leave the party");
        tBoth("party_help_kick",
            "&d/party kick <joueur> &7- Expulser un membre",
            "&d/party kick <player> &7- Kick a member");
        tBoth("party_help_transfer",
            "&d/party transfer <joueur> &7- Transférer le leadership",
            "&d/party transfer <player> &7- Transfer leadership");
        tBoth("party_help_disband",
            "&d/party disband &7- Dissoudre la party",
            "&d/party disband &7- Disband the party");
        tBoth("party_help_list",
            "&d/party list &7- Voir les membres",
            "&d/party list &7- View members");
        tBoth("party_help_pub",
            "&d/party pub &7- Rendre la party publique (VIP)",
            "&d/party pub &7- Make party public (VIP)");

        // ───────────── DUEL ─────────────
        tBoth("duel_sent",
            "&dDemande de duel envoyée à &f%player%&d!",
            "&dDuel request sent to &f%player%&d!");
        tBoth("duel_received_title",
            "&d⚔ &5Demande de duel!",
            "&d⚔ &5Duel Request!");
        tBoth("duel_received_player",
            "&dJoueur: &f%player%",
            "&dPlayer: &f%player%");
        tBoth("duel_received_mode",
            "&dMode: &f%mode%",
            "&dMode: &f%mode%");
        tBoth("duel_accept_button",
            "&a[ACCEPTER]",
            "&a[ACCEPT]");
        tBoth("duel_deny_button",
            "&c[REFUSER]",
            "&c[DECLINE]");
        tBoth("duel_accept_hover",
            "Cliquez pour accepter le duel",
            "Click to accept the duel");
        tBoth("duel_deny_hover",
            "Cliquez pour refuser le duel",
            "Click to decline the duel");
        tBoth("duel_action",
            "Action: ",
            "Action: ");
        tBoth("duel_started",
            "&5&lDUEL COMMENCÉ! &dContre &f%player% &den &f%mode%",
            "&5&lDUEL STARTED! &dAgainst &f%player% &din &f%mode%");
        tBoth("duel_winner",
            "&a&l⚔ VICTOIRE!",
            "&a&l⚔ VICTORY!");
        tBoth("duel_winner_ffa",
            "&aVous êtes le dernier en vie de la FFA!",
            "&aYou are the last one standing in the FFA!");
        tBoth("duel_winner_against",
            "&aVous avez gagné contre &d%player%",
            "&aYou won against &d%player%");
        tBoth("duel_eliminated",
            "&c&l⚔ ÉLIMINÉ",
            "&c&l⚔ ELIMINATED");
        tBoth("duel_eliminated_ffa",
            "&cVous avez été éliminé de la FFA.",
            "&cYou have been eliminated from the FFA.");
        tBoth("duel_eliminated_against",
            "&cVous avez perdu contre &d%player%",
            "&cYou lost against &d%player%");
        tBoth("duel_ended",
            "&7Le duel est terminé.",
            "&7The duel has ended.");
        tBoth("duel_countdown",
            "&d&l%count% &7...",
            "&d&l%count% &7...");
        tBoth("duel_go",
            "&a&lGO! &7Combattez!",
            "&a&lGO! &7Fight!");
        tBoth("duel_usage",
            "&dUsage: /duel <joueur> &7ou &d/duel leave",
            "&dUsage: /duel <player> &7or &d/duel leave");
        tBoth("duel_leave_success",
            "&cVous avez quitté le duel.",
            "&cYou left the duel.");
        tBoth("duel_leave_opponent_won",
            "&d%player% &ca quitté le duel. &aVictoire!",
            "&d%player% &cleft the duel. &aVictory!");
        tBoth("duel_not_in",
            "&cVous n'êtes pas en duel.",
            "&cYou are not in a duel.");
        tBoth("duel_target_online",
            "&cCe joueur n'est plus en ligne.",
            "&cThis player is no longer online.");
        tBoth("duel_accept_success",
            "&aVous avez accepté le duel de &d%player%&a!",
            "&aYou accepted the duel from &d%player%&a!");
        tBoth("duel_accept_fail",
            "&cImpossible de démarrer le duel.",
            "&cCould not start the duel.");
        tBoth("duel_accept_no_perm",
            "&cVous n'avez pas la permission d'accepter les duels.",
            "&cYou don't have permission to accept duels.");
        tBoth("duel_deny_broadcast",
            "&d%player% &ca refusé votre duel.",
            "&d%player% &crejected your duel.");
        tBoth("duel_queue_left",
            "&cQueue quittée.",
            "&cQueue left.");
        tBoth("duel_queue_not_in",
            "&cVous n'êtes pas en queue.",
            "&cYou are not in queue.");
        tBoth("duel_cannot_self",
            "&cVous ne pouvez pas vous défié vous-même.",
            "&cYou cannot duel yourself.");
        tBoth("duel_queue_disabled",
            "&cLa file d'attente est désactivée.",
            "&cQueue is disabled.");
        tBoth("duel_both_in_duel",
            "&cUn des joueurs est déjà en duel!",
            "&cOne of the players is already in a duel!");

        // ───────────── QUEUE ─────────────
        tBoth("queue_joined",
            "&dQueue rejoinue pour &f%mode%&d! &7En attente d'un adversaire...",
            "&dQueue joined for &f%mode%&d! &7Waiting for an opponent...");
        tBoth("queue_left",
            "&cQueue &f%mode% &cquittée.",
            "&cQueue for &f%mode% &cleft.");
        tBoth("queue_full",
            "&cLa file est pleine pour ce mode.",
            "&cThe queue is full for this mode.");
        tBoth("queue_no_arena",
            "&cAucune arène disponible pour &f%mode%&c!",
            "&cNo arena available for &f%mode%&c!");
        tBoth("queue_action_bar",
            "En queue pour ",
            "Queuing for ");
        tBoth("queue_action_bar_count",
            " &7(%count% joueur%s%)",
            " &7(%count% player%s%)");
        tBoth("queue_toggle_on",
            "&aMode libre activé.",
            "&aFree mode enabled.");
        tBoth("queue_toggle_off",
            "&cMode libre désactivé.",
            "&cFree mode disabled.");

        // ───────────── ARENA ─────────────
        tBoth("arena_created",
            "&aArène &d%name% &acréée! Mode: %mode%",
            "&aArena &d%name% &acreated! Mode: %mode%");
        tBoth("arena_already_exists",
            "&cCette arène existe déjà.",
            "&cThis arena already exists.");
        tBoth("arena_deleted",
            "&aArène &d%name% &asupprimée.",
            "&aArena &d%name% &adeleted.");
        tBoth("arena_spawn_set",
            "&aPoint d'apparition §d%slot% &adéfini pour &d%name%",
            "&aSpawn point &d%slot% &aset for &d%name%");
        tBoth("arena_min_set",
            "&aCoin minimum défini pour &d%name%",
            "&aMinimum corner set for &d%name%");
        tBoth("arena_max_set",
            "&aCoin maximum défini pour &d%name%",
            "&aMaximum corner set for &d%name%");
        tBoth("arena_not_found",
            "&cArène introuvable.",
            "&cArena not found.");
        tBoth("arena_list_empty",
            "&cAucune arène configurée.",
            "&cNo arena configured.");
        tBoth("arena_list_header",
            "&dArènes:",
            "&dArenas:");
        tBoth("arena_list_entry",
            "&7- &d%name% &7(Mode: &f%mode%&7)",
            "&7- &d%name% &7(Mode: &f%mode%&7)");
        tBoth("arena_not_defined",
            "&cNon défini",
            "&cNot defined");
        tBoth("arena_configured",
            "&aOui",
            "&aYes");
        tBoth("arena_not_configured",
            "&cNon",
            "&cNo");
        tBoth("arena_usage_create",
            "&dUsage: /da arena create <nom> <mode>",
            "&dUsage: /da arena create <name> <mode>");
        tBoth("arena_usage_delete",
            "&dUsage: /da arena delete <nom>",
            "&dUsage: /da arena delete <name>");
        tBoth("arena_usage_setspawn",
            "&dUsage: /da arena setspawn <nom> <1|2>",
            "&dUsage: /da arena setspawn <name> <1|2>");
        tBoth("arena_usage_setmin",
            "&dUsage: /da arena setmin <nom>",
            "&dUsage: /da arena setmin <name>");
        tBoth("arena_usage_setmax",
            "&dUsage: /da arena setmax <nom>",
            "&dUsage: /da arena setmax <name>");
        tBoth("arena_usage_info",
            "&dUsage: /da arena info <nom>",
            "&dUsage: /da arena info <name>");
        tBoth("arena_invalid_slot",
            "&cSlot invalide. Utilisez 1 ou 2.",
            "&cInvalid slot. Use 1 or 2.");
        tBoth("arena_unknown_mode",
            "&cMode inconnu. Modes: ",
            "&cUnknown mode. Modes: ");
        tBoth("arena_config_reloaded",
            "&aConfiguration rechargée.",
            "&aConfiguration reloaded.");
        tBoth("arena_info_title",
            "&5═══════════════════════",
            "&5═══════════════════════");
        tBoth("arena_info_name",
            "&dArène: &f%name%",
            "&dArena: &f%name%");
        tBoth("arena_info_mode",
            "&dMode: %mode%",
            "&dMode: %mode%");
        tBoth("arena_info_configured",
            "&dConfigurée: %configured%",
            "&dConfigured: %configured%");
        tBoth("arena_usage_tp",
            "&dUsage: /da arena tp <nom>",
            "&dUsage: /da arena tp <name>");
        tBoth("arena_teleported",
            "&7Téléporté à l'arène &d%name%&7.",
            "&7Teleported to arena &d%name%&7.");
        tBoth("arena_no_spawn",
            "&cL'arène &d%name% &cn'a pas de spawn défini.",
            "&cArena &d%name% &chas no spawn set.");

        // ───────────── VIP ─────────────
        tBoth("vip_set",
            "&d%player% &aest maintenant VIP!",
            "&d%player% &ais now VIP!");
        tBoth("vip_received",
            "&aVous avez reçu le grade &dVIP&a!",
            "&aYou have received the &dVIP&agrade!");
        tBoth("vip_removed",
            "&d%player% &c n'est plus VIP.",
            "&d%player% &cis no longer VIP.");
        tBoth("vip_removed_self",
            "&cVotre grade VIP a été retiré.",
            "&cYour VIP grade has been removed.");
        tBoth("vip_not_vip",
            "&cVous n'êtes pas VIP.",
            "&cYou are not VIP.");
        tBoth("vip_color_changed",
            "&dCouleur changée en &f%player%&d!",
            "&dColor changed to &f%player%&d!");
        tBoth("vip_color_invalid",
            "&cCouleur introuvable. Utilisez /vip color pour voir les options.",
            "&cColor not found. Use /vip color to see options.");
        tBoth("vip_info_title",
            "&d&l--- VIP ---",
            "&d&l--- VIP ---");
        tBoth("vip_info_status",
            "&dVIP: %status%",
            "&dVIP: %status%");
        tBoth("vip_info_yes",
            "&aOui",
            "&aYes");
        tBoth("vip_info_no",
            "&cNon",
            "&cNo");
        tBoth("vip_info_color",
            "&dCouleur: %color%•",
            "&dColor: %color%•");
        tBoth("vip_help_set",
            "&d/vip set <joueur> &7- Donner le VIP",
            "&d/vip set <player> &7- Give VIP");
        tBoth("vip_help_remove",
            "&d/vip remove <joueur> &7- Retirer le VIP",
            "&d/vip remove <player> &7- Remove VIP");
        tBoth("vip_help_color",
            "&d/vip color &7- Changer la couleur de nom",
            "&d/vip color &7- Change name color");
        tBoth("vip_help_badges",
            "&d/vip badges &7- Choisir votre badge",
            "&d/vip badges &7- Choose your badge");
        tBoth("vip_help_info",
            "&d/vip info &7- Infos VIP",
            "&d/vip info &7- VIP Info");
        tBoth("vip_color_red", "Rouge", "Red");
        tBoth("vip_color_gold", "Or", "Gold");
        tBoth("vip_color_yellow", "Jaune", "Yellow");
        tBoth("vip_color_green", "Vert", "Green");
        tBoth("vip_color_aqua", "Aqua", "Aqua");
        tBoth("vip_color_pink", "Rose", "Pink");
        tBoth("vip_color_purple", "Violet", "Purple");
        tBoth("vip_color_white", "Blanc", "White");
        tBoth("vip_color_gray", "Gris", "Gray");
        tBoth("vip_color_black", "Noir", "Black");
        tBoth("vip_colors_available",
            "&dCouleurs disponibles:",
            "&dAvailable colors:");
        tBoth("vip_badges_title",
            "&5Badge VIP",
            "&5VIP Badge");
        tBoth("vip_badge_selected",
            "&aSelectionné",
            "&aSelected");
        tBoth("vip_badge_click",
            "&7Cliquez pour sélectionner",
            "&7Click to select");

        // ───────────── SPEC ─────────────
        tBoth("spec_usage",
            "&dUsage: /spec <joueur>",
            "&dUsage: /spec <player>");
        tBoth("spec_target_not_in_duel",
            "&cCe joueur n'est pas en duel.",
            "&cThis player is not in a duel.");
        tBoth("spec_already_in_duel",
            "&cVous êtes déjà en duel.",
            "&cYou are already in a duel.");
        tBoth("spec_started",
            "&7Vous spectate &d%player% &7(&d%mode%&7)",
            "&7You are spectating &d%player% &7(&d%mode%&7)");
        tBoth("spec_target_notified",
            "&d%player% &7vous spectate.",
            "&d%player% &7is spectating you.");
        tBoth("spec_target_broadcast",
            "&d%player% &7est maintenant spectateur.",
            "&d%player% &7is now a spectator.");
        tBoth("spec_stopped",
            "&aVous avez arrêté de spectater.",
            "&aYou stopped spectating.");

        // ───────────── SETTINGS ─────────────
        tBoth("settings_title",
            "&d&lParamètres",
            "&d&lSettings");
        tBoth("settings_friends_on",
            "&dAmis: &aActivé",
            "&dFriends: &aEnabled");
        tBoth("settings_friends_off",
            "&dAmis: &cDésactivé",
            "&dFriends: &cDisabled");
        tBoth("settings_duels_on",
            "&dDuels: &aActivé",
            "&dDuels: &aEnabled");
        tBoth("settings_duels_off",
            "&dDuels: &cDésactivé",
            "&dDuels: &cDisabled");
        tBoth("settings_toggle_hint",
            "&7Utilisez &d/settings friends &7ou &d/settings duels",
            "&7Use &d/settings friends &7or &d/settings duels");
        tBoth("settings_toggle_hint2",
            "&7pour basculer un paramètre.",
            "&7to toggle a setting.");
        tBoth("settings_friends_toggled",
            "&dDemandes d'amis: %status%",
            "&dFriend requests: %status%");
        tBoth("settings_duels_toggled",
            "&dDemandes de duel: %status%",
            "&dDuel requests: %status%");
        tBoth("settings_language_fr",
            "&dLangue: &fFrançais",
            "&dLanguage: &fFrançais");
        tBoth("settings_language_en",
            "&dLangue: &fEnglish",
            "&dLanguage: &fEnglish");
        tBoth("settings_select_language",
            "&dSélectionnez une langue:",
            "&dSelect a language:");
        tBoth("language_changed",
            "&aLangue changée en &f%language%&a!",
            "&aLanguage changed to &f%language%&a!");
        tBoth("status_enabled",
            "&aActivé",
            "&aEnabled");
        tBoth("status_disabled",
            "&cDésactivé",
            "&cDisabled");

        // ───────────── DUEL GUI ─────────────
        tBoth("gui_duel_title",
            "Défi → %target%",
            "Challenge → %target%");
        tBoth("gui_mode_select",
            "Sélection de mode",
            "Mode Selection");
        tBoth("gui_arenas_available",
            "&aArènes disponibles",
            "&aArenas available");
        tBoth("gui_no_arena",
            "&cAucune arène",
            "&cNo arena");
        tBoth("gui_free_mode",
            "&aMode libre",
            "&aFree mode");
        tBoth("gui_blocks",
            "&7Blocs: ",
            "&7Blocks: ");
        tBoth("gui_blocks_breakable",
            "&aCassables",
            "&aBreakable");
        tBoth("gui_blocks_unbreakable",
            "&cNon cassables",
            "&cUnbreakable");
        tBoth("gui_click_to_play",
            "&d&lCliquez pour jouer",
            "&d&lClick to play");

        // ───────────── KIT EDITOR ─────────────
        tBoth("gui_kit_editor_title",
            "Éditeur de kits",
            "Kit Editor");
        tBoth("gui_kit_custom",
            "&a&lKit personnalisé",
            "&a&lCustom Kit");
        tBoth("gui_kit_default",
            "&7Kit par défaut",
            "&7Default Kit");
        tBoth("gui_kit_click_edit",
            "&d&lCliquez pour éditer",
            "&d&lClick to edit");
        tBoth("gui_kit_title",
            "Kit %mode%",
            "Kit %mode%");
        tBoth("gui_kit_save",
            "&a&lSauvegarder",
            "&a&lSave");
        tBoth("gui_kit_save_lore",
            "&7Cliquez pour sauvegarder",
            "&7Click to save");
        tBoth("gui_kit_reset",
            "&c&lRéinitialiser",
            "&c&lReset");
        tBoth("gui_kit_reset_lore",
            "&7Cliquez pour réinitialiser",
            "&7Click to reset");
        tBoth("gui_kit_trims",
            "&5&lTrims VIP",
            "&5&lVIP Trims");
        tBoth("gui_kit_trims_lore",
            "&7Personnalisez les trims",
            "&7Customize your armor");
        tBoth("gui_kit_trims_lore2",
            "&7de votre armure",
            "&7trims");
        tBoth("gui_kit_back",
            "&d&lRetour",
            "&d&lBack");
        tBoth("gui_kit_back_lore",
            "&7Retour au menu",
            "&7Back to menu");
        tBoth("gui_armor_select_title",
            "Choisir une pièce d'armure",
            "Select Armor Piece");
        tBoth("gui_armor_boots",
            "Bottes",
            "Boots");
        tBoth("gui_armor_leggings",
            "Jambières",
            "Leggings");
        tBoth("gui_armor_chestplate",
            "Plastron",
            "Chestplate");
        tBoth("gui_armor_helmet",
            "Casque",
            "Helmet");
        tBoth("gui_armor_type",
            "&7Type: &f%type%",
            "&7Type: &f%type%");
        tBoth("gui_armor_trim",
            "&7Trim: %trim%",
            "&7Trim: %trim%");
        tBoth("gui_armor_click_edit",
            "&d&lCliquez pour éditer",
            "&d&lClick to edit");
        tBoth("gui_armor_empty",
            "&7Aucune armure dans ce slot",
            "&7No armor in this slot");
        tBoth("gui_armor_click_add",
            "&d&lCliquez pour ajouter",
            "&d&lClick to add");
        tBoth("gui_armor_back_lore",
            "&7Retour à l'éditeur",
            "&7Back to editor");
        tBoth("gui_trim_pattern_title",
            "Choisir un pattern",
            "Select Pattern");
        tBoth("gui_trim_pattern_info",
            "&7Pattern de trim",
            "&7Trim pattern");
        tBoth("gui_trim_pattern_selected",
            "&aSélectionné actuellement",
            "&aCurrently selected");
        tBoth("gui_trim_pattern_click",
            "&d&lCliquez pour sélectionner",
            "&d&lClick to select");
        tBoth("gui_trim_pattern_back",
            "&7Retour à la sélection de pièce",
            "&7Back to piece selection");
        tBoth("gui_trim_material_title",
            "Choisir un matériau",
            "Select Material");
        tBoth("gui_trim_material_info",
            "&7Matériau de trim",
            "&7Trim material");
        tBoth("gui_trim_material_selected",
            "&aSélectionné actuellement",
            "&aCurrently selected");
        tBoth("gui_trim_material_click",
            "&d&lCliquez pour sélectionner",
            "&d&lClick to select");
        tBoth("gui_trim_material_back",
            "&7Retour à la sélection de pattern",
            "&7Back to pattern selection");
        tBoth("gui_kit_saved",
            "&aKit &d%mode% &asauvegardé!",
            "&aKit &d%mode% &asaved!");
        tBoth("gui_kit_reset_done",
            "&dKit réinitialisé aux valeurs par défaut.",
            "&dKit reset to default values.");
        tBoth("gui_trim_applied",
            "&dTrim appliqué sur &f%slot%&d: &f%pattern% &7/ &f%material%",
            "&dTrim applied to &f%slot%&d: &f%pattern% &7/ &f%material%");

        // ───────────── PARTY GUI ─────────────
        tBoth("gui_party_title",
            "Party",
            "Party");
        tBoth("gui_party_create",
            "&a&lCréer une party",
            "&a&lCreate Party");
        tBoth("gui_party_create_lore1",
            "&7Créez votre propre party",
            "&7Create your own party");
        tBoth("gui_party_create_lore2",
            "&7pour jouer avec vos amis.",
            "&7to play with your friends.");
        tBoth("gui_party_create_click",
            "&a&lCliquez pour créer",
            "&a&lClick to create");
        tBoth("gui_party_join",
            "&a&lRejoindre la party",
            "&a&lJoin Party");
        tBoth("gui_party_join_lore1",
            "&7Invité par",
            "&7Invited by");
        tBoth("gui_party_join_lore2",
            "&7Membres: &f",
            "&7Members: &f");
        tBoth("gui_party_join_click",
            "&a&lCliquez pour accepter",
            "&a&lClick to accept");
        tBoth("gui_party_decline",
            "&c&lRefuser l'invitation",
            "&c&lDecline Invitation");
        tBoth("gui_party_decline_lore1",
            "&7Refuser l'invitation de",
            "&7Decline invitation from");
        tBoth("gui_party_decline_click",
            "&c&lCliquez pour refuser",
            "&c&lClick to decline");
        tBoth("gui_party_leader_title",
            "Party (Leader)",
            "Party (Leader)");
        tBoth("gui_party_leader_invite",
            "&d&lInviter un joueur",
            "&d&lInvite Player");
        tBoth("gui_party_leader_invite_lore1",
            "&7Invitez un joueur en ligne",
            "&7Invite an online player");
        tBoth("gui_party_leader_invite_lore2",
            "&7dans votre party",
            "&7to your party");
        tBoth("gui_party_leader_invite_click",
            "&d&lCliquez pour inviter",
            "&d&lClick to invite");
        tBoth("gui_party_leader_ffa",
            "&d&lLancer un FFA",
            "&d&lStart FFA");
        tBoth("gui_party_leader_ffa_lore1",
            "&7Lancez un combat libre",
            "&7Start a free-for-all");
        tBoth("gui_party_leader_ffa_lore2",
            "&7avec tous les membres",
            "&7with all members");
        tBoth("gui_party_leader_ffa_lore3",
            "&7de la party",
            "&7of the party");
        tBoth("gui_party_leader_ffa_click",
            "&d&lCliquez pour lancer",
            "&d&lClick to start");
        tBoth("gui_party_leader_transfer",
            "&d&lTransférer le leadership",
            "&d&lTransfer Leadership");
        tBoth("gui_party_leader_transfer_lore1",
            "&7Transférez la direction",
            "&7Transfer leadership");
        tBoth("gui_party_leader_transfer_lore2",
            "&7de la party à un membre",
            "&7to a member");
        tBoth("gui_party_leader_transfer_click",
            "&d&lCliquez pour transférer",
            "&d&lClick to transfer");
        tBoth("gui_party_leader_disband",
            "&c&lDissoudre la party",
            "&c&lDisband Party");
        tBoth("gui_party_leader_disband_lore1",
            "&7Dissout la party et",
            "&7Disbands the party");
        tBoth("gui_party_leader_disband_lore2",
            "&7expulse tous les membres",
            "&7and removes all members");
        tBoth("gui_party_leader_disband_click",
            "&c&lCliquez pour dissoudre",
            "&c&lClick to disband");
        tBoth("gui_party_leader_leave",
            "&c&lQuitter la party",
            "&c&lLeave Party");
        tBoth("gui_party_leader_leave_lore1",
            "&7Quittez votre propre party",
            "&7Leave your own party");
        tBoth("gui_party_leader_leave_lore2",
            "&7(Transfert au 1er membre)",
            "&7(Transfer to first member)");
        tBoth("gui_party_leader_leave_click",
            "&c&lCliquez pour quitter",
            "&c&lClick to leave");
        tBoth("gui_party_back_lobby",
            "&d&lRetour",
            "&d&lBack");
        tBoth("gui_party_back_lobby_lore",
            "&7Retour au lobby",
            "&7Back to lobby");
        tBoth("gui_party_member_leave",
            "&c&lQuitter la party",
            "&c&lLeave Party");
        tBoth("gui_party_member_leave_lore",
            "&7Quittez la party",
            "&7Leave the party");
        tBoth("gui_party_member_leave_click",
            "&c&lCliquez pour quitter",
            "&c&lClick to leave");
        tBoth("gui_party_kick_title",
            "Kick un membre",
            "Kick a Member");
        tBoth("gui_party_kick",
            "&c&lKick &f%player%",
            "&c&lKick &f%player%");
        tBoth("gui_party_kick_back",
            "&d&lRetour",
            "&d&lBack");
        tBoth("gui_party_kick_back_lore",
            "&7Retour à la party",
            "&7Back to party");
        tBoth("gui_party_ffa_title",
            "Choisir un mode FFA",
            "Select FFA Mode");
        tBoth("gui_party_ffa_launch",
            "&7Lancer un FFA",
            "&7Start an FFA");
        tBoth("gui_party_ffa_with_mode",
            "&7avec ce mode",
            "&7with this mode");
        tBoth("gui_party_ffa_click_launch",
            "&d&lCliquez pour lancer",
            "&d&lClick to start");
        tBoth("gui_party_ffa_unavailable",
            "&c&lIndisponible",
            "&c&lUnavailable");
        tBoth("gui_party_transfer_title",
            "Transférer le leadership",
            "Transfer Leadership");
        tBoth("gui_party_transfer_to",
            "&d&lTransférer à &f%player%",
            "&d&lTransfer to &f%player%");
        tBoth("gui_party_transfer_back",
            "&d&lRetour",
            "&d&lBack");
        tBoth("gui_party_transfer_back_lore",
            "&7Retour à la party",
            "&7Back to party");

        // ───────────── SCOREBOARD ─────────────
        tBoth("sb_use_queue",
            "Use &b⚔&7 to queue",
            "Use &b⚔&7 to queue");
        tBoth("sb_use_duel",
            "or &b/duel&7 to duel.",
            "or &b/duel&7 to duel.");
        tBoth("sb_ip",
            "&6fedora.free-node.ovh",
            "&6fedora.free-node.ovh");
        tBoth("sb_vs",
            "&7vs",
            "&7vs");
        tBoth("sb_mode",
            "&dMode: &f%mode%",
            "&dMode: &f%mode%");

        // ───────────── TAB ─────────────
        tBoth("tab_mode_friends",
            " ★ Mode Amis",
            " ★ Friend Mode");
        tBoth("tab_ip",
            " fedora.free-node.ovh",
            " fedora.free-node.ovh");
        tBoth("tab_spectators",
            "Spectateurs",
            "Spectators");
        tBoth("tab_all_players",
            "Tous les joueurs",
            "All players");
        tBoth("tab_friends_only",
            "Amis uniquement",
            "Friends only");

        // ───────────── LOBBY ITEMS ─────────────
        tBoth("lobby_item_queue",
            "&d&lQueue",
            "&d&lQueue");
        tBoth("lobby_item_queue_lore",
            "&7Rejoindre une queue de duel",
            "&7Join a duel queue");
        tBoth("lobby_item_kits",
            "&5&lKits",
            "&5&lKits");
        tBoth("lobby_item_kits_lore",
            "&7Éditez vos kits",
            "&7Edit your kits");
        tBoth("lobby_item_party",
            "&d&lParty",
            "&d&lParty");
        tBoth("lobby_item_party_lore",
            "&7Gérez votre party",
            "&7Manage your party");
        tBoth("lobby_item_elysta",
            "&d&lElytra VIP",
            "&d&lVIP Elytra");
        tBoth("lobby_item_elysta_lore1",
            "&7Élytra exclusive VIP",
            "&7VIP exclusive Elytra");
        tBoth("lobby_item_elysta_lore2",
            "&7Incassable",
            "&7Unbreakable");

        // ───────────── LOBBY JOIN/QUIT ─────────────
        tBoth("lobby_join",
            "&5+ &d%player% &7a rejoint le serveur",
            "&5+ &d%player% &7joined the server");
        tBoth("lobby_quit",
            "&5- &d%player% &7a quitté le serveur",
            "&5- &d%player% &7left the server");

        // ───────────── LEAVE ─────────────
        tBoth("leave_not_in_duel",
            "&cVous n'êtes pas en duel.",
            "&cYou are not in a duel.");

        // ───────────── MISC ─────────────
        tBoth("gamemode_disabled",
            "&cCe bloc ne peut pas être cassé dans ce mode.",
            "&cThis block cannot be broken in this mode.");
        tBoth("queue_title",
            "Rejoindre une queue",
            "Join a Queue");

        // ───────────── LOBBY BUILD MODE ─────────────
        tBoth("lobby_build_mode_enabled",
            "&aMode build activé. /da lobby build pour quitter.",
            "&aBuild mode enabled. /da lobby build to exit.");
        tBoth("lobby_build_mode_disabled",
            "&cMode build désactivé.",
            "&cBuild mode disabled.");
        tBoth("lobby_item_settings",
            "&5&lParamètres",
            "&5&lSettings");
        tBoth("lobby_item_settings_lore",
            "&7Changez vos paramètres",
            "&7Change your settings");
        tBoth("lobby_block_interact_disabled",
            "&cVous ne pouvez pas interagir avec les blocs ici.",
            "&cYou cannot interact with blocks here.");

        // ───────────── TITLES ─────────────
        tBoth("title_victory",
            "VICTOIRE",
            "VICTORY");
        tBoth("title_defeat",
            "DÉFAITE",
            "DEFEAT");
        tBoth("title_duel_found",
            "DUEL TROUVÉ",
            "DUEL FOUND");
        tBoth("title_party_disbanded",
            "PARTY DISSOUTE",
            "PARTY DISBANDED");

        // ───────────── REPORTS ─────────────
        tBoth("report_usage",
            "&cUsage: /report <joueur> <raison>",
            "&cUsage: /report <player> <reason>");
        tBoth("report_cannot_self",
            "&cVous ne pouvez pas vous signaler.",
            "&cYou cannot report yourself.");
        tBoth("report_success",
            "&aReport &d#%id% &aenvoyé contre &f%player%&a!",
            "&aReport &d#%id% &asubmitted against &f%player%&a!");
        tBoth("report_usage_admin",
            "&dUsage: /da report [close|delete] <id>",
            "&dUsage: /da report [close|delete] <id>");
        tBoth("report_not_found",
            "&cReport introuvable.",
            "&cReport not found.");
        tBoth("report_closed",
            "&aReport &d#%id% &afermé.",
            "&aReport &d#%id% &aclosed.");
        tBoth("report_deleted",
            "&cReport &d#%id% &csupprimé.",
            "&cReport &d#%id% &deleted.");

        // ───────────── GUI COMMON ─────────────
        tBoth("gui_back",
            "&d← Retour",
            "&d← Back");
        tBoth("gui_back_lobby",
            "&7Retour au lobby",
            "&7Back to lobby");
        tBoth("gui_back_party",
            "&7Retour à la party",
            "&7Back to party");
        tBoth("gui_back_menu",
            "&7Retour au menu",
            "&7Back to menu");
        tBoth("gui_back_editor",
            "&7Retour à l'éditeur",
            "&7Back to editor");
        tBoth("gui_back_armor_select",
            "&7Retour à la sélection de pièce",
            "&7Back to piece selection");
        tBoth("gui_back_pattern_select",
            "&7Retour à la sélection de pattern",
            "&7Back to pattern selection");
        tBoth("gui_unavailable",
            "&cIndisponible",
            "&cUnavailable");
        tBoth("gui_party_ffa_unavailable",
            "&c&lIndisponible",
            "&c&lUnavailable");

        // ───────────── KIT EDITOR GUI (aliases) ─────────────
        tBoth("gui_kit_armor_select",
            "Choisir une pièce d'armure",
            "Select Armor Piece");
        tBoth("gui_slot_boots",
            "Bottes",
            "Boots");
        tBoth("gui_slot_leggings",
            "Jambières",
            "Leggings");
        tBoth("gui_slot_chestplate",
            "Plastron",
            "Chestplate");
        tBoth("gui_slot_helmet",
            "Casque",
            "Helmet");
        tBoth("gui_kit_armor_type",
            "&7Type:",
            "&7Type:");
        tBoth("gui_kit_armor_trim",
            "&7Trim:",
            "&7Trim:");
        tBoth("gui_kit_no_armor",
            "&7Aucune armure dans ce slot",
            "&7No armor in this slot");
        tBoth("gui_kit_click_add",
            "&d&lCliquez pour ajouter",
            "&d&lClick to add");
        tBoth("gui_kit_trims_lore1",
            "&7Personnalisez les trims",
            "&7Customize your armor");
        tBoth("gui_kit_pattern_select",
            "Choisir un pattern",
            "Select Pattern");
        tBoth("gui_kit_pattern_info",
            "&7Pattern de trim",
            "&7Trim pattern");
        tBoth("gui_kit_pattern_selected",
            "&aSélectionné actuellement",
            "&aCurrently selected");
        tBoth("gui_kit_click_select",
            "&d&lCliquez pour sélectionner",
            "&d&lClick to select");
        tBoth("gui_kit_material_select",
            "Choisir un matériau",
            "Select Material");
        tBoth("gui_kit_material_info",
            "&7Matériau de trim",
            "&7Trim material");
        tBoth("gui_kit_material_selected",
            "&aSélectionné actuellement",
            "&aCurrently selected");
        tBoth("bugreport_usage",
            "&cUtilisation: /bugreport <bug>",
            "&cUsage: /bugreport <bug>");
        tBoth("bugreport_sent",
            "&a&lMerci! &aVotre bug report a été envoyé sur Discord.",
            "&a&lThanks! &aYour bug report has been sent to Discord.");
        tBoth("bugreport_cooldown",
            "&cVous devez patienter %time% avant de pouvoir signaler un nouveau bug.",
            "&cYou must wait %time% before reporting another bug.");
    }
}
