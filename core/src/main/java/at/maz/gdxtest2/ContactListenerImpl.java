package at.maz.gdxtest2;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

public class ContactListenerImpl implements ContactListener {
    private boolean targetHit = false;

    public ContactListenerImpl() {
    }

    @Override
    public void beginContact(Contact contact) {
        if (contact.getFixtureA().getBody().getUserData() instanceof Target || contact.getFixtureB().getBody().getUserData() instanceof Target) {
            targetHit = true;
        }
    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold manifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse contactImpulse) {

    }

    public boolean isTargetHit() {
        return targetHit;
    }

    public void setTargetHit(boolean targetHit) {
        this.targetHit = targetHit;
    }
}
