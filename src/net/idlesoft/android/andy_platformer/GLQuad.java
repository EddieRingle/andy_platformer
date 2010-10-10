package net.idlesoft.android.andy_platformer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class GLQuad {
    public FloatBuffer m_vertexBuffer, m_textureBuffer;
    public ByteBuffer m_indexBuffer;
    public int width, height, texStart, texEnd;
    public float xPos, yPos, xRawVel, yRawVel, xVel, yVel;
    public boolean onGround = false;
    public World _world;
    public boolean facingForward = true;
    public boolean alive = true;
    public boolean visible = true;

    public GLQuad(World world, int w, int h, int texS, int texE) {
        _world = world;
        width = w;
        height = h;
        texStart = texS;
        texEnd = texE;
        xPos = 0.0f;
        yPos = 0.0f;
        xVel = 0.0f;
        yVel = 0.0f;
        xRawVel = 0.0f;
        yRawVel = 0.0f;

        float vertices[] = { 0.0f, 0.0f, 0.0f, // bottom-left
                w, 0.0f, 0.0f, // bottom-right
                0.0f, h, 0.0f, // top-left
                w, h, 0.0f, // top-left
        };

        float texture[] = { 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f };

        byte indices[] = { 0, 1, 3, 0, 3, 2 };

        ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        m_vertexBuffer = byteBuf.asFloatBuffer();
        m_vertexBuffer.put(vertices);
        m_vertexBuffer.position(0);

        byteBuf = ByteBuffer.allocateDirect(texture.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        m_textureBuffer = byteBuf.asFloatBuffer();
        m_textureBuffer.put(texture);
        m_textureBuffer.position(0);

        m_indexBuffer = ByteBuffer.allocateDirect(indices.length);
        m_indexBuffer.put(indices);
        m_indexBuffer.position(0);
    }

    public void move(int w, int h) {
        int platformsLen = _world.platforms.length;
        xPos += xVel;
        if ((xPos < 0.0f) || ((xPos + width) > 2400.0f)) {
            if (xPos < 0.0f) {
                xPos = 0;
            } else {
                xPos -= xVel;
            }
            xRawVel = 0.0f;
        }
        for (int i = 0; i < platformsLen; i++) {
            if ((xPos < _world.platforms[i].xPos + _world.platforms[i].width)
                    && (xPos + width > _world.platforms[i].xPos)) {
                if ((yPos < _world.platforms[i].yPos + _world.platforms[i].height)
                        && (yPos + height > _world.platforms[i].yPos)) {
                    xPos -= xVel;
                }
            }
        }
        yPos += yVel;
        if (yPos + height < 0.0f) {
            alive = false;
        }
        for (int i = 0; i < platformsLen; i++) {
            if ((yPos < _world.platforms[i].yPos + _world.platforms[i].height)
                    && (yPos + height > _world.platforms[i].yPos)) {
                if ((xPos < _world.platforms[i].xPos + _world.platforms[i].width)
                        && (xPos + width > _world.platforms[i].xPos)) {
                    yPos -= yVel;
                    if (yPos >= _world.platforms[i].yPos + _world.platforms[i].height) {
                        onGround = true;
                        yPos = _world.platforms[i].yPos + _world.platforms[i].height + 1;
                        yRawVel = 0.0f;
                    } else {
                        onGround = false;
                    }
                }
            }
        }
        for (int i = 0; i < platformsLen; i++) {
            if (yPos == _world.platforms[i].yPos + _world.platforms[i].height + 1) {
                if ((xPos <= _world.platforms[i].xPos + _world.platforms[i].width)
                        && (xPos + width >= _world.platforms[i].xPos)) {
                    onGround = true;
                } else {
                    onGround = false;
                }
                if (onGround) {
                    break;
                }
            }
        }
        /*
         * for (int i = 0; i < platformsLen; i++) { if (xPos + this.width > _world.platforms[i].xPos
         * && xPos < _world.platforms[i].xPos + _world.platforms[i].width) { if (yPos + this.height
         * > _world.platforms[i].yPos && yPos < _world.platforms[i].yPos +
         * _world.platforms[i].height) { xPos -= xVel; xRawVel = 0.0f; } } if (xPos + this.width >
         * _world.platforms[i].xPos && xPos < _world.platforms[i].xPos + _world.platforms[i].width)
         * { if (yPos + this.height > _world.platforms[i].yPos && yPos < _world.platforms[i].yPos +
         * _world.platforms[i].height) { yPos -= yVel; if (yPos >= _world.platforms[i].yPos +
         * _world.platforms[i].height) { onGround = true; yPos = _world.platforms[i].yPos +
         * _world.platforms[i].height + 1; yRawVel = 0.0f; } else { onGround = false; } } } if (yPos
         * == _world.platforms[i].yPos + _world.platforms[i].height + 1) { if (xPos <=
         * _world.platforms[i].xPos + _world.platforms[i].width && xPos + width >=
         * _world.platforms[i].xPos) onGround = true; else onGround = false; } }
         */
    }

    public void draw(GL10 gl, int texOffset) {
        gl.glPushMatrix();

        gl.glTranslatef(xPos, yPos, 1.0f);

        gl.glBindTexture(GL10.GL_TEXTURE_2D, _world.textures[(texStart + texOffset)]);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, m_vertexBuffer);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, m_textureBuffer);

        gl.glDrawElements(GL10.GL_TRIANGLES, 6, GL10.GL_UNSIGNED_BYTE, m_indexBuffer);

        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

        gl.glPopMatrix();
    }
}
