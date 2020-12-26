package com.willfp.ecoenchants.enchantments.ecoenchants.normal;

import com.willfp.eco.core.proxy.proxies.CooldownProxy;
import com.willfp.eco.util.ProxyUtils;
import com.willfp.ecoenchants.enchantments.EcoEnchant;
import com.willfp.ecoenchants.enchantments.EcoEnchants;
import com.willfp.ecoenchants.enchantments.meta.EnchantmentType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

public class Cleave extends EcoEnchant {
    public Cleave() {
        super(
                "cleave", EnchantmentType.NORMAL
        );
    }

    // START OF LISTENERS

    @Override
    public void onMeleeAttack(@NotNull final LivingEntity attacker,
                              @NotNull final LivingEntity victim,
                              final int level,
                              @NotNull final EntityDamageByEntityEvent event) {
        if (victim.hasMetadata("cleaved")) {
            return;
        }

        if (attacker instanceof Player
                && ProxyUtils.getProxy(CooldownProxy.class).getAttackCooldown((Player) attacker) != 1.0f
                && !this.getConfig().getBool(EcoEnchants.CONFIG_LOCATION + "allow-not-fully-charged")) {
            return;
        }

        double damagePerLevel = this.getConfig().getDouble(EcoEnchants.CONFIG_LOCATION + "damage-percentage-per-level") * 0.01;
        double radiusPerLevel = this.getConfig().getDouble(EcoEnchants.CONFIG_LOCATION + "radius-per-level");
        final double damage = damagePerLevel * level * event.getDamage();
        final double radius = radiusPerLevel * level;

        victim.getNearbyEntities(radius, radius, radius).stream()
                .filter(entity -> entity instanceof LivingEntity)
                .filter(entity -> !entity.equals(attacker))
                .forEach(entity -> {
                    entity.setMetadata("cleaved", new FixedMetadataValue(this.getPlugin(), true));
                    ((LivingEntity) entity).damage(damage, attacker);
                    this.getPlugin().getScheduler().runLater(() -> entity.removeMetadata("cleaved", this.getPlugin()), 5);
                });
    }
}
