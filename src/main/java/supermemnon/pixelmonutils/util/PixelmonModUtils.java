package supermemnon.pixelmonutils.util;

import com.pixelmonmod.pixelmon.api.dialogue.Dialogue;
import com.pixelmonmod.pixelmon.entities.npcs.NPCEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class PixelmonModUtils {
    public static void customNpcChat(Entity npc, ServerPlayerEntity player, String[] messages) {
        if (messages.length < 1) {
            return;
        }
        String name = (npc.getCustomName() != null) ? npc.getCustomName().getString() : npc.getName().getString();
        List<Dialogue> dialogues = new ArrayList<>();
//        Dialogue messageDialogue = Dialogue.builder()
//                                                .setText(message)
//                                                .setName(npc.getName().getString())
//                                                .build();
//        dialogues.add(messageDialogue);
        for (int i = 0; i < messages.length; i++) {
            Dialogue messageDialogue = Dialogue.builder()
                                                .setText(FormattingHelper.formatWithAmpersand(messages[i]))
                                                .setName(name)
                                                .build();
            dialogues.add(messageDialogue);
        }
        Dialogue.setPlayerDialogueData(player, dialogues, true);
    }
}
