package nadiendev.perfectmobfarm.screen;

import nadiendev.perfectmobfarm.PerfectMobFarm;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.Locale;

/**
 * Mob farm screen
 */
public class MobFarmScreen extends AbstractContainerScreen<MobFarmMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(PerfectMobFarm.MODID, "textures/gui/mobfarm-gui-2.png");

    /** Progress bar groove, relative to the top left corner of the gui */
    private static final int BAR_X = 152;
    private static final int BAR_Y = 6;
    private static final int BAR_WIDTH = 4;
    private static final int BAR_HEIGHT = 41;

    /** Position of the filled bar sprite in the 256x256 texture (right of the gui, same height as the groove) */
    private static final int BAR_U = 177;
    private static final int BAR_V = 6;

    /**
     * Energy bar (energy farms only): a horizontal bar drawn with fills between the title (y 6 to 14) and the
     * inventory label (y 39), left of the second shard slot (x 58). The frame is 1 pixel wide, the inside is
     * ENERGY_WIDTH x ENERGY_HEIGHT (the frame ends at x 54).
     */
    private static final int ENERGY_X = 8;
    private static final int ENERGY_Y = 21;
    private static final int ENERGY_WIDTH = 44;
    private static final int ENERGY_HEIGHT = 8;

    private static final int ENERGY_SHADOW_COLOR = 0xFF373737;
    private static final int ENERGY_LIGHT_COLOR = 0xFFFFFFFF;
    private static final int ENERGY_EMPTY_COLOR = 0xFF2A2A2A;
    private static final int SLOT_INNER_COLOR = 0xFF8B8B8B;
    private static final int SLOT_FRAME_SIZE = 18;

    private static final int ENERGY_FULL_TOP_COLOR = 0xFFFF4B3E;
    private static final int ENERGY_FULL_BOTTOM_COLOR = 0xFFB01818;
    private static final int ENERGY_SHINE_COLOR = 0xFFFF9C94;

    public MobFarmScreen(MobFarmMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        // The size of the gui texture
        super(pMenu, pPlayerInventory, pTitle, 176, 133);

        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.extractBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);

        // The bar fills from the bottom to the top
        int progress = menu.getProgress(BAR_HEIGHT);
        if (progress > 0) {
            int offset = BAR_HEIGHT - progress;
            pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + BAR_X, y + BAR_Y + offset, (float) BAR_U,
                    (float) (BAR_V + offset), BAR_WIDTH, progress, 256, 256);
        }

        // Shard A is already part of the texture; the optional slots get a frame while they are available
        for (int index = MobFarmMenu.SHARD_B_INDEX; index <= MobFarmMenu.MOB_SLOT_UPGRADE_INDEX; index++) {
            Slot slot = menu.slots.get(index);
            if (slot.isActive()) {
                renderSlotFrame(pGuiGraphics, x + slot.x - 1, y + slot.y - 1);
            }
        }

        if (menu.usesEnergy()) {
            renderEnergyBar(pGuiGraphics, x + ENERGY_X, y + ENERGY_Y);
        }
    }

    /**
     * Draw a vanilla style sunken 18x18 slot frame with its top left corner at (pX, pY)
     */
    private void renderSlotFrame(GuiGraphicsExtractor pGuiGraphics, int pX, int pY) {
        // Light bottom and right edges, dark top and left edges
        pGuiGraphics.fill(pX, pY, pX + SLOT_FRAME_SIZE, pY + SLOT_FRAME_SIZE, ENERGY_LIGHT_COLOR);
        pGuiGraphics.fill(pX, pY, pX + SLOT_FRAME_SIZE - 1, pY + SLOT_FRAME_SIZE - 1, ENERGY_SHADOW_COLOR);
        pGuiGraphics.fill(pX + 1, pY + 1, pX + SLOT_FRAME_SIZE - 1, pY + SLOT_FRAME_SIZE - 1, SLOT_INNER_COLOR);
    }

    /**
     * Draw the energy bar with its top left frame corner at (pX, pY), like a vanilla sunken slot; it fills from
     * the left to the right
     */
    private void renderEnergyBar(GuiGraphicsExtractor pGuiGraphics, int pX, int pY) {
        // Light bottom and right edges, dark top and left edges
        pGuiGraphics.fill(pX, pY, pX + ENERGY_WIDTH + 2, pY + ENERGY_HEIGHT + 2, ENERGY_LIGHT_COLOR);
        pGuiGraphics.fill(pX, pY, pX + ENERGY_WIDTH + 1, pY + ENERGY_HEIGHT + 1, ENERGY_SHADOW_COLOR);
        int innerX = pX + 1;
        int innerY = pY + 1;
        pGuiGraphics.fill(innerX, innerY, innerX + ENERGY_WIDTH, innerY + ENERGY_HEIGHT, ENERGY_EMPTY_COLOR);

        int filled = menu.getEnergyScaled(ENERGY_WIDTH);
        if (filled > 0) {
            int half = ENERGY_HEIGHT / 2;
            pGuiGraphics.fill(innerX, innerY, innerX + filled, innerY + half, ENERGY_FULL_TOP_COLOR);
            pGuiGraphics.fill(innerX, innerY + half, innerX + filled, innerY + ENERGY_HEIGHT, ENERGY_FULL_BOTTOM_COLOR);
            pGuiGraphics.fill(innerX, innerY, innerX + filled, innerY + 1, ENERGY_SHINE_COLOR);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        // The background is drawn by extractBackground, and the slots and the item tooltip by the parent
        super.extractRenderState(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        renderEmptySlotTooltip(pGuiGraphics, pMouseX, pMouseY);
        renderBarTooltip(pGuiGraphics, pMouseX, pMouseY);
        renderEnergyTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    /**
     * Name the optional slot under the mouse when it is empty and nothing is held by the cursor
     */
    private void renderEmptySlotTooltip(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY) {
        if (hoveredSlot == null || !hoveredSlot.isActive() || hoveredSlot.hasItem() || !menu.getCarried().isEmpty()) {
            return;
        }

        Component text = switch (hoveredSlot.index) {
            case MobFarmMenu.SHARD_B_INDEX -> Component.translatableWithFallback(
                    "gui.perfectmobfarmrevived.second_shard_slot", "Second mob shard slot");
            case MobFarmMenu.SPEED_UPGRADE_INDEX -> Component.translatableWithFallback(
                    "gui.perfectmobfarmrevived.speed_upgrade_slot", "Speed Upgrade (up to 4)");
            case MobFarmMenu.MOB_SLOT_UPGRADE_INDEX -> Component.translatableWithFallback(
                    "gui.perfectmobfarmrevived.mob_slot_upgrade_slot", "Mob Slot Upgrade");
            default -> null;
        };
        if (text != null) {
            pGuiGraphics.setTooltipForNextFrame(font, text, pMouseX, pMouseY);
        }
    }

    /**
     * Draw "elapsed s / total s" (or "Inactive", "Disabled", "No energy") when the mouse is over the progress bar
     */
    private void renderBarTooltip(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY) {
        int x = leftPos + BAR_X;
        int y = topPos + BAR_Y;
        if (pMouseX < x || pMouseX >= x + BAR_WIDTH || pMouseY < y || pMouseY >= y + BAR_HEIGHT) {
            return;
        }

        Component text;
        if (menu.isRedstoneDisabled()) {
            text = Component.translatableWithFallback("gui.perfectmobfarmrevived.disabled_redstone",
                    "Disabled");
        } else if (menu.isEnergyStarved()) {
            text = Component.translatableWithFallback("gui.perfectmobfarmrevived.no_energy", "No energy");
        } else if (menu.isActive() && menu.getCooldown() > 0) {
            text = Component.literal(String.format(Locale.ROOT, "%.1fs / %.1fs",
                    menu.getTimer() / 20.0F, menu.getCooldown() / 20.0F));
        } else {
            text = Component.translatableWithFallback("gui.perfectmobfarmrevived.inactive", "Inactive");
        }
        pGuiGraphics.setTooltipForNextFrame(font, text, pMouseX, pMouseY);
    }

    /**
     * Draw "stored / capacity FE" when the mouse is over the energy bar of an energy farm
     */
    private void renderEnergyTooltip(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY) {
        if (!menu.usesEnergy()) {
            return;
        }
        int x = leftPos + ENERGY_X;
        int y = topPos + ENERGY_Y;
        if (pMouseX < x || pMouseX >= x + ENERGY_WIDTH + 2 || pMouseY < y || pMouseY >= y + ENERGY_HEIGHT + 2) {
            return;
        }

        Component text = Component.translatableWithFallback("gui.perfectmobfarmrevived.energy",
                "%s / %s FE", String.format(Locale.ROOT, "%,d", menu.getEnergy()),
                String.format(Locale.ROOT, "%,d", menu.getEnergyCapacity()));
        pGuiGraphics.setTooltipForNextFrame(font, text, pMouseX, pMouseY);
    }
}
