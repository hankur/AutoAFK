package me.prunt.autoafk;

import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.inventivetalent.particle.ParticleEffect;

public class ParticleTask implements Runnable {
    private Main main;
    private int id;
    private Player p;

    public ParticleTask(Main main, Player p) {
	this.main = main;
	this.p = p;
    }

    public int getId() {
	return id;
    }

    public void setId(int id) {
	this.id = id;
    }

    @Override
    public void run() {
	int count = main.getConfig().getInt("particles.count");

	if (main.getServer().getVersion().contains("1.8")) {
	    ParticleEffect.valueOf(main.getConfig().getString("particles.type"))
		    .send(main.getServer().getOnlinePlayers(), p.getLocation(), 0, 1, 0, 1, count);
	} else {
	    p.getWorld().spawnParticle(Particle.valueOf(main.getConfig().getString("particles.type")), p.getLocation(),
		    count);
	}
    }
}
