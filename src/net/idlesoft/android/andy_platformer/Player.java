package net.idlesoft.android.andy_platformer;

public class Player extends GLQuad {
    public int lives = 3;

    public Player(World world, int width, int height, int texStart, int texEnd) {
        super(world, width, height, texStart, texEnd);
    }
}
