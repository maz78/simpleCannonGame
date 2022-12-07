package at.maz.gdxtest2;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class DebugSprite {
    private Vector2 DIMENSION = new Vector2(5.0f, 5.0f);

    private Sprite sprite = new Sprite(new Texture("debugSprite.png"));


    public void draw(SpriteBatch batch, Vector2 position) {
        batch.draw(sprite, position.x - DIMENSION.x / 2, position.y - DIMENSION.y / 2, position.x + DIMENSION.x / 2, position.y + DIMENSION.y / 2, DIMENSION.x, DIMENSION.y, 1, 1, 0);
    }

}

