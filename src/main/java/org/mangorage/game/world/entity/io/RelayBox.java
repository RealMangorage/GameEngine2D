package org.mangorage.game.world.entity.io;

import org.mangorage.game.input.MouseButton;
import org.mangorage.game.render.RenderContext;
import org.mangorage.game.world.World;
import org.mangorage.game.world.pos.BoundingBox;
import org.mangorage.game.world.pos.Position;
import org.mangorage.game.world.registeries.Entities;

import java.awt.*;

public final class RelayBox extends EntityIO {

    public RelayBox(World world, BoundingBox box) {
        super(
                Entities.RELAY_BOX_ENTITY_TYPE,
                world,
                box,
                1,
                1,
                1.0 / 225.0,
                0.18
        );
    }

    @Override
    public void onClick(MouseButton mouseButton) {
        if (mouseButton == MouseButton.MIDDLE) {
            outputs.clear();
            items.clear();
        }
    }

    @Override
    public void render(RenderContext ctx) {
        renderConnectionsAndItems(ctx, Color.GRAY, Color.YELLOW, 16);

        ctx.submit(g -> {
            Position pos = getPosition();
            var box = getBoundingBox();

            int baseX = pos.x();
            int baseY = pos.y();

            g.setColor(Color.DARK_GRAY);

            for (var p : box.parts()) {
                g.fillRect(
                        baseX + p.offsetX(),
                        baseY + p.offsetY(),
                        p.width(),
                        p.height()
                );
            }
        });
    }
}