package at.maz.cannongame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import java.util.Random;

public class Target {
    private final World world;
    private final Body target;
    private final Sprite sprite = new Sprite(new Texture("bird.png"));

    private static final float WIDTH = 1.8f;
    private final float HEIGHT = 6f;

    // Center point is used as origin
    private final TargetPosition position;

    public enum TargetPosition {
        // No random y-value for target, but distinct positions to make it easier for AI.
        UNDEFINED(new Vector2(Vector2.Zero)),
        POS1(new Vector2(WIDTH / 2, 5)),
        POS2(new Vector2(WIDTH / 2, 12)),
        POS3(new Vector2(WIDTH / 2, 19)),
        POS4(new Vector2(WIDTH / 2, 26)),
        POS5(new Vector2(WIDTH / 2, 33)),
        POS6(new Vector2(WIDTH / 2, 40)),
        POS7(new Vector2(WIDTH / 2, 47)),
        POS8(new Vector2(WIDTH / 2, 54)),
        POS9(new Vector2(WIDTH / 2, 61)),
        POS10(new Vector2(WIDTH / 2, 68)),
        POS11(new Vector2(WIDTH / 2, 75));

        private final Vector2 pos;

        TargetPosition(Vector2 pos) {
            this.pos = pos;
        }
    }

    public Target(World world) {
        this.world = world;
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.StaticBody;
        position = TargetPosition.values()[new Random().nextInt(TargetPosition.values().length - 1) + 1];
        bd.position.set(position.pos.x, position.pos.y);
        target = world.createBody(bd);
        target.setUserData(this);
        PolygonShape rect = new PolygonShape();
        rect.setAsBox(WIDTH / 2f, HEIGHT / 2f);
        FixtureDef fd = new FixtureDef();
        fd.shape = rect;
        fd.density = 0.5f;
        fd.friction = 0.8f;
        fd.restitution = 0.2f;
        target.createFixture(fd);
    }

    public void draw(SpriteBatch batch) {
        batch.draw(sprite, this.getPosition().pos.x - WIDTH / 2, this.getPosition().pos.y - HEIGHT / 2, WIDTH, HEIGHT);
    }

    public TargetPosition getPosition() {
        return position;
    }

    public void clear() {
        world.destroyBody(target);
    }

}

