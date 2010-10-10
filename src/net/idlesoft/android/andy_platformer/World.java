package net.idlesoft.android.andy_platformer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

public class World {
	public Player player;
	public Enemy enemy;
	public Background background;
	public Platform[] platforms;
	public ReentrantLock _lock;
	public int[] texDrawableIDs = {
			// Andy drawables
			R.drawable.andy_skate_left,
			R.drawable.andy_skate_right,
			R.drawable.andy_jump_left,
			R.drawable.andy_jump_right,
			// Enemy drawables
			R.drawable.enemy1,
			// Background drawables
			R.drawable.bg,
			// Platform drawables
			R.drawable.gray_bricks
	};
	public int[] textures;

	public World(Activity act)
	{
		player = new Player(this, 50, 50, 0, 3);
		player.xPos = 25;
		player.yPos = 30;
		enemy = new Enemy(this, 50, 50, 4, 4);
		enemy.xPos = 778;
		enemy.yPos = 30;
		background = new Background(this, 2400, 400, 5, 5);
		int charWidth, charHeight, charXPos, charYPos;
		try {
			BufferedReader mapReader = new BufferedReader(new InputStreamReader(act.getAssets().open("level1.map")));
			String mapLine = mapReader.readLine();
			// First line defines how many platforms we have
			int platformCount = Integer.parseInt(mapLine);
			// Initialize array of Platforms
			platforms = new Platform[platformCount];
			// Read the next line
			mapLine = mapReader.readLine();
			int count = 0;
			while (mapLine != null && count < platformCount) {
				String[] lineChars = mapLine.split(",");
				int len = lineChars.length;
				int[] lineInts = new int[len];
				for (int i = 0; i < len; i++)
					lineInts[i] = Integer.parseInt(lineChars[i]);
				// get width
				charWidth = lineInts[1];
				// get height
				charHeight = lineInts[2];
				// get x position
				charXPos = lineInts[3];
				// get Y position
				charYPos = lineInts[4];
				// create the platform object
				platforms[count] = new Platform(this, charWidth, charHeight, lineInts[0], lineInts[0]);
				platforms[count].xPos = charXPos;
				platforms[count].yPos = charYPos;
				count++;
				// Read another line
				mapLine = mapReader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		_lock = new ReentrantLock();
	}

	public void loadGLTextures(GL10 gl, Context context)
	{
		// Get length of drawable ID array
		int len = texDrawableIDs.length;

		textures = new int[len];

		// Generate enough texture names for all the drawable IDs
		gl.glGenTextures(len, textures, 0);

		InputStream is;
		Bitmap bitmap;
		for (int i = 0; i < len; i++) {
			// Get drawable
			is = context.getResources().openRawResource(texDrawableIDs[i]);
			bitmap = null;
			try {
				bitmap = BitmapFactory.decodeStream(is);
			} finally {
				try {
					is.close();
					is = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// Bind texture
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[i]);

			// Set some texture parameters
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);

			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);;

			// Clean up
			bitmap.recycle();
		}
	}
}
