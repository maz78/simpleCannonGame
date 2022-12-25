package at.maz.cannongame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class PowerBar {

    private final Sprite powerBarSprite = new Sprite(new Texture("powerBar.png"));
    private final Sprite powerBarStepSprite = new Sprite(new Texture("powerBarStep.png"));
    private final Sprite powerBarNoseSprite = new Sprite(new Texture("powerBarNose.png"));

    private final Vector2 DIMENSIONS;
    private final float INNERBAR_TOTAL_LENGTH;
    private final Vector2 INNERBAR_POSITION;
    private final Vector2 position;

    public PowerBar(Vector2 position, float width) {
        this.position = new Vector2(position.x, position.y);
        DIMENSIONS = new Vector2(width, 4.5f);
        INNERBAR_POSITION = new Vector2(this.position.x + DIMENSIONS.x * 0.138f, this.position.y + DIMENSIONS.y * 0.33f);
        INNERBAR_TOTAL_LENGTH = width * 0.8f;
    }

    public void draw(SpriteBatch batch, float powerPercent) {
        batch.draw(powerBarSprite, position.x, position.y, DIMENSIONS.x, DIMENSIONS.y);
        float innerBarLength = INNERBAR_TOTAL_LENGTH * powerPercent / 100f;
        batch.draw(powerBarStepSprite, INNERBAR_POSITION.x, INNERBAR_POSITION.y, innerBarLength, DIMENSIONS.y * 0.5f);
        batch.draw(powerBarNoseSprite, INNERBAR_POSITION.x + innerBarLength, INNERBAR_POSITION.y, INNERBAR_TOTAL_LENGTH * 0.06f, DIMENSIONS.y * 0.5f);
    }

}
