package at.maz.cannongame;

import at.maz.cannongame.util.PercentValue;
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Random;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class CannonGame extends ApplicationAdapter {
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

    private record Shot(float rotaion, float powerpercent, boolean hit, Target.TargetPosition targetPosition) {
    }

    private final LinkedList<Shot> shotsFired = new LinkedList<>();
    private final int ROLLINGACCURACYINTERVAL = 20;
    private String rollingAccuracyPercent = "0.0";
    private FileHandle shotsDataCSV;
    private boolean randomAutoMode = false;
    private int MAXSECBETWEENSHOTS = 10;
    private float timeSinceLastShot = 0;

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
    private Cloud cloud;

    private float rotation = 45;
    private final PercentValue powerPercent = new PercentValue(60);

    @Override
    public void create() {
        camera = new OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT);
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        camera.update();

        // World etc.
        batch = new SpriteBatch();
        batch.setProjectionMatrix(camera.combined);
        renderer = new Box2DDebugRenderer();
        world = new World(new Vector2(0, -10), false);
        this.contactListener = new ContactListenerImpl();
        world.setContactListener(contactListener);
        font = new BitmapFont(Gdx.files.internal("gjFont02-hd.fnt"), Gdx.files.internal("gjFont02-hd.png"), false);
        font.getData().setScale(0.06f, 0.06f);
        font.setColor(Color.FIREBRICK);

        // Debug options
        renderer.setDrawBodies(true);
        debugSprite = new DebugSprite();

        // Floor
        BodyEditorLoader bodyEditorLoader = new BodyEditorLoader(new FileHandle("assets/cannongame.json"));
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

        //Cloud
        if (cloud == null)
            cloud = new Cloud(new Vector2(WORLD_WIDTH, WORLD_HEIGHT));

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode) {
                    case Input.Keys.LEFT -> rotating = Optional.of(true);
                    case Input.Keys.RIGHT -> rotating = Optional.of(false);
                    case Input.Keys.UP -> loadingPower = Optional.of(true);
                    case Input.Keys.DOWN -> loadingPower = Optional.of(false);
                    case Input.Keys.SPACE -> createNewBullet();

                    case Input.Keys.ESCAPE -> Gdx.app.exit();

                    case Input.Keys.S -> saveShotsDataToCSV();
                    case Input.Keys.R -> randomAutoMode = !randomAutoMode;
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
        bullet = new Bullet(world, cannon.getTopPosition(), cannon.getShootingDirection().scl(0.28f * (45 + powerPercent.getValue() * 0.45f)));
        shotsFired.add(new Shot(rotation, powerPercent.getValue(), false, target != null ? target.getPosition() : Target.TargetPosition.UNDEFINED));
        timeSinceLastShot = 0;
    }

    private void destroyBullet() {
        timeSinceLastShot = 0;
        if (bullet != null) {
            bullet.clear();
            bullet = null;
        }
        calcRollingAccuracy();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(135 / 255f, 206 / 255f, 235 / 255f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        world.step(Gdx.graphics.getDeltaTime(), 6, 2);
        renderer.render(world, camera.combined);

        rotating.ifPresent(bool -> rotation += (bool ? rotation <= 90f ? +0.5f : 0 : rotation >= -20f ? -0.5f : 0));
        loadingPower.ifPresent(bool -> powerPercent.incDec(bool ? Gdx.graphics.getDeltaTime() * KEYPRESSED_CLICKSPEED : Gdx.graphics.getDeltaTime() * -KEYPRESSED_CLICKSPEED));

        if (target == null) {
            target = new Target(world);
        }

        if (contactListener.isTargetHit()) {
            // Last shot is a hit, adapt list of shots.
            shotsFired.set(shotsFired.size() - 1, new Shot(shotsFired.get(shotsFired.size() - 1).rotaion, shotsFired.get(shotsFired.size() - 1).powerpercent, true, shotsFired.get(shotsFired.size() - 1).targetPosition));
            calcRollingAccuracy();

            destroyBullet();
            target.clear();
            target = null;
            contactListener.setTargetHit(false);
        }

        batch.begin();
        if (cloud != null) {
            cloud.drawAndMove(batch);
            if (cloud.getXPosition() < -1.5f * cloud.getXDimension())
                cloud = null;
        } else {
            cloud = new Cloud(new Vector2(WORLD_WIDTH, WORLD_HEIGHT));
        }

        if (target != null) {
            target.draw(batch);
        }
        batch.draw(floorSprite, floor.getPosition().x, floor.getPosition().y, WORLD_WIDTH, WORLD_HEIGHT * 0.063f);
        batch.draw(wallSprite, wall.getPosition().x - 2, wall.getPosition().y - 15, 4, 30);
        if (bullet != null) {
            bullet.draw(batch);
            timeSinceLastShot += Gdx.graphics.getDeltaTime();
        }
        if (bullet != null && (bullet.getPosition().x > WORLD_WIDTH || bullet.getPosition().x < 0 || bullet.getPosition().y < 0))
            destroyBullet();

        cannon.draw(batch, rotation);
        powerBar.draw(batch, powerPercent.getValue());

        font.draw(batch, "Last" + ROLLINGACCURACYINTERVAL + ": " + rollingAccuracyPercent + "%", WORLD_WIDTH - 30, WORLD_HEIGHT - 2);
        font.draw(batch, shotsFired.stream().filter(Shot::hit).count() + "/" + shotsFired.size(), WORLD_WIDTH - 30, WORLD_HEIGHT - 5);

        if (randomAutoMode && (bullet == null || timeSinceLastShot >= MAXSECBETWEENSHOTS)) {
            rotation = (float) new Random().nextInt(221) / 2f - 20;
            powerPercent.setValue((float) new Random().nextInt(201) / 2f);
            cannon.draw(batch, rotation);
            powerBar.draw(batch, powerPercent.getValue());
            createNewBullet();
        }

        //debugSprite.draw(batch, target.getPosition());

        batch.end();
    }

    private void calcRollingAccuracy() {
        // Calculate rolling accuracy
        Iterator<Shot> reversedIt = new LinkedList<Shot>(shotsFired.subList(Math.max(0, shotsFired.size() - ROLLINGACCURACYINTERVAL), shotsFired.size())).descendingIterator();
        int count = 0;
        float rollingAcc = 0;
        while (reversedIt.hasNext()) {
            rollingAcc += (reversedIt.next()).hit ? 1 : 0;
            count++;
        }
        rollingAccuracyPercent = String.format("%3.1f", (float) rollingAcc / (float) count * 100f);
    }

    private void saveShotsDataToCSV() {
        Gdx.app.log("", "Saving CSV data.");
        shotsDataCSV = Gdx.files.local("shotsData.csv");
        shotsDataCSV.writeString("ROTATION,POWER,HIT,TARGET_POS\r\n", false);
        shotsFired.forEach(shot -> shotsDataCSV.writeString(shot.rotaion + "," + shot.powerpercent + "," + shot.hit + "," + shot.targetPosition + "\r\n", true));
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
