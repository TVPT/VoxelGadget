package com.thevoxelbox.voxelgadget;

import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class VoxelGadget extends JavaPlugin {

    protected static final Logger log = Logger.getLogger("Minecraft");
    private GadgetListener p;
    public static Server s;

    public VoxelGadget() {
        this.p = new GadgetListener();
    }

    public void onDisable() {
    }

    public void onEnable() {
        s = this.getServer();
        //s.getPluginManager().registerEvents(p, this); //this threw errors even with recommended build...
        s.getPluginManager().registerEvents(p, this);
        p.loadConfig();
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info(pdfFile.getName() + " version " + pdfFile.getVersion()
                + " is enabled!");
    }
}
