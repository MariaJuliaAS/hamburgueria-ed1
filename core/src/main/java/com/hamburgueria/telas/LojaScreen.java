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
import com.hamburgueria.logica.Loja;
import com.hamburgueria.logica.Partida;
import com.hamburgueria.logica.ResultadoCompra;
import com.hamburgueria.modelo.Ingrediente;

/** Loja entre os níveis. Teclas 1 a 4 compram; ENTER começa o próximo nível. */
public class LojaScreen extends ScreenAdapter {

    private static final float LARGURA_MUNDO = 960f;
    private static final float ALTURA_MUNDO = 640f;
    private static final float LINHA_X = 180f;
    private static final float LINHA_LARGURA = 600f;
    private static final float LINHA_ALTURA = 48f;

    private static final int[] TECLAS = {Input.Keys.NUM_1, Input.Keys.NUM_2, Input.Keys.NUM_3, Input.Keys.NUM_4};

    private final HamburgueriaGame jogo;
    private final Partida partida;
    private final OrthographicCamera camera;
    private final FitViewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer formas;
    private BitmapFont fonte;

    private Ingrediente[] itens;
    private String mensagem = "";

    public LojaScreen(HamburgueriaGame jogo, Partida partida) {
        this.jogo = jogo;
        this.partida = partida;
        camera = new OrthographicCamera();
        viewport = new FitViewport(LARGURA_MUNDO, ALTURA_MUNDO, camera);
        viewport.apply();
        camera.position.set(LARGURA_MUNDO / 2f, ALTURA_MUNDO / 2f, 0f);
        camera.update();
        batch = new SpriteBatch();
        formas = new ShapeRenderer();
        fonte = new BitmapFont();
        itens = Loja.itensAVenda(partida);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            jogo.continuarPartida(partida);
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            jogo.mostrarMenu();
            return;
        }
        for (int i = 0; i < TECLAS.length && i < itens.length; i++) {
            if (Gdx.input.isKeyJustPressed(TECLAS[i])) {
                comprar(itens[i]);
                break;
            }
        }

        ScreenUtils.clear(0.12f, 0.10f, 0.09f, 1f);
        formas.setProjectionMatrix(camera.combined);
        formas.begin(ShapeRenderer.ShapeType.Filled);
        formas.setColor(new Color(0.35f, 0.19f, 0.10f, 1f));
        formas.rect(0f, 0f, LARGURA_MUNDO, 90f);
        for (int i = 0; i < itens.length; i++) {
            float y = linhaY(i);
            boolean podePagar = partida.getMoedas() >= itens[i].getPreco();
            formas.setColor(podePagar ? new Color(0.22f, 0.30f, 0.20f, 1f) : new Color(0.25f, 0.20f, 0.20f, 1f));
            formas.rect(LINHA_X, y, LINHA_LARGURA, LINHA_ALTURA);
            formas.setColor(JogoScreen.corDoIngrediente(itens[i]));
            formas.rect(LINHA_X + 70f, y + 12f, 36f, 24f);
        }
        formas.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        fonte.setColor(new Color(1f, 0.82f, 0.43f, 1f));
        fonte.getData().setScale(2.6f);
        fonte.draw(batch, "NIVEL " + partida.getNivel() + " CONCLUIDO!", 250f, 610f);
        fonte.getData().setScale(1.3f);
        fonte.setColor(Color.WHITE);
        fonte.draw(batch, "Recompensa: +" + Partida.BONUS_NIVEL_PONTOS + " pontos e +"
                + partida.getUltimaRecompensaMoedas() + " moedas", 290f, 550f);
        fonte.draw(batch, "Pontos: " + partida.getPontos() + "     Moedas: " + partida.getMoedas(), 340f, 515f);

        fonte.getData().setScale(1.2f);
        fonte.setColor(new Color(1f, 0.82f, 0.43f, 1f));
        fonte.draw(batch, "LOJA - ingredientes novos", LINHA_X, 465f);
        fonte.setColor(Color.WHITE);
        if (itens.length == 0) {
            fonte.draw(batch, "Voce ja comprou todos os ingredientes!", LINHA_X, 420f);
        }
        for (int i = 0; i < itens.length; i++) {
            float y = linhaY(i);
            boolean podePagar = partida.getMoedas() >= itens[i].getPreco();
            fonte.setColor(Color.WHITE);
            fonte.draw(batch, "[" + (i + 1) + "]", LINHA_X + 15f, y + 32f);
            fonte.draw(batch, itens[i].getNome(), LINHA_X + 125f, y + 32f);
            fonte.setColor(podePagar ? new Color(0.6f, 1f, 0.6f, 1f) : new Color(1f, 0.6f, 0.6f, 1f));
            fonte.draw(batch, itens[i].getPreco() + " moedas", LINHA_X + 470f, y + 32f);
        }

        fonte.setColor(new Color(0.6f, 0.9f, 1f, 1f));
        fonte.draw(batch, mensagem, LINHA_X, 140f);
        fonte.getData().setScale(1f);
        fonte.setColor(Color.LIGHT_GRAY);
        fonte.draw(batch, "Proximo: nivel " + (partida.getNivel() + 1) + " - meta de "
                + (Partida.META_BASE + partida.getNivel() * Partida.META_POR_NIVEL)
                + " entregas, receitas de ate " + Math.min(8, 4 + partida.getNivel()) + " ingredientes",
                LINHA_X, 115f);
        fonte.draw(batch, "1-4 compra    |    ENTER - Proximo nivel    |    ESC - Menu", 290f, 50f);
        batch.end();
    }

    private void comprar(Ingrediente ingrediente) {
        ResultadoCompra resultado = Loja.comprar(partida, ingrediente);
        switch (resultado) {
            case COMPRADO:
                mensagem = ingrediente.getNome() + " liberado! Use a tecla " + (ingrediente.ordinal() + 1)
                        + " na cozinha";
                itens = Loja.itensAVenda(partida);
                break;
            case SEM_MOEDAS:
                mensagem = "Moedas insuficientes para " + ingrediente.getNome();
                break;
            default:
                mensagem = "Voce ja tem " + ingrediente.getNome();
                break;
        }
    }

    private float linhaY(int indice) {
        return 400f - indice * (LINHA_ALTURA + 12f);
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
