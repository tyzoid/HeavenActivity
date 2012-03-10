package net.blockheaven.kaipr.heavenactivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

public class HeavenActivityData {

    /**
     * Plugin reference
     */
    protected HeavenActivity plugin;
    
    public List<Map<String, Map<ActivitySource, Integer>>> playersActivities;
    
    public HeavenActivityData(HeavenActivity plugin) {
        this.plugin = plugin;
        playersActivities = new ArrayList<Map<String, Map<ActivitySource, Integer>>>(plugin.config.maxSequences);
    }
    
    public void initNewSequence() {
        synchronized(playersActivities) {
            long sequenceInitStarted = System.currentTimeMillis();
            if (playersActivities.size() == plugin.config.maxSequences) {
                // TODO: Collect stats
                // Map<String, Map<ActivitySource, Integer>> oldSequence = playersActivities.remove(0);
                playersActivities.remove(0);
            }
            playersActivities.add(new HashMap<String, Map<ActivitySource, Integer>>());
            plugin.debugMsg("New sequence initiated", sequenceInitStarted);
        }
    }
    
    public void addActivity(String playerName, ActivitySource source) {
        addActivity(playerName, source, 1);
    }
    
    /**
     * Adds given amount of activity to the given playerName
     * 
     * @param playerName
     * @param source
     * @param count
     */
    public void addActivity(String playerName, ActivitySource source, Integer count) {
        
        playerName = playerName.toLowerCase();

        if (getCurrentSequence().containsKey(playerName)) {
            final Integer oldCount = getCurrentSequence().get(playerName).get(source);
            if (oldCount != null) {
                count += oldCount;
            }
        } else {
            getCurrentSequence().put(playerName, new HashMap<ActivitySource, Integer>(ActivitySource.values().length));
        }
        
        getCurrentSequence().get(playerName).put(source, count);
        
    }
    
    /**
     * Calculates and returns activity of given Player
     * 
     * @param player
     * @return
     */
    public int getActivity(Player player) {
        return getActivity(player.getName());
    }
    
    /**
     * Calculates and returns activity of given playerName
     * 
     * @param playerName
     * @return
     */
    public int getActivity(String playerName) {
        return getActivity(playerName, plugin.config.defaultSequences);
    }
    
    /**
     * Calculates and returns activity of given playerName for the last given sequences
     * 
     * @param playerName
     * @param sequences
     * @return
     */
    public int getActivity(String playerName, int sequences) {
        
        long started = System.currentTimeMillis();
        
        playerName = playerName.toLowerCase();
        
        int startSequence = playersActivities.size() - sequences;
        if (startSequence < 0) startSequence = 0;
        
        final Iterator<Map<String, Map<ActivitySource, Integer>>> sequenceIterator = playersActivities.listIterator(startSequence);
        
        Double activityPoints = 0.0;
        final Map<ActivitySource, Double> multiplierSet = plugin.getCumulatedMultiplierSet(plugin.getServer().getPlayer(playerName));
        
        for (int i1 = playersActivities.size(); i1 > 0; i1--) {
            final Map<ActivitySource, Integer> playerSequence = sequenceIterator.next().get(playerName);
            if (playerSequence == null) continue;
            
            final Iterator<ActivitySource> sourceIterator = playerSequence.keySet().iterator();
            
            for (int i2 = playerSequence.size(); i2 > 0; i2--) {
                final ActivitySource source = sourceIterator.next();
                if (multiplierSet.containsKey(source)) {
                    activityPoints += playerSequence.get(source) * plugin.config.pointsFor(source) * multiplierSet.get(source);
                } else {
                    activityPoints += playerSequence.get(source) * plugin.config.pointsFor(source);
                }
            }
        }
        
        final int activity = (int)(activityPoints * plugin.config.pointMultiplier / sequences);
        
        //TODO: Remove debug code
        System.out.println("[HeavenActivity] Debug message: activityPoints=" + activityPoints + ", activity=" + activity + ", sequences=" + sequences + ", pointMultiplyer=" + plugin.config.pointMultiplier);
        
        if (plugin.config.debug)
            plugin.debugMsg("Activity (" + String.valueOf(activity) + ") calculated for player " + playerName + " using " + String.valueOf(sequences) + " sequences.", started);
        
        return (activity > 100) ? 100 : activity;
    
    }
    
    private Map<String, Map<ActivitySource, Integer>> getCurrentSequence() {
        return playersActivities.get(playersActivities.size() - 1);
    }

}
