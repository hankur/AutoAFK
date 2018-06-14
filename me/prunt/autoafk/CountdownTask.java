package me.prunt.autoafk;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.connorlinfoot.titleapi.TitleAPI;

public class CountdownTask implements Runnable {
    private Main main;
    private int id;
    private int i;
    private String t;
    private Player p;

    public CountdownTask(Main main, Player p, String type) {
	this.main = main;
	this.p = p;
	this.i = 5;
	this.t = type;
    }

    public int getId() {
	return id;
    }

    public void setId(int id) {
	this.id = id;
    }

    @Override
    public void run() {
	// when it's time to put into afk or kick
	if (this.i == 0) {
	    // title and sound
	    if (main.getServer().getVersion().contains("1.10") || main.getServer().getVersion().contains("1.9")
		    || main.getServer().getVersion().contains("1.8")) {
		TitleAPI.sendTitle(p, 0, 40, 20, "",
			main.getMessage("messages.afk-on").replaceAll("%player%", p.getName()));
	    } else {
		p.sendTitle("", main.getMessage("messages.afk-on").replaceAll("%player%", p.getName()), 0, 40, 20);
	    }
	    p.playSound(p.getLocation(), Sound.valueOf(main.getConfig().getString("countdown.sound")), 1, 1);

	    if (this.t == "afk") {
		// put into afk
		main.addAFK(p);
	    } else if (this.t == "kick") {
		// Kick from server
		main.kick(p);
	    }

	    // Cancel task
	    main.getServer().getScheduler().cancelTask(getId());
	} else { // still countdown
	    // title and sound
	    if (main.getServer().getVersion().contains("1.10") || main.getServer().getVersion().contains("1.9")
		    || main.getServer().getVersion().contains("1.8")) {
		TitleAPI.sendTitle(p, 0, 40, 20,
			main.getMessage("countdown.title").replaceAll("%count%", String.valueOf(i)),
			main.getMessage("countdown.subtitle." + this.t).replaceAll("%count%", String.valueOf(i)));
	    } else {
		p.sendTitle(main.getMessage("countdown.title").replaceAll("%count%", String.valueOf(i)),
			main.getMessage("countdown.subtitle." + this.t).replaceAll("%count%", String.valueOf(i)), 0, 21,
			0);
	    }
	    p.playSound(p.getLocation(), Sound.valueOf(main.getConfig().getString("countdown.sound")), 1, 1);

	    // reduce countdown nr
	    this.i--;
	}
    }
}
