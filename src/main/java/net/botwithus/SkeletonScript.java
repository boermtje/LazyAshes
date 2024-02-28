package net.botwithus;

import net.botwithus.internal.scripts.ScriptDefinition;
import net.botwithus.rs3.game.Client;
import net.botwithus.rs3.game.queries.builders.characters.NpcQuery;
import net.botwithus.rs3.game.queries.builders.items.InventoryItemQuery;
import net.botwithus.rs3.game.queries.builders.objects.SceneObjectQuery;
import net.botwithus.rs3.game.queries.results.EntityResultSet;
import net.botwithus.rs3.game.queries.results.ResultSet;
import net.botwithus.rs3.game.scene.entities.characters.npc.Npc;
import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer;
import net.botwithus.rs3.game.scene.entities.object.SceneObject;
import net.botwithus.rs3.script.Execution;
import net.botwithus.rs3.script.LoopingScript;
import net.botwithus.rs3.script.config.ScriptConfig;
import net.botwithus.rs3.events.impl.SkillUpdateEvent;
import net.botwithus.rs3.game.skills.Skills;
import net.botwithus.rs3.game.*;
import net.botwithus.rs3.util.RandomGenerator;

import java.util.*;
import java.util.stream.Collectors;

public class SkeletonScript extends LoopingScript {

    private BotState botState = BotState.IDLE;
    private boolean someBool = true;
    private Random random = new Random();
    private HashMap<String, Integer> HarvestType;
    private HashMap<String, Area> Colonies;

    /////////////////////////////////////Botstate//////////////////////////
    enum BotState {
        //define your own states here
        IDLE,
        SKILLING,
        //...
    }

    public SkeletonScript(String s, ScriptConfig scriptConfig, ScriptDefinition scriptDefinition) {
        super(s, scriptConfig, scriptDefinition);
        this.sgc = new SkeletonScriptGraphicsContext(getConsole(), this);
        initializeMaps(); // Call to initialize maps
    }

    private void initializeMaps() {
        // Initialization logic for priorityObjects, islands, levelRequirements
        // HashMap for priority objects with their level requirements
        HarvestType = new HashMap<>();
        HarvestType.put("Incandescent", 95);
        HarvestType.put("Luminous", 90);
        HarvestType.put("Radiant", 85);
        HarvestType.put("Brilliant", 80);
        HarvestType.put("Elder", 75);
        HarvestType.put("Lustrous", 70);
        HarvestType.put("Vibrant", 60);
        HarvestType.put("Gleaming", 50);
        HarvestType.put("Sparkling", 40);
        HarvestType.put("Glowing", 30);
        HarvestType.put("Bright", 20);
        HarvestType.put("Flickering", 10);
        HarvestType.put("Pale", 1);

        Colonies = new HashMap<>();
        Area.Rectangular Pale = new Area.Rectangular(new Coordinate(3989, 6095, 1), new Coordinate(4007, 6119, 1));
        Colonies.put("Pale", Pale);
        Area.Rectangular Flickering = new Area.Rectangular(new Coordinate(3990, 6067, 1), new Coordinate(4014, 6041, 1));
        Colonies.put("Flickering", Flickering);
        Area.Rectangular Bright = new Area.Rectangular(new Coordinate(4125, 6093, 1), new Coordinate(4146, 6068, 1));
        Colonies.put("Bright", Bright);
        Area.Rectangular Sparkling = new Area.Rectangular(new Coordinate(4191, 6108, 1), new Coordinate(4204, 6085, 1));
        Colonies.put("Sparkling", Sparkling);
        Area.Rectangular Gleaming = new Area.Rectangular(new Coordinate(4325, 6055, 1), new Coordinate(4365, 6037, 1));
        Colonies.put("Gleaming", Gleaming);
        Area.Rectangular Vibrant = new Area.Rectangular(new Coordinate(4371, 6086, 1), new Coordinate(4385, 6070, 1));
        Colonies.put("Vibrant", Vibrant);
        Area.Rectangular Lustrous = new Area.Rectangular(new Coordinate(4371, 6086, 1), new Coordinate(4385, 6070, 1));
        Colonies.put("Lustrous", Lustrous);
        Area.Rectangular Elder = new Area.Rectangular(new Coordinate(4371, 6086, 1), new Coordinate(4385, 6070, 1));
        Colonies.put("Elder", Elder);
        Area.Rectangular Brilliant = new Area.Rectangular(new Coordinate(4371, 6086, 1), new Coordinate(4385, 6070, 1));
        Colonies.put("Brilliant", Brilliant);
        Area.Rectangular Radiant = new Area.Rectangular(new Coordinate(4371, 6086, 1), new Coordinate(4385, 6070, 1));
        Colonies.put("Radiant", Radiant);
        Area.Rectangular Luminous = new Area.Rectangular(new Coordinate(4371, 6086, 1), new Coordinate(4385, 6070, 1));
        Colonies.put("Luminous", Luminous);
        Area.Rectangular Incandescent = new Area.Rectangular(new Coordinate(4371, 6086, 1), new Coordinate(4385, 6070, 1));
        Colonies.put("Incandescent", Incandescent);
    }

    @Override
    public void onLoop() {
        //Loops every 100ms by default, to change:
        //this.loopDelay = 500;
        LocalPlayer player = Client.getLocalPlayer();
        if (player == null || Client.getGameState() != Client.GameState.LOGGED_IN || botState == BotState.IDLE) {
            //wait some time so we dont immediately start on login.
            Execution.delay(random.nextLong(3000, 7000));
            return;
        }

        /////////////////////////////////////Botstate//////////////////////////
        switch (botState) {
            case IDLE -> {
                println("We're idle!");
                Execution.delay(random.nextLong(1000, 3000));
            }
            case SKILLING -> {
                //do some code that handles your skilling
                Execution.delay(handleSkilling(player));
            }
        }
    }

    private long handleSkilling(LocalPlayer player) {
        Area currentColony = determineCurrentColony(player);
        if (currentColony == null) {
            return 1000;
        }
        println("We are on island: " + currentColony.getArea());
        int currentLevel = Skills.DIVINATION.getLevel();
        String currentHarvestType = HarvestType.entrySet().stream().filter(entry -> entry.getValue() <= currentLevel).map(Map.Entry::getKey).collect(Collectors.toList()).get(0);
        println("We are harvesting: " + currentHarvestType);
        SceneObject rift = SceneObjectQuery.newQuery().name("Rift").inside(currentColony).results();
        if (rift == null) {
            println("Rift not found");
            return 1000;
        }
        if (rift.interact("Harvest")) {
            Execution.delayUntil(() -> {
                Npc wisps = NpcQuery.newQuery().name("Wisp").inside(currentColony).results();
                return wisps != null;
            }, 5000);
        }
        return 1000;

    }

    private Area determineCurrentColony(LocalPlayer player) {
        for (Map.Entry<String, Area> entry : Colonies.entrySet()) {
            println("Checking island: " + entry.getKey());
            if (entry.getValue().contains(player.getCoordinate())) {
                println("Player is at Colony: " + entry.getKey());
                return entry.getValue();
            }
        }
        println("Player is not on any known island");
        return null;
    }

    /////////////////STATISTICS////////////////////
    //XP Gain & Level Gain base is set to zero,
    private int xpGained = 0;
    private int levelsGained = 0;
    private long startTime;
    private int xpPerHour;
    private String ttl; // Time to level

    //XP Gain & Level Gain is calculated and added to base
    @Override
    public boolean initialize() {
        startTime = System.currentTimeMillis();
        xpGained = 0;
        levelsGained = 0;

        subscribe(SkillUpdateEvent.class, skillUpdateEvent -> {
            if (skillUpdateEvent.getId() == Skills.DIVINATION.getId()) {
                xpGained += (skillUpdateEvent.getExperience() - skillUpdateEvent.getOldExperience());
                if (skillUpdateEvent.getOldActualLevel() < skillUpdateEvent.getActualLevel()) {
                    levelsGained++;
                }
            }
        });

        return super.initialize();
    }

    public String levelsGained() {
        return levelsGained + " Levels";
    }

    public String xpPerHour() {
        long currentTime = System.currentTimeMillis();
        if (currentTime > startTime) {
            xpPerHour = (int) (xpGained * 3600000.0 / (currentTime - startTime));
        }
        return xpPerHour + " XP/hr";
    }

    public String ttl() {
        if (xpPerHour > 0) {
            int xpToNextLevel = Skills.DIVINATION.getExperienceToNextLevel();
            int totalSeconds = (int) (xpToNextLevel * 3600.0 / xpPerHour);
            int hours = totalSeconds / 3600;
            int minutes = (totalSeconds % 3600) / 60;
            int seconds = totalSeconds % 60;
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return "N/A";
    }

    public String timePassed() {
        long currentTime = System.currentTimeMillis();
        long elapsedMillis = currentTime - startTime;
        long hours = elapsedMillis / 3600000;
        long minutes = (elapsedMillis % 3600000) / 60000;
        long seconds = (elapsedMillis % 60000) / 1000;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public String xpGained() {
        return xpGained + " XP";
    }



    ////////////////////Botstate/////////////////////
    public BotState getBotState() {
        return botState;
    }

    public void setBotState(BotState botState) {
        this.botState = botState;
    }

    public boolean isSomeBool() {
        return someBool;
    }

    public void setSomeBool(boolean someBool) {
        this.someBool = someBool;
    }
}
