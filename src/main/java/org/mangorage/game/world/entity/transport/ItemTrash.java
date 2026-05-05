package org.mangorage.game.world.entity.transport;

import org.mangorage.game.client.render.core.RenderContext;
import org.mangorage.game.world.World;
import org.mangorage.game.world.entity.Entity;
import org.mangorage.game.world.pos.BoundingBox;
import org.mangorage.game.world.pos.Position;
import org.mangorage.game.world.pos.Facing;
import org.mangorage.game.world.resource.item.Item;
import org.mangorage.game.world.resource.item.IItemReceiver;
import org.mangorage.game.world.registeries.Entities;

import java.awt.*;

public final class ItemTrash extends Entity implements IItemReceiver {

	public ItemTrash(World world, BoundingBox box) {
		super(Entities.ITEM_TRASH_ENTITY_TYPE, world, box);
	}

	@Override
	public boolean acceptItem(Item item, org.mangorage.game.world.pos.Position source) {
		System.out.println("Trashed item: " + item.name());
		return true;
	}

	@Override
	public void render(RenderContext ctx) {

		ctx.submit(g -> {
			Position pos = getPosition();
			var box = getBoundingBox();

			int x = pos.x();
			int y = pos.y();

			g.setColor(Color.RED);

			if (box.parts().isEmpty()) {
				int w = (getFacing() == Facing.EAST || getFacing() == Facing.WEST) ? box.width() : box.height();
				int h = (getFacing() == Facing.EAST || getFacing() == Facing.WEST) ? box.height() : box.width();
				g.fillRect(x, y, w, h);
			} else {
				for (var p : box.parts(getFacing())) {
					int px = x + p.offsetX();
					int py = y + p.offsetY();

					g.fillRect(px, py, p.width(), p.height());
				}
			}

			g.setColor(Color.WHITE);
			g.drawString("TRASH", x + 2, y + 12);
		});
	}
}