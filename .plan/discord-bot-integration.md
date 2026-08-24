# Plan : Bot Discord JDA intégré au plugin

## Résumé
Remplacer les webhooks Discord par un vrai bot JDA tournant dans le plugin. Le bot gère 3 fonctionnalités :
1. `/bugreport` → crée un **thread** dans un salon dédié
2. Résultats de duel → envoie un **embed** dans un salon par ID
3. `/report` → crée un **salon temporaire** dans une catégorie dédiée
4. Un **EventListener** JDA écoute les réactions (✅) pour fermer/supprimer les threads et salons

---

## Fichiers à créer

### `DiscordBotManager.java` (nouveau)
**Package :** `fr.duelplugin.managers`

Responsabilités :
- Initialiser JDA avec le bot token (asynchrone via `JDABuilder`)
- Stocker les IDs de salon/catégorie depuis config.yml
- `sendDuelResult(winner, loser, mode, duration)` → envoie un embed dans `duel-result-channel-id`
- `createBugReportThread(playerName, uuid, bug)` → crée un thread nommé `bug-{playerName}-{timestamp}` dans `bug-report-channel-id`, y envoie un embed, retourne le `Message` pour pouvoir stocker l'ID
- `createReportChannel(reporter, reported, reason, reportId)` → crée un salon temporaire nommé `report-{reported}-{id}` dans `report-category-id`, y envoie un embed avec bouton de fermeture
- `closeThread(threadId)` / `deleteChannel(channelId)` → fermeture/suppression
- `shutdown()` → appelle `jda.shutdownNow()` dans `onDisable()`

EventListener JDA interne :
- `onMessageReactionAdd` → si l'emoji est ✅ et l'utilisateur a la permission Discord `ADMINISTRATOR` :
  - Si c'est un thread dans le salon bug-reports → ferme + supprime le thread
  - Si c'est un salon dans la catégorie reports → supprime le salon

---

## Fichiers à modifier

### `pom.xml`
- Bump version `1.12.27` → `1.12.28`
- Ajouter JDA dependency :
```xml
<dependency>
    <groupId>net.dv8tion</groupId>
    <artifactId>JDA</artifactId>
    <version>5.2.2</version>
</dependency>
```
- Ajouter maven-shade-plugin pour bundler JDA dans le jar :
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.6.0</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals><goal>shade</goal></goals>
        </execution>
    </executions>
</plugin>
```

### `config.yml`
- Supprimer `discord-webhook-url` et `discord-bugreport-webhook-url`
- Ajouter :
```yaml
discord:
  bot-token: ""
  bug-report-channel-id: ""
  duel-result-channel-id: ""
  report-category-id: ""
```

### `DuelPlugin.java`
- Ajouter champ `DiscordBotManager discordBotManager`
- Le construire dans `onEnable()` (après `discordWebhookManager`, avant les commands)
- Ajouter getter `getDiscordBotManager()`
- Dans `onDisable()`, appeler `discordBotManager.shutdown()`
- Supprimer l'ancien `discordWebhookManager` (ou le garder comme fallback, à décider)

### `DuelManager.java`
- Ligne ~370 : remplacer `plugin.getDiscordWebhookManager().sendDuelResult(...)` par `plugin.getDiscordBotManager().sendDuelResult(...)`

### `BugReportCommand.java`
- Supprimer tout le code HttpClient/JSON (doublon)
- Remplacer par un appel à `plugin.getDiscordBotManager().createBugReportThread(playerName, uuid, bug)`
- Garder le cooldown 1h

### `ReportCommand.java`
- Après `plugin.getReportManager().createReport(...)`, ajouter un appel à `plugin.getDiscordBotManager().createReportChannel(reporter, reported, reason, reportId)`

### `LanguageManager.java`
- Ajouter des clés de traduction pour les messages Discord (optionnel, les embeds sont en dur)

---

## Ordre d'implémentation

1. **pom.xml** — ajouter JDA + shade plugin, bump version
2. **config.yml** — nouvelle config Discord bot
3. **DiscordBotManager.java** — créer la classe complète avec :
   - Initialisation JDA
   - `sendDuelResult()`
   - `createBugReportThread()`
   - `createReportChannel()`
   - EventListener pour les réactions ✅
4. **DuelPlugin.java** — enregistrer le nouveau manager
5. **DuelManager.java** — utiliser DiscordBotManager au lieu du webhook
6. **BugReportCommand.java** — refactoriser pour utiliser DiscordBotManager
7. **ReportCommand.java** — ajouter l'appel Discord
8. **Build + test compilation**

---

## Points d'attention

- **Shading JDA** : JDA a beaucoup de dépendances (annotations, OkHttp, etc.). Le shade plugin doit tout inclure. Pas de relocation nécessaire tant qu'il n'y a pas de conflit avec d'autres plugins.
- **Thread safety** : JDA est asynchrone. Les appels depuis le thread Bukkit doivent être safe (JDA gère ça via sa propre queue).
- **Rate limits** : JDA gère automatiquement les rate limits Discord.
- **Bot permissions** : Le bot Discord a besoin des permissions : `Send Messages`, `Create Public Threads`, `Create Private Threads`, `Manage Threads`, `Read Message History`, `Add Reactions`.
- **Shutdown** : `onDisable()` doit appeler `jda.shutdownNow()` pour fermer proprement la connexion WebSocket.
