package net.idlesoft.android.andy_platformer;

import android.os.SystemClock;

public class GameLogic extends Thread {
    public World _world;
    public int _sWidth, _sHeight;
    public AndyPlatformer _activity;
    public long moveTime = SystemClock.uptimeMillis();

    public GameLogic(World world, int w, int h, AndyPlatformer activity) {
        _world = world;
        _sWidth = w;
        _sHeight = h;
        _activity = activity;
    }

    public void doLogic() {
        /* Get time difference */
        long deltaTime = SystemClock.uptimeMillis() - moveTime;
        /* Calculate acceleration due to gravity based on time */
        float gravity = 9.81f / 3.0f;
        boolean screenTouched = false;
        boolean playerIsAlive = _world.player.alive;

        /* Check if the player died */
        if (!playerIsAlive) {
            // Reset his position
            _world.player.xPos = 25;
            _world.player.yPos = 30;
            // It's a Miracle!
            _world.player.alive = true;
        }

        /**
         * Input
         */
        if ((_activity.touchX != -1) || (_activity.touchX != -1)) {
            screenTouched = true;
            _activity.touchX = -1;
            _activity.touchY = -1;
        }

        /**
         *  Player Physics
         */
        /* Only change player velocity if he's on the ground */
        if (_world.player.onGround) {
            if (_activity.sensorX != 0) {
                _world.player.xRawVel = _activity.sensorX * 2.0f;
            } else {
                _world.player.xRawVel = 0.0f;
            }
        }
        /* Vertical velocity */
        if (screenTouched && playerIsAlive) {
            if (_world.player.onGround) {
                _world.player.yRawVel = 40.0f + Math.abs((_world.player.xVel * 0.25f));
                _world.player.onGround = false;
            }
            screenTouched = false;
        }

        /* Apply gravity */
        _world.player.yRawVel -= gravity;

        /* Slow player's x velocity down gradually */
        if ((_world.player.xRawVel > 0.0f) && _world.player.onGround) {
            _world.player.xRawVel--;
        } else if ((_world.player.xVel < 0.0f) && _world.player.onGround) {
            _world.player.xRawVel++;
        }

        /* Control velocities based on time */
        _world.player.yVel = _world.player.yRawVel * (deltaTime / 20.0f);
        _world.player.xVel = _world.player.xRawVel * (deltaTime / 20.0f);

        /* Set direction player is facing */
        if (Math.abs(_world.player.xVel) > 1.5f) {
            _world.player.facingForward = (_world.player.xVel > 1.5f);
        }

        /**
         * Move stuff
         */

        _world.player.move(_sWidth, _sHeight);
        moveTime = SystemClock.uptimeMillis();
    }

    public void run() {
        while (!isInterrupted()) {
            if (!_world.paused) {
                _world._lock.lock();
                doLogic();
                _world._lock.unlock();
                if (_activity.done) {
                    return;
                }
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return;
    }

}
