package io.github.animationbasica;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;



public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private FitViewport viewport;


    // sprite del player
    Texture sheet;
    TextureRegion frames[] = new TextureRegion[16];
    TextureRegion[][] grid;
    Animation<TextureRegion> walkDown, walkUp, walkLeft, walkRight;
    TextureRegion idleDown, idleUp, idleLeft, idleRight;
    float stateTime;

    // fondo
    Texture background;
    TextureRegion bgRegion;
    float bgx, bgy;

    // estado del player
    float posx, posy;
    float speed;
    int facing;
    boolean moving;
    final int FACE_DOWN = 0;
    final int FACE_UP = 1;
    final int FACE_LEFT = 2;
    final int FACE_RIGHT = 3;



    @Override
    public void create() {
        batch = new SpriteBatch();
        viewport = new FitViewport(800, 480);

        // fondo con rayas verticales para que se vea que se mueve
        Pixmap bgPixmap = new Pixmap(1024, 512, Pixmap.Format.RGB888);
        bgPixmap.setColor(new Color(0.2f, 0.6f, 0.9f, 1f));
        bgPixmap.fill();
        bgPixmap.setColor(new Color(0.1f, 0.4f, 0.6f, 1f));

        for (int i = 0; i < 20; i++) {
            bgPixmap.fillRectangle(i * 60, 0, 30, 512);
        }

        background = new Texture(bgPixmap);
        background.setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.MirroredRepeat);
        bgRegion = new TextureRegion(background);
        bgPixmap.dispose();

        // cargar el spritesheet del persoaje 4x4
        sheet = new Texture(Gdx.files.internal("character.png"));

        // como cada uno ocupa lo mismo, pillamos dimensiones y dividimos entre la cantidad de sprites
        int tileW = sheet.getWidth() / 4;
        int tileH = sheet.getHeight() / 4;

        // separamos las regiones en una matriz
        grid = TextureRegion.split(sheet, tileW, tileH);

        int index = 0;

        // organizamos las regiones en un array para acceder por indices
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                frames[index++] = grid[i][j];
            }
        }

        // frame quieto de cada direccion
        idleDown = grid[0][0];
        idleUp = grid[1][0];
        idleLeft = grid[2][0];
        idleRight = grid[3][1]; // está mal la posición en el sheet

        // animaciones de caminar por direccion
        walkDown = new Animation<TextureRegion>(0.12f, grid[0][0], grid[0][1], grid[0][2], grid[0][3]);
        walkUp = new Animation<TextureRegion>(0.12f, grid[1][0], grid[1][1], grid[1][2], grid[1][3]);
        walkLeft = new Animation<TextureRegion>(0.12f, grid[2][0], grid[2][1], grid[2][2], grid[2][3]);
        walkRight = new Animation<TextureRegion>(0.12f, grid[3][0], grid[3][1], grid[3][2], grid[3][3]);

        stateTime = 0f;

        posx = 200;
        posy = 100;
        speed = 250f;
        facing = FACE_DOWN;
        moving = false;

        bgx = 0;
        bgy = 0;
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        stateTime += delta; // vamos acumulando tiempo para el loop de animacion

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        // leemos teclas
        boolean moveLeft = Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.LEFT);
        boolean moveRight = Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.RIGHT);
        boolean moveUp = Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.UP);
        boolean moveDown = Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.DOWN);

        // convertimos teclas a movimiento horizontal/vertical
        float moveX = 0f;
        float moveY = 0f;

        if (moveLeft) moveX -= 1f;
        if (moveRight) moveX += 1f;
        if (moveUp) moveY += 1f;
        if (moveDown) moveY -= 1f;

        moving = (moveX != 0f || moveY != 0f);

        // si hay movimiento, actualizamos hacia donde mira
        if (moveX < 0f) {
            facing = FACE_LEFT;
        } else if (moveX > 0f) {
            facing = FACE_RIGHT;
        } else if (moveY > 0f) {
            facing = FACE_UP;
        } else if (moveY < 0f) {
            facing = FACE_DOWN;
        }

        // mover player y fondo (el fondo va mas lento para el efecto)
        posx += moveX * speed * delta;
        posy += moveY * speed * delta;
        bgx += moveX * speed * delta * 0.5f;
        bgy += moveY * speed * delta * 0.5f;


        // movemos la ventana del fondo para dar sensacion de scroll
        bgRegion.setRegion((int) bgx, (int) bgy, (int) viewport.getWorldWidth(), (int) viewport.getWorldHeight());


        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        // seleccionamos el frame para pintar segun el estado dle player
        TextureRegion frame;
        if (moving) {
            if (facing == FACE_DOWN) frame = walkDown.getKeyFrame(stateTime, true);
            else if (facing == FACE_UP) frame = walkUp.getKeyFrame(stateTime, true);
            else if (facing == FACE_LEFT) frame = walkLeft.getKeyFrame(stateTime, true);
            else frame = walkRight.getKeyFrame(stateTime, true);
        } else {
            if (facing == FACE_DOWN) frame = idleDown;
            else if (facing == FACE_UP) frame = idleUp;
            else if (facing == FACE_LEFT) frame = idleLeft;
            else frame = idleRight;
        }

        batch.begin();

        // primero pintamos el fondo y luego el player por encima
        batch.draw(bgRegion, 0, 0);

        batch.draw(frame, posx, posy);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        sheet.dispose();
        background.dispose();
    }
}
