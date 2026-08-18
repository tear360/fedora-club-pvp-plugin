# Fedora Club - Duel Plugin

Plugin de duel PvP pour **Paper Minecraft 1.21.4**, créé pour le serveur **Fedora Club**.

**IP :** `fedora.free-node.ovh`

---

## Installation

1. Télécharge le fichier `DuelPlugin-1.0.0.jar` depuis les [Releases](https://github.com/tear360/fedora-club-pvp-plugin/releases) ou compile-le toi-même (voir ci-dessous).
2. Place le `.jar` dans le dossier `plugins/` de ton serveur Paper 1.21.4.
3. Redémarre le serveur.
4. Le fichier `config.yml` sera généré automatiquement dans `plugins/DuelPlugin/`.

### Compiler le plugin

```bash
# Besoin de Maven installé
git clone https://github.com/tear360/fedora-club-pvp-plugin.git
cd fedora-club-pvp-plugin
mvn clean package
```

Le JAR sera dans `target/DuelPlugin-1.0.0.jar`.

---

## Commandes

### Joueurs

| Commande | Description |
|----------|-------------|
| `/duel <joueur>` | Ouvre le menu pour choisir un mode de jeu, puis envoie la demande |
| `/acceptduel [joueur]` | Accepte un duel en attente |
| `/denyduel [joueur]` | Refuse un duel en attente |

**Comment ça marche :**
1. Tape `/duel NomDuJoueur`
2. Un menu GUI s'ouvre avec tous les modes de jeu
3. Clique sur le mode que tu veux
4. Le joueur cible reçoit un message dans le chat avec des boutons cliquables **[ACCEPTER]** et **[REFUSER]**
5. Il peut cliquer sur le bouton ou taper `/acceptduel`

### Admin (permission `duelplugin.admin`)

| Commande | Description |
|----------|-------------|
| `/da setlobby` | Définit le point d'apparition du lobby |
| `/da create <nom> <mode>` | Crée une arène |
| `/da delete <nom>` | Supprime une arène |
| `/da setspawn <nom> <1\|2>` | Définit le spawn du joueur 1 ou 2 |
| `/da setmin <nom>` | Définit le coin minimum de la zone |
| `/da setmax <nom>` | Définit le coin maximum de la zone |
| `/da info <nom>` | Affiche les infos d'une arène |
| `/da list` | Liste toutes les arènes |
| `/da reload` | Recharge la configuration |

---

## Modes de jeu

| Mode | Armure | Arme principale | blocs cassables |
|------|--------|-----------------|-----------------|
| **Sword** | Diamond Prot 3 | Diamond Sword Sharp 5 + Sweeping 3 | Non |
| **Axe** | Diamond (base) | Diamond Axe + Sword Sharp 5 | Non |
| **UHC** | Diamond Prot 2-3 | Diamond Sword Sharp 3 + Axe Eff 3 | **Oui** |
| **Pot** | Diamond Prot 4 + Unb 3 | Diamond Sword Sharp 5 + Unb 3 | Non |
| **NethPot** | Netherite Prot 4 + Unb 3 + Mending | Netherite Sword Sharp 5 + Unb 3 | Non |
| **Mace** | Netherite Prot 4 | Mace Breach 4 + Density 5/Wind Burst 1 | Non |
| **Vanilla** | Diamond Prot 3-4 + Unb 3 | Diamond Sword Sharp 5 + Unb 3 | **Oui** |
| **SMP** | Netherite Prot 4 + Mending | 2x Netherite Swords (KB I + normal) | Non |
| **DiaSMP** | Diamond Prot 4 + Mending | Diamond Sword + Axe Sharp 5 | **Oui** |
| **Spear-Mace** | Netherite Prot 4 | Mace Density 5 + Trident Impaling 5 | Non |

### Détails des kits

<details>
<summary><b>Sword</b></summary>

- Diamond Sword : Sharpness 5, Sweeping Edge 3, Unbreaking 3
- Diamond Armor : Protection 3, Unbreaking 3 (toutes pièces)
- Bow : Power 2, Unbreaking 3
- 32 Flèches, 8 Ender Pearls, 8 Golden Apples
- Shield : Unbreaking 3
</details>

<details>
<summary><b>Axe</b></summary>

- Diamond Sword : Sharpness 5, Unbreaking 3
- Diamond Axe : Sharpness 5, Unbreaking 3
- Diamond Armor (base, sans enchants)
- Shield : Unbreaking 3
- Bow, Crossbow, 6 Flèches
- 8 Golden Apples
</details>

<details>
<summary><b>UHC</b></summary>

- Diamond Helmet Prot 3 / Chestplate Prot 2 / Leggings Prot 2 / Boots Prot 3
- Diamond Sword : Sharpness 3
- Diamond Axe : Efficiency 3
- Bow : Power 1 / Crossbow : Piercing 1
- Diamond Pickaxe : Efficiency 3
- Shield
- 8 Golden Apples, 2 Golden Heads
- 4 Water Buckets, 2 Lava Buckets
- 2x64 Oak Planks
- 10 Flèches (+1 chargée dans l'arbalète)
- **Pas de régénération naturelle**
</details>

<details>
<summary><b>Pot</b></summary>

- Diamond Sword : Sharpness 5, Unbreaking 3
- Diamond Armor : Protection 4, Unbreaking 3
- 26 Splash Potions de Healing II
- 3 Strength II, 3 Speed II, 3 Regeneration II (Splash)
- 5 Steaks
</details>

<details>
<summary><b>NethPot</b></summary>

- Netherite Sword : Sharpness 5, Unbreaking 3
- Netherite Armor : Protection 4, Unbreaking 3, Mending
- 5 Sets Strength II + Speed II (Splash)
- 64 Golden Apples, 64 XP Bottles
- 2 Totems of Undying
</details>

<details>
<summary><b>Mace</b></summary>

- Netherite Sword : Sharpness 5, Unbreaking 3
- Netherite Axe : Sharpness 5, Unbreaking 3
- Mace 1 : Breach 4, Unbreaking 3
- Mace 2 : Density 5, Wind Burst 1, Unbreaking 3
- Netherite Armor : Protection 4, Boots Feather Falling 4
- Shield : Unbreaking 3, Mending
- Elytra (150 durabilité)
- 2x64 Golden Apples, 4x64 Ender Pearls, 2x64 Wind Charges
- 2 Totems of Undying
- 11 Strength II + 10 Speed II (Splash)
- Shulker Box : 14 Speed II + 13 Strength II
</details>

<details>
<summary><b>Vanilla</b></summary>

- Diamond Sword : Sharpness 5, Unbreaking 3
- Diamond Armor : Protection 3-4, Unbreaking 3
- Bow : Power 2, Unbreaking 3
- 32 Flèches, 8 Ender Pearls, 8 Golden Apples
- Shield : Unbreaking 3
- Oak Planks, Cobblestone, Water Bucket
- **Les joueurs peuvent casser/poser leurs propres blocs**
</details>

<details>
<summary><b>SMP</b></summary>

- Netherite Sword 1 : Sharpness 5, Fire Aspect 2, Knockback 1, Unbreaking 3
- Netherite Sword 2 : Sharpness 5, Fire Aspect 2, Unbreaking 3
- Netherite Axe : Sharpness 5, Unbreaking 3
- Netherite Armor : Protection 4, Unbreaking 3, Mending, Boots Feather Falling 4, Leggings Swift Sneak 3
- Shield : Unbreaking 3, Mending
- 2x64 Golden Apples, 2x64 Ender Pearls, 64 XP Bottles
- 1 Totem of Undying
- 12 Strength II + Speed II (Splash), 3 Fire Resistance 8min
</details>

<details>
<summary><b>DiaSMP</b></summary>

- Diamond Sword : Sharpness 5, Fire Aspect 2, Unbreaking 3
- Diamond Axe : Sharpness 5, Unbreaking 3
- Netherite Pickaxe : Efficiency 5, Silk Touch, Mending, Unbreaking 3
- Diamond Armor : Protection 4, Unbreaking 3, Mending, Boots Feather Falling 4, Leggings Swift Sneak 3
- Shield : Unbreaking 3, Mending
- 2x64 Golden Apples, 2x64 Ender Pearls, 64 XP Bottles
- 1 Totem of Undying
- 64 Oak Logs, 64 Cobwebs, 64 Chorus Fruit
- 16 Strength II, 3 Speed II, 3 Fire Resistance 8min (Splash)
- **Les joueurs peuvent casser/poser leurs propres blocs**
</details>

<details>
<summary><b>Spear-Mace</b></summary>

- Netherite Sword : Sharpness 5, Sweeping Edge 3, Knockback 1, Unbreaking 3
- Netherite Axe : Sharpness 5, Unbreaking 3
- Trident : Impaling 5, Unbreaking 3
- Mace : Density 5, Wind Burst 1, Unbreaking 3
- Netherite Armor : Protection 4, Boots Feather Falling 4
- Shield : Unbreaking 3, Mending
- Elytra, 16 Ender Pearls, 64 Golden Apples, 64 Wind Charges
- 2 Totems of Undying
</details>

---

## Créer une arène

1. **Créer l'arène :**
   ```
   /da create nomArena Sword
   ```

2. **Se positionner au spawn du joueur 1 et taper :**
   ```
   /da setspawn nomArena 1
   ```

3. **Se positionner au spawn du joueur 2 et taper :**
   ```
   /da setspawn nomArena 2
   ```

4. **Définir la zone (optionnel, requis pour les modes avec blocs) :**
   ```
   /da setmin nomArena    # Se placer au coin minimum
   /da setmax nomArena    # Se placer au coin maximum
   ```

5. **Vérifier la config :**
   ```
   /da info nomArena
   ```

### Modes avec interaction de blocs

Pour les modes **Vanilla**, **UHC** et **DiaSMP**, les joueurs peuvent casser et poser leurs propres blocs pendant le duel. Les blocs de l'arène sont protégés : un snapshot est pris au début du duel et tout est restauré à la fin.

Les autres modes (Sword, Axe, Pot, NethPot, Mace, SMP, Spear-Mace) n'ont aucune interaction avec les blocs de l'arène.

---

## Configuration

Le fichier `config.yml` se trouve dans `plugins/DuelPlugin/config.yml` :

```yaml
messages:
  prefix: "&8[&6Fedora &eClub&8] &r"
  # Tous les messages du plugin sont personnalisables ici

scoreboard:
  title: "&6&lFEDORA &e&lCLUB"
  # Le scoreboard affiche le nom du serveur en sidebar

lobby:
  world: world
  spawn-x: 0
  spawn-y: 64
  spawn-z: 0
```

Pour changer le lobby, tape `/da setlobby` depuis l'endroit souhaité.

---

## Permissions

| Permission | Description | Défaut |
|------------|-------------|--------|
| `duelplugin.admin` | Commandes admin (/da) | OP |
| `duelplugin.play` | Peut jouer aux duels | true (tous) |

---

## Tech

- **Version Minecraft :** Paper 1.21.4
- **API :** Paper API
- **Java :** 21+
- **Build :** Maven

## License

MIT
