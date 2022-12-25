package at.maz.cannongame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Cannon {
    private final Vector2 CANNON_DIMENSIONS = new Vector2(1.8f, 6f);
    private final Vector2 CANNON_ORIGIN = new Vector2(CANNON_DIMENSIONS.x / 2, CANNON_DIMENSIONS.y * 0.2f);
    private final Sprite cannonSprite = new Sprite(new Texture("cannon.png"));
    private final Sprite cannonStandSprite = new Sprite(new Texture("cannonStand.png"));
    private final Vector2 position;
    private float rotation = 0;

    public Cannon(Vector2 position) {
        this.position = new Vector2(position.x - CANNON_ORIGIN.x, position.y - CANNON_ORIGIN.y);
    }

    public void draw(SpriteBatch batch, Float rotation) {
        this.rotation = rotation;
        batch.draw(cannonSprite, position.x, position.y, CANNON_ORIGIN.x, CANNON_ORIGIN.y, CANNON_DIMENSIONS.x, CANNON_DIMENSIONS.y, 1f, 1f, rotation);
        batch.draw(cannonStandSprite, position.x, position.y - CANNON_ORIGIN.y, CANNON_DIMENSIONS.x, CANNON_DIMENSIONS.y);
    }

    public Vector2 getTopPosition() {
        // Position of cannon outlet
        return getShootingDirection().setLength(CANNON_DIMENSIONS.y - CANNON_ORIGIN.y).add(position).add(CANNON_ORIGIN.x, CANNON_ORIGIN.y);
    }

    public Vector2 getShootingDirection() {
        return new Vector2(1f, 1f).setAngleDeg(rotation - 2 + 90f);
    }
}
