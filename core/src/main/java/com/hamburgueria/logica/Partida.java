package com.hamburgueria.logica;

import com.hamburgueria.estruturas.Fila;
import com.hamburgueria.estruturas.ListaEncadeada;
import com.hamburgueria.estruturas.No;
import com.hamburgueria.modelo.Cliente;
import com.hamburgueria.modelo.Ingrediente;
import com.hamburgueria.modelo.Pedido;
import com.hamburgueria.modelo.Receita;
import com.hamburgueria.modelo.StatusPedido;

import java.util.Random;

/**
 * Regras de uma partida, sem nenhuma dependência do LibGDX (por isso dá para testar no terminal).
 *
 * Fluxo do jogo:
 *  1. Clientes chegam e entram na Fila (o primeiro fica no balcão).
 *  2. O atendente atende o primeiro da fila: nasce um Pedido, que vai para a ListaEncadeada de pedidos ativos.
 *  3. Na cozinha, o jogador monta o lanche empilhando ingredientes (Pilha).
 *  4. Ao entregar, o lanche é conferido com a receita: acerto dá pontos, erro tira pontos.
 *  5. Quem perde a paciência vai embora e o jogador perde uma vida. Sem vidas, o jogo acaba.
 *
 * A dificuldade sobe com o tempo: os clientes chegam mais rápido e têm menos paciência.
 * (java.util.Random só sorteia números; não é uma Collection.)
 */
public class Partida {

    public static final int CAPACIDADE_FILA = 5;
    public static final int MAX_PEDIDOS_ATIVOS = 4;
    public static final int VIDAS_INICIAIS = 3;
    public static final int PENALIDADE_ERRO = 20;

    private static final float PRIMEIRO_CLIENTE = 1f;
    private static final float INTERVALO_INICIAL = 12f;
    private static final float INTERVALO_MINIMO = 4f;
    private static final float DURACAO_NIVEL = 30f;
    private static final float PACIENCIA_BASE = 45f;
    private static final float PACIENCIA_MINIMA = 20f;
    private static final float PACIENCIA_EXTRA_MAXIMA = 10f;

    private static final String[] NOMES = {
            "Ana", "Bia", "Caio", "Davi", "Eva", "Fabi", "Gabi", "Hugo", "Iara", "João"
    };

    private final Receita[] cardapio;
    private final Random sorteio;

    private final Fila<Cliente> fila = new Fila<>();
    private final ListaEncadeada<Pedido> pedidosAtivos = new ListaEncadeada<>();
    private final Cozinha cozinha = new Cozinha();
    private Pedido pedidoEmPreparo;

    private int pontos;
    private int vidas = VIDAS_INICIAIS;
    private int entregues;
    private float tempo;
    private float tempoParaProximoCliente = PRIMEIRO_CLIENTE;
    private boolean fimDeJogo;
    private String ultimoEvento = "Bom trabalho!";

    public Partida(Receita[] cardapio) {
        this(cardapio, System.nanoTime());
    }

    /** A semente fixa permite repetir o mesmo sorteio nos testes. */
    public Partida(Receita[] cardapio, long semente) {
        if (cardapio == null || cardapio.length == 0) {
            throw new IllegalArgumentException("O cardápio precisa de pelo menos uma receita");
        }
        this.cardapio = new Receita[cardapio.length];
        for (int i = 0; i < cardapio.length; i++) {
            this.cardapio[i] = cardapio[i];
        }
        this.sorteio = new Random(semente);
    }

    // ---------------------------------------------------------------- tempo

    /** Faz o jogo andar. O delta é o tempo do frame, em segundos. */
    public void atualizar(float delta) {
        if (fimDeJogo) {
            return;
        }
        tempo += delta;
        atualizarClienteDoBalcao(delta);
        atualizarPedidos(delta);
        atualizarChegadas(delta);
        if (vidas <= 0) {
            fimDeJogo = true;
            ultimoEvento = "Fim de jogo!";
        }
    }

    /** Só quem está no balcão perde paciência enquanto espera ser atendido. */
    private void atualizarClienteDoBalcao(float delta) {
        if (fila.estaVazia()) {
            return;
        }
        Cliente doBalcao = fila.primeiro();
        doBalcao.atualizar(delta);
        if (doBalcao.acabouPaciencia()) {
            fila.desenfileirar();
            perderVida(doBalcao.getNome() + " foi embora sem ser atendido");
        }
    }

    /**
     * Percorre de trás para frente para poder remover da lista durante o laço
     * sem pular nenhum pedido.
     */
    private void atualizarPedidos(float delta) {
        for (int i = pedidosAtivos.tamanho() - 1; i >= 0; i--) {
            Pedido pedido = pedidosAtivos.obter(i);
            pedido.atualizar(delta);
            if (pedido.getStatus() == StatusPedido.CANCELADO) {
                pedidosAtivos.removerPosicao(i);
                if (pedido == pedidoEmPreparo) {
                    pedidoEmPreparo = null;
                    cozinha.limpar();
                }
                perderVida(pedido.getCliente().getNome() + " desistiu do pedido");
            }
        }
    }

    private void atualizarChegadas(float delta) {
        tempoParaProximoCliente -= delta;
        if (tempoParaProximoCliente > 0f) {
            return;
        }
        if (fila.tamanho() < CAPACIDADE_FILA) {
            fila.enfileirar(criarCliente());
        }
        tempoParaProximoCliente = intervaloDeChegada();
    }

    private Cliente criarCliente() {
        String nome = NOMES[sorteio.nextInt(NOMES.length)];
        float base = Math.max(PACIENCIA_MINIMA, PACIENCIA_BASE - getNivel() * 3f);
        float paciencia = base + sorteio.nextFloat() * PACIENCIA_EXTRA_MAXIMA;
        return new Cliente(nome, paciencia);
    }

    private float intervaloDeChegada() {
        return Math.max(INTERVALO_MINIMO, INTERVALO_INICIAL - getNivel() * 1.5f);
    }

    // ----------------------------------------------------------- ações do jogador

    /**
     * Atende o primeiro cliente da fila e cria o pedido dele.
     * Retorna o pedido, ou null se não havia cliente ou a cozinha está lotada.
     */
    public Pedido atenderCliente() {
        if (fimDeJogo || fila.estaVazia()) {
            return null;
        }
        if (pedidosAtivos.tamanho() >= MAX_PEDIDOS_ATIVOS) {
            ultimoEvento = "Cozinha lotada! Entregue um pedido antes";
            return null;
        }
        Cliente cliente = fila.desenfileirar();
        Receita receita = cardapio[sorteio.nextInt(cardapio.length)];
        Pedido pedido = new Pedido(cliente, receita);
        pedidosAtivos.adicionar(pedido);
        ultimoEvento = cliente.getNome() + " pediu " + receita.getNome();
        return pedido;
    }

    /**
     * Coloca um ingrediente no lanche. Se ainda não há pedido em preparo, começa o
     * pedido mais antigo que está aguardando. Retorna false se não deu para adicionar.
     */
    public boolean adicionarIngrediente(Ingrediente ingrediente) {
        if (fimDeJogo) {
            return false;
        }
        if (pedidoEmPreparo == null) {
            Pedido proximo = primeiroPedidoAguardando();
            if (proximo == null) {
                return false;
            }
            proximo.iniciarPreparo();
            pedidoEmPreparo = proximo;
        }
        return cozinha.adicionar(ingrediente);
    }

    /** Tira o último ingrediente colocado. */
    public boolean desfazerIngrediente() {
        return !fimDeJogo && cozinha.desfazer();
    }

    /** Confere o lanche com o pedido e entrega se estiver certo. */
    public ResultadoEntrega entregar() {
        if (fimDeJogo || pedidoEmPreparo == null) {
            return ResultadoEntrega.SEM_PEDIDO;
        }
        if (cozinha.estaVazia()) {
            return ResultadoEntrega.LANCHE_VAZIO;
        }
        Pedido pedido = pedidoEmPreparo;
        if (!pedido.getReceita().confere(cozinha.getMontagem())) {
            cozinha.limpar();
            pontos = Math.max(0, pontos - PENALIDADE_ERRO);
            ultimoEvento = "Lanche errado! -" + PENALIDADE_ERRO + " pontos";
            return ResultadoEntrega.ERRADO;
        }
        pedido.marcarPronto();
        pedido.entregar();
        int ganhos = pedido.calcularPontos();
        pontos += ganhos;
        entregues++;
        pedidosAtivos.remover(pedido);
        pedidoEmPreparo = null;
        cozinha.limpar();
        ultimoEvento = pedido.getCliente().getNome() + " recebeu o lanche! +" + ganhos + " pontos";
        return ResultadoEntrega.ENTREGUE;
    }

    // ------------------------------------------------------------------ apoio

    private Pedido primeiroPedidoAguardando() {
        for (No<Pedido> no = pedidosAtivos.getInicio(); no != null; no = no.getProximo()) {
            if (no.getDado().getStatus() == StatusPedido.AGUARDANDO) {
                return no.getDado();
            }
        }
        return null;
    }

    private void perderVida(String motivo) {
        vidas--;
        ultimoEvento = motivo;
    }

    /** Nível de dificuldade: sobe um a cada 30 segundos. */
    public int getNivel() {
        return (int) (tempo / DURACAO_NIVEL);
    }

    public Fila<Cliente> getFila() {
        return fila;
    }

    public ListaEncadeada<Pedido> getPedidosAtivos() {
        return pedidosAtivos;
    }

    public Cozinha getCozinha() {
        return cozinha;
    }

    public Pedido getPedidoEmPreparo() {
        return pedidoEmPreparo;
    }

    public int getPontos() {
        return pontos;
    }

    public int getVidas() {
        return vidas;
    }

    public int getEntregues() {
        return entregues;
    }

    public float getTempo() {
        return tempo;
    }

    public boolean estaEmFimDeJogo() {
        return fimDeJogo;
    }

    public String getUltimoEvento() {
        return ultimoEvento;
    }
}
