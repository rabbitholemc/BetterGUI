package io.github.braayy.bettergui.listener;

import io.github.braayy.bettergui.GUI;
import io.github.braayy.bettergui.input.InputRequest;
import io.github.braayy.bettergui.input.InputTypes;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.PlayerSignCommandPreprocessEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

public final class InputListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInputChat(AsyncChatEvent event) {
        List<MetadataValue> metadataList = event.getPlayer().getMetadata(GUI.INPUT_KEY);
        if (metadataList.size() == 0) return;

        InputRequest inputRequest = (InputRequest) metadataList.get(0);
        if (inputRequest.inputType() != InputTypes.CHAT) return;
        event.setCancelled(true);

        String plainTextMessage = PlainTextComponentSerializer.plainText().serialize(event.originalMessage());
        inputRequest.callback().accept(plainTextMessage);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInputChatCommand(PlayerCommandPreprocessEvent event) {
        List<MetadataValue> metadataList = event.getPlayer().getMetadata(GUI.INPUT_KEY);
        if (metadataList.size() == 0) return;

        InputRequest inputRequest = (InputRequest) metadataList.get(0);
        if (inputRequest.inputType() != InputTypes.CHAT) return;

        event.setCancelled(true);
    }

}
