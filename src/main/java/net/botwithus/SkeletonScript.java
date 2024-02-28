package net.botwithus;

import net.botwithus.api.game.hud.inventories.Backpack;
import net.botwithus.api.game.hud.inventories.BackpackInventory;
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
import net.botwithus.rs3.imgui.NativeInteger;
import net.botwithus.rs3.script.Execution;
import net.botwithus.rs3.script.LoopingScript;
import net.botwithus.rs3.script.config.ScriptConfig;
import net.botwithus.rs3.events.impl.SkillUpdateEvent;
import net.botwithus.rs3.game.skills.Skills;
import net.botwithus.rs3.game.*;
import net.botwithus.rs3.util.RandomGenerator;
import net.botwithus.rs3.util.Regex;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SkeletonScript extends LoopingScript {

    private BotState botState = BotState.IDLE;
    private WispType wispState = WispType.Pale;
    private boolean someBool = true;
    private Random random = new Random();
    public HashMap<String, Area> Colonies;

    /////////////////////////////////////Botstate//////////////////////////
    enum BotState {
        //define your own states here
        IDLE,
        SKILLING,
        DEPOSIT,
        //...
    }

    enum WispType {
        Pale,
        Flickering,
        Bright,
        Sparkling,
        Gleaming,
        Vibrant,
        Lustrous,
        Elder,
        Brilliant,
        Radiant,
        Luminous,
        Incandescent
    }

    public SkeletonScript(String s, ScriptConfig scriptConfig, ScriptDefinition scriptDefinition) {
        super(s, scriptConfig, scriptDefinition);
        this.sgc = new SkeletonScriptGraphicsContext(getConsole(), this);
        initializeMaps(); // Call to initialize maps
    }

    private void initializeMaps() {
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
        this.loopDelay = 3000;
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
                Execution.delay(handleSkilling(player, wispState.name()));
            }
            case DEPOSIT -> {
                    //do some code that handles your skilling
                    Execution.delay(deposit());
            }
        }
    }

    private long deposit() {
        println("Backpack is full");
        Execution.delay(random.nextLong(1000, 2000));
        SceneObject rift = SceneObjectQuery.newQuery().name("Energy rift").results().first();
        if (rift != null) {
            rift.interact("Convert memories");
            Execution.delayUntil(5000, () -> !containsMemoryItems());
        }
        SceneObject Rift = SceneObjectQuery.newQuery().name("Energy Rift").results().first();
        if (Rift != null) {
            Rift.interact("Convert memories");
            Execution.delayUntil(3000, () -> !containsMemoryItems());
        }
        if (containsMemoryItems() == true) {
            botState = BotState.DEPOSIT;
            println("Backpack is still full");
        }
        else {
            botState = BotState.SKILLING;
            println("Backpack is empty");
        }
        return random.nextLong(1000, 1500);
    }

    private boolean containsMemoryItems() {
        Pattern memoryPattern = Regex.getPatternForContainsString(" memory");
        for (Item item : Backpack.getItems()) {
            if (memoryPattern.matcher(item.getName()).find()) {
                return true;
            }
        }
        return false;
    }

    private long handleSkilling(LocalPlayer player, String WispType) {
            if (Backpack.isFull()) {
                botState = BotState.DEPOSIT;
            }
            else if (player.getAnimationId() == -1) {
                println("Player is not harvesting");
                println("Harvesting " + WispType + " wisp");
                Npc wisp = NpcQuery.newQuery().name(WispType + " wisp").results().nearest();
                if (wisp != null) {
                    println("Wisp found!");
                    wisp.interact("Harvest");
                    Execution.delay(random.nextLong(3000, 7000));
                    return random.nextLong(1000, 1500);
                }
                else {
                    println("Wisp is null");
                    return 1000;
                }
            }
            else {
                println("Player is already busy");
                Execution.delay(random.nextLong(3000, 7000));
            }
        return 1000;
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

    public WispType getwispState() {
        return wispState;
    }

    public void setWispType(WispType wispType) {
        this.wispState = wispType;
    }

    public boolean isSomeBool() {
        return someBool;
    }

    public void setSomeBool(boolean someBool) {
        this.someBool = someBool;
    }
}
