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
import com.hamburgueria.estruturas.No;
import com.hamburgueria.estruturas.Pilha;
import com.hamburgueria.logica.Partida;
import com.hamburgueria.modelo.Cliente;
import com.hamburgueria.modelo.Cenario;
import com.hamburgueria.modelo.Tile;
import com.hamburgueria.modelo.Ingrediente;
import com.hamburgueria.modelo.Pedido;

/** Tela principal da partida. A lógica continua concentrada na classe Partida. */
public class JogoScreen extends ScreenAdapter {

    private static final float LARGURA_MUNDO = 960f;
    private static final float ALTURA_MUNDO = 640f;
    private static final float BALCAO_X = 440f;
    private static final float LARGURA_BALCAO = 40f;
    private static final float FILA_X = 510f;
    private static final float FILA_Y = 300f;
    private static final float CLIENTE_LARGURA = 56f;
    private static final float CLIENTE_ALTURA = 80f;
    private static final float CLIENTE_ESPACO = 80f;
    private static final float CARTAO_X = 20f;
    private static final float CARTAO_LARGURA = 400f;
    private static final float CARTAO_ALTURA = 50f;
    private static final float PILHA_X = 170f;
    private static final float PILHA_Y = 50f;
    private static final float CAMADA_LARGURA = 200f;
    private static final float CAMADA_ALTURA = 26f;

    // Mesma ordem do enum Ingrediente: PAO, CARNE, QUEIJO, ALFACE, TOMATE.
    private static final Color[] CORES = {
            new Color(0.85f, 0.62f, 0.28f, 1f),
            new Color(0.40f, 0.22f, 0.12f, 1f),
            new Color(0.98f, 0.80f, 0.20f, 1f),
            new Color(0.40f, 0.70f, 0.25f, 1f),
            new Color(0.85f, 0.20f, 0.18f, 1f)
    };
    private static final int[] TECLAS_INGREDIENTES = {
            Input.Keys.NUM_1, Input.Keys.NUM_2, Input.Keys.NUM_3,
            Input.Keys.NUM_4, Input.Keys.NUM_5
    };

    private final HamburgueriaGame jogo;
    private final Partida partida;
    private final Cenario cenario;
    private final OrthographicCamera camera;
    private final FitViewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer formas;
    private BitmapFont fonte;

    public JogoScreen(HamburgueriaGame jogo, Partida partida) {
        this.jogo = jogo;
        this.partida = partida;
        this.cenario = new Cenario();
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
        if (partida.estaEmFimDeJogo()) {
            jogo.mostrarGameOver(partida);
            return;
        }

        tratarEntrada();
        partida.atualizar(Math.min(delta, 0.1f));

        if (partida.estaEmFimDeJogo()) {
            jogo.mostrarGameOver(partida);
            return;
        }

        ScreenUtils.clear(0.13f, 0.14f, 0.18f, 1f);
        desenharFormas();
        desenharTextos();
    }

    private void tratarEntrada() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            jogo.mostrarMenu();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) partida.atenderCliente();

        Ingrediente[] ingredientes = Ingrediente.values();
        for (int i = 0; i < TECLAS_INGREDIENTES.length && i < ingredientes.length; i++) {
            if (Gdx.input.isKeyJustPressed(TECLAS_INGREDIENTES[i])) {
                partida.adicionarIngrediente(ingredientes[i]);
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) partida.desfazerIngrediente();
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) partida.entregar();
    }

    private void desenharFormas() {
        formas.setProjectionMatrix(camera.combined);
        formas.begin(ShapeRenderer.ShapeType.Filled);

        desenharCenario();

        int posicao = 0;
        for (No<Cliente> no = partida.getFila().getNoInicio(); no != null; no = no.getProximo()) {
            Cliente cliente = no.getDado();
            float x = FILA_X + posicao * CLIENTE_ESPACO;
            formas.setColor(0.92f, 0.76f, 0.62f, 1f);
            formas.rect(x, FILA_Y, CLIENTE_LARGURA, CLIENTE_ALTURA);
            desenharBarra(x, FILA_Y + CLIENTE_ALTURA + 10f, CLIENTE_LARGURA, 8f,
                    cliente.getPercentualPaciencia());
            posicao++;
        }

        int indice = 0;
        for (No<Pedido> no = partida.getPedidosAtivos().getInicio(); no != null; no = no.getProximo()) {
            Pedido pedido = no.getDado();
            float y = cartaoY(indice);
            formas.setColor(pedido == partida.getPedidoEmPreparo()
                    ? new Color(0.38f, 0.32f, 0.14f, 1f)
                    : new Color(0.22f, 0.24f, 0.30f, 1f));
            formas.rect(CARTAO_X, y, CARTAO_LARGURA, CARTAO_ALTURA);
            desenharBarra(CARTAO_X + 8f, y + 6f, CARTAO_LARGURA - 16f, 6f,
                    pedido.getCliente().getPercentualPaciencia());
            indice++;
        }

        formas.setColor(0.80f, 0.80f, 0.80f, 1f);
        formas.rect(PILHA_X - 10f, PILHA_Y - 10f, CAMADA_LARGURA + 20f, 8f);
        Pilha<Ingrediente> montagem = partida.getCozinha().getMontagem();
        int camada = montagem.tamanho() - 1;
        for (No<Ingrediente> no = montagem.getNoTopo(); no != null; no = no.getProximo()) {
            formas.setColor(CORES[no.getDado().ordinal()]);
            formas.rect(PILHA_X, PILHA_Y + camada * CAMADA_ALTURA,
                    CAMADA_LARGURA, CAMADA_ALTURA - 2f);
            camada--;
        }
        formas.end();
    }


    /** Desenha o mapa percorrendo todas as posicoes da matriz Tile[][]. */
    private void desenharCenario() {
        for (int linha = 0; linha < Cenario.LINHAS; linha++) {
            for (int coluna = 0; coluna < Cenario.COLUNAS; coluna++) {
                Tile tile = cenario.getTile(linha, coluna);
                float x = coluna * Cenario.TAMANHO_TILE;
                float y = (Cenario.LINHAS - 1 - linha) * Cenario.TAMANHO_TILE;

                switch (tile) {
                    case BALCAO:
                        formas.setColor(0.48f, 0.27f, 0.14f, 1f);
                        break;
                    case CHAPA:
                        formas.setColor(0.28f, 0.30f, 0.33f, 1f);
                        break;
                    case BANCADA:
                        formas.setColor(0.68f, 0.43f, 0.23f, 1f);
                        break;
                    default:
                        boolean alternado = (linha + coluna) % 2 == 0;
                        formas.setColor(alternado
                                ? new Color(0.20f, 0.22f, 0.24f, 1f)
                                : new Color(0.23f, 0.25f, 0.27f, 1f));
                        break;
                }
                formas.rect(x, y, Cenario.TAMANHO_TILE, Cenario.TAMANHO_TILE);
            }
        }
    }

    private void desenharBarra(float x, float y, float largura, float altura, float percentual) {
        percentual = Math.max(0f, Math.min(1f, percentual));
        formas.setColor(0.30f, 0.30f, 0.30f, 1f);
        formas.rect(x, y, largura, altura);
        if (percentual > 0.5f) formas.setColor(0.35f, 0.75f, 0.35f, 1f);
        else if (percentual > 0.25f) formas.setColor(0.95f, 0.80f, 0.20f, 1f);
        else formas.setColor(0.85f, 0.25f, 0.20f, 1f);
        formas.rect(x, y, largura * percentual, altura);
    }

    private float cartaoY(int indice) {
        return 500f - indice * 60f;
    }

    private void desenharTextos() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        fonte.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        fonte.draw(batch, "Pontos: " + partida.getPontos()
                + "    Vidas: " + partida.getVidas()
                + "    Nivel: " + (partida.getNivel() + 1)
                + "    Tempo: " + (int) partida.getTempo() + "s", 20f, 630f);
        fonte.draw(batch, "ENTER atende | 1 Pao 2 Carne 3 Queijo 4 Alface 5 Tomate"
                + " | BACKSPACE desfaz | ESPACO entrega | ESC menu", 20f, 608f);
        fonte.draw(batch, "Pedidos ativos", CARTAO_X, 580f);
        fonte.draw(batch, "Fila de clientes", FILA_X, 440f);

        int indice = 0;
        for (No<Pedido> no = partida.getPedidosAtivos().getInicio(); no != null; no = no.getProximo()) {
            Pedido pedido = no.getDado();
            float y = cartaoY(indice);
            fonte.draw(batch, "#" + pedido.getId() + " " + pedido.getCliente().getNome()
                    + " - " + pedido.getStatus(), CARTAO_X + 8f, y + 44f);
            fonte.draw(batch, pedido.getReceita().toString(), CARTAO_X + 8f, y + 28f);
            indice++;
        }

        int posicao = 0;
        for (No<Cliente> no = partida.getFila().getNoInicio(); no != null; no = no.getProximo()) {
            fonte.draw(batch, no.getDado().getNome(), FILA_X + posicao * CLIENTE_ESPACO, FILA_Y - 6f);
            posicao++;
        }

        fonte.draw(batch, "Cozinha", 20f, 280f);
        Pedido emPreparo = partida.getPedidoEmPreparo();
        fonte.draw(batch, emPreparo != null
                ? "Montando: " + emPreparo.getReceita().toString()
                : "Atenda um cliente e comece a montar o lanche", 20f, 258f);
        fonte.draw(batch, "Camadas: " + partida.getCozinha().tamanho(), 20f, 236f);
        fonte.draw(batch, partida.getUltimoEvento(), 20f, 24f);
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
