# Modern Poses API

Modern Poses backports the player's poses, off hand and melee combat from Minecraft 26.3 to 1.7.10. Everything public
is in `com.grihail.modernposes.api`. Other classes are internal and can change without notice.

## Setup

Build against the API jar and require the mod at runtime:

```groovy
// dependencies.gradle
compileOnly(files("libs/modern-poses-1.3.1-api.jar"))
```

```java
@Mod(modid = "yourmod", dependencies = "required-after:modernposes@[1.3.1,)")
```

The mod id is `modernposes`. It needs UniMixins, and must be installed on both client and server.

`ModernCombat`, `WeaponItem` and `ToughArmor` need 1.3.1 or later. `ModernHands.offhandEnabled` and
`ModernCombat.attackCooldownEnabled` are newer than 1.3.1.

Players can turn parts of the mod off in `config/modernposes.cfg`, so don't assume every feature is there:

- Check `ModernHands.offhandEnabled()` and `ModernCombat.attackCooldownEnabled()` before relying on the off hand or the
  attack charge.
- A turned-off `sneak`, `swim` or `crawl` pose is removed from every `PoseSet`, yours included, so `posesOf` doesn't
  return it. `fall_flying` and poses with other names are kept.

Register providers, triggers and held models during `init`.

## Poses

Modern Poses owns the player's hitbox and eye height. The client picks the pose of its own player; the server validates
it and sends it to everyone else.

### Custom body sizes

Return a `PoseSet` from a `PoseProvider`. The first provider that returns non-null wins; otherwise the player gets
`PoseSet.VANILLA`.

```java
PoseSet SMALL = new PoseSet(
    Arrays.asList(
        new PoseSize("stand", 0.4F, 0.9F, 0.75F),   // name, width, height, eye height
        new PoseSize("sneak", 0.4F, 0.7F, 0.55F),
        new PoseSize("swim", 0.4F, 0.3F, 0.2F),
        new PoseSize("crawl", 0.4F, 0.3F, 0.2F)),
    Arrays.asList("stand", "sneak", "crawl"),       // tried in order when the wanted pose doesn't fit
    true);                                          // modern water movement and sprint-swimming

ModernPoses.register(player -> isSmall(player) ? SMALL : null);
```

| Pose | Taken when |
|---|---|
| `stand` | Default. Required. |
| `sneak` | Holding sneak, or under a low ceiling. |
| `swim` | Sprint-swimming. |
| `crawl` | Under a ceiling too low for `sneak`. |
| `sprint` | Sprinting on land. Optional; put a low one last in `fallback`. |
| `fall_flying` | A trigger asks for it. If omitted, it copies `swim`, or `stand` if there is no `swim`. |

Width and height must be within 0.1..4, and the eye within 0.1..height.

The rules that bite:

- `posesFor` runs on both sides, as often as every tick. Cache your sets.
- Both sides must return the same sizes for the same player, or the server rejects the client's poses.
- On the client, return `PoseSet.WAIT` until your data for that player arrives.
- When a player's set changes, call `ModernPoses.refresh(player)` on the server.

### Requesting a pose

A `PoseTrigger` is asked on the owning client every tick, before the built-in rules. The first name the player's set
contains wins. The pose still goes through `fallback` and the server check.

```java
ModernPoses.registerTrigger(p -> isGliding(p) ? PoseSet.FALL_FLYING : null);
```

### Reading the state

| `ModernPoses` method | Returns | Side |
|---|---|---|
| `pose(e)` | Current `PoseSize`, or `null` if not known yet | both |
| `poseName(e)` | Pose name, `"stand"` if unknown | both |
| `poseHeight(e)` | Hitbox height | both |
| `isLying(e)` | Swimming or crawling | both |
| `isFallFlying(e)` | In the gliding pose | both |
| `posesOf(p)` | The player's `PoseSet` | both |
| `fits(p, pose, slack)` | Whether the pose's box fits among the blocks | both |
| `swimAmount(e, partial)` | 0..1 blend into the lying render pose | client |
| `fallFlyingAmount(e, partial)` | 0..1 blend into the gliding render pose | client |
| `isSprintSwimming(e)` | Sprint-swimming right now | client, own player |

## Hands

The off hand is a separate inventory slot, saved with the player and synced to clients. The main hand is the right arm,
the off hand the left.

| `ModernHands` method | Purpose |
|---|---|
| `offhandEnabled()` | Whether the off hand is on in the config. When off, `getOffhand` returns `null` and `setOffhand` does nothing. |
| `getOffhand(p)` / `setOffhand(p, stack)` | Off hand stack. Set it on the server. |
| `getHeld(p, hand)` / `setHeld(p, hand, stack)` | Stack in either hand. |
| `usedHand(p)` | Hand using an item, or `null`. |
| `swingingHand(p)` | Hand of the current swing; `MAIN` if none. |
| `useTicks(p, hand, partial)` | Ticks the hand has been using its item. |
| `swingProgress(p, hand, partial)` | Swing progress 0..1 for that hand. |
| `armPose(p, hand)` | What the arm is doing, as an `ArmPose`. |
| `registerHeldModel(item, model)` | Draw the item with your own model. Client. |
| `renderHeld(p, hand, partial)` | Draw what a hand holds, with the matrix at the arm's pivot. Client. |

### Item interfaces

These interfaces mostly set animation and hand behaviour. Gameplay such as damage, charging and zoom stays in your item.

| Interface | Effect |
|---|---|
| `OffhandItem` | Shift-click in the inventory sends it to the off hand. |
| `RaisedBlockItem` | Blocks by being raised, like a shield. Use with `EnumAction.block`. 1.7.10's damage halving is off: your code decides what is stopped. |
| `KineticItem` | Held forward and charged, like a spear. Its methods time the stages of the charge. |
| `StabItem` | The attack is a thrust lasting `swingTicks(stack)` instead of 6 ticks. |
| `ScopeItem` | Lifted to the eye while used. First-person hands are hidden. |

A minimal shield:

```java
public class MyShield extends Item implements OffhandItem, RaisedBlockItem {

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.block;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        player.setItemInUse(stack, getMaxItemUseDuration(stack));
        return stack;
    }
}
```

### Custom bodies and models

If you render your own player body, read `armPose` and apply the matching angles. `ArmAngles` and `SpearMotion` are
plain math, in radians on `ModelBiped`'s axes, and touch neither a model nor GL.

| `ArmPose` | Angles |
|---|---|
| `BLOCK` | `ArmAngles.blocking` |
| `BOW_AND_ARROW` | `ArmAngles.bowAim` |
| `SPYGLASS` | `ArmAngles.spyglass` |
| `SPEAR` | `SpearMotion.holdingArm`; `SpearMotion.stabArmX` for the stab |
| any, while swinging | `ArmAngles.swing` |

Then draw the item with `ModernHands.renderHeld`, after `postRender` of the arm or at your model's hand locator.

A `HeldModel` replaces the item-in-hand drawing in both first and third person. The matrix is already in 26.3's item
space for that hand, so apply only the model's display transform and draw it. For the left hand, negate the x
translation and the y and z rotation. Override `ownSwing` if the model animates its own attack, and `renderAtEye` for a
`ScopeItem` in third person.

## Combat

Melee follows 26.3: attacks charge, and damage scales with the charge. `AttackEntityEvent` and
`Item.onLeftClickEntity` still fire first and can cancel the attack.

| `ModernCombat` | Purpose |
|---|---|
| `attackCooldownEnabled()` | Whether the attack charge is on in the config. When off, `attackStrength` is always 1 and players have no `ATTACK_SPEED`. |
| `attackStrength(p, partial)` | Charge of the next attack, 0..1. |
| `attackDelay(p)` | Ticks a full charge takes with the held item. |
| `resetAttack(p)` | Restart the charge. |
| `ATTACK_SPEED` | The attribute (`generic.attackSpeed`, base 4). Players only. |

### Weapons

Anything extending `ItemSword`, `ItemAxe`, `ItemPickaxe`, `ItemSpade` or `ItemHoe` gets 26.3's damage and speed from
its `ToolMaterial`, with no work on your side. Swords also sweep. An axe with a modded material gets attack speed -3.0.

Any other item has attack speed 4 and effectively no cooldown, even with its own attack damage. To change that,
implement `WeaponItem`:

```java
public class MyHalberd extends Item implements WeaponItem {

    @Override
    public double attackSpeedModifier() {
        return -3.0D;   // added to the base of 4: 1 hit per second
    }

    @Override
    public boolean sweeps(ItemStack stack) {
        return true;
    }
}
```

`attackSpeedModifier` takes effect only if your `getItemAttributeModifiers` calls `super`. On an `ItemSword` or
`ItemTool` subclass, `WeaponItem` also turns off the automatic balance, so you set the damage yourself.

Tooltips switch to 26.3's "When in Main Hand" format when the item has the speed modifier. For the damage to appear
there, use the vanilla weapon UUID `CB3F55D3-645C-4F38-A497-9C13A33DB5CF` (`Item.field_111210_e`).

### Armor

Diamond `ItemArmor` has toughness 2 per piece. Implement `ToughArmor` on other armor:

```java
public class MyArmor extends ItemArmor implements ToughArmor {
    // constructor omitted

    @Override
    public float armorToughness(ItemStack stack) {
        return 3.0F;
    }
}
```

If a player wears any `ISpecialArmor`, the whole set uses Forge's calculation and 26.3's armor formula doesn't apply.
