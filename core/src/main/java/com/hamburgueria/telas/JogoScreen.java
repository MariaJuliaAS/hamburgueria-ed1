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
    private static final float ESPERA_Y = 470f;
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

    // Mesma ordem do enum Ingrediente: PAO, CARNE, QUEIJO, ALFACE, TOMATE, CEBOLA, PICLES, BACON, OVO.
    private static final Color[] CORES = {
            new Color(0.85f, 0.62f, 0.28f, 1f),
            new Color(0.40f, 0.22f, 0.12f, 1f),
            new Color(0.98f, 0.80f, 0.20f, 1f),
            new Color(0.40f, 0.70f, 0.25f, 1f),
            new Color(0.85f, 0.20f, 0.18f, 1f),
            new Color(0.80f, 0.65f, 0.85f, 1f),
            new Color(0.25f, 0.50f, 0.30f, 1f),
            new Color(0.72f, 0.30f, 0.25f, 1f),
            new Color(0.97f, 0.95f, 0.80f, 1f)
    };
    private static final int[] TECLAS_INGREDIENTES = {
            Input.Keys.NUM_1, Input.Keys.NUM_2, Input.Keys.NUM_3, Input.Keys.NUM_4, Input.Keys.NUM_5,
            Input.Keys.NUM_6, Input.Keys.NUM_7, Input.Keys.NUM_8, Input.Keys.NUM_9
    };
    private static final float LEGENDA_X = 780f;
    private static final float VELOCIDADE_CLIENTE = 6f;
    private static final float LEGENDA_TOPO = 248f;

    /** Cor usada para desenhar o ingrediente (também na loja). */
    public static Color corDoIngrediente(Ingrediente ingrediente) {
        return CORES[ingrediente.ordinal()];
    }

    private final HamburgueriaGame jogo;
    private final Partida partida;
    private final Cenario cenario;
    private final OrthographicCamera camera;
    private final FitViewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer formas;
    private BitmapFont fonte;

    private String resultadoPesquisa = "";
    private final CampoTexto campo = new CampoTexto();

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
        if (partida.isNivelConcluido()) {
            jogo.mostrarLoja(partida);
            return;
        }

        tratarEntrada();
        // A partida fica parada enquanto o jogador digita o nome da pesquisa.
        if (!campo.isAtivo()) {
            partida.atualizar(Math.min(delta, 0.1f));
        }

        if (partida.estaEmFimDeJogo()) {
            jogo.mostrarGameOver(partida);
            return;
        }
        if (partida.isNivelConcluido()) {
            jogo.mostrarLoja(partida);
            return;
        }

        animarClientes(delta);
        ScreenUtils.clear(0.13f, 0.14f, 0.18f, 1f);
        desenharFormas();
        desenharTextos();
    }

    /** Faz cada cliente andar até o lugar dele: o slot na fila ou o ponto de espera do lanche. */
    private void animarClientes(float delta) {
        int posicao = 0;
        for (No<Cliente> no = partida.getFila().getNoInicio(); no != null; no = no.getProximo()) {
            moverPara(no.getDado(), FILA_X + posicao * CLIENTE_ESPACO, FILA_Y, delta);
            posicao++;
        }
        int esperando = 0;
        for (No<Pedido> no = partida.getPedidosAtivos().getInicio(); no != null; no = no.getProximo()) {
            moverPara(no.getDado().getCliente(), FILA_X + esperando * CLIENTE_ESPACO, ESPERA_Y, delta);
            esperando++;
        }
    }

    private void moverPara(Cliente cliente, float alvoX, float alvoY, float delta) {
        if (!cliente.temPosicaoNaTela()) {
            cliente.setPosicaoNaTela(LARGURA_MUNDO + CLIENTE_LARGURA, alvoY); // chega pela direita
        }
        float passo = Math.min(1f, VELOCIDADE_CLIENTE * delta);
        cliente.setPosicaoNaTela(cliente.getTelaX() + (alvoX - cliente.getTelaX()) * passo,
                cliente.getTelaY() + (alvoY - cliente.getTelaY()) * passo);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(campo);
    }

    private void tratarEntrada() {
        boolean acabou = campo.terminou();
        if (acabou) {
            String texto = campo.getResultado();
            if (texto != null && !texto.trim().isEmpty()) {
                resultadoPesquisa = partida.pesquisar(texto);
            }
        }
        if (acabou || campo.isAtivo()) {
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            jogo.mostrarMenu();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            campo.abrir("Pesquisar cliente pelo nome", "", 12, false);
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
            float x = cliente.getTelaX();
            float y = cliente.getTelaY();
            formas.setColor(0.92f, 0.76f, 0.62f, 1f);
            formas.rect(x, y, CLIENTE_LARGURA, CLIENTE_ALTURA);
            desenharBarra(x, y + CLIENTE_ALTURA + 10f, CLIENTE_LARGURA, 8f,
                    cliente.getPercentualPaciencia());
            posicao++;
        }

        // Quem já fez o pedido espera aqui, ao lado da fila, com a paciência ainda correndo.
        int esperando = 0;
        for (No<Pedido> no = partida.getPedidosAtivos().getInicio(); no != null; no = no.getProximo()) {
            Pedido pedido = no.getDado();
            float x = pedido.getCliente().getTelaX();
            float y = pedido.getCliente().getTelaY();
            formas.setColor(pedido == partida.getPedidoEmPreparo()
                    ? new Color(0.95f, 0.85f, 0.45f, 1f)
                    : new Color(0.75f, 0.85f, 0.95f, 1f));
            formas.rect(x, y, CLIENTE_LARGURA, CLIENTE_ALTURA);
            desenharBarra(x, y + CLIENTE_ALTURA + 10f, CLIENTE_LARGURA, 8f,
                    pedido.getCliente().getPercentualPaciencia());
            esperando++;
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

        // Legenda: só os ingredientes já liberados, com a tecla de cada um.
        Ingrediente[] todos = Ingrediente.values();
        int linha = 0;
        for (int i = 0; i < todos.length; i++) {
            if (partida.isDesbloqueado(todos[i])) {
                formas.setColor(CORES[i]);
                formas.rect(LEGENDA_X, LEGENDA_TOPO - 12f - linha * 20f, 14f, 14f);
                linha++;
            }
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
                + "    Moedas: " + partida.getMoedas()
                + "    Nivel: " + partida.getNivel()
                + " (" + partida.getEntreguesNoNivel() + "/" + partida.getMetaEntregas() + " entregas)", 20f, 630f);
        fonte.draw(batch, "ENTER atende | 1-9 ingredientes | BACKSPACE desfaz | ESPACO entrega"
                + " | F pesquisa | ESC menu", 20f, 608f);
        fonte.draw(batch, "Pedidos ativos (mais urgentes primeiro)", CARTAO_X, 580f);
        if (campo.isAtivo()) {
            fonte.draw(batch, "Pesquisando... (jogo pausado)", FILA_X, 40f);
        } else if (!resultadoPesquisa.isEmpty()) {
            fonte.setColor(0.6f, 0.9f, 1f, 1f);
            fonte.draw(batch, resultadoPesquisa, FILA_X, 40f);
            fonte.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        }
        fonte.draw(batch, "Fila de clientes", FILA_X, 440f);
        fonte.draw(batch, "Esperando o lanche", FILA_X, 590f);

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
            Cliente cliente = no.getDado();
            fonte.draw(batch, cliente.getNome(), cliente.getTelaX(), cliente.getTelaY() - 6f);
            posicao++;
        }

        int aguardando = 0;
        for (No<Pedido> no = partida.getPedidosAtivos().getInicio(); no != null; no = no.getProximo()) {
            Cliente cliente = no.getDado().getCliente();
            fonte.draw(batch, cliente.getNome(), cliente.getTelaX(), cliente.getTelaY() - 6f);
            aguardando++;
        }

        fonte.draw(batch, "Ingredientes", LEGENDA_X, LEGENDA_TOPO + 22f);
        Ingrediente[] todos = Ingrediente.values();
        int linha = 0;
        for (int i = 0; i < todos.length; i++) {
            if (partida.isDesbloqueado(todos[i])) {
                fonte.draw(batch, (i + 1) + " " + todos[i].getNome(), LEGENDA_X + 22f,
                        LEGENDA_TOPO - linha * 20f);
                linha++;
            }
        }

        fonte.draw(batch, "Cozinha", 20f, 280f);
        Pedido emPreparo = partida.getPedidoEmPreparo();
        fonte.draw(batch, emPreparo != null
                ? "Montando: " + emPreparo.getReceita().toString()
                : "Atenda um cliente e comece a montar o lanche", 20f, 258f);
        fonte.draw(batch, "Camadas: " + partida.getCozinha().tamanho(), 20f, 236f);
        fonte.draw(batch, partida.getUltimoEvento(), 20f, 24f);
        batch.end();

        campo.desenhar(camera.combined, formas, batch, fonte, Gdx.graphics.getDeltaTime());
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
