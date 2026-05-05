package org.mangorage.game.world.entity.transport;

import org.mangorage.game.render.RenderContext;
import org.mangorage.game.world.World;
import org.mangorage.game.world.entity.Entity;
import org.mangorage.game.world.pos.BoundingBox;
import org.mangorage.game.world.pos.Position;
import org.mangorage.game.world.resource.item.Item;
import org.mangorage.game.world.resource.item.IItemReceiver;
import org.mangorage.game.world.registeries.Entities;

import java.awt.*;

public final class ItemTrash extends Entity implements IItemReceiver {

	public ItemTrash(World world, BoundingBox box) {
		super(Entities.ITEM_TRASH_ENTITY_TYPE, world, box);
	}

	@Override
	public boolean acceptItem(Item item) {
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
			g.fillRect(x, y, box.width(), box.height());

			g.setColor(Color.WHITE);
			g.drawString("TRASH", x + 2, y + 12);
		});
	}
}