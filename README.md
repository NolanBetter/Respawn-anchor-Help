# Respawn Anchor Help

Client-side Fabric mod for Minecraft 26.2. Three small combat quality-of-life changes,
each independently toggleable.

## Features

### Anchor / glowstone swap

Runs your hotbar around the anchor cycle so you never reach for a number key mid-fight:

- **Place a respawn anchor** → the hotbar moves to your glowstone.
- **Charge that anchor with the glowstone** → the hotbar moves back to your anchors.

Both items have to already be on your hotbar. If either is missing, nothing happens.

The mod remembers which slots you were last holding anchors and glowstone in, so with
several stacks of either on the bar it returns to the ones you were actually using rather
than always snapping to the leftmost.

**Placing is not the same as clicking.** Right-clicking an anchor while holding an anchor
detonates it — it does not place anything — and that must not move your hotbar to
glowstone. So the mod does not assume a click was a placement: it records the two blocks a
placement could land in, and only swaps if a respawn anchor actually *appeared* in the
world that was not there before.

**One click, one swap.** Vanilla repeats block placement while right-click is held, so
without a guard a single held button would walk the cycle place → charge → place by
itself. The mod requires the use key to come back up between swaps, which keeps the
property that every interaction is a separate click of yours.

**It is a hotbar selection change and nothing else.** The mod never clicks, places, uses
or moves an item for you, and adds no packets beyond the ordinary "held slot changed" one
that pressing a number key sends.

### Right-click a totem to equip it

Right-click a totem and it goes to your offhand:

- **in any inventory screen** — right-click the slot it is sitting in,
- **in the world** — right-click while holding one.

Both are one action per click. The inventory case runs vanilla's own slot click with the
offhand button, exactly what the offhand-swap key does, so it behaves identically. The
world case sends the same single `SWAP_ITEM_WITH_OFFHAND` action that key sends.

The world case only fires when the click had nothing else to do — right-clicking a chest
while holding a totem still opens the chest — and a held right-click will not repeat it.

Once the totem is equipped you go back to whatever you were holding before you reached for
it, so you land back on your crystals rather than on an empty slot.

### Going back to your own item

All of the assists *borrow* the hotbar and give it back. The slot they give back is the one
**you** last chose by hand — not whatever happened to be selected when the assist fired.

That distinction is what makes the awkward cases work. If a totem goes while the food
assist has you on food, the totem takes the selection over, and handing it back afterwards
returns you to your own item rather than dropping you back on the food. Equally, if the
totem assist moves you to a totem slot but you equip one from your inventory instead, the
loan ends the moment a totem is back in your offhand, wherever it came from, and you get
your slot back.

Touching the hotbar yourself cancels any loan outright — at that point you have said where
you want to be, so the mod stops moving you.

One thing worth knowing: if you switch to food *manually* rather than letting the assist do
it, that counts as your own choice, so the mod returns you there. Leave the food assist on
if you want to end up back on your combat item instead.

### Sword swap on hit

Hit a player and the hotbar moves to your sword. Hit again and it moves back to what you
were holding.

**Every hit lands with the item that was already in your hand when you clicked.** The first
hit is a crystals-in-hand hit and leaves you on the sword; the second is the sword hit and
puts you back. The mod never changes what you are holding partway through an attack, so one
click is still one attack — the same shape as the anchor cycle.

That also means it costs you a weak first hit rather than handing you sword damage for
free. It is a trade, not an upgrade.

Only attacks on players count, so detonating a crystal leaves the hotbar alone, and a held
attack key will not flip you back and forth — each swap needs its own click.

### Totem swap assist

When your offhand totem goes, the mod selects a replacement totem from your hotbar. If
you have none left it selects a shield instead (optional).

The delay is configurable — 0 selects it the same tick the totem goes, the default 20
ticks is one second.

**It selects the slot; it does not equip anything by itself.** Press your offhand-swap key
(F), or right-click the totem — see above.

### Food swap assist

Drop below the health threshold and the mod selects the best food on your hotbar, then
hands your previous slot back once you are healed. You still hold right-click to eat.

Food has to be on the hotbar. Rotten flesh, spider eyes, pufferfish, chorus fruit,
suspicious stew, poisonous potatoes and raw chicken are skipped; anything else is ranked
by nutrition plus saturation. If you switch slots yourself while hurt, the mod leaves you
alone rather than fighting you for the selection.

### Hide explosion particles

Suppresses `explosion` and `explosion_emitter` particles, so crystals, anchors and TNT
stop blanking out your screen. Sound, damage and knockback are untouched.

### Hide totem particles

Suppresses the `totem_of_undying` particle burst when a totem pops. The on-screen totem
animation and sound are left alone.

## Multiplayer

Everything here is client-side and works on any server, vanilla or modded — nothing needs
installing server-side, and the server is never sent anything it would not receive from an
ordinary player pressing a hotbar key.

**Nothing in this mod acts without an input from you.** Every feature is either a hotbar
selection change, or a single vanilla action carried out in direct response to a click you
made on that exact item. One input is always one action: a held button will not repeat
anything, and nothing at all happens on a timer or off the back of taking damage.

That is a deliberate line — it is what separates this from an auto-totem or an auto-eat,
which act for you with no input at all and are what server anti-cheat is actually looking
for.

Whether a server *permits* it is still a rules question rather than a technical one, and
worth checking against the rules of the server you play on.

## Config

Every setting lives in a [YetAnotherConfigLib](https://modrinth.com/mod/yacl) screen,
reachable three ways:

- the **RA** button in the top-left of the title screen and the pause menu,
- **Mod Menu → Respawn Anchor Help → Configure**, if Mod Menu is installed,
- `/anchorhelp config`.

YACL is a required dependency. Mod Menu is optional.

The button sits in the corner rather than joining the icon row that Mod Menu and ukulib
build, because where that row lands depends on which mods are installed and there is no
reliable way to slot into it without risking an overlap.

## Controls

Commands (client-side, never sent to the server):

```
/anchorhelp                                       show all toggles
/anchorhelp config                                open the config screen
/anchorhelp <swap|explosions|totems>              show one
/anchorhelp <swap|explosions|totems> on|off|toggle
```

`/rah` is an alias for `/anchorhelp`.

There are also three keybinds under **Options → Controls → Respawn Anchor Help**, one per
toggle. They are unbound by default so they cannot collide with anything you already use.

Settings persist to `config/respawn-anchor-help.properties`. All three default to on.

## Building

```
./gradlew build
```

The jar lands in `build/libs/`. Requires JDK 25.

Minecraft 26.2 is unobfuscated, so this project uses the `net.fabricmc.fabric-loom`
plugin with no `mappings` line, `implementation` rather than `modImplementation`, and
`jar` rather than `remapJar`.

## License

Available under the CC0 license.
