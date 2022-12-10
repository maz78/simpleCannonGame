package at.maz.gdxtest2;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import java.util.Random;

public class Cloud {

    private final Sprite sprite = new Sprite(new Texture("powerBar.png"));
    private final Vector2 position;
    private float xPosition;
    private final SpriteBatch batch;
    private final Vector2 DIMENSION = new Vector2(8f, 2f);

    public Cloud(Vector2 worldDimensions, SpriteBatch batch) {
        this.batch = batch;
        this.xPosition = worldDimensions.x + DIMENSION.x;
        this.position = new Vector2(xPosition, worldDimensions.y - (DIMENSION.y + new Random().nextInt(5) + 0.5f));
    }

    public void draw(float xPosition) {
        batch.draw(sprite, xPosition, position.y, DIMENSION.x, DIMENSION.y);
        this.xPosition = xPosition;
    }

    public float getXPosition() {
        return xPosition;
    }
}
