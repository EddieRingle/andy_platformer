package net.idlesoft.android.andy_platformer;

import android.os.SystemClock;

public class GameLogic extends Thread {
	public World _world;
	public int _sWidth, _sHeight;
	public AndyPlatformer _activity;
	public long moveTime = SystemClock.uptimeMillis();

	public GameLogic(World world, int w, int h, AndyPlatformer activity)
	{
		_world = world;
		_sWidth = w;
		_sHeight = h;
		_activity = activity;
	}

	public void doLogic()
	{
		// Get Input
		if (_activity.touchX != -1 || _activity.touchX != -1) {
			if (_world.player.onGround) {
				_world.player.yRawVel = 15.0f;
				_world.player.onGround = false;
			}
			_activity.touchX = -1;
			_activity.touchY = -1;
		}

		// Only change x velocity if we're on the ground
		if (_world.player.onGround) {
		    if (_activity.sensorX != 0) {
    			_world.player.xRawVel = (float)_activity.sensorX * 1.5f;
    		} else {
    			_world.player.xRawVel = 0.0f;
    		}
		}

		if (_world.player.xRawVel > 0)
			_world.player.facingForward = true;
		else if (_world.player.xRawVel < 0)
			_world.player.facingForward = false;

		// Time-based velocities

		long deltaTime = SystemClock.uptimeMillis() - moveTime;
		_world.player.yVel = _world.player.yRawVel * ((float)deltaTime / 20.0f);
		_world.player.xVel = _world.player.xRawVel * ((float)deltaTime / 20.0f);

		//_world.player.yVel = _world.player.yRawVel;
		//_world.player.xVel = _world.player.xRawVel;

		// Move stuff

		_world.player.move(_sWidth, _sHeight);
		moveTime = SystemClock.uptimeMillis();

		// Physics and what-not

		if (!_world.player.onGround) {
			_world.player.yRawVel--;
		} else if (_world.player.onGround) {
			_world.player.yRawVel = 0.0f;
		}
		if (_world.player.xRawVel > 0.0f && _world.player.onGround) {
			_world.player.xRawVel--;
		} else if (_world.player.xVel < 0.0f && _world.player.onGround) {
			_world.player.xRawVel++;
		}

		// Check if the player died
		if (!_world.player.alive) {
			// Reset his position
			_world.player.xPos = 25;
			_world.player.yPos = 30;
			// It's a Miracle!
			_world.player.alive = true;
		}
	}

	public void run()
	{
		while (!this.isInterrupted()) {
		    if (!_world.paused) {
    			_world._lock.lock();
    			this.doLogic();
    			_world._lock.unlock();
    			if (_activity.done)
    				return;
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
