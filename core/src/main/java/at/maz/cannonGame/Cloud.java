package at.maz.cannongame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import java.util.Random;

public class Cloud {

    private final Sprite sprite = new Sprite(new Texture("powerBar.png")); // TODO change to cloud-image
    private final Vector2 position;
    private float xPosition;
    private final Vector2 DIMENSION = new Vector2(18f, 5f);
    private final float speed = 0.5f + new Random().nextInt(9) / 10;

    public Cloud(Vector2 worldDimensions) {
        this.xPosition = worldDimensions.x + 1f;
        this.position = new Vector2(xPosition, worldDimensions.y - (DIMENSION.y + new Random().nextInt(10) + 0.5f));
    }

    public void drawAndMove(SpriteBatch batch) {
        batch.draw(sprite, xPosition, position.y, DIMENSION.x, DIMENSION.y);
        xPosition -= Gdx.graphics.getDeltaTime() * speed;
    }

    public float getXPosition() {
        return xPosition;
    }

    public float getXDimension() {
        return DIMENSION.x;
    }

}
