package net.blockheaven.kaipr.heavenactivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import org.bukkit.configuration.Configuration;
import org.mbertoli.jfep.Parser;

public class HeavenActivityConfig {

    //TODO! Update config!

    /**
     * Plugin reference
     */
    protected HeavenActivity plugin;
    
    /**
     * Configuration object
     */
    protected Configuration config;
    
    /**
     * Configuration values
     */
    public boolean debug;
    public int maxSequences;
    public int defaultSequences;
    public int sequenceInterval;
    public int notificationSequence;
    public int incomeSequence;
    public Double pointMultiplier;
    public boolean chatTracking;
    public boolean chatTrackCancelled;
    public Double chatPoints;
    public Double chatCharPoints;
    public boolean commandTracking;
    public boolean commandTrackCancelled;
    public Double commandPoints;
    public Double commandCharPoints;
    public boolean moveTracking;
    public Integer moveDelay;
    public Double movePoints;
    public boolean blockTracking;
    public Integer blockDelay;
    public Double blockPlacePoints;
    public Double blockBreakPoints;
    public boolean incomeEnabled;
    public int incomeMinActivity;
    public boolean incomeAllowNegative;
    public Parser incomeExpression;
    public String incomeSourceAccount;
    public Map<String, Map<ActivitySource, Double>> multiplierSets = new HashMap<String, Map<ActivitySource, Double>>();
    public boolean logCommands;
    
    
    public HeavenActivityConfig(HeavenActivity plugin) { //TODO! Fix config!
        this.plugin = plugin;
        
        File configFile = new File(plugin.getDataFolder() + File.separator + "config.yml");
        if (!configFile.exists()) {
            try {
                HeavenActivity.logger.warning("[HeavenActivity] Config file not found - generating default.");
                configFile.getParentFile().mkdir();
                configFile.createNewFile();
                OutputStream output = new FileOutputStream(configFile, false);
                InputStream input = HeavenActivityConfig.class.getResourceAsStream("/resources/" + configFile.getName());
                byte[] buf = new byte[8192];
                while (true) {
                    int length = input.read(buf);
                    if (length < 0) {
                        break;
                    }
                    output.write(buf, 0, length);
                }
                input.close();
                output.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        config = plugin.getConfig();
        load();
    }
    
    public void load() {
        plugin.reloadConfig();
        
        if (config.get("income.base_value") != null && config.get("income.expression") == null) {
            HeavenActivity.logger.info("[HeavenActivity] Migrating pre-1.0 income configuration to income expression...");
            int baseValue             = config.getInt("income.base_value", 8);
            int targetActivity        = config.getInt("income.target_activity", 50);
            int activityModifier      = config.getInt("income.activity_modifier", 75);
            double balanceMultiplier  = config.getDouble("income.balance_multiplier", 0.0);
            
            StringBuilder exp = new StringBuilder();
            exp.append(baseValue);
            exp.append(" + (((player_activity - ").append(targetActivity).append(") / ").append(activityModifier).append(") * ").append(baseValue).append(")");
            exp.append(" + (player_balance * ").append(balanceMultiplier).append(")");
            config.set("income.expression", exp.toString());
            config.set("income.base_value", null);
            config.set("income.target_activity", null);
            config.set("income.activity_modifier", null);
            config.set("income.balance_multiplier", null);
            plugin.saveConfig();
            plugin.reloadConfig();
        }
        
        debug                         = config.getBoolean("general.debug", false);
        maxSequences                  = config.getInt("general.max_sequences", 15);
        defaultSequences              = config.getInt("general.default_sequences", maxSequences);
        sequenceInterval              = config.getInt("general.sequence_interval", 60);
        notificationSequence          = config.getInt("general.notification_sequence", 6);
        incomeSequence                = config.getInt("general.income_sequence", 15);
        pointMultiplier               = config.getDouble("general.point_multiplier", 1.0);
        logCommands                   = config.getBoolean("general.log_commands", false);
        
        incomeEnabled                 = config.getBoolean("income.enabled", true);
        incomeMinActivity             = config.getInt("income.min_activity", 1);
        incomeAllowNegative           = config.getBoolean("income.allow_negative", true);
        incomeExpression              = new Parser(config.getString("income.expression", "8 + (((player_activity - 50) / 75) * 8)"));
        incomeSourceAccount           = config.getString("income.source_account", null);
        
        chatTracking                  = config.getBoolean("chat.tracking", true);
        chatTrackCancelled            = config.getBoolean("chat.track_cancelled", true);
        chatPoints                    = config.getDouble("chat.points", 1.0);
        chatCharPoints                = config.getDouble("chat.char_points", 0.50);
        commandTracking               = config.getBoolean("command.tracking", true);
        commandTrackCancelled         = config.getBoolean("command.track_cancelled", true);
        commandPoints                 = config.getDouble("command.points", 1.0);
        commandCharPoints             = config.getDouble("command.char_points", 0.55);
        moveTracking                  = config.getBoolean("move.tracking", true);
        moveDelay                     = config.getInt("move.delay", 1200);
        movePoints                    = config.getDouble("move.points", 0.5);
        blockTracking                 = config.getBoolean("block.tracking", true);
        blockDelay                    = config.getInt("block.delay", 900);
        blockPlacePoints              = config.getDouble("block.place_points", 4.0);
        blockBreakPoints              = config.getDouble("block.break_points", 2.0);

        Set<String> multiplierNames  = config.getConfigurationSection("multiplier").getKeys(true);
        if (multiplierNames != null && multiplierNames.size() > 0) {
            Iterator<String> multiplierSetNameIterator = multiplierNames.iterator();
            while (multiplierSetNameIterator.hasNext()) {
                String multiplierSetName = multiplierSetNameIterator.next();
                
                Map<ActivitySource, Double> multiplierSet = new HashMap<ActivitySource, Double>();
                
                final Iterator<String> sourceIterator = config.getConfigurationSection("multiplier").getKeys(true).iterator();
                while (sourceIterator.hasNext()) {
                    final String source = sourceIterator.next();
                    multiplierSet.put(ActivitySource.parseActivitySource(source), config.getDouble("multiplier." + multiplierSetName + "." + source, 1.0));
                }
                
                multiplierSets.put(multiplierSetName, multiplierSet);
            }
        }
    }
    
    public Double pointsFor(ActivitySource source) {
        switch(source) {
        case MOVE:
            return movePoints;
        case BLOCK_BREAK:
            return blockBreakPoints;
        case BLOCK_PLACE:
            return blockPlacePoints;
        case CHAT:
            return chatPoints;
        case CHAT_CHAR:
            return chatCharPoints;
        case COMMAND:
            return commandPoints;
        case COMMAND_CHAR:
            return commandCharPoints;
        }
        
        return null;
    }
    
}
