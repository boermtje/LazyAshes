package net.botwithus;

import net.botwithus.api.game.hud.inventories.Backpack;
import net.botwithus.internal.scripts.ScriptDefinition;
import net.botwithus.rs3.events.impl.ChatMessageEvent;
import net.botwithus.rs3.game.Client;
import net.botwithus.rs3.game.hud.interfaces.Interfaces;
import net.botwithus.rs3.game.js5.types.NpcType;
import net.botwithus.rs3.game.queries.builders.animations.ProjectileQuery;
import net.botwithus.rs3.game.queries.builders.characters.NpcQuery;
import net.botwithus.rs3.game.queries.builders.objects.SceneObjectQuery;
import net.botwithus.rs3.game.scene.entities.characters.npc.Npc;
import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer;
import net.botwithus.rs3.game.scene.entities.object.SceneObject;
import net.botwithus.rs3.script.Execution;
import net.botwithus.rs3.script.LoopingScript;
import net.botwithus.rs3.script.config.ScriptConfig;
import net.botwithus.rs3.events.impl.SkillUpdateEvent;
import net.botwithus.rs3.game.skills.Skills;
import net.botwithus.rs3.game.*;
import net.botwithus.rs3.game.actionbar.ActionBar;
import net.botwithus.rs3.events.EventBus;

import java.util.Random;

public class SkeletonScript extends LoopingScript {

    private BotState botState = BotState.IDLE;
    private boolean someBool = true;
    private Random random = new Random();

    enum BotState {
        //define your own states here
        IDLE,
        SKILLING,
        BANKING,
        //...
    }

    public SkeletonScript(String s, ScriptConfig scriptConfig, ScriptDefinition scriptDefinition) {
        super(s, scriptConfig, scriptDefinition);
        this.sgc = new SkeletonScriptGraphicsContext(getConsole(), this);

        // Subscribe to ChatMessageEvent
        subscribe(ChatMessageEvent.class, chatMessageEvent -> {
            // Idles when stunned
            if (chatMessageEvent.getMessage().contains("stunned")) {
                Execution.delay(4000); // Idle for 4 seconds
                println("Got hit, Idle");
            }
            else if (chatMessageEvent.getMessage().contains("No edible food could")) {
                botState = BotState.IDLE;
                println("No Food - Going IDLE");
            }
        });
    }

    @Override
    public void onLoop() {
        //Loops every 100ms by default, to change:
        //this.loopDelay = 500;
        LocalPlayer player = Client.getLocalPlayer();
        if (player == null || Client.getGameState() != Client.GameState.LOGGED_IN || botState == BotState.IDLE) {
            //wait some time so we dont immediately start on login.
            Execution.delay(random.nextLong(3000,7000));
            return;
        }
        switch (botState) {
            case IDLE -> {
                //do nothing
                println("We're idle!");
                Execution.delay(random.nextLong(1000,3000));
            }
            case SKILLING -> {
                //do some code that handles your skilling
                Execution.delay(handleSkilling(player));
            }
            case BANKING -> {
                //handle your banking logic, etc
            }
        }
    }

    public boolean shouldEat(LocalPlayer player) {
        double healthPercentage = ((double) player.getCurrentHealth() / player.getMaximumHealth()) * 100;
        return healthPercentage < 40;
    }


    private long handleSkilling(LocalPlayer player) {
        //for example, if skilling progress interface is open, return a randomized value to keep waiting.
        if (Interfaces.isOpen(1251))
            return random.nextLong(250, 1500);
        //if our inventory is full, lets bank.
        if (Backpack.isFull()) {
            println("BackPack Full going IDLE");
            botState = BotState.IDLE;
            return random.nextLong(250, 1500);
        }

// Elsewhere in your code, where you want to check if the player should eat
        if (shouldEat(player)) {
            ActionBar.useAbility("Eat Food");
            println("Eating");
        }

        //click my tree, mine my rock, etc...
        Npc GullibleTourist = NpcQuery.newQuery().id(36512).option("Pickpocket").results().first();
        if (GullibleTourist != null) {
            if (player.getAnimationId() == -1) {
                GullibleTourist.interact("Pickpocket");
                println("Stealing the money");

            } else if (player.getAnimationId() == 24887) {
                // Do nothing for animation ID 24887
                println("Already Pickpocketing");
            }
        } else {
            botState = BotState.IDLE;
            println("No Tourist, going idle");
        }
        return random.nextLong(1500,3000);
    }
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
            if (skillUpdateEvent.getId() == Skills.THIEVING.getId()) {
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
            int xpToNextLevel = Skills.THIEVING.getExperienceToNextLevel();
            int totalSeconds = (int) (xpToNextLevel * 3600.0 / xpPerHour);
            int hours = totalSeconds / 3600;
            int minutes = (totalSeconds % 3600) / 60;
            int seconds = totalSeconds % 60;
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return "N/A";
    }

    public String xpGained() {
        return xpGained + " XP";
    }

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
