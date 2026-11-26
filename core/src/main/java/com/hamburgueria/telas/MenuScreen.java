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
import com.hamburgueria.modelo.RegistroRanking;

/**
 * Menu inicial. ENTER ou ESPAÇO inicia uma nova partida.
 * Mostra os 5 melhores do ranking; B pesquisa uma pontuação (busca binária no ranking ordenado).
 */
public class MenuScreen extends ScreenAdapter {

    private static final float LARGURA_MUNDO = 960f;
    private static final float ALTURA_MUNDO = 640f;
    private static final int TAMANHO_TOP = 5;

    private final HamburgueriaGame jogo;
    private final OrthographicCamera camera;
    private final FitViewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer formas;
    private BitmapFont fonte;

    private final RegistroRanking[] ranking;
    private final RegistroRanking[] topo;
    private String resultadoBusca = "";
    private final CampoTexto campo = new CampoTexto();
    private boolean digitandoNome;

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

        ranking = RankingArquivo.carregar();
        // O top 5 mostra só a melhor pontuação de cada jogador; a busca binária usa todas as partidas.
        topo = RankingArquivo.melhores(RankingArquivo.melhoresPorJogador(ranking), TAMANHO_TOP);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(campo);
    }

    @Override
    public void render(float delta) {
        boolean acabou = campo.terminou();
        if (acabou) {
            tratarResultado();
        }
        if (!acabou && !campo.isAtivo()) {
            if (jogo.getNomeJogador() == null) {
                pedirNome(); // primeira vez no menu: pergunta quem vai jogar
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
                pedirPontuacao();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
                pedirNome();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                    || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                jogo.iniciarPartida();
                return;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                Gdx.app.exit();
                return;
            }
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
        fonte.draw(batch, "HAMBURGUERIA", 270f, 620f);
        fonte.draw(batch, "MALUCA", 370f, 560f);
        fonte.getData().setScale(1.4f);
        fonte.setColor(Color.WHITE);
        fonte.draw(batch, "Monte os pedidos antes que os clientes percam a paciencia!", 225f, 490f);
        fonte.setColor(new Color(1f, 0.82f, 0.43f, 1f));
        fonte.getData().setScale(1.8f);
        fonte.draw(batch, "PRESSIONE ENTER PARA JOGAR", 285f, 440f);

        fonte.getData().setScale(1.2f);
        fonte.setColor(new Color(0.6f, 0.9f, 1f, 1f));
        String nome = jogo.getNomeJogador();
        fonte.draw(batch, "Jogador: " + (nome == null ? "-" : nome) + "  (N para trocar)", 350f, 410f);
        fonte.setColor(new Color(1f, 0.82f, 0.43f, 1f));
        fonte.getData().setScale(1.4f);
        fonte.draw(batch, "TOP 5 - MELHOR DE CADA JOGADOR", 305f, 375f);
        fonte.getData().setScale(1.2f);
        fonte.setColor(Color.WHITE);
        if (topo.length == 0) {
            fonte.draw(batch, "Nenhuma partida registrada ainda", 330f, 340f);
        }
        for (int i = 0; i < topo.length; i++) {
            fonte.draw(batch, (i + 1) + ". " + topo[i].getNome() + " - " + topo[i].getPontos() + " pts",
                    360f, 340f - i * 26f);
        }
        fonte.setColor(new Color(0.6f, 0.9f, 1f, 1f));
        fonte.draw(batch, resultadoBusca, 280f, 175f);

        fonte.getData().setScale(1f);
        fonte.setColor(Color.LIGHT_GRAY);
        fonte.draw(batch, "N - Trocar nome    |    B - Pesquisar pontuacao no ranking    |    ESC - Sair", 270f, 70f);
        batch.end();

        campo.desenhar(camera.combined, formas, batch, fonte, delta);
    }

    /** Abre a caixa de texto para digitar o nome do jogador. */
    private void pedirNome() {
        digitandoNome = true;
        campo.abrir("Nome do jogador", jogo.getNomeJogador(), 12, false);
    }

    /** Abre a caixa de texto para digitar uma pontuação. */
    private void pedirPontuacao() {
        digitandoNome = false;
        campo.abrir("Pesquisar pontuacao no ranking", "", 9, true);
    }

    /** Chamado quando o jogador confirmou ou cancelou a caixa de texto. */
    private void tratarResultado() {
        String texto = campo.getResultado();
        if (digitandoNome) {
            if (texto != null && !texto.trim().isEmpty()) {
                jogo.setNomeJogador(texto);
            } else if (jogo.getNomeJogador() == null) {
                jogo.setNomeJogador("Jogador");
            }
        } else if (texto != null && !texto.trim().isEmpty()) {
            buscarPontuacao(texto.trim());
        }
    }

    /** Roda a busca binária no ranking ordenado. */
    private void buscarPontuacao(String texto) {
        int pontos;
        try {
            pontos = Integer.parseInt(texto);
        } catch (NumberFormatException erro) {
            resultadoBusca = "Digite apenas numeros inteiros";
            return;
        }
        int posicao = RankingArquivo.buscarPontuacao(ranking, pontos);
        if (posicao == -1) {
            resultadoBusca = "Pontuacao " + pontos + " nao encontrada no ranking";
        } else {
            resultadoBusca = "Pontuacao " + pontos + " encontrada: " + posicao + "o lugar ("
                    + ranking[posicao - 1].getNome() + ")";
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
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
