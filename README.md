# 🌿 MTWWiet Plugin

Een uitgebreide Minecraft plugin voor een realistisch wiet kweeksysteem met planten, oogsten, verkoop en economie functionaliteiten. Volledig gemodulariseerd met robuuste anti-dupe bescherming en natuurlijke conversatie systemen.

## 📋 Inhoudsopgave

- [Features](#-features)
- [Demo Video](#-demo-video)
- [Installatie](#-installatie)
- [Configuratie](#-configuratie)
- [Commando's](#-commando's)
- [Permissies](#-permissies)
- [Items](#-items)
- [Anti-Dupe Systeem](#-anti-dupe-systeem)
- [Dealer Conversatie Systeem](#-dealer-conversatie-systeem)
- [Failsafe Verkoop Systeem](#-failsafe-verkoop-systeem)
- [API & Development](#-api--development)
- [Changelog](#-changelog)
- [Support](#-support)
- [License](#-license)

## ✨ Features

### 🌱 Plant Systeem
- **Realistische groei**: Planten groeien van sapling naar volgroeide double plant
- **Hologram countdown**: Live countdown timer die elke seconde update
- **3-seconden groei**: Configureerbare groei tijd (standaard 3 seconden voor testing)
- **Visuele feedback**: Hologrammen tonen groei status en oogst bereidheid
- **Particle effects**: Visuele effecten bij groei voltooiing
- **Persistence**: Planten blijven bestaan na server restart

### 🎮 Oogst Minigame
- **Interactieve oogst**: Klik op volgroeide planten om oogst minigame te starten
- **Inventory sluiten = mislukt**: Automatische failure bij inventory sluiten
- **Geen schade**: Spelers krijgen geen schade bij failure (0 damage)
- **Succesvolle oogst**: Beloningen met wiet items met NBT metadata
- **15 seconden timer**: Tijdslimiet voor oogst minigame

### 💰 Verkoop Systeem
- **Deur kloppen**: Verkoop wiet aan NPCs achter deuren via natuurlijke conversatie
- **NBT metadata verificatie**: Alleen echte wiet items met NBT tags werken
- **Politie meldingen**: Realistische kans op politie meldingen met coördinaten
- **Economie integratie**: Volledige Vault integratie voor geld transacties
- **Failsafe systeem**: Robuuste bescherming tegen scams en exploits

### 🛡️ Anti-Dupe Systeem
- **Real-time monitoring**: Detecteert verdachte activiteiten automatisch
- **Admin meldingen**: Instant notificaties naar admins bij verdachte activiteiten
- **Live inventory viewer**: `/invcheck <speler>` commando voor admins
- **Unieke item IDs**: Elk item heeft een unieke UUID voor duplicatie preventie
- **Clickable messages**: Admin meldingen bevatten clickable links

### 🏪 Shop Systeem
- **Chest interactie**: Klik op chests om shop te openen
- **Configureerbare prijzen**: Alle prijzen aanpasbaar in config
- **NBT metadata verificatie**: Alleen echte items kunnen gekocht worden
- **Balance checks**: Automatische controle van speler saldo

### 👥 NPC Systeem
- **Dealer NPCs**: Spawn en beheer dealer NPCs
- **Chat-based conversatie**: Natuurlijke conversatie systeem
- **Automatische despawning**: NPCs verdwijnen na bepaalde tijd
- **Citizens integratie**: Volledige Citizens API ondersteuning
- **Gangster stijl**: Realistische dealer conversaties

### 🔧 Modulaire Architectuur
- **PluginContext**: Centrale service registry
- **Module systeem**: Gescheiden functionaliteiten per module
- **Event-driven**: Moderne event-driven architectuur
- **Configurable**: Volledig configureerbare messages en instellingen

## 🎥 Demo Video

Bekijk de plugin in actie! Deze video toont alle features en functionaliteiten van de MTWWiet plugin:

[![MTWWiet Plugin Demo](https://img.youtube.com/vi/bvWoa7nXgJE/0.jpg)](https://www.youtube.com/watch?v=bvWoa7nXgJE)

**[Bekijk de volledige demo op YouTube](https://www.youtube.com/watch?v=bvWoa7nXgJE)**

De video toont:
- 🌱 Plant systeem met groei en hologrammen
- 🎮 Oogst minigame functionaliteit
- 💰 Verkoop systeem met dealer conversaties
- 🛡️ Anti-dupe systeem in actie
- 🏪 Shop systeem en NPC interacties

## 🚀 Installatie

### Vereisten
- **Minecraft Server**: 1.13+ (Spigot/Paper)
- **Java**: 8 of hoger
- **Vault**: Verplicht voor economie functionaliteit
- **Citizens**: Optioneel voor NPC functionaliteit
- **HolographicDisplays**: Optioneel voor hologrammen

### Stappen
1. **Download** de `MTWWiet-1.0.0.jar` uit de releases
2. **Plaats** de JAR in je `plugins` folder
3. **Start** je server
4. **Configureer** de plugin via `config.yml` en `messages.yml`
5. **Herstart** je server

## ⚙️ Configuratie

### Plant Groei Instellingen
```yaml
plant:
  grow-time-seconds: 3  # Groei tijd in seconden
  hologram-height: 2    # Hoogte van groei hologram
  harvest-hologram-height: 4  # Hoogte van oogst hologram
```

### Custom Items (NBT Metadata)
```yaml
custom-items:
  weed-seed:
    name: "§aWietzaadje"
    material: "SEEDS"
    nbt-tag: "weed_seed"
  weed:
    name: "§aWiet"
    material: "POTATO"
    nbt-tag: "weed"
```

### Verkoop Instellingen
```yaml
verkoop:
  deur:
    cooldown-time: 300000  # 5 minuten cooldown
    politie-kans: 0.3      # 30% kans op politie melding
    min-prijs-per-stuk: 10
    max-prijs-per-stuk: 20
    conversation-delay: 2000  # 2 seconden tussen berichten
```

### Dealer Conversatie
```yaml
dealer:
  conversation:
    min-amount: 1
    max-amount: 10
    price-per-seed: 50
    cancel-commands: ["nee", "stop", "cancel"]
```

## 📝 Commando's

### Speler Commando's
| Commando | Beschrijving | Permissie |
|----------|--------------|-----------|
| `/wiet` | Hoofdcommando met help | `weedplugin.use` |
| `/wiet balans` | Bekijk je saldo | `weedplugin.balans` |
| `/wiet winkel` | Open de winkel | `weedplugin.winkel` |

### Admin Commando's
| Commando | Beschrijving | Permissie |
|----------|--------------|-----------|
| `/wiet geefgeld <speler> <bedrag>` | Geef geld aan speler | `weedplugin.geefgeld` |
| `/wiet npc spawn` | Spawn dealer NPC | `weedplugin.npc` |
| `/wiet npc despawn` | Verwijder dealer NPC | `weedplugin.npc` |
| `/wiet npc list` | Toon alle NPCs | `weedplugin.npc` |
| `/wiet npc remove <id>` | Verwijder specifieke NPC | `weedplugin.npc` |
| `/invcheck <speler>` | Bekijk speler inventory | `weedplugin.admin` |
| `/wiet reload` | Herlaad configuratie | `weedplugin.reload` |
| `/wiet cleanuparmorstands` | Verwijder alle armor stands | `weedplugin.cleanuparmorstands` |
| `/wiet zaadje <speler> <aantal>` | Geef zaadjes aan speler | `weedplugin.zaadje` |

## 🔐 Permissies

### Basis Permissies
- `weedplugin.use` - Basis plugin gebruik
- `weedplugin.help` - Help informatie bekijken
- `weedplugin.plant` - Wiet planten
- `weedplugin.oogsten` - Wiet oogsten
- `weedplugin.balans` - Balans bekijken
- `weedplugin.winkel` - Winkel gebruiken
- `weedplugin.shop` - Shop gebruiken
- `weedplugin.verkopen` - Verkopen aan deuren

### Verkoop Permissies
- `weedplugin.verkoop.deur` - Verkopen aan deuren (specifiek)
- `weedplugin.politie` - Politie meldingen ontvangen

### Admin Permissies
- `weedplugin.admin` - Admin functies (inventory check, anti-dupe)
- `weedplugin.geefgeld` - Geld geven aan spelers
- `weedplugin.npc` - NPC beheer
- `weedplugin.reload` - Configuratie herladen
- `weedplugin.cleanuparmorstands` - Cleanup uitvoeren
- `weedplugin.zaadje` - Wietzaadjes geven
- `weedplugin.prijs` - Prijzen bekijken
- `weedplugin.plant.others` - Planten van anderen kapotslaan
- `weedplugin.anti-dupe.notify` - Anti-dupe meldingen ontvangen

### Wildcard Permissie
- `weedplugin.*` - Alle plugin permissies

## 🎯 Items

### Wietzaadje
- **Materiaal**: Seeds
- **NBT Metadata**: `weed_seed` boolean tag
- **Gebruik**: Plant op gras om wiet te laten groeien
- **Configuratie**: Volledig configureerbare naam

### Wiet
- **Materiaal**: Potato Item
- **NBT Metadata**: `weed` boolean tag
- **Gebruik**: Verkoop aan deuren voor geld
- **Configuratie**: Volledig configureerbare naam

## 🛡️ Anti-Dupe Systeem

### Detectie
- **Verdachte toename**: Detecteert plotselinge toename van wiet items (>5 extra)
- **Creative mode**: Detecteert wiet items in creative mode
- **Real-time monitoring**: Continu monitoring van alle spelers
- **Inventory snapshots**: Vergelijkt inventory states

### Admin Meldingen
```
§c§l[ANTI-DUPE] §fVerdachte activiteit gedetecteerd!
§7Speler: §ePlayerName
§7Reden: §eVerdachte toename van wiet items
§7Details: §eVan 2 naar 15 items
§7Verdachte activiteiten: §e3
§7Klik hier om inventory te bekijken: §a/invcheck PlayerName
```

### Inventory Check
- **Commando**: `/invcheck <speler>`
- **Permissie**: `weedplugin.admin`
- **Functionaliteit**: Live inventory bekijken en items beheren

## 💬 Dealer Conversatie Systeem

### Natuurlijke Conversatie
- **Chat-based**: Spelers typen bedragen in chat
- **Range validation**: Controleert min/max bedragen
- **Confirmation steps**: Bevestiging van aankoop
- **Cancellation**: Eenvoudig annuleren met commando's

### Conversatie Flow
1. **Speler klikt NPC** → Conversatie start
2. **NPC vraagt hoeveelheid** → "Hoeveel zaadjes wil je kopen? (1-10)"
3. **Speler typt bedrag** → Validatie van input
4. **NPC toont prijs** → "Oké, 5 zaadjes voor 250 euro. Bevestig je aankoop? (ja/nee)"
5. **Speler bevestigt** → Transactie voltooid

### Gangster Stijl Berichten
- **Realistische dealer taal**
- **Scam detection** bij verdachte activiteit
- **Configurable messages** in messages.yml

## 🛡️ Failsafe Verkoop Systeem

### Inventory Checks
- **Bij elke stap**: Controleert wiet hoeveelheid
- **Voor transactie**: Final check van inventory
- **Scam detection**: Detecteert te weinig wiet

### Scam Detection Berichten
```
"Wat doe je hier? Probeer je me te belazeren? Wegwezen!"
"Waar is je wiet gebleven? Probeer je me te scammen? Wegwezen!"
"Je hebt maar X wiet! Probeer je me te belazeren? Wegwezen!"
```

### Natuurlijke Conversatie Timing
- **2 seconden delays** tussen berichten
- **Speler responses** zichtbaar in chat
- **Realistische flow** zoals echte conversatie

## 🔧 API & Development

### PluginContext
```java
// Haal services op
PlantModule plantModule = PluginContext.getInstance(plugin)
    .getService(PlantModule.class).orElse(null);

CustomItems customItems = PluginContext.getInstance(plugin)
    .getService(CustomItems.class).orElse(null);

HarvestMinigameModule minigameModule = PluginContext.getInstance(plugin)
    .getService(HarvestMinigameModule.class).orElse(null);
```

### Custom Items API
```java
// Maak custom items
ItemStack weedSeed = customItems.createWeedSeed();
ItemStack weed = customItems.createWeed();

// Check item types
boolean isWeed = customItems.isWeed(item);
boolean isWeedSeed = customItems.isWeedSeed(item);

// Count items
int weedCount = customItems.countWeedItems(player);
```

### Anti-Dupe API
```java
// Check voor verdachte activiteiten
antiDupeManager.checkForSuspiciousActivity(player);

// Maak snapshot
antiDupeManager.takeSnapshot(player);

// Alert admins
antiDupeManager.alertAdmins(player, reason, details);
```

### Module System
```java
// Register modules
PluginContext.getInstance(plugin).registerService(PlantModule.class, new PlantModule(plugin));
PluginContext.getInstance(plugin).registerService(CustomItems.class, new CustomItems());

// Get services
Optional<PlantModule> plantModule = PluginContext.getInstance(plugin).getService(PlantModule.class);
```

## 📋 Changelog

### v1.0.0 (Latest)
- ✅ **Modulaire refactor** - Volledig herschreven architectuur
- ✅ **NBT metadata systeem** - Vervangen van lore-based naar NBT tags
- ✅ **Failsafe verkoop systeem** - Robuuste bescherming tegen scams
- ✅ **Natuurlijke conversaties** - Chat-based dealer conversaties
- ✅ **Inventory close detection** - Automatische minigame failure
- ✅ **0 damage bij failure** - Geen schade meer bij oogst failure
- ✅ **Anti-dupe verbeteringen** - Enhanced detection en admin alerts
- ✅ **Message systeem** - Volledig configureerbare messages
- ✅ **Permissions overzicht** - Uitgebreid permissions bestand
- ✅ **Gangster stijl** - Realistische dealer conversaties
- ✅ **Particle effects** - Visuele effecten bij groei
- ✅ **Persistence** - Planten blijven bestaan na restart

### Belangrijke Verbeteringen
- **Performance**: Modulaire architectuur verbetert performance
- **Security**: Failsafe systeem voorkomt exploits
- **User Experience**: Natuurlijke conversaties en feedback
- **Admin Tools**: Enhanced monitoring en control tools
- **Configurability**: Alles configureerbaar via config files

## 📞 Support

### Problemen Oplossen
1. **Check console logs** voor error berichten
2. **Verifieer dependencies** (Vault, Citizens, HolographicDisplays)
3. **Check permissies** van spelers
4. **Herlaad configuratie** met `/wiet reload`
5. **Check permissions bestand** voor correcte setup

### Veelvoorkomende Problemen
- **"Vault niet gevonden"**: Installeer Vault plugin
- **"Hologrammen werken niet"**: Installeer HolographicDisplays
- **"NPCs spawnen niet"**: Installeer Citizens plugin
- **"Message keys getoond"**: Check messages.yml voor ontbrekende keys
- **"Inventory close werkt niet"**: Check MinigameInventoryListener registratie

### Contact
- **Maker**: Djorr
- **Project**: MTWWiet Plugin
- **Versie**: 1.0.0
- **Discord**: https://discord.rubixdevelopment.nl

## 📄 License

```
MTWWiet Plugin - Custom License

Copyright (c) 2024 Djorr

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

1. **Fork Requirement**: You MUST create a fork of this project if you intend to use, modify, or distribute it.

2. **Attribution**: You MUST provide clear attribution to the original author (Djorr) and reference this as the official MTWWiet plugin.

3. **No Commercial Use**: This software may not be used for commercial purposes without explicit written permission from the original author.

4. **No Warranty**: The software is provided "as is", without warranty of any kind.

5. **Liability**: In no event shall the author be liable for any claims, damages or other liability.

By using this software, you agree to these terms and conditions.
```

---

**🌿 MTWWiet Plugin** - Een realistisch wiet kweeksysteem voor Minecraft servers

*Gemaakt met ❤️ door Djorr*

### 🎯 Key Features Summary
- ✅ **Modulaire Architectuur** - Schone, onderhoudbare code
- ✅ **NBT Metadata Systeem** - Veilige item verificatie
- ✅ **Failsafe Verkoop** - Robuuste scam bescherming
- ✅ **Natuurlijke Conversaties** - Realistische dealer interacties
- ✅ **Anti-Dupe Systeem** - Geavanceerde exploit preventie
- ✅ **Admin Tools** - Uitgebreide monitoring mogelijkheden
- ✅ **Configurable Messages** - Volledig aanpasbare teksten
- ✅ **Performance Optimized** - Efficiënte resource gebruik 