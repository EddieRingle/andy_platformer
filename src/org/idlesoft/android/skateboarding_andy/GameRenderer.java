package org.idlesoft.android.skateboarding_andy;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Paint;
import android.os.SystemClock;
import android.opengl.GLSurfaceView;

import com.google.android.opengles.spritetext.LabelMaker;
import com.google.android.opengles.spritetext.NumericSprite;

public class GameRenderer implements GLSurfaceView.Renderer {
	public Paint m_labelPaint;
	public LabelMaker m_labels;
	public int width, height, fps, frames = 0;
	public int m_labelFPS;
	public int m_msPerFrame;
	public long m_startTime;
	public NumericSprite m_numericSprite;
	public Context m_context;
	public World _world;

	public GameRenderer(Context context, World world)
	{
		m_context = context;
		_world = world;
		m_labelPaint = new Paint();
		m_labelPaint.setTextSize(32.0f);
		m_labelPaint.setAntiAlias(true);
		m_labelPaint.setARGB(0xff, 0xff, 0xff, 0xff);
	}

	public void drawFPS(GL10 gl, float rightMargin)
	{
		long time = SystemClock.uptimeMillis();
		if (m_startTime == 0)
			m_startTime = time;
		if (frames++ == 12) {
			frames = 0;
			long delta = time - m_startTime;
			m_startTime = time;
			m_msPerFrame = (int) (delta * (1.0f / 12.0f));
		}
		if (m_msPerFrame > 0) {
			m_numericSprite.setValue(1000 / m_msPerFrame);
			float numWidth = m_numericSprite.width();
			float x = rightMargin - numWidth;
			m_numericSprite.draw(gl, x, 0, this.width, this.height);
		}
	}

	@Override
	public void onDrawFrame(GL10 gl)
	{
		// Get lock from World
		_world._lock.lock();

		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();

		// Do all the calculations once, here
		float offset = 0.0f;
		float pxPos = _world.player.xPos;
		float pWidth = (float)_world.player.width;
		float halfPWidth = pWidth / 2.0f;
		float halfSWidth = (float)this.width / 2.0f;
		if (pxPos + halfPWidth > halfSWidth && pxPos + halfPWidth < 2400.0f - halfSWidth)
			offset = (pxPos + halfPWidth) - halfSWidth;
		else if (pxPos + halfPWidth >= 2400.0f - halfSWidth)
			offset = 2400.0f - (float)this.width;
		// Move camera
		gl.glTranslatef(-offset, 0.0f, 0.0f);

		//_world.background.draw(gl, 0);

		// Get length of platforms array
		int len = _world.platforms.length;
		// Decide if the platforms are visible and draw them if they are
		for (int i = 0; i < len; i++) {
			if (_world.platforms[i].xPos + _world.platforms[i].width >= offset && _world.platforms[i].xPos <= offset + this.width) {
				_world.platforms[i].visible = true;
				_world.platforms[i].draw(gl, 0);
			} else {
				_world.platforms[i].visible = false;
			}
		}

		gl.glPushMatrix();
		int texOffset = 0;
		if (_world.player.onGround) {
			if (_world.player.facingForward)
				texOffset = 0;
			else
				texOffset = 1;
		} else {
			if (_world.player.facingForward)
				texOffset = 2;
			else
				texOffset = 3;
		}
		_world.player.draw(gl, texOffset);
		gl.glPopMatrix();

		gl.glPushMatrix();
		_world.enemy.draw(gl, 0);
		gl.glPopMatrix();

		m_labels.beginDrawing(gl, this.width, this.height);
		float msPFX = this.width - m_labels.getWidth(m_labelFPS) - 1;
		m_labels.draw(gl, msPFX, 0, m_labelFPS);
		m_labels.endDrawing(gl);

		drawFPS(gl, msPFX);

		// Release lock on World
		_world._lock.unlock();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int w, int h)
	{
		this.width = w;
		this.height = h;

		gl.glViewport(0, 0, w, h);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();

		gl.glOrthof(0.0f, (float)w, 0.0f, (float)h, -1.0f, 1.0f);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config)
	{
		if (m_labels != null)
			m_labels.shutdown(gl);
		else
			m_labels = new LabelMaker(true, 256, 64);
		m_labels.initialize(gl);
		m_labels.beginAdding(gl);
		m_labelFPS = m_labels.add(gl, " FPS", m_labelPaint);
		m_labels.endAdding(gl);

		if (m_numericSprite != null)
			m_numericSprite.shutdown(gl);
		else
			m_numericSprite = new NumericSprite();
		m_numericSprite.initialize(gl, m_labelPaint);

		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glShadeModel(GL10.GL_FLAT);
		gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glDisable(GL10.GL_LIGHTING);
		gl.glDisable(GL10.GL_DITHER);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);

		_world.loadGLTextures(gl, m_context);
	}
}
