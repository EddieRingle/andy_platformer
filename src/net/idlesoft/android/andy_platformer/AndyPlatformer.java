package net.idlesoft.android.andy_platformer;

import android.app.Activity;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;

public class AndyPlatformer extends Activity {
    public GameView m_gameView;
    public World _world;
	public GameRenderer _renderer;
	public GameLogic _logic;
    public SensorManager m_sensorManager;
    public int m_sensor = SensorManager.SENSOR_ORIENTATION;
    public long lastUpdate = -1;
    public int touchX = -1, touchY = -1, sensorX = 0;
    public boolean done = false;

    public OnTouchListener onTouch = new OnTouchListener() {
		@Override
		public boolean onTouch(View arg0, MotionEvent arg1)
		{
			if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
				touchX = (int)arg1.getX();
				touchY = (int)arg1.getY();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return false;
		}
	};

	public SensorListener sensorListener = new SensorListener() {
		@Override
		public void onSensorChanged(int sensor, float[] values) {
			float pitch = values[2];
			if (pitch < -3)
				sensorX = (int)(-pitch / 3.0f);
			else if (pitch > 3)
				sensorX = (int)(-pitch / 3.0f);
			else
				sensorX = 0;
		}
		
		@Override
		public void onAccuracyChanged(int sensor, int accuracy) {
		}
	};
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        m_gameView = new GameView(this);
        setContentView(m_gameView);

        m_sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        m_sensorManager.registerListener(sensorListener, m_sensor, SensorManager.SENSOR_DELAY_GAME);

        _world = (World)getLastNonConfigurationInstance();
        if (_world == null) {
            _world = new World(AndyPlatformer.this);
        }

		_renderer = new GameRenderer(getApplicationContext(), _world);

		_logic = new GameLogic(_world, getWindowManager().getDefaultDisplay().getWidth(), getWindowManager().getDefaultDisplay().getHeight(), AndyPlatformer.this);

		m_gameView.setRenderer(_renderer);

		m_gameView.setOnTouchListener(onTouch);

		_logic.start();
    }

    public Object onRetainNonconfigurationInstance() {
        return _world;
    }

    @Override
    public void onResume()
    {
    	super.onResume();
    	System.gc();
    	m_gameView.onResume();
    	this.done = false;
    	_world.paused = false;
    }

    @Override
    public void onPause()
    {
    	super.onPause();
    	m_gameView.onPause();
    	_logic.interrupt();
    	_world.paused = true;
    }
}