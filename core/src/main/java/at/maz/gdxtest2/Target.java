package at.maz.gdxtest2;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import java.util.Random;

public class Target {
    private final World world;
    private final Body target;
    private final Sprite sprite = new Sprite(new Texture("target.png"));

    private final float WIDTH = 1.8f;
    private final float HEIGHT = 6f;

    private TargetPosition position;

    public enum TargetPosition {
        POS0(new Vector2(0, 1)),
        POS1(new Vector2(0, 10)),
        POS2(new Vector2(0, 20)),
        POS3(new Vector2(0, 30)),
        POS4(new Vector2(0, 40)),
        POS5(new Vector2(0, 50)),
        POS6(new Vector2(0, 60)),
        POS7(new Vector2(0, 70));

        private final Vector2 pos;

        TargetPosition(Vector2 pos) {
            this.pos = pos;
        }
    }

    public Target(World world) {
        this.world = world;
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.StaticBody;
        position = TargetPosition.values()[new Random().nextInt(TargetPosition.values().length)];
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
        batch.draw(sprite, target.getPosition().x, target.getPosition().y, WIDTH, HEIGHT);
    }

    public Vector2 getPosition() {
        return position.pos;
    }

    public void clear() {
        world.destroyBody(target);
    }

}

