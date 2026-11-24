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
import com.hamburgueria.logica.RankingArquivo;

/** Tela apresentada quando o jogador perde todas as vidas. */
public class GameOverScreen extends ScreenAdapter {

    private static final float LARGURA_MUNDO = 960f;
    private static final float ALTURA_MUNDO = 640f;

    private final HamburgueriaGame jogo;
    private final int pontos;
    private final int entregues;
    private final float tempo;
    private final OrthographicCamera camera;
    private final FitViewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer formas;
    private BitmapFont fonte;
    private boolean solicitouNome;

    public GameOverScreen(HamburgueriaGame jogo, int pontos, int entregues, float tempo) {
        this.jogo = jogo;
        this.pontos = pontos;
        this.entregues = entregues;
        this.tempo = tempo;
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
    public void show() {
        if (solicitouNome) return;
        solicitouNome = true;
        Gdx.input.getTextInput(new com.badlogic.gdx.Input.TextInputListener() {
            @Override
            public void input(String texto) {
                RankingArquivo.salvar(texto, pontos);
            }

            @Override
            public void canceled() {
                RankingArquivo.salvar("Jogador", pontos);
            }
        }, "Salvar pontuacao", "Jogador", "Digite seu nome para o ranking");
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            jogo.iniciarPartida();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            jogo.mostrarMenu();
            return;
        }

        ScreenUtils.clear(0.12f, 0.08f, 0.08f, 1f);
        formas.setProjectionMatrix(camera.combined);
        formas.begin(ShapeRenderer.ShapeType.Filled);
        formas.setColor(new Color(0.35f, 0.19f, 0.10f, 1f));
        formas.rect(0f, 0f, LARGURA_MUNDO, 105f);
        formas.setColor(new Color(0.83f, 0.48f, 0.17f, 1f));
        formas.rect(160f, 105f, 640f, 10f);
        formas.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        fonte.setColor(new Color(1f, 0.45f, 0.35f, 1f));
        fonte.getData().setScale(3f);
        fonte.draw(batch, "FIM DE JOGO", 330f, 445f);
        fonte.getData().setScale(1.5f);
        fonte.setColor(Color.WHITE);
        fonte.draw(batch, "Pontuacao final: " + pontos, 370f, 345f);
        fonte.draw(batch, "Pedidos entregues: " + entregues, 370f, 305f);
        fonte.draw(batch, "Tempo sobrevivido: " + (int) tempo + " segundos", 370f, 265f);
        fonte.setColor(Color.LIGHT_GRAY);
        fonte.draw(batch, "Sua pontuacao sera registrada no ranking.txt", 315f, 225f);
        fonte.setColor(new Color(1f, 0.82f, 0.43f, 1f));
        fonte.draw(batch, "ENTER ou R - Jogar novamente", 335f, 190f);
        fonte.draw(batch, "ESC - Voltar ao menu", 375f, 155f);
        fonte.getData().setScale(1f);
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
