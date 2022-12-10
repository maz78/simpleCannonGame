package at.maz.gdxtest2;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Bullet {
    private final float BULLET_RADIUS = 0.7f;

    private final World world;
    private final Body bullet;
    private final Sprite bulletSprite = new Sprite(new Texture("bullet.png"));

    public Bullet(World world, Vector2 position, Vector2 impulse) {
        this.world = world;
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.DynamicBody;
        bd.position.set(position.x, position.y);
        bullet = world.createBody(bd);
        bullet.setUserData(this);
        CircleShape circle = new CircleShape();
        circle.setRadius(BULLET_RADIUS);
        FixtureDef fd = new FixtureDef();
        fd.shape = circle;
        fd.density = 0.5f;
        fd.friction = 0.8f;
        fd.restitution = 0.2f;
        bullet.createFixture(fd);
        bullet.applyLinearImpulse(impulse.x, impulse.y, bullet.getPosition().x, bullet.getPosition().y, false);
    }

    public void draw(SpriteBatch batch) {
        batch.draw(bulletSprite, bullet.getPosition().x - BULLET_RADIUS, bullet.getPosition().y - BULLET_RADIUS, BULLET_RADIUS * 2, BULLET_RADIUS * 2);
    }

    public Vector2 getPosition() {
        return bullet.getPosition();
    }

    public void clear() {
        world.destroyBody(bullet);
    }

}

