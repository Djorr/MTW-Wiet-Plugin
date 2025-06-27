# 🌿 WeedPlugin - Minecraft Wiet Plantage Plugin

Een uitgebreide Minecraft plugin voor het kweken, oogsten en verkopen van wiet in je server. Deze plugin voegt een complete wiet economie toe aan je Minecraft wereld met realistische groei, oogst minigames en verkoop systemen.

## 📋 Inhoudsopgave

- [Features](#-features)
- [Installatie](#-installatie)
- [Dependencies](#-dependencies)
- [Configuratie](#-configuratie)
- [Gebruik](#-gebruik)
- [Commands](#-commands)
- [Permissions](#-permissions)
- [API Versie](#-api-versie)
- [Support](#-support)
- [Data persistentie](#-data-persistentie)

## ✨ Features

### 🌱 Wiet Kweken Systeem
- **Planten van wiet**: Gebruik `/wiet plant` om wiet te planten op geschikte locaties
- **Realistische groei**: Wiet planten groeien over tijd (standaard 5 minuten)
- **Visuele feedback**: Hologrammen tonen groei progressie
- **Fysica bescherming**: Planten kunnen niet kapot door andere spelers of physics

### 🎮 Oogst Minigame
- **Interactieve oogst**: Klik op groene blokken binnen de tijdlimiet
- **Tijdsdruk**: 15 seconden om alle wiet blokken te vinden
- **Beloningen**: Succesvolle oogst geeft wiet items
- **Punishment**: Mislukking geeft schade en verliest de plant

### 💰 Verkoop Systeem
- **Deur-aan-deur verkoop**: Klop op deuren met wiet in je hand
- **NPC verkoop**: Verkoop aan speciale NPCs in de wereld
- **Realistische dialogen**: Dynamische gesprekken met bewoners
- **Risico systeem**: Kans dat bewoners de politie bellen
- **Cooldown systeem**: Voorkomt spam van deuren

### 🛒 Shop Systeem
- **Growshop**: Koop zaden, zakjes en upgrades
- **Economie integratie**: Volledig geïntegreerd met Vault
- **Configuratie prijzen**: Alle prijzen aanpasbaar in config

### 🎯 NPC Systeem
- **Automatische spawn**: NPCs spawnen op geconfigureerde locaties
- **Interactieve verkoop**: Klik op NPCs om wiet te verkopen
- **Citizens integratie**: Gebruikt Citizens voor NPC management

### 📦 Custom Items
- **Wietzaadje**: Voor het planten van nieuwe wiet
- **Wiet**: Geoogste wiet voor verkoop
- **Zakjes**: Voor het verpakken van wiet
- **Gevulde wietzakken**: Verpakte wiet voor verkoop

## 🚀 Installatie

1. **Download de plugin**: Plaats de `WeedPlugin.jar` in je `plugins` folder
2. **Installeer dependencies**: Zorg dat alle required plugins geïnstalleerd zijn
3. **Start de server**: De plugin laadt automatisch
4. **Configureer**: Pas de `config.yml` aan naar wens
5. **Herstart**: Herstart de server voor volledige activatie

## 📦 Dependencies

Deze plugin heeft de volgende dependencies nodig:

- **Vault** - Voor economie integratie
- **Citizens** - Voor NPC functionaliteit  
- **HolographicDisplays** - Voor hologrammen

### Installatie van Dependencies

```bash
# Download en plaats deze plugins in je plugins folder:
# - Vault.jar
# - Citizens.jar  
# - HolographicDisplays.jar
```

## ⚙️ Configuratie

### Plant Groei Instellingen
```yaml
plant:
  grow-time-seconds: 300  # 5 minuten groei tijd
```

### Oogst Minigame Instellingen
```yaml
oogst-minigame:
  time-limit-seconds: 15  # Tijdslimiet voor oogst
  fail-damage: 2          # Schade bij mislukking
```

### Verkoop Systeem Instellingen
```yaml
verkoop:
  npc:
    prijs-per-zak: 50     # Prijs per zak bij NPC verkoop
  
  deur:
    min-prijs: 40         # Minimale prijs per stuk
    max-prijs: 60         # Maximale prijs per stuk
    fail-chance: 0.3      # 30% kans op mislukking
    politie-kans: 0.3     # 30% kans dat politie wordt gebeld
    max-afstand: 6        # Maximale afstand tijdens gesprek
```

### Shop Prijzen
```yaml
shop:
  zaad: 10               # Prijs voor wietzaadje
  zakje: 5               # Prijs voor leeg zakje
  upgrade: 100           # Prijs voor upgrade
```

## 🎮 Gebruik

### Wiet Planten
1. Verkrijg een wietzaadje via `/wiet zaadje` of de shop
2. Ga naar een geschikte locatie (gras blok)
3. Gebruik `/wiet plant` om de wiet te planten
4. Wacht tot de plant volledig gegroeid is (5 minuten)

### Wiet Oogsten
1. Ga naar een volgroeide wiet plant
2. Klik op de plant om het oogst minigame te starten
3. Klik op alle groene blokken binnen 15 seconden
4. Bij succes ontvang je wiet items

### Wiet Verkopen

#### Deur-aan-deur Verkoop
1. Houd wiet in je hand
2. Klik 3 keer op een deur (linksklik)
3. Luister naar het gesprek
4. Verkoop je wiet voor een goede prijs

#### NPC Verkoop
1. Zoek een wiet verkoper NPC
2. Klik op de NPC
3. Verkoop je wiet direct

### Shop Gebruik
1. Gebruik `/wiet winkel` om de shop te openen
2. Koop zaden, zakjes of upgrades
3. Gebruik je items om meer wiet te kweken

## 📝 Commands

| Command | Beschrijving | Gebruik |
|---------|-------------|---------|
| `/wiet plant` | Plant een wiet plant | `/wiet plant` |
| `/wiet balans` | Toon je saldo | `/wiet balans` |
| `/wiet geefgeld` | Geef geld aan speler | `/wiet geefgeld <speler> <bedrag>` |
| `/wiet npc spawn` | Spawn een NPC | `/wiet npc spawn` |
| `/wiet winkel` | Open de shop | `/wiet winkel` |
| `/wiet reload` | Herlaad configuratie | `/wiet reload` |
| `/wiet zaadje` | Krijg een wietzaadje | `/wiet zaadje` |

### Aliases
- `/weed` - Alias voor `/wiet`

## 🔐 Permissions

De WeedPlugin gebruikt een uitgebreid permissie systeem. Alle commando's en functies vereisen specifieke permissies, tenzij de speler OP is.

### Commando Permissies

| Permission | Beschrijving | Commando |
|------------|-------------|----------|
| `weedplugin.plant` | Wiet planten | `/wiet plant` |
| `weedplugin.balans` | Saldo bekijken | `/wiet balans` |
| `weedplugin.geefgeld` | Geld geven aan spelers | `/wiet geefgeld` |
| `weedplugin.npc` | NPCs spawnen | `/wiet npc spawn` |
| `weedplugin.winkel` | Shop openen | `/wiet winkel` |
| `weedplugin.reload` | Config herladen | `/wiet reload` |
| `weedplugin.cleanup` | Cleanup commando's | `/wiet cleanuparmorstands` |
| `weedplugin.zaadje` | Wietzaadje krijgen | `/wiet zaadje` |

### Functie Permissies

| Permission | Beschrijving | Functie |
|------------|-------------|---------|
| `weedplugin.oogsten` | Wiet oogsten | Oogst minigame starten |
| `weedplugin.verkoop.deur` | Deur-aan-deur verkoop | Verkopen aan deuren |
| `weedplugin.verkoop.npc` | NPC verkoop | Verkopen aan NPCs |
| `weedplugin.shop` | Shop gebruiken | Items kopen in shop |

### Wildcard Permissies

| Permission | Beschrijving |
|------------|-------------|
| `weedplugin.*` | Alle WeedPlugin permissies |
| `weedplugin.verkoop.*` | Alle verkoop permissies |

### Permission Plugin Voorbeelden

#### LuckPerms
```yaml
# Basis gebruiker
/lp group default permission set weedplugin.plant true
/lp group default permission set weedplugin.balans true
/lp group default permission set weedplugin.oogsten true
/lp group default permission set weedplugin.verkoop.deur true
/lp group default permission set weedplugin.verkoop.npc true
/lp group default permission set weedplugin.shop true
/lp group default permission set weedplugin.zaadje true

# Verkoper (kan verkopen)
/lp group verkoper permission set weedplugin.verkoop.* true

# Admin (kan alles)
/lp group admin permission set weedplugin.* true
```

#### PermissionsEx
```yaml
# Basis gebruiker
/pex group default add weedplugin.plant
/pex group default add weedplugin.balans
/pex group default add weedplugin.oogsten
/pex group default add weedplugin.verkoop.deur
/pex group default add weedplugin.verkoop.npc
/pex group default add weedplugin.shop
/pex group default add weedplugin.zaadje

# Verkoper
/pex group verkoper add weedplugin.verkoop.*

# Admin
/pex group admin add weedplugin.*
```

### OP Spelers
Spelers met OP status kunnen alle commando's en functies gebruiken zonder specifieke permissies.

### Standaard Instellingen
- **Nieuwe spelers**: Geen permissies (moeten toegewezen worden)
- **OP spelers**: Alle permissies automatisch
- **Server eigenaren**: Alle permissies automatisch

## 🎯 API Versie

Deze plugin is ontwikkeld voor **Minecraft 1.12** en hoger.

## 🛠️ Technische Details

### Bestandsstructuur
```
WeedPlugin/
├── src/main/java/nl/yourname/weedplugin/
│   ├── command/          # Commando handlers
│   ├── item/            # Custom items
│   ├── listener/        # Event listeners
│   ├── manager/         # Plant management
│   ├── minigame/        # Oogst minigame
│   ├── model/           # Data models
│   ├── util/            # Utility classes
│   └── WeedPlugin.java  # Hoofdklasse
└── src/main/resources/
    ├── config.yml       # Plugin configuratie
    ├── messages.yml     # Berichten
    └── plugin.yml       # Plugin metadata
```

### Belangrijke Classes
- **WeedPlugin**: Hoofdklasse die alles initialiseert
- **PlantManager**: Beheert alle wiet planten
- **HarvestMinigame**: Oogst minigame logica
- **SellListener**: Verkoop systeem
- **CustomItems**: Custom item generatie

## 🐛 Bekende Issues

- Geen bekende issues op dit moment

## 🔄 Updates

### Versie 1.0
- Initiële release
- Basis wiet kweken systeem
- Oogst minigame
- Verkoop systeem
- Shop integratie
- NPC support

## 📞 Support

Voor vragen, bugs of feature requests:

1. **GitHub Issues**: Maak een issue aan in de repository
2. **Documentatie**: Lees deze README volledig
3. **Configuratie**: Check de `config.yml` voor alle opties

## 📄 Licentie

Dit project is gelicenseerd onder de MIT License - zie het [LICENSE](LICENSE) bestand voor details.

## Data persistentie

Deze plugin slaat belangrijke speldata automatisch op in YAML-bestanden in de pluginmap (`plugins/WeedPlugin/`). Hierdoor blijven planten, NPC's en deur-cooldowns behouden bij een server-restart of crash. Timers worden gepauzeerd tijdens downtime en hervat bij het opstarten.

### plants.yml
- **Locatie**: `plugins/WeedPlugin/plants.yml`
- **Inhoud**: Alle geplante wietplanten, inclusief locatie, eigenaar, planttijd, groeitijd, en status (klaar of niet).
- **Gedrag**: Als de server uitgaat, wordt de resterende groeitijd opgeslagen. Bij het opstarten worden planten en hun timers correct hervat.

### doorcooldowns.yml
- **Locatie**: `plugins/WeedPlugin/doorcooldowns.yml`
- **Inhoud**: Cooldowns per deur (locatie) per speler, met timestamp van de laatste verkoop.
- **Gedrag**: Cooldowns worden opgeslagen en bij het opstarten weer geladen, zodat spelers niet kunnen exploiten door te reloggen.

### npcs.yml
- **Locatie**: `plugins/WeedPlugin/npcs.yml`
- **Inhoud**: Alle shop-NPC's, inclusief hun locatie, type en naam.
- **Gedrag**: NPC's worden automatisch opnieuw gespawned bij het opstarten van de server, zodat de shop altijd beschikbaar blijft.

---

**Let op**: Deze plugin is puur voor entertainment doeleinden en simuleert een fictieve wiet economie in Minecraft. Het is niet bedoeld om echte illegale activiteiten te promoten. 