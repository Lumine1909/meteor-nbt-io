package io.github.lumine1909.nbtio.mixin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.lumine1909.nbtio.NbtIoAddon;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.commands.NbtCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NbtCommand.class)
public abstract class NbtCommandMixin extends Command {

    public NbtCommandMixin(String name, String description, String... aliases) {
        super(name, description, aliases);
    }

    @Unique
    private int importFromNbt(String nbtAsString, String failMessage) {
        try {
            LocalPlayer player = Minecraft.getInstance().player;
            assert player != null;
            ItemStack item = ItemStack.CODEC.decode(player.registryAccess().createSerializationContext(NbtOps.INSTANCE), TagParser.parseCompoundFully(nbtAsString)).getOrThrow().getFirst();
            setStack(item);
            this.info("Loaded nbt as item to your main hand");
        } catch (Exception e) {
            NbtIoAddon.LOG.error("Failed to import nbt!", e);
            this.info(failMessage);
            return 0;
        }
        return SINGLE_SUCCESS;
    }

    @Unique
    private int exportToNbt() {
        try {
            LocalPlayer player = Minecraft.getInstance().player;
            assert player != null;
            ItemStack item = player.inventoryMenu.getSlot(36 + player.getInventory().getSelectedSlot()).getItem();
            String nbt = ItemStack.OPTIONAL_CODEC.encodeStart(player.registryAccess().createSerializationContext(NbtOps.INSTANCE), item).getOrThrow().toString();
            Minecraft.getInstance().keyboardHandler.setClipboard(nbt);
            this.info("Saved nbt to your clipboard");
            return SINGLE_SUCCESS;
        } catch (Exception e) {
            NbtIoAddon.LOG.error("Failed to import nbt: ", e);
            this.info("Failed to save nbt to clipboard, check log for details");
            return 0;
        }
    }

    @Unique
    private static void setStack(ItemStack stack) {
        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        player.connection.send(new ServerboundSetCreativeModeSlotPacket(36 + player.getInventory().getSelectedSlot(), stack));
        player.inventoryMenu.getSlot(36 + player.getInventory().getSelectedSlot()).setByPlayer(stack);
    }

    @Inject(
        method = "build",
        at = @At(value = "TAIL"),
        remap = false
    )
    private void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder, CallbackInfo cib) {
        builder.then(literal("import").executes(context -> {
            String nbtAsString = Minecraft.getInstance().keyboardHandler.getClipboard();
            return importFromNbt(nbtAsString, "Failed to parse nbt from clipboard, check log for details");
        }).then(argument("nbt", StringArgumentType.greedyString()).executes(context -> {
            String nbtAsString = StringArgumentType.getString(context, "nbt");
            return importFromNbt(nbtAsString, "Failed to parse nbt from input, check log for details");
        })));
        builder.then(literal("export").executes(context -> exportToNbt()));
    }
}
