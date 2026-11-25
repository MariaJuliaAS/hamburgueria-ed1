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
    private Texture fundoRestaurante;
    private Texture spritePapelPedido;
    private Texture[] spritesIngredientes;
    private Texture[][] spritesClientes;

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
        fonte.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        carregarSpritesCenario();
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
        desenharFundo();
        desenharPapeisPedidos();
        desenharSpritesClientes();
        desenharFormas();
        desenharSpritesIngredientes();
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

        formas.end();
        formas.begin(ShapeRenderer.ShapeType.Filled);

        int posicao = 0;
        for (No<Cliente> no = partida.getFila().getNoInicio(); no != null; no = no.getProximo()) {
            Cliente cliente = no.getDado();
            float x = cliente.getTelaX();
            float y = cliente.getTelaY();
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
            desenharBarra(x, y + CLIENTE_ALTURA + 10f, CLIENTE_LARGURA, 8f,
                    pedido.getCliente().getPercentualPaciencia());
            esperando++;
        }

        // A pilha e os ícones dos ingredientes são desenhados com sprites, não retângulos.
        Pilha<Ingrediente> montagem = partida.getCozinha().getMontagem();
        formas.end();
    }


    /** Carrega o fundo estático do restaurante. Personagens, pedidos e montagem ficam em camadas separadas. */
    private void carregarSpritesCenario() {
        fundoRestaurante = new Texture(Gdx.files.internal("sprites/background_restaurante.png"));
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
        batch.draw(fundoRestaurante, 0f, 0f, LARGURA_MUNDO, ALTURA_MUNDO);
        batch.end();
    }


    /** Desenha cada pedido em um pergaminho separado, lado a lado. O mais novo fica à direita. */
    private void desenharPapeisPedidos() {
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

    /** Desenha os ingredientes montados e os ícones da legenda com sprites pixel art. */
    private void desenharSpritesIngredientes() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        Pilha<Ingrediente> montagem = partida.getCozinha().getMontagem();
        int camada = montagem.tamanho() - 1;
        for (No<Ingrediente> no = montagem.getNoTopo(); no != null; no = no.getProximo()) {
            Texture sprite = spritesIngredientes[no.getDado().ordinal()];
            batch.draw(sprite, PILHA_X + 56f, PILHA_Y + camada * 32f, 88f, 32f);
            camada--;
        }

        Ingrediente[] todos = Ingrediente.values();
        int linha = 0;
        for (int i = 0; i < todos.length; i++) {
            if (partida.isDesbloqueado(todos[i])) {
                batch.draw(spritesIngredientes[i], LEGENDA_X, LEGENDA_TOPO - 15f - linha * 20f, 18f, 18f);
                linha++;
            }
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

    private void desenharTextos() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        fonte.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        fonte.draw(batch, "Pontos: " + partida.getPontos()
                + "    Vidas: " + partida.getVidas()
                + "    Moedas: " + partida.getMoedas()
                + "    Dia: " + partida.getNivel()
                + " (" + partida.getEntreguesNoNivel() + "/" + partida.getMetaEntregas() + " entregas)", 20f, 630f);
        fonte.draw(batch, "ENTER atende | 1-9 ingredientes | BACKSPACE desfaz | ESPACO entrega"
                + " | F pesquisa | ESC menu", 20f, 608f);
        if (campo.isAtivo()) {
            fonte.draw(batch, "Pesquisando... (jogo pausado)", FILA_X, 40f);
        } else if (!resultadoPesquisa.isEmpty()) {
            fonte.setColor(0.6f, 0.9f, 1f, 1f);
            fonte.draw(batch, resultadoPesquisa, FILA_X, 40f);
            fonte.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        }
        fonte.draw(batch, "Fila de clientes", FILA_X, 440f);
        fonte.draw(batch, "Esperando o lanche", FILA_X, 590f);

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
            fonte.setColor(com.badlogic.gdx.graphics.Color.WHITE);
            indicePedido++;
        }
        fonte.getData().setScale(escalaAnterior);

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
        if (fundoRestaurante != null) fundoRestaurante.dispose();
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
        spritePapelPedido = null;
        spritesIngredientes = null;
        spritesClientes = null;
        batch = null;
        formas = null;
        fonte = null;
    }
}
