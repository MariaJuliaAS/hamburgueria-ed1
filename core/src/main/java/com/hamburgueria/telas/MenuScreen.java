package com.hamburgueria.telas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.hamburgueria.HamburgueriaGame;

/** Menu inicial. ENTER ou ESPAÇO inicia uma nova partida. */
public class MenuScreen extends ScreenAdapter {

    private static final float LARGURA_MUNDO = 960f;
    private static final float ALTURA_MUNDO = 640f;

    private final HamburgueriaGame jogo;
    private final OrthographicCamera camera;
    private final FitViewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer formas;
    private BitmapFont fonte;

    public MenuScreen(HamburgueriaGame jogo) {
        this.jogo = jogo;
        camera = new OrthographicCamera();
        viewport = new FitViewport(LARGURA_MUNDO, ALTURA_MUNDO, camera);
        viewport.apply();
        camera.position.set(LARGURA_MUNDO / 2f, ALTURA_MUNDO / 2f, 0f);
        camera.update();

        batch = new SpriteBatch();
        formas = new ShapeRenderer();
        fonte = new BitmapFont();
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            jogo.iniciarPartida();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
            return;
        }

        ScreenUtils.clear(0.12f, 0.10f, 0.09f, 1f);
        formas.setProjectionMatrix(camera.combined);
        formas.begin(ShapeRenderer.ShapeType.Filled);
        formas.setColor(new Color(0.35f, 0.19f, 0.10f, 1f));
        formas.rect(0f, 0f, LARGURA_MUNDO, 115f);
        formas.setColor(new Color(0.83f, 0.48f, 0.17f, 1f));
        formas.rect(120f, 112f, 720f, 12f);
        formas.setColor(new Color(0.94f, 0.72f, 0.34f, 1f));
        formas.rect(160f, 124f, 640f, 8f);
        formas.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        fonte.setColor(new Color(1f, 0.82f, 0.43f, 1f));
        fonte.getData().setScale(3f);
        fonte.draw(batch, "HAMBURGUERIA", 270f, 440f);
        fonte.draw(batch, "MALUCA", 370f, 350f);
        fonte.getData().setScale(1.4f);
        fonte.setColor(Color.WHITE);
        fonte.draw(batch, "Monte os pedidos antes que os clientes percam a paciencia!", 225f, 265f);
        fonte.setColor(new Color(1f, 0.82f, 0.43f, 1f));
        fonte.getData().setScale(1.8f);
        fonte.draw(batch, "PRESSIONE ENTER PARA JOGAR", 285f, 190f);
        fonte.getData().setScale(1f);
        fonte.setColor(Color.LIGHT_GRAY);
        fonte.draw(batch, "ESC - Sair", 435f, 70f);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (formas != null) formas.dispose();
        if (fonte != null) fonte.dispose();
        batch = null;
        formas = null;
        fonte = null;
    }
}
