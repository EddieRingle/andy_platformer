package net.idlesoft.android.andy_platformer;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;

public class GameView extends GLSurfaceView {
    public GameView(Context context) {
        super(context);
        setEGLConfigChooser(8, 8, 8, 8, 0, 0);
        getHolder().setFormat(PixelFormat.RGBA_8888);
    }
}
