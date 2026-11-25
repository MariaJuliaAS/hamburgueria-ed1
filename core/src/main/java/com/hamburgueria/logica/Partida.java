package com.hamburgueria.logica;

import com.hamburgueria.estruturas.Algoritmos;
import com.hamburgueria.estruturas.Comparador;
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
 *  4. Ao entregar, o lanche é conferido com a receita: acerto dá pontos e moedas (com gorjeta), erro tira pontos.
 *  5. Quem perde a paciência vai embora (mesmo depois de ter feito o pedido): o jogador perde uma vida e,
 *     se o pedido já tinha sido feito, também perde pontos.
 *  6. Cada nível tem uma meta de entregas. Ao cumprir a meta o nível termina, o jogador ganha uma recompensa
 *     e pode gastar as moedas na loja (Loja) para liberar ingredientes novos antes do próximo nível.
 *  7. Níveis mais altos têm clientes chegando mais rápido, menos paciência, mais pedidos ao mesmo tempo
 *     e receitas com mais ingredientes.
 * (java.util.Random só sorteia números; não é uma Collection.)
 */
public class Partida {

    public static final int CAPACIDADE_FILA = 5;
    public static final int MAX_PEDIDOS_ATIVOS = 4;
    public static final int VIDAS_INICIAIS = 3;
    public static final int PENALIDADE_ERRO = 20;
    public static final int PENALIDADE_CANCELAMENTO = 30;

    public static final int META_BASE = 4;
    public static final int META_POR_NIVEL = 2;
    public static final int BONUS_NIVEL_PONTOS = 50;
    public static final int BONUS_NIVEL_MOEDAS_BASE = 20;
    public static final int BONUS_NIVEL_MOEDAS_POR_NIVEL = 10;

    private static final float PRIMEIRO_CLIENTE = 1f;
    private static final float INTERVALO_INICIAL = 11f;
    private static final float INTERVALO_MINIMO = 4f;
    private static final float PACIENCIA_BASE = 50f;
    private static final float PACIENCIA_MINIMA = 22f;
    private static final float PACIENCIA_EXTRA_MAXIMA = 10f;

    /** Pedidos mais perto de desistir (menos paciência sobrando) vêm primeiro. */
    private static final Comparador<Pedido> POR_URGENCIA =
            (a, b) -> Float.compare(a.getCliente().getPacienciaAtual(), b.getCliente().getPacienciaAtual());

    private static final String[] NOMES = {
            "Ana", "Bia", "Caio", "Davi", "Eva", "Fabi", "Gabi", "Hugo", "Iara", "João"
    };

    private final Receita[] cardapio;
    private final Random sorteio;
    /** Vetor indexado pela posição (ordinal) do ingrediente: true se já está liberado para uso. */
    private final boolean[] desbloqueados;

    private final Fila<Cliente> fila = new Fila<>();
    private final ListaEncadeada<Pedido> pedidosAtivos = new ListaEncadeada<>();
    private final Cozinha cozinha = new Cozinha();
    private Pedido pedidoEmPreparo;

    private int pontos;
    private int moedas;
    private int vidas = VIDAS_INICIAIS;
    private int entregues;
    private int nivel = 1;
    private int entreguesNoNivel;
    private boolean nivelConcluido;
    private int ultimaRecompensaMoedas;
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

        Ingrediente[] todos = Ingrediente.values();
        this.desbloqueados = new boolean[todos.length];
        for (int i = 0; i < todos.length; i++) {
            desbloqueados[i] = todos[i].getPreco() == 0;
        }
    }

    // ---------------------------------------------------------------- tempo

    /** Faz o jogo andar. O delta é o tempo do frame, em segundos. */
    public void atualizar(float delta) {
        if (fimDeJogo || nivelConcluido) {
            return;
        }
        tempo += delta;
        atualizarClienteDoBalcao(delta);
        atualizarPedidos(delta);
        ordenarPedidosPorUrgencia();
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
                pontos = Math.max(0, pontos - PENALIDADE_CANCELAMENTO);
                perderVida(pedido.getCliente().getNome() + " desistiu do pedido! -"
                        + PENALIDADE_CANCELAMENTO + " pontos");
            }
        }
    }

    /**
     * Insertion Sort na lista de pedidos ativos pela paciência restante. A lista fica quase ordenada
     * de um frame para o outro, que é o melhor caso do Insertion Sort. Serve para mostrar quem está mais perto
     * de desistir; o preparo continua na ordem de chegada (ver primeiroPedidoAguardando).
     */
    public void ordenarPedidosPorUrgencia() {
        Algoritmos.insertionSort(pedidosAtivos, POR_URGENCIA);
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
        float base = Math.max(PACIENCIA_MINIMA, PACIENCIA_BASE - (nivel - 1) * 4f);
        float paciencia = base + sorteio.nextFloat() * PACIENCIA_EXTRA_MAXIMA;
        return new Cliente(nome, paciencia);
    }

    private float intervaloDeChegada() {
        return Math.max(INTERVALO_MINIMO, INTERVALO_INICIAL - (nivel - 1) * 1.5f);
    }

    // ----------------------------------------------------------- ações do jogador

    /**
     * Atende o primeiro cliente da fila e cria o pedido dele.
     * Retorna o pedido, ou null se não havia cliente ou a cozinha está lotada.
     */
    public Pedido atenderCliente() {
        if (fimDeJogo || nivelConcluido || fila.estaVazia()) {
            return null;
        }
        if (pedidosAtivos.tamanho() >= getMaxPedidosAtivos()) {
            ultimoEvento = "Cozinha lotada! Entregue um pedido antes";
            return null;
        }
        Cliente cliente = fila.desenfileirar();
        Receita receita = sortearReceita();
        Pedido pedido = new Pedido(cliente, receita);
        pedidosAtivos.adicionar(pedido);
        ultimoEvento = cliente.getNome() + " pediu " + receita.getNome();
        return pedido;
    }

    /**
     * Sorteia a receita do pedido entre as que o nível permite (quantidade de ingredientes) e cujos
     * ingredientes já foram liberados. Se nenhuma serve, relaxa o limite do nível e, por último, usa o cardápio todo.
     */
    private Receita sortearReceita() {
        int limite = getMaxIngredientesPorReceita();
        int elegiveis = contarElegiveis(limite);
        if (elegiveis == 0) {
            limite = Integer.MAX_VALUE;
            elegiveis = contarElegiveis(limite);
        }
        if (elegiveis == 0) {
            return cardapio[sorteio.nextInt(cardapio.length)];
        }
        int alvo = sorteio.nextInt(elegiveis);
        for (int i = 0; i < cardapio.length; i++) {
            if (receitaElegivel(cardapio[i], limite)) {
                if (alvo == 0) {
                    return cardapio[i];
                }
                alvo--;
            }
        }
        return cardapio[0];
    }

    private int contarElegiveis(int limite) {
        int total = 0;
        for (int i = 0; i < cardapio.length; i++) {
            if (receitaElegivel(cardapio[i], limite)) {
                total++;
            }
        }
        return total;
    }

    private boolean receitaElegivel(Receita receita, int limite) {
        if (receita.getQuantidade() > limite) {
            return false;
        }
        for (int i = 0; i < receita.getQuantidade(); i++) {
            if (!isDesbloqueado(receita.getIngrediente(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Coloca um ingrediente no lanche. Se ainda não há pedido em preparo, começa o
     * pedido aguardando mais antigo (quem pediu primeiro). Retorna false se não deu para adicionar.
     */
    public boolean adicionarIngrediente(Ingrediente ingrediente) {
        if (fimDeJogo || nivelConcluido) {
            return false;
        }
        if (!isDesbloqueado(ingrediente)) {
            ultimoEvento = ingrediente.getNome() + " ainda nao foi comprado na loja";
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
        return !fimDeJogo && !nivelConcluido && cozinha.desfazer();
    }

    /** Confere o lanche com o pedido e entrega se estiver certo. */
    public ResultadoEntrega entregar() {
        if (fimDeJogo || nivelConcluido || pedidoEmPreparo == null) {
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
        int ganhoMoedas = pedido.calcularMoedas();
        pontos += ganhos;
        moedas += ganhoMoedas;
        entregues++;
        entreguesNoNivel++;
        pedidosAtivos.remover(pedido);
        pedidoEmPreparo = null;
        cozinha.limpar();
        ultimoEvento = pedido.getCliente().getNome() + " recebeu o lanche! +" + ganhos + " pontos, +"
                + ganhoMoedas + " moedas";
        if (entreguesNoNivel >= getMetaEntregas()) {
            concluirNivel();
        }
        return ResultadoEntrega.ENTREGUE;
    }

    // ------------------------------------------------------------------ níveis

    /** Meta de entregas do nível atual. */
    public int getMetaEntregas() {
        return META_BASE + (nivel - 1) * META_POR_NIVEL;
    }

    /** Receitas do nível N podem ter até N + 3 ingredientes (limitado à altura da bancada). */
    public int getMaxIngredientesPorReceita() {
        return Math.min(Cozinha.ALTURA_MAXIMA, 3 + nivel);
    }

    /** Quantos pedidos podem estar ativos ao mesmo tempo: 3 no nível 1 e 4 a partir do nível 2. */
    public int getMaxPedidosAtivos() {
        return Math.min(MAX_PEDIDOS_ATIVOS, 2 + nivel);
    }

    /** Encerra o nível: dá a recompensa e limpa o balcão para a loja. */
    private void concluirNivel() {
        nivelConcluido = true;
        ultimaRecompensaMoedas = BONUS_NIVEL_MOEDAS_BASE + BONUS_NIVEL_MOEDAS_POR_NIVEL * nivel;
        moedas += ultimaRecompensaMoedas;
        pontos += BONUS_NIVEL_PONTOS;
        fila.limpar();
        pedidosAtivos.limpar();
        cozinha.limpar();
        pedidoEmPreparo = null;
        ultimoEvento = "Nivel " + nivel + " concluido! +" + BONUS_NIVEL_PONTOS + " pontos, +"
                + ultimaRecompensaMoedas + " moedas";
    }

    /** Sai da loja e começa o próximo nível (mais difícil). Só vale depois de concluir o nível atual. */
    public void iniciarProximoNivel() {
        if (!nivelConcluido || fimDeJogo) {
            return;
        }
        nivel++;
        entreguesNoNivel = 0;
        nivelConcluido = false;
        tempoParaProximoCliente = PRIMEIRO_CLIENTE;
        ultimoEvento = "Nivel " + nivel + "! Meta: " + getMetaEntregas() + " entregas";
    }

    // ------------------------------------------------------------ ingredientes

    public boolean isDesbloqueado(Ingrediente ingrediente) {
        return desbloqueados[ingrediente.ordinal()];
    }

    /** Libera o ingrediente (usado pela loja depois de cobrar). */
    public void desbloquear(Ingrediente ingrediente) {
        desbloqueados[ingrediente.ordinal()] = true;
    }

    /** Tira moedas da carteira se houver saldo. Retorna false (sem mexer em nada) se não houver. */
    public boolean gastarMoedas(int valor) {
        if (valor < 0 || valor > moedas) {
            return false;
        }
        moedas -= valor;
        return true;
    }

    // ----------------------------------------------------------------- busca

    /** Busca sequencial de um pedido ativo pelo nome do cliente (ignora maiúsculas). Null se não achar. */
    public Pedido buscarPedidoPorCliente(String nome) {
        return Algoritmos.buscaSequencial(pedidosAtivos.getInicio(),
                pedido -> pedido.getCliente().getNome().equalsIgnoreCase(nome));
    }

    /** Busca sequencial de um cliente na fila pelo nome. Retorna a posição (0 = balcão) ou -1. */
    public int posicaoNaFila(String nome) {
        return Algoritmos.indiceSequencial(fila.getNoInicio(),
                cliente -> cliente.getNome().equalsIgnoreCase(nome));
    }

    /** Pesquisa por nome nos pedidos ativos e na fila e devolve uma frase pronta para mostrar na tela. */
    public String pesquisar(String nome) {
        String procurado = nome == null ? "" : nome.trim();
        if (procurado.isEmpty()) {
            return "Digite um nome para pesquisar";
        }
        Pedido pedido = buscarPedidoPorCliente(procurado);
        if (pedido != null) {
            return "Pedido #" + pedido.getId() + " de " + pedido.getCliente().getNome() + ": "
                    + pedido.getReceita().getNome() + " (" + pedido.getStatus() + ", paciencia "
                    + Math.round(pedido.getCliente().getPercentualPaciencia() * 100f) + "%)";
        }
        int posicao = posicaoNaFila(procurado);
        if (posicao != -1) {
            return procurado + " esta na fila, posicao " + (posicao + 1);
        }
        return "\"" + procurado + "\" nao encontrado";
    }

    // ------------------------------------------------------------------ apoio

    /**
     * Pedido AGUARDANDO mais antigo (menor id), ou seja, do cliente que pediu primeiro.
     * Não depende da ordem da lista, que é reordenada por urgência.
     */
    private Pedido primeiroPedidoAguardando() {
        Pedido maisAntigo = null;
        for (No<Pedido> no = pedidosAtivos.getInicio(); no != null; no = no.getProximo()) {
            Pedido pedido = no.getDado();
            if (pedido.getStatus() == StatusPedido.AGUARDANDO
                    && (maisAntigo == null || pedido.getId() < maisAntigo.getId())) {
                maisAntigo = pedido;
            }
        }
        return maisAntigo;
    }

    private void perderVida(String motivo) {
        vidas--;
        ultimoEvento = motivo;
    }

    /** Nível atual, começando em 1. */
    public int getNivel() {
        return nivel;
    }

    public int getEntreguesNoNivel() {
        return entreguesNoNivel;
    }

    public boolean isNivelConcluido() {
        return nivelConcluido;
    }

    /** Moedas que o nível recém-concluído rendeu de bônus (para mostrar na loja). */
    public int getUltimaRecompensaMoedas() {
        return ultimaRecompensaMoedas;
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

    public int getMoedas() {
        return moedas;
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
