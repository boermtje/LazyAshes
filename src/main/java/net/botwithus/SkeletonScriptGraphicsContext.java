package net.botwithus;

import net.botwithus.rs3.imgui.ImGui;
import net.botwithus.rs3.imgui.ImGuiWindowFlag;
import net.botwithus.rs3.imgui.NativeInteger;
import net.botwithus.rs3.script.ScriptConsole;
import net.botwithus.rs3.script.ScriptGraphicsContext;

import java.util.Arrays;

public class SkeletonScriptGraphicsContext extends ScriptGraphicsContext {

    private SkeletonScript script;

    public SkeletonScriptGraphicsContext(ScriptConsole scriptConsole, SkeletonScript script) {
        super(scriptConsole);
        this.script = script;
    }

    @Override
    public void drawSettings() {
        if (ImGui.Begin("LazyRuneSpan", ImGuiWindowFlag.None.getValue())) {
            if (ImGui.BeginTabBar("My bar", ImGuiWindowFlag.None.getValue())) {
                if (ImGui.BeginTabItem("Settings", ImGuiWindowFlag.None.getValue())) {
                    ImGui.Text("My scripts state is: " + script.getBotState());
                    ImGui.Text("We are harvesting: " + script.getwispState());
                    // Wisp type selection combo box
                    String[] wispTypes = Arrays.stream(SkeletonScript.WispType.values())
                            .map(Enum::name)
                            .toArray(String[]::new);
                    NativeInteger selectedWisp = new NativeInteger(script.getwispState().ordinal());
                    if (ImGui.Combo("Wisp Type", selectedWisp, wispTypes)) {
                        script.setWispType(SkeletonScript.WispType.values()[selectedWisp.get()]);
                    }
                    if (ImGui.Button("Start")) {
                        //button has been clicked
                        script.setBotState(SkeletonScript.BotState.SKILLING);
                    }
                    ImGui.SameLine();
                    if (ImGui.Button("Stop")) {
                        //has been clicked
                        script.setBotState(SkeletonScript.BotState.IDLE);
                    }
                    ImGui.EndTabItem();
                }
                if (ImGui.BeginTabItem("Instructions", ImGuiWindowFlag.None.getValue())) {
                    ImGui.Text("Go to which spot you want to train at");
                    ImGui.Text("Select memory type and click start");
                    ImGui.EndTabItem();
                }

                if (ImGui.BeginTabItem("Stats", ImGuiWindowFlag.None.getValue())) {
                    ImGui.Text("Time Passed: " + script.timePassed());
                    ImGui.Text("TTL: " + script.ttl());
                    ImGui.Text("XP Gained: " + script.xpGained());
                    ImGui.Text("Levels Gained: " + script.levelsGained());
                    ImGui.Text("XP/HR: " + script.xpPerHour());
                    ImGui.EndTabItem();
                }
                ImGui.EndTabBar();
            }
            ImGui.End();
        }

    }

    @Override
    public void drawOverlay() { super.drawOverlay(); }
}
