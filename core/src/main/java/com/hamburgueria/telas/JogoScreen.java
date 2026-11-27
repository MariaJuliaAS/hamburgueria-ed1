package com.hamburgueria.telas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
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
    private static final float FILA_X = 520f;
    private static final float FILA_Y = 250f;
    private static final float ESPERA_Y = 440f;
    private static final float CLIENTE_LARGURA = 70f;
    private static final float CLIENTE_ALTURA = 100f;
    private static final float CLIENTE_ESPACO = 88f;
    private static final float PAPEL_LARGURA = 112f;
    private static final float PAPEL_ALTURA = 165f;
    private static final float PAPEL_X_DIREITA = 838f;
    private static final float PAPEL_ESPACO = 124f;
    private static final float PAPEL_Y = 455f;
    // Estação de montagem inspirada em jogos de montagem de hambúrguer: ingredientes clicáveis
    // à esquerda, área de empilhamento ao centro e ações na parte inferior.
    private static final float SELETOR_X = 38f;
    private static final float SELETOR_Y = 112f;
    private static final float BOTAO_INGREDIENTE = 70f;
    private static final float SELETOR_GAP_X = 12f;
    private static final float SELETOR_GAP_Y = 12f;
    private static final float PILHA_X = 340f;
    private static final float PILHA_Y = 118f;
    private static final float CAMADA_LARGURA = 130f;
    private static final float CAMADA_ALTURA = 38f;
    private static final float AREA_MONTAGEM_X = 305f;
    private static final float AREA_MONTAGEM_Y = 100f;
    private static final float AREA_MONTAGEM_LARGURA = 205f;
    private static final float AREA_MONTAGEM_ALTURA = 310f;
    private static final float BOTAO_DESFAZER_X = 305f;
    private static final float BOTAO_DESFAZER_Y = 42f;
    private static final float BOTAO_ENTREGAR_X = 410f;
    private static final float BOTAO_ACAO_Y = 42f;
    private static final float BOTAO_ACAO_LARGURA = 95f;
    private static final float BOTAO_ACAO_ALTURA = 42f;

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
    private Texture fundoRestaurante;
    private Texture fundoCozinha;
    private Texture hudPainel, hudTrofeu, hudCoracao, hudMoedas, hudCalendario;
    private boolean telaCozinha = false; // Começa na recepção; só entra na montagem após atender.
    private Texture spritePapelPedido;
    private Texture[] spritesIngredientes;
    private Texture[][] spritesClientes;

    private Ingrediente ingredienteSelecionado;
    private Ingrediente ingredientePressionado;
    // Compatibilidade com versões do LibGDX que não possuem isButtonJustReleased().
    private boolean mouseLeftDownAnterior = false;
    private float origemAnimacaoX;
    private float origemAnimacaoY;
    private float tempoAnimacaoIngrediente;
    private boolean animandoIngrediente;

    private String resultadoPesquisa = "";
    private final CampoTexto campo = new CampoTexto();

    // Aviso visual quando um cliente novo entra na fila da recepção.
    private final Array<Cliente> clientesJaNotificados = new Array<>();
    private float tempoNotificacaoCliente = 0f;
    private String textoNotificacaoCliente = "";

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
        fonte.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        carregarSpritesCenario();
        carregarSpritesHud();
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
            detectarChegadaDeClientes(delta);
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
        if (animandoIngrediente) {
            tempoAnimacaoIngrediente += delta;
            if (tempoAnimacaoIngrediente >= 0.18f) animandoIngrediente = false;
        }
        ScreenUtils.clear(0.13f, 0.14f, 0.18f, 1f);
        desenharFundo();
        if (telaCozinha) {
            desenharPapeisPedidos();
            desenharFormas();
            desenharSpritesIngredientes();
        } else {
            desenharSpritesClientes();
            desenharFormas();
        }
        desenharTextos();
        desenharNotificacaoCliente();
    }

    /** Mostra uma notificação sempre que um novo cliente aparece na fila da recepção. */
    private void detectarChegadaDeClientes(float delta) {
        if (tempoNotificacaoCliente > 0f) {
            tempoNotificacaoCliente = Math.max(0f, tempoNotificacaoCliente - delta);
        }

        int novos = 0;
        for (No<Cliente> no = partida.getFila().getNoInicio(); no != null; no = no.getProximo()) {
            Cliente cliente = no.getDado();
            if (!clientesJaNotificados.contains(cliente, true)) {
                clientesJaNotificados.add(cliente);
                novos++;
            }
        }

        if (novos > 0) {
            tempoNotificacaoCliente = 4.2f;
            int totalFila = partida.getFila().tamanho();
            if (novos == 1) {
                textoNotificacaoCliente = "CLIENTE CHEGOU!  Fila: " + totalFila;
            } else {
                textoNotificacaoCliente = novos + " CLIENTES CHEGARAM!  Fila: " + totalFila;
            }
        }
    }

    /** Desenha o aviso em destaque, inclusive quando o jogador estiver na cozinha. */
    private void desenharNotificacaoCliente() {
        if (tempoNotificacaoCliente <= 0f || textoNotificacaoCliente.isEmpty()) return;

        float alpha = tempoNotificacaoCliente < 0.65f ? tempoNotificacaoCliente / 0.65f : 1f;
        float x = 260f;
        float y = 535f;
        float largura = 440f;
        float altura = 58f;

        formas.setProjectionMatrix(camera.combined);
        formas.begin(ShapeRenderer.ShapeType.Filled);
        formas.setColor(0.12f, 0.08f, 0.05f, 0.92f * alpha);
        formas.rect(x - 5f, y - 5f, largura + 10f, altura + 10f);
        formas.setColor(0.95f, 0.66f, 0.20f, alpha);
        formas.rect(x - 2f, y - 2f, largura + 4f, altura + 4f);
        formas.setColor(0.34f, 0.20f, 0.10f, 0.96f * alpha);
        formas.rect(x, y, largura, altura);
        formas.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        fonte.setColor(1f, 0.88f, 0.45f, alpha);
        fonte.draw(batch, "!  " + textoNotificacaoCliente, x + 18f, y + 36f);
        fonte.setColor(Color.WHITE);
        String instrucao = telaCozinha
                ? "Pressione R para ir atender na recepcao"
                : "Pressione ENTER para atender o cliente";
        fonte.draw(batch, instrucao, x + 18f, y + 17f);
        batch.end();
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
        boolean mouseLeftDown = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        boolean mouseLeftSolto = mouseLeftDownAnterior && !mouseLeftDown;
        mouseLeftDownAnterior = mouseLeftDown;
        Vector3 mouse = mouseNoMundo();

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
        // R alterna entre recepção e cozinha, independentemente da tela atual.
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            telaCozinha = !telaCozinha;
            ingredientePressionado = null;
            animandoIngrediente = false;
            return;
        }
        if (!telaCozinha && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            Pedido pedidoAceito = partida.atenderCliente();
            if (pedidoAceito != null) {
                telaCozinha = true;
                ingredienteSelecionado = null;
                ingredientePressionado = null;
                animandoIngrediente = false;
            }
            return;
        }
        if (!telaCozinha) {
            // Na recepção, ESPAÇO ou botão entrega o hambúrguer já montado.
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                if (partida.getCozinha().getMontagem().tamanho() > 0) partida.entregar();
            }
            if (mouseLeftSolto && dentroBotao(mouse.x, mouse.y, BOTAO_ENTREGAR_X, BOTAO_ACAO_Y,
                    BOTAO_ACAO_LARGURA, BOTAO_ACAO_ALTURA)) {
                if (partida.getCozinha().getMontagem().tamanho() > 0) partida.entregar();
            }
            return; // Recepção não permite montar ingredientes.
        }

        Ingrediente[] ingredientes = Ingrediente.values();
        for (int i = 0; i < TECLAS_INGREDIENTES.length && i < ingredientes.length; i++) {
            if (Gdx.input.isKeyJustPressed(TECLAS_INGREDIENTES[i])) {
                adicionarIngredienteComAnimacao(ingredientes[i], centroBotaoX(i), centroBotaoY(i));
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) partida.desfazerIngrediente();
        // SPACE na cozinha apenas leva o jogador de volta à recepção para fazer a entrega.
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            telaCozinha = false;
            ingredientePressionado = null;
            animandoIngrediente = false;
            return;
        }

        // Mouse: clicar num ingrediente seleciona; clicar na pilha o adiciona.
        // Também aceita arrastar um ingrediente e soltá-lo sobre a área do hambúrguer.
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            ingredientePressionado = ingredienteNoBotao(mouse.x, mouse.y);
        }
        if (mouseLeftSolto) {
            if (ingredientePressionado != null) {
                if (dentroAreaMontagem(mouse.x, mouse.y)) {
                    adicionarIngredienteComAnimacao(ingredientePressionado, centroBotaoX(ingredientePressionado.ordinal()),
                            centroBotaoY(ingredientePressionado.ordinal()));
                } else {
                    ingredienteSelecionado = ingredientePressionado;
                    adicionarIngredienteComAnimacao(ingredientePressionado, centroBotaoX(ingredientePressionado.ordinal()),
                            centroBotaoY(ingredientePressionado.ordinal()));
                }
            } else if (dentroAreaMontagem(mouse.x, mouse.y) && ingredienteSelecionado != null) {
                adicionarIngredienteComAnimacao(ingredienteSelecionado, centroBotaoX(ingredienteSelecionado.ordinal()),
                        centroBotaoY(ingredienteSelecionado.ordinal()));
            } else if (dentroBotao(mouse.x, mouse.y, BOTAO_DESFAZER_X, BOTAO_DESFAZER_Y,
                    BOTAO_ACAO_LARGURA, BOTAO_ACAO_ALTURA)) {
                partida.desfazerIngrediente();
            } else if (dentroBotao(mouse.x, mouse.y, BOTAO_ENTREGAR_X, BOTAO_ACAO_Y,
                    BOTAO_ACAO_LARGURA, BOTAO_ACAO_ALTURA)) {
                // O botão na cozinha é para voltar à recepção, não entrega diretamente.
                telaCozinha = false;
                animandoIngrediente = false;
            }
            ingredientePressionado = null;
        }
    }

    private Vector3 mouseNoMundo() {
        Vector3 pos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
        viewport.unproject(pos);
        return pos;
    }

    private Ingrediente ingredienteNoBotao(float x, float y) {
        Ingrediente[] todos = Ingrediente.values();
        for (int i = 0; i < todos.length; i++) {
            float bx = SELETOR_X + (i % 3) * (BOTAO_INGREDIENTE + SELETOR_GAP_X);
            float by = SELETOR_Y + (2 - i / 3) * (BOTAO_INGREDIENTE + SELETOR_GAP_Y);
            if (dentroBotao(x, y, bx, by, BOTAO_INGREDIENTE, BOTAO_INGREDIENTE) && partida.isDesbloqueado(todos[i])) {
                return todos[i];
            }
        }
        return null;
    }

    private float centroBotaoX(int indice) {
        return SELETOR_X + (indice % 3) * (BOTAO_INGREDIENTE + SELETOR_GAP_X) + BOTAO_INGREDIENTE / 2f;
    }

    private float centroBotaoY(int indice) {
        return SELETOR_Y + (2 - indice / 3) * (BOTAO_INGREDIENTE + SELETOR_GAP_Y) + BOTAO_INGREDIENTE / 2f;
    }

    private boolean dentroAreaMontagem(float x, float y) {
        return dentroBotao(x, y, AREA_MONTAGEM_X, AREA_MONTAGEM_Y, AREA_MONTAGEM_LARGURA, AREA_MONTAGEM_ALTURA);
    }

    private boolean dentroBotao(float x, float y, float bx, float by, float largura, float altura) {
        return x >= bx && x <= bx + largura && y >= by && y <= by + altura;
    }

    private void adicionarIngredienteComAnimacao(Ingrediente ingrediente, float origemX, float origemY) {
        if (partida.adicionarIngrediente(ingrediente)) {
            ingredienteSelecionado = ingrediente;
            origemAnimacaoX = origemX;
            origemAnimacaoY = origemY;
            tempoAnimacaoIngrediente = 0f;
            animandoIngrediente = true;
        }
    }

    private void desenharFormas() {
        formas.setProjectionMatrix(camera.combined);
        formas.begin(ShapeRenderer.ShapeType.Filled);
        if (telaCozinha) {
            // Os painéis da montagem só existem na segunda tela (cozinha).
            desenharPainel(25f, 95f, 265f, 275f, new Color(0.28f, 0.18f, 0.12f, 1f),
                    new Color(0.72f, 0.48f, 0.25f, 1f));
            desenharPainel(AREA_MONTAGEM_X, AREA_MONTAGEM_Y, AREA_MONTAGEM_LARGURA, AREA_MONTAGEM_ALTURA,
                    new Color(0.24f, 0.18f, 0.14f, 1f), new Color(0.78f, 0.62f, 0.39f, 1f));
            Ingrediente[] botoes = Ingrediente.values();
            for (int i = 0; i < botoes.length; i++) {
                float bx = SELETOR_X + (i % 3) * (BOTAO_INGREDIENTE + SELETOR_GAP_X);
                float by = SELETOR_Y + (2 - i / 3) * (BOTAO_INGREDIENTE + SELETOR_GAP_Y);
                Color base = partida.isDesbloqueado(botoes[i]) ? new Color(0.83f, 0.71f, 0.48f, 1f)
                        : new Color(0.34f, 0.32f, 0.29f, 1f);
                desenharPainel(bx, by, BOTAO_INGREDIENTE, BOTAO_INGREDIENTE, base,
                        botoes[i] == ingredienteSelecionado ? new Color(0.95f, 0.78f, 0.26f, 1f)
                                : new Color(0.48f, 0.32f, 0.18f, 1f));
            }
            desenharPainel(BOTAO_DESFAZER_X, BOTAO_DESFAZER_Y, BOTAO_ACAO_LARGURA, BOTAO_ACAO_ALTURA,
                    new Color(0.48f, 0.31f, 0.19f, 1f), new Color(0.86f, 0.63f, 0.35f, 1f));
            desenharPainel(BOTAO_ENTREGAR_X, BOTAO_ACAO_Y, BOTAO_ACAO_LARGURA, BOTAO_ACAO_ALTURA,
                    new Color(0.30f, 0.49f, 0.26f, 1f), new Color(0.67f, 0.84f, 0.45f, 1f));
        } else {
            // Botão de entrega aparece na recepção, depois que o hambúrguer foi montado.
            if (partida.getCozinha().getMontagem().tamanho() > 0) {
                desenharPainel(BOTAO_ENTREGAR_X, BOTAO_ACAO_Y, BOTAO_ACAO_LARGURA, BOTAO_ACAO_ALTURA,
                        new Color(0.30f, 0.49f, 0.26f, 1f), new Color(0.67f, 0.84f, 0.45f, 1f));
            }
            // Na recepção, barras de paciência dos clientes na fila.
            for (No<Cliente> no = partida.getFila().getNoInicio(); no != null; no = no.getProximo()) {
                Cliente cliente = no.getDado();
                desenharBarra(cliente.getTelaX(), cliente.getTelaY() + CLIENTE_ALTURA + 10f,
                        CLIENTE_LARGURA, 8f, cliente.getPercentualPaciencia());
            }
        }
        formas.end();
    }

    private void desenharPainel(float x, float y, float largura, float altura, Color preenchimento, Color borda) {
        formas.setColor(new Color(0.12f, 0.09f, 0.07f, 1f));
        formas.rect(x - 4f, y - 4f, largura + 8f, altura + 8f);
        formas.setColor(borda);
        formas.rect(x - 2f, y - 2f, largura + 4f, altura + 4f);
        formas.setColor(preenchimento);
        formas.rect(x, y, largura, altura);
    }

    /** Carrega o fundo estático do restaurante. Personagens, pedidos e montagem ficam em camadas separadas. */
    private void carregarSpritesHud() {
        hudPainel = new Texture(Gdx.files.internal("sprites/hud/painel.png"));
        hudTrofeu = new Texture(Gdx.files.internal("sprites/hud/trofeu.png"));
        hudCoracao = new Texture(Gdx.files.internal("sprites/hud/coracao.png"));
        hudMoedas = new Texture(Gdx.files.internal("sprites/hud/moedas.png"));
        hudCalendario = new Texture(Gdx.files.internal("sprites/hud/calendario.png"));
        Texture[] hud = {hudPainel, hudTrofeu, hudCoracao, hudMoedas, hudCalendario};
        for (Texture textura : hud) {
            textura.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }
    }

    private void carregarSpritesCenario() {
        fundoRestaurante = new Texture(Gdx.files.internal("sprites/background_restaurante.png"));
        fundoCozinha = new Texture(Gdx.files.internal("sprites/background_cozinha.png"));
        fundoCozinha.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        spritePapelPedido = new Texture(Gdx.files.internal("sprites/papel_pedido.png"));
        spritePapelPedido.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        fundoRestaurante.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        String[] arquivos = {"pao", "carne", "queijo", "alface", "tomate", "cebola", "picles", "bacon", "ovo"};
        spritesIngredientes = new Texture[arquivos.length];
        for (int i = 0; i < arquivos.length; i++) {
            spritesIngredientes[i] = new Texture(Gdx.files.internal("sprites/ingredientes/" + arquivos[i] + ".png"));
            spritesIngredientes[i].setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }

        // 3 personagens, cada um com 3 estados: normal, impaciente (laranja) e furioso.
        String[] personagens = {"cliente_1", "cliente_2", "cliente_3"};
        String[] estados = {"normal", "impaciente", "furioso"};
        spritesClientes = new Texture[personagens.length][estados.length];
        for (int p = 0; p < personagens.length; p++) {
            for (int e = 0; e < estados.length; e++) {
                spritesClientes[p][e] = new Texture(Gdx.files.internal(
                        "sprites/clientes/" + personagens[p] + "_" + estados[e] + ".png"));
                spritesClientes[p][e].setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            }
        }
    }

    private void desenharFundo() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(telaCozinha ? fundoCozinha : fundoRestaurante, 0f, 0f, LARGURA_MUNDO, ALTURA_MUNDO);
        batch.end();
    }


    /** Desenha cada pedido em um pergaminho separado, lado a lado. O mais novo fica à direita. */
    private void desenharPapeisPedidos() {
        if (!telaCozinha) return;
        int quantidade = partida.getPedidosAtivos().tamanho();
        if (quantidade == 0) return;
        float passo = PAPEL_ESPACO;
        if (quantidade > 7) passo = Math.min(PAPEL_ESPACO, 808f / (quantidade - 1));
        float largura = quantidade > 7 ? Math.min(PAPEL_LARGURA, passo - 3f) : PAPEL_LARGURA;
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        int indice = 0;
        for (No<Pedido> no = partida.getPedidosAtivos().getInicio(); no != null; no = no.getProximo()) {
            float x = PAPEL_X_DIREITA - (quantidade - 1 - indice) * passo;
            batch.draw(spritePapelPedido, x, PAPEL_Y, largura, PAPEL_ALTURA);
            indice++;
        }
        batch.end();
    }

    /** Desenha os clientes com sprite e altera a expressão/cor conforme a paciência. */
    private void desenharSpritesClientes() {
        if (telaCozinha) return;
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (No<Cliente> no = partida.getFila().getNoInicio(); no != null; no = no.getProximo()) {
            desenharCliente(no.getDado());
        }
        for (No<Pedido> no = partida.getPedidosAtivos().getInicio(); no != null; no = no.getProximo()) {
            desenharCliente(no.getDado().getCliente());
        }
        batch.end();
    }

    private void desenharCliente(Cliente cliente) {
        int personagem = Math.floorMod(cliente.getNome().hashCode(), spritesClientes.length);
        float paciencia = cliente.getPercentualPaciencia();
        int estado = paciencia > 0.5f ? 0 : (paciencia > 0.25f ? 1 : 2);
        Texture sprite = spritesClientes[personagem][estado];
        batch.draw(sprite, cliente.getTelaX(), cliente.getTelaY(), CLIENTE_LARGURA, CLIENTE_ALTURA);
    }

    /** Renderiza a estação: ingredientes clicáveis e o hambúrguer empilhado visualmente. */
    private void desenharSpritesIngredientes() {
        if (!telaCozinha) return;
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        Ingrediente[] todos = Ingrediente.values();
        for (int i = 0; i < todos.length; i++) {
            float bx = SELETOR_X + (i % 3) * (BOTAO_INGREDIENTE + SELETOR_GAP_X);
            float by = SELETOR_Y + (2 - i / 3) * (BOTAO_INGREDIENTE + SELETOR_GAP_Y);
            Texture sprite = spritesIngredientes[i];
            if (!partida.isDesbloqueado(todos[i])) batch.setColor(0.45f, 0.45f, 0.45f, 0.85f);
            else batch.setColor(Color.WHITE);
            batch.draw(sprite, bx + 14f, by + 21f, 42f, 42f);
        }
        batch.setColor(Color.WHITE);

        // Durante o arraste, mostra um ícone seguindo o cursor.
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && ingredientePressionado != null) {
            Vector3 mouse = mouseNoMundo();
            batch.draw(spritesIngredientes[ingredientePressionado.ordinal()], mouse.x - 24f, mouse.y - 24f, 48f, 48f);
        }

        Pilha<Ingrediente> montagem = partida.getCozinha().getMontagem();
        int camada = montagem.tamanho() - 1;
        for (No<Ingrediente> no = montagem.getNoTopo(); no != null; no = no.getProximo()) {
            Texture sprite = spritesIngredientes[no.getDado().ordinal()];
            float destinoX = PILHA_X;
            float destinoY = PILHA_Y + camada * 34f;
            if (animandoIngrediente && camada == montagem.tamanho() - 1) {
                float t = Math.min(1f, tempoAnimacaoIngrediente / 0.18f);
                float suave = t * t * (3f - 2f * t);
                float x = origemAnimacaoX - 75f + (destinoX - (origemAnimacaoX - 75f)) * suave;
                float y = origemAnimacaoY - 20f + (destinoY - (origemAnimacaoY - 20f)) * suave;
                float escala = 0.55f + 0.45f * suave;
                batch.draw(sprite, x, y, CAMADA_LARGURA * escala, CAMADA_ALTURA * escala);
            } else {
                batch.draw(sprite, destinoX, destinoY, CAMADA_LARGURA, CAMADA_ALTURA);
            }
            camada--;
        }
        batch.end();
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

    /** HUD superior em pixel art, seguindo a referência visual enviada. */
    private void desenharHudSuperior() {
        // Quatro placas independentes para manter alinhamento e espaçamento consistentes.
        batch.draw(hudPainel, 18f, 581f, 220f, 46f);
        batch.draw(hudPainel, 241f, 581f, 292f, 46f);
        batch.draw(hudPainel, 536f, 581f, 205f, 46f);
        batch.draw(hudPainel, 744f, 581f, 198f, 46f);

        batch.draw(hudTrofeu, 29f, 587f, 27f, 32f);
        batch.draw(hudCoracao, 252f, 590f, 25f, 25f);
        batch.draw(hudCoracao, 279f, 590f, 25f, 25f);
        batch.draw(hudCoracao, 306f, 590f, 25f, 25f);
        batch.draw(hudCoracao, 333f, 590f, 25f, 25f);
        batch.draw(hudMoedas, 548f, 586f, 31f, 33f);
        batch.draw(hudCalendario, 756f, 586f, 31f, 33f);

        float escala = fonte.getScaleX();
        fonte.getData().setScale(1.05f);
        fonte.setColor(1f, 0.78f, 0.32f, 1f);
        fonte.draw(batch, "PONTOS: " + partida.getPontos(), 61f, 610f);
        fonte.draw(batch, "VIDAS: " + partida.getVidas(), 369f, 610f);
        fonte.draw(batch, "MOEDAS: " + partida.getMoedas(), 584f, 610f);
        fonte.draw(batch, "DIA: " + partida.getNivel() + " (" + partida.getEntreguesNoNivel()
                + "/" + partida.getMetaEntregas() + ")", 791f, 610f);

        fonte.getData().setScale(0.88f);
        fonte.setColor(1f, 1f, 0.94f, 1f);
        String instrucoes = telaCozinha
                ? "COZINHA  |  [R]: recepção  |  1-9 ingredientes  |  [BACKSPACE] - desfaz  |  [ESPAÇO] - ir entregar"
                : "RECEPÇÃO  |  [ENTER]: atender  |  [ESPAÇO/CLIQUE]: entregar pedido pronto  |  [F]: pesquisar  |  [ESC]: menu";
        fonte.draw(batch, instrucoes, 28f, 566f);
        fonte.getData().setScale(escala);
        fonte.setColor(Color.WHITE);
    }

    private void desenharTextos() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        desenharHudSuperior();
        fonte.setColor(Color.WHITE);
        if (!telaCozinha) {
            if (partida.getCozinha().getMontagem().tamanho() > 0) {
                fonte.setColor(new Color(1f, 0.9f, 0.55f, 1f));
                fonte.draw(batch, "HAMBURGUER PRONTO! Entregue ao cliente", 330f, 100f);
                fonte.setColor(Color.WHITE);
                fonte.draw(batch, "SPACE", BOTAO_ENTREGAR_X + 23f, BOTAO_ACAO_Y + 27f);
                fonte.draw(batch, "ENTREGAR", BOTAO_ENTREGAR_X + 15f, BOTAO_ACAO_Y + 12f);
            }
            fonte.draw(batch, "Fila de clientes", FILA_X, 440f);
            if (partida.getFila().estaVazia()) {
                fonte.draw(batch, "Aguardando clientes...", FILA_X, 400f);
            } else {
                fonte.draw(batch, "Pressione ENTER para aceitar o primeiro cliente", 20f, 70f);
            }
        } else {
            int quantidadePedidos = partida.getPedidosAtivos().tamanho();
            int indicePedido = 0;
            float passoPapeis = PAPEL_ESPACO;
            if (quantidadePedidos > 7) passoPapeis = Math.min(PAPEL_ESPACO, 808f / (quantidadePedidos - 1));
            float larguraPapel = quantidadePedidos > 7 ? Math.min(PAPEL_LARGURA, passoPapeis - 3f) : PAPEL_LARGURA;
            float escalaAnterior = fonte.getScaleX();
            fonte.getData().setScale(0.62f);
            GlyphLayout layout = new GlyphLayout();
            for (No<Pedido> no = partida.getPedidosAtivos().getInicio(); no != null; no = no.getProximo()) {
                Pedido pedido = no.getDado();
                float x = PAPEL_X_DIREITA - (quantidadePedidos - 1 - indicePedido) * passoPapeis;
                float y = PAPEL_Y;
                float margem = Math.max(8f, larguraPapel * 0.10f);
                float larguraTexto = larguraPapel - margem * 2f;
                fonte.setColor(0.20f, 0.14f, 0.10f, 1f);
                fonte.draw(batch, "PEDIDO #" + pedido.getId(), x + margem, y + PAPEL_ALTURA - 22f);
                fonte.draw(batch, pedido.getCliente().getNome(), x + margem, y + PAPEL_ALTURA - 36f);
                fonte.draw(batch, pedido.getReceita().getNome(), x + margem, y + PAPEL_ALTURA - 51f);
                fonte.draw(batch, "ITENS:", x + margem, y + PAPEL_ALTURA - 65f);
                StringBuilder ingredientesTexto = new StringBuilder();
                for (int k = 0; k < pedido.getReceita().getQuantidade(); k++) {
                    if (k > 0) ingredientesTexto.append(", ");
                    ingredientesTexto.append(pedido.getReceita().getIngrediente(k).getNome());
                }
                String[] partes = ingredientesTexto.toString().split(", ");
                String linhaTexto = "";
                int linha = 0;
                for (String parte : partes) {
                    String candidata = linhaTexto.isEmpty() ? parte : linhaTexto + ", " + parte;
                    layout.setText(fonte, candidata);
                    if (layout.width > larguraTexto && !linhaTexto.isEmpty()) {
                        fonte.draw(batch, linhaTexto, x + margem, y + PAPEL_ALTURA - 79f - linha * 11f);
                        linhaTexto = parte;
                        linha++;
                    } else linhaTexto = candidata;
                }
                if (!linhaTexto.isEmpty()) fonte.draw(batch, linhaTexto, x + margem, y + PAPEL_ALTURA - 79f - linha * 11f);
                fonte.draw(batch, pedido.getStatus().toString(), x + margem, y + 23f);
                fonte.setColor(Color.WHITE);
                indicePedido++;
            }
            fonte.getData().setScale(escalaAnterior);
            fonte.setColor(new Color(0.98f, 0.88f, 0.65f, 1f));
            fonte.draw(batch, "INGREDIENTES", 42f, 354f);
            fonte.getData().setScale(0.68f);
            Ingrediente[] todos = Ingrediente.values();
            for (int i = 0; i < todos.length; i++) {
                float bx = SELETOR_X + (i % 3) * (BOTAO_INGREDIENTE + SELETOR_GAP_X);
                float by = SELETOR_Y + (2 - i / 3) * (BOTAO_INGREDIENTE + SELETOR_GAP_Y);
                fonte.setColor(partida.isDesbloqueado(todos[i]) ? new Color(0.20f, 0.13f, 0.08f, 1f)
                        : new Color(0.68f, 0.65f, 0.60f, 1f));
                String nome = todos[i].getNome();
                GlyphLayout nomeLayout = new GlyphLayout(fonte, nome);
                fonte.draw(batch, nome, bx + (BOTAO_INGREDIENTE - nomeLayout.width) / 2f, by + 14f);
                fonte.setColor(new Color(0.32f, 0.22f, 0.13f, 1f));
                fonte.draw(batch, String.valueOf(i + 1), bx + 5f, by + BOTAO_INGREDIENTE - 5f);
            }
            fonte.getData().setScale(1f);
            fonte.setColor(new Color(0.98f, 0.88f, 0.65f, 1f));
            fonte.draw(batch, "MONTE SEU HAMBURGUER", AREA_MONTAGEM_X + 14f, AREA_MONTAGEM_Y + AREA_MONTAGEM_ALTURA - 15f);
            fonte.setColor(Color.WHITE);
            fonte.draw(batch, "Clique ou arraste ingrediente", AREA_MONTAGEM_X + 15f, AREA_MONTAGEM_Y + 22f);
            fonte.draw(batch, "BACKSPACE", BOTAO_DESFAZER_X + 12f, BOTAO_DESFAZER_Y + 27f);
            fonte.draw(batch, "DESFAZER", BOTAO_DESFAZER_X + 12f, BOTAO_DESFAZER_Y + 12f);
            fonte.draw(batch, "SPACE", BOTAO_ENTREGAR_X + 23f, BOTAO_ACAO_Y + 27f);
            fonte.draw(batch, "RECEPCAO", BOTAO_ENTREGAR_X + 10f, BOTAO_ACAO_Y + 12f);
            Pedido emPreparo = partida.getPedidoEmPreparo();
            fonte.setColor(new Color(1f, 0.88f, 0.50f, 1f));
            fonte.draw(batch, emPreparo != null ? "Preparando: " + emPreparo.getReceita().getNome()
                    : "Atenda um cliente na recepcao", 315f, 425f);
            fonte.setColor(Color.WHITE);
            fonte.draw(batch, "Camadas: " + partida.getCozinha().tamanho() + "/" + com.hamburgueria.logica.Cozinha.ALTURA_MAXIMA,
                    AREA_MONTAGEM_X + 5f, 405f);
        }
        if (campo.isAtivo()) fonte.draw(batch, "Pesquisando... (jogo pausado)", FILA_X, 40f);
        else if (!resultadoPesquisa.isEmpty()) {
            fonte.setColor(0.6f, 0.9f, 1f, 1f);
            fonte.draw(batch, resultadoPesquisa, FILA_X, 40f);
            fonte.setColor(Color.WHITE);
        }
        fonte.draw(batch, partida.getUltimoEvento(), 20f, 24f);
        if (!telaCozinha) {
            for (No<Cliente> no = partida.getFila().getNoInicio(); no != null; no = no.getProximo()) {
                Cliente cliente = no.getDado();
                fonte.draw(batch, cliente.getNome(), cliente.getTelaX(), cliente.getTelaY() - 6f);
            }
        }
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
        if (fundoRestaurante != null) fundoRestaurante.dispose();
        if (fundoCozinha != null) fundoCozinha.dispose();
        if (hudPainel != null) hudPainel.dispose();
        if (hudTrofeu != null) hudTrofeu.dispose();
        if (hudCoracao != null) hudCoracao.dispose();
        if (hudMoedas != null) hudMoedas.dispose();
        if (hudCalendario != null) hudCalendario.dispose();
        if (spritePapelPedido != null) spritePapelPedido.dispose();
        if (spritesIngredientes != null) {
            for (Texture sprite : spritesIngredientes) if (sprite != null) sprite.dispose();
        }
        if (spritesClientes != null) {
            for (Texture[] grupo : spritesClientes) {
                for (Texture sprite : grupo) if (sprite != null) sprite.dispose();
            }
        }
        fundoRestaurante = null;
        fundoCozinha = null;
        spritePapelPedido = null;
        spritesIngredientes = null;
        spritesClientes = null;
        batch = null;
        formas = null;
        fonte = null;
    }
}
