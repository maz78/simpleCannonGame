package at.maz.gdxtest2;

import at.maz.gdxtest2.util.PercentValue;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class GdxTest2 extends ApplicationAdapter {
    private SpriteBatch batch;
    private Box2DDebugRenderer renderer;
    private OrthographicCamera camera;
    private ContactListenerImpl contactListener;

    //world unit = meters
    private final float WORLD_WIDTH = 100f;
    private final float WORLD_HEIGHT = 80f;

    private Optional<Boolean> rotating = Optional.empty();
    private Optional<Boolean> loadingPower = Optional.empty();
    // Clicks per second
    private final float KEYPRESSED_CLICKSPEED = 22f;

    private record Shot(float rotaion, float powerpercent, boolean hit) {
    }

    private List<Shot> shotsFired = new ArrayList<>();
    private final int ROLLINGACCURACYINTERVAL = 20;

    private World world;
    private Body floor;
    private Sprite floorSprite = new Sprite();
    private Body wall;
    private Sprite wallSprite = new Sprite();


    private DebugSprite debugSprite;
    private Cannon cannon;
    private Bullet bullet;
    private PowerBar powerBar;
    private Target target;
    private BitmapFont font;

    private float rotation = 45;
    private PercentValue powerPercent = new PercentValue(60);
    private float powerPercentKeyRepeat = 0;
    private float rollingAccuracyPercent = 0;

    @Override
    public void create() {
        camera = new OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT);
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        camera.update();

        // World etc.
        batch = new SpriteBatch();
        batch.setProjectionMatrix(camera.combined);
        renderer = new Box2DDebugRenderer();
        world = new World(new Vector2(0, -10), true);
        this.contactListener = new ContactListenerImpl();
        world.setContactListener(contactListener);
        font = new BitmapFont(Gdx.files.internal("gjFont02-hd.fnt"), Gdx.files.internal("gjFont02-hd.png"), false);
        font.getData().setScale(0.06f, 0.06f);
        font.setColor(Color.FIREBRICK);

        // Debug options
        renderer.setDrawBodies(true);
        debugSprite = new DebugSprite();

        // Floor
        BodyEditorLoader bodyEditorLoader = new BodyEditorLoader(new FileHandle("assets/gdxTest2.json"));
        floorSprite = new Sprite(new Texture("floor.png"));
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.StaticBody;
        bd.position.set(0, 0);
        floor = world.createBody(bd);
        FixtureDef floorFixtureDef = new FixtureDef();
        floorFixtureDef.density = 1;
        floorFixtureDef.friction = 0.5f;
        floorFixtureDef.restitution = 0.0f;
        bodyEditorLoader.attachFixture(floor, "floor", floorFixtureDef, WORLD_WIDTH);

        //Wall
        Vector2 wallPosition = new Vector2(WORLD_WIDTH * 0.5f, WORLD_HEIGHT);
        wallSprite = new Sprite(new Texture("wall.png"));
        BodyDef wallBd = new BodyDef();
        wallBd.type = BodyDef.BodyType.DynamicBody;
        wallBd.position.set(wallPosition.x, wallPosition.y);
        wall = world.createBody(wallBd);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(2, 15);
        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        fd.density = 2f;
        fd.friction = 1f;
        fd.restitution = 0.2f;
        wall.createFixture(fd);

        // Cannon
        cannon = new Cannon(new Vector2(WORLD_WIDTH * 0.95f, WORLD_HEIGHT * 0.09f));

        // Power bar
        powerBar = new PowerBar(new Vector2(WORLD_WIDTH * 0.4f, WORLD_HEIGHT * 0.003f), WORLD_WIDTH * 0.2f);


        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode) {
                    case Input.Keys.LEFT -> rotating = Optional.of(true);
                    case Input.Keys.RIGHT -> rotating = Optional.of(false);
                    case Input.Keys.UP -> loadingPower = Optional.of(true);
                    case Input.Keys.DOWN -> loadingPower = Optional.of(false);
                    case Input.Keys.SPACE -> {
                        createNewBullet();
                    }
                }
                return true;
            }

            @Override
            public boolean keyUp(int keycode) {
                switch (keycode) {
                    case Input.Keys.LEFT, Input.Keys.RIGHT -> rotating = Optional.empty();
                    case Input.Keys.UP, Input.Keys.DOWN -> loadingPower = Optional.empty();
                }
                return true;
            }
        });
    }

    private void createNewBullet() {
        destroyBullet();
        bullet = new Bullet(world, cannon.getTopPosition(), cannon.getShootingDirection().scl((float) 0.28f * (45 + powerPercent.getValue() * 0.45f)));
        shotsFired.add(new Shot(rotation, powerPercent.getValue(), false));
    }

    private void destroyBullet() {
        if (bullet != null) {
            bullet.clear();
            bullet = null;
        }
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(135 / 255f, 206 / 255f, 235 / 255f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        world.step(Gdx.graphics.getDeltaTime(), 6, 2);
        renderer.render(world, camera.combined);

        rotating.ifPresent(bool -> rotation += (bool ? rotation <= 90f ? +0.5f : 0 : rotation >= -20 ? -0.5f : 0));
        loadingPower.ifPresent(bool -> powerPercent.inc(bool ? Gdx.graphics.getDeltaTime() * KEYPRESSED_CLICKSPEED : Gdx.graphics.getDeltaTime() * -KEYPRESSED_CLICKSPEED));

        if (target == null) {
            target = new Target(world);
        }

        if (contactListener.isTargetHit()) {
            // Last shot is a hit, adapt list of shots.
            ListIterator listIterator = shotsFired.listIterator(shotsFired.size() - 1);
            listIterator.next();
            listIterator.set(new Shot(11f, powerPercent.getValue(), true));

            destroyBullet();
            target.clear();
            target = null;
            contactListener.setTargetHit(false);
        }

        batch.begin();
        if (target != null) {
            target.draw(batch);
        }
        batch.draw(floorSprite, floor.getPosition().x, floor.getPosition().y, WORLD_WIDTH, WORLD_HEIGHT * 0.063f);
        batch.draw(wallSprite, wall.getPosition().x - 2, wall.getPosition().y - 15, 4, 30);
        if (bullet != null) {
            bullet.draw(batch);
        }
        if (bullet != null && (bullet.getPosition().x > WORLD_WIDTH || bullet.getPosition().x < 0 || bullet.getPosition().y < 0))
            destroyBullet();

        cannon.draw(batch, rotation);
        powerBar.draw(batch, powerPercent.getValue());

        font.draw(batch, "Last" + ROLLINGACCURACYINTERVAL + ": " + 3.4f + "%", WORLD_WIDTH - 30, WORLD_HEIGHT - 2);
        font.draw(batch, shotsFired.stream().filter(shot -> shot.hit()).count() + "/" + shotsFired.size(), WORLD_WIDTH - 30, WORLD_HEIGHT - 5);

        //debugSprite.draw(batch, target.getPosition());

        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
