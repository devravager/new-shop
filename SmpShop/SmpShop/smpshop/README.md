# SMPShop

A GUI shop plugin for Paper/Spigot servers, built around the same general
layout used by popular SMP economy servers: a main menu with categories for
**Overworld**, **Nether**, **End**, **Gear**, and **Food**, each opening into
a full-page item shop with click-to-buy and a quantity selector.

This is an original implementation inspired by that genre of plugin — it
does not use any other project's name, branding, or copyrighted text. All
GUI titles, messages, and item groupings are fully yours to edit in
`config.yml`.

## Requirements
- Java 17+
- Paper or Spigot 1.20+
- [Vault](https://www.spigotmc.org/resources/vault.34315/) plus any
  Vault-compatible economy plugin (EssentialsX, CMI, etc.) for buying/selling

## Building
```bash
mvn clean package
```
The compiled jar will be at `target/SMPShop.jar`. Drop it into your
server's `plugins/` folder and restart.

## Commands
| Command      | Description                              | Permission       |
|--------------|-------------------------------------------|------------------|
| `/shop`      | Opens the main category menu              | `smpshop.use`    |
| `/sell`      | Sells the item in your hand               | `smpshop.use`    |
| `/sell <n>`  | Sells `n` of the item in your hand        | `smpshop.use`    |
| `/sellall`   | Sells every sellable item in your inventory | `smpshop.use`  |
| `/smpshop reload` | Reloads `config.yml` without a restart | `smpshop.admin`  |

## Customizing
Everything lives in `config.yml`:
- `categories` — icon, display name, lore, and menu title per category
- `shops.<category>.items` — material, GUI slot, buy price, and sell price
  for every item
- `messages` / `sounds` — all player-facing text and sound effects

Add a brand-new category by adding an entry under both `categories` and
`shops`, plus a slot for it under `gui.category-slots`.

## GUI flow
1. `/shop` → main menu with one icon per category
2. Click a category → 54-slot menu listing that category's items
3. Left-click an item → buy 1 instantly
4. Right-click an item → opens a quantity selector (±1 / ±10, confirm, cancel)
