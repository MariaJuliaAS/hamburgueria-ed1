package com.hamburgueria.logica;

import com.hamburgueria.modelo.Cliente;
import com.hamburgueria.modelo.Ingrediente;
import com.hamburgueria.modelo.Pedido;
import com.hamburgueria.modelo.Receita;
import com.hamburgueria.modelo.StatusPedido;

/**
 * Teste simples (sem JUnit) da cozinha e da partida.
 * Usa um cardápio com uma única receita para o sorteio não atrapalhar os resultados.
 */
public class TestePartida {

    private static int falhas = 0;

    public static void main(String[] args) {
        testarCozinha();
        testarChegadaEAtendimento();
        testarEntregas();
        testarSemPedido();
        testarLimiteDeCamadas();
        testarDificuldade();
        testarNiveis();
        testarLoja();
        testarReceitasPorNivel();
        testarPedidoCancelado();
        testarFimDeJogo();

        System.out.println();
        if (falhas == 0) {
            System.out.println("Todos os testes passaram.");
        } else {
            System.out.println(falhas + " teste(s) falharam.");
            System.exit(1);
        }
    }

    private static Receita[] cardapioUnico() {
        return new Receita[]{new Receita("X-Burger", new Ingrediente[]{
                Ingrediente.PAO, Ingrediente.CARNE, Ingrediente.QUEIJO, Ingrediente.PAO}, 100)};
    }

    /** Cria uma partida e faz o primeiro cliente chegar. */
    private static Partida partidaComUmCliente() {
        Partida partida = new Partida(cardapioUnico(), 7L);
        partida.atualizar(1.1f);
        return partida;
    }

    private static void montarXBurger(Partida partida) {
        partida.adicionarIngrediente(Ingrediente.PAO);
        partida.adicionarIngrediente(Ingrediente.CARNE);
        partida.adicionarIngrediente(Ingrediente.QUEIJO);
        partida.adicionarIngrediente(Ingrediente.PAO);
    }

    private static void testarCozinha() {
        System.out.println("== Cozinha ==");
        Cozinha cozinha = new Cozinha();
        verificar(cozinha.estaVazia(), "cozinha começa vazia");
        verificar(!cozinha.desfazer(), "desfazer sem nada retorna false");
        verificar(cozinha.adicionar(Ingrediente.PAO), "adicionar pão");
        verificar(cozinha.adicionar(Ingrediente.CARNE), "adicionar carne");
        verificar(cozinha.tamanho() == 2, "duas camadas");
        verificar(cozinha.getMontagem().topo() == Ingrediente.CARNE, "topo é a carne");
        verificar(cozinha.desfazer(), "desfazer retorna true");
        verificar(cozinha.getMontagem().topo() == Ingrediente.PAO, "depois de desfazer o topo é o pão");
        cozinha.limpar();
        verificar(cozinha.estaVazia(), "limpar esvazia");
    }

    private static void testarChegadaEAtendimento() {
        System.out.println("== Chegada e atendimento ==");
        Partida partida = new Partida(cardapioUnico(), 7L);
        verificar(partida.getFila().estaVazia(), "fila começa vazia");
        verificar(partida.getVidas() == Partida.VIDAS_INICIAIS, "começa com as vidas iniciais");
        verificar(partida.getPontos() == 0, "começa com 0 pontos");
        verificar(partida.atenderCliente() == null, "atender com fila vazia retorna null");

        partida.atualizar(0.5f);
        verificar(partida.getFila().estaVazia(), "nenhum cliente antes de 1 segundo");
        partida.atualizar(0.6f);
        verificar(partida.getFila().tamanho() == 1, "primeiro cliente chega depois de 1 segundo");

        Pedido pedido = partida.atenderCliente();
        verificar(pedido != null, "atender cria um pedido");
        verificar(partida.getFila().estaVazia(), "o cliente sai da fila ao ser atendido");
        verificar(partida.getPedidosAtivos().tamanho() == 1, "pedido entra na lista de ativos");
        verificar(pedido.getStatus() == StatusPedido.AGUARDANDO, "pedido começa aguardando");
        verificar(pedido.getReceita().getNome().equals("X-Burger"), "pedido usa a receita do cardápio");
    }

    private static void testarEntregas() {
        System.out.println("== Entregas ==");
        Partida partida = partidaComUmCliente();
        Pedido pedido = partida.atenderCliente();

        verificar(partida.adicionarIngrediente(Ingrediente.PAO), "adicionar o primeiro ingrediente");
        verificar(pedido.getStatus() == StatusPedido.EM_PREPARO, "pedido vai para em preparo");
        verificar(partida.getPedidoEmPreparo() == pedido, "pedido em preparo é o atendido");
        partida.adicionarIngrediente(Ingrediente.CARNE);
        partida.adicionarIngrediente(Ingrediente.QUEIJO);
        partida.adicionarIngrediente(Ingrediente.ALFACE);
        verificar(partida.desfazerIngrediente(), "desfazer a alface");
        partida.adicionarIngrediente(Ingrediente.PAO);
        verificar(partida.getCozinha().tamanho() == 4, "lanche com 4 camadas");

        verificar(partida.entregar() == ResultadoEntrega.ENTREGUE, "lanche certo é entregue");
        verificar(partida.getPontos() == 150, "entrega imediata: 100 + 50 de bônus");
        verificar(partida.getEntregues() == 1, "conta uma entrega");
        verificar(partida.getPedidosAtivos().estaVazia(), "pedido sai da lista");
        verificar(partida.getPedidoEmPreparo() == null, "nada em preparo depois de entregar");
        verificar(partida.getCozinha().estaVazia(), "cozinha limpa depois de entregar");

        // Segundo cliente: errar o lanche.
        partida.atualizar(12f);
        verificar(partida.getFila().tamanho() == 1, "segundo cliente chegou");
        Pedido segundo = partida.atenderCliente();
        partida.adicionarIngrediente(Ingrediente.PAO);
        partida.adicionarIngrediente(Ingrediente.CARNE);
        verificar(partida.entregar() == ResultadoEntrega.ERRADO, "lanche incompleto é errado");
        verificar(partida.getPontos() == 130, "erro tira 20 pontos");
        verificar(partida.getCozinha().estaVazia(), "lanche errado é descartado");
        verificar(partida.getPedidoEmPreparo() == segundo, "o pedido continua em preparo");
        verificar(segundo.getStatus() == StatusPedido.EM_PREPARO, "e continua ativo");
        verificar(partida.entregar() == ResultadoEntrega.LANCHE_VAZIO, "entregar sem montar nada");
        verificar(partida.getPontos() == 130, "lanche vazio não tira pontos");

        montarXBurger(partida);
        verificar(partida.entregar() == ResultadoEntrega.ENTREGUE, "refazendo certo, entrega");
        verificar(partida.getPontos() == 280, "130 + 150");
        verificar(partida.getEntregues() == 2, "duas entregas");
    }

    private static void testarSemPedido() {
        System.out.println("== Sem pedido ==");
        Partida partida = new Partida(cardapioUnico(), 7L);
        verificar(!partida.adicionarIngrediente(Ingrediente.PAO), "não monta sem pedido");
        verificar(partida.entregar() == ResultadoEntrega.SEM_PEDIDO, "entregar sem pedido");
        verificar(!partida.desfazerIngrediente(), "desfazer sem nada");
    }

    private static void testarLimiteDeCamadas() {
        System.out.println("== Limite de camadas ==");
        Partida partida = partidaComUmCliente();
        partida.atenderCliente();
        boolean todasAceitas = true;
        for (int i = 0; i < Cozinha.ALTURA_MAXIMA; i++) {
            todasAceitas = todasAceitas && partida.adicionarIngrediente(Ingrediente.PAO);
        }
        verificar(todasAceitas, "aceita até a altura máxima");
        verificar(!partida.adicionarIngrediente(Ingrediente.PAO), "recusa além da altura máxima");
        verificar(partida.getCozinha().tamanho() == Cozinha.ALTURA_MAXIMA, "mantém a altura máxima");
    }

    private static void testarDificuldade() {
        System.out.println("== Dificuldade ==");
        Partida partida = new Partida(cardapioUnico(), 7L);
        verificar(partida.getNivel() == 1, "começa no nível 1");
        partida.atualizar(35f);
        verificar(partida.getNivel() == 1, "o nível não sobe só com o tempo");
        verificar(partida.getTempo() == 35f, "tempo acumulado");
    }

    /** Põe um cliente na fila, atende, monta a receita sorteada e entrega. Não deixa o tempo passar. */
    private static Pedido entregarRapido(Partida partida) {
        partida.getFila().enfileirar(new Cliente("Teste", 60f));
        Pedido pedido = partida.atenderCliente();
        for (int i = 0; i < pedido.getReceita().getQuantidade(); i++) {
            partida.adicionarIngrediente(pedido.getReceita().getIngrediente(i));
        }
        partida.entregar();
        return pedido;
    }

    private static void testarNiveis() {
        System.out.println("== Níveis, recompensa e gorjeta ==");
        Partida partida = new Partida(cardapioUnico(), 7L);
        verificar(partida.getMetaEntregas() == Partida.META_BASE, "meta do nível 1");
        verificar(partida.getMaxIngredientesPorReceita() == 4, "nível 1: receitas de até 4 ingredientes");
        verificar(partida.getMaxPedidosAtivos() == 3, "nível 1: até 3 pedidos ao mesmo tempo");

        Pedido primeiro = entregarRapido(partida);
        verificar(primeiro.calcularGorjeta() == 10, "gorjeta com paciência cheia é 10% da receita");
        verificar(partida.getMoedas() == 10 + primeiro.calcularGorjeta(), "entrega rende moedas + gorjeta");
        verificar(partida.getEntreguesNoNivel() == 1, "conta entregas do nível");

        for (int i = 1; i < Partida.META_BASE - 1; i++) {
            entregarRapido(partida);
        }
        verificar(!partida.isNivelConcluido(), "uma entrega antes da meta ainda não conclui");
        int pontosAntes = partida.getPontos();
        int moedasAntes = partida.getMoedas();
        partida.getFila().enfileirar(new Cliente("Zeca", 60f));
        partida.getFila().enfileirar(new Cliente("Lia", 60f));
        Pedido ultimo = entregarRapido(partida);

        verificar(partida.isNivelConcluido(), "cumprir a meta conclui o nível");
        verificar(partida.getPontos() == pontosAntes + ultimo.calcularPontos() + Partida.BONUS_NIVEL_PONTOS,
                "bônus de pontos do nível");
        int bonusMoedas = Partida.BONUS_NIVEL_MOEDAS_BASE + Partida.BONUS_NIVEL_MOEDAS_POR_NIVEL;
        verificar(partida.getUltimaRecompensaMoedas() == bonusMoedas, "recompensa em moedas do nível");
        verificar(partida.getMoedas() == moedasAntes + ultimo.calcularMoedas() + bonusMoedas, "moedas somadas");
        verificar(partida.getFila().estaVazia() && partida.getPedidosAtivos().estaVazia(),
                "balcão limpo ao concluir o nível");
        verificar(partida.atenderCliente() == null, "não atende com o nível concluído");
        float tempo = partida.getTempo();
        partida.atualizar(5f);
        verificar(partida.getTempo() == tempo, "o tempo para com o nível concluído");

        partida.iniciarProximoNivel();
        verificar(partida.getNivel() == 2 && !partida.isNivelConcluido(), "próximo nível começa");
        verificar(partida.getMetaEntregas() == Partida.META_BASE + Partida.META_POR_NIVEL, "meta aumenta");
        verificar(partida.getMaxIngredientesPorReceita() == 5, "nível 2: receitas de até 5 ingredientes");
        verificar(partida.getMaxPedidosAtivos() == 4, "nível 2: até 4 pedidos ao mesmo tempo");
        verificar(partida.getEntreguesNoNivel() == 0, "contador de entregas zera");
        partida.iniciarProximoNivel();
        verificar(partida.getNivel() == 2, "não pula de nível sem concluir o atual");
    }

    private static void testarLoja() {
        System.out.println("== Loja ==");
        Partida partida = new Partida(cardapioUnico(), 7L);

        verificar(partida.isDesbloqueado(Ingrediente.PAO) && partida.isDesbloqueado(Ingrediente.TOMATE),
                "ingredientes básicos já começam liberados");
        verificar(!partida.isDesbloqueado(Ingrediente.BACON), "bacon começa bloqueado");

        Ingrediente[] itens = Loja.itensAVenda(partida);
        verificar(itens.length == 4, "4 ingredientes à venda");
        boolean ordenado = true;
        for (int i = 1; i < itens.length; i++) {
            if (itens[i - 1].getPreco() > itens[i].getPreco()) {
                ordenado = false;
            }
        }
        verificar(ordenado && itens[0] == Ingrediente.CEBOLA, "itens ordenados do mais barato ao mais caro");

        verificar(Loja.comprar(partida, Ingrediente.CEBOLA) == ResultadoCompra.SEM_MOEDAS, "sem moedas não compra");
        verificar(!partida.isDesbloqueado(Ingrediente.CEBOLA) && partida.getMoedas() == 0, "nada muda sem moedas");
        verificar(Loja.comprar(partida, Ingrediente.PAO) == ResultadoCompra.JA_DESBLOQUEADO, "já liberado");

        verificar(!partida.gastarMoedas(1), "gastar sem saldo falha");
        for (int i = 0; i < Partida.META_BASE; i++) {
            entregarRapido(partida);
        }
        int moedas = partida.getMoedas();
        verificar(moedas >= Ingrediente.CEBOLA.getPreco(), "o nível rendeu moedas para comprar (" + moedas + ")");
        verificar(Loja.comprar(partida, Ingrediente.CEBOLA) == ResultadoCompra.COMPRADO, "compra com moedas");
        verificar(partida.getMoedas() == moedas - Ingrediente.CEBOLA.getPreco(), "moedas descontadas");
        verificar(partida.isDesbloqueado(Ingrediente.CEBOLA), "ingrediente liberado");
        verificar(Loja.itensAVenda(partida).length == 3, "sai da lista da loja");
        verificar(Loja.comprar(partida, Ingrediente.CEBOLA) == ResultadoCompra.JA_DESBLOQUEADO, "não compra duas vezes");

        partida.iniciarProximoNivel();
        partida.getFila().enfileirar(new Cliente("Teste", 60f));
        partida.atenderCliente();
        verificar(!partida.adicionarIngrediente(Ingrediente.BACON), "ingrediente bloqueado não entra no lanche");
        verificar(partida.adicionarIngrediente(Ingrediente.CEBOLA), "ingrediente comprado entra no lanche");
    }

    private static void testarReceitasPorNivel() {
        System.out.println("== Receitas por nível ==");
        Partida partida = new Partida(Cardapio.padrao(), 99L);
        verificar(receitasSorteadas(partida, 200, 4, false), "nível 1: só receitas de até 4 ingredientes, sem itens da loja");

        // Sobe até o nível 2 sem comprar nada.
        for (int i = 0; i < partida.getMetaEntregas() && !partida.isNivelConcluido(); i++) {
            entregarRapido(partida);
        }
        partida.iniciarProximoNivel();
        verificar(partida.getNivel() == 2, "chegou ao nível 2");
        verificar(receitasSorteadas(partida, 200, 5, false), "nível 2: até 5 ingredientes e ainda sem itens da loja");

        // Libera tudo e sobe até o nível 5: aparecem as receitas grandes.
        Ingrediente[] todos = Ingrediente.values();
        for (int i = 0; i < todos.length; i++) {
            partida.desbloquear(todos[i]);
        }
        while (partida.getNivel() < 5) {
            while (!partida.isNivelConcluido()) {
                entregarRapido(partida);
            }
            partida.iniciarProximoNivel();
        }
        verificar(partida.getMaxIngredientesPorReceita() == 8, "nível 5: até 8 ingredientes");
        verificar(receitasSorteadas(partida, 400, 8, true), "nível 5: aparecem receitas de 8 ingredientes");

        Partida limitada = new Partida(new Receita[]{new Receita("Gigante", new Ingrediente[]{
                Ingrediente.PAO, Ingrediente.CARNE, Ingrediente.QUEIJO, Ingrediente.ALFACE,
                Ingrediente.TOMATE, Ingrediente.PAO}, 100)}, 5L);
        limitada.getFila().enfileirar(new Cliente("Teste", 60f));
        verificar(limitada.atenderCliente() != null, "cardápio só com receita grande ainda gera pedido");
    }

    /**
     * Atende várias vezes e confere o limite de ingredientes e se aparecem itens da loja.
     * Retorna true se nenhuma receita passou do limite e (quando exigido) se alguma chegou ao limite.
     */
    private static boolean receitasSorteadas(Partida partida, int vezes, int limite, boolean exigirLimite) {
        boolean respeitou = true;
        boolean chegouAoLimite = false;
        for (int i = 0; i < vezes; i++) {
            partida.getPedidosAtivos().limpar();
            partida.getFila().enfileirar(new Cliente("Teste", 60f));
            Pedido pedido = partida.atenderCliente();
            if (pedido.getReceita().getQuantidade() > limite) {
                respeitou = false;
            }
            if (pedido.getReceita().getQuantidade() == limite) {
                chegouAoLimite = true;
            }
            for (int j = 0; j < pedido.getReceita().getQuantidade(); j++) {
                if (!partida.isDesbloqueado(pedido.getReceita().getIngrediente(j))) {
                    respeitou = false;
                }
            }
        }
        partida.getPedidosAtivos().limpar(); // não deixa pedido sobrando para os próximos testes
        return respeitou && (!exigirLimite || chegouAoLimite);
    }

    private static void testarPedidoCancelado() {
        System.out.println("== Pedido cancelado por paciência ==");
        Partida partida = partidaComUmCliente();
        Pedido pedido = partida.atenderCliente();
        partida.adicionarIngrediente(Ingrediente.PAO);
        verificar(partida.getPedidoEmPreparo() == pedido, "pedido em preparo");

        for (int i = 0; i < 60; i++) {
            partida.atualizar(1f);
        }
        verificar(pedido.getStatus() == StatusPedido.CANCELADO, "paciência esgotada cancela o pedido");
        verificar(partida.getPedidosAtivos().estaVazia(), "pedido cancelado sai da lista");
        verificar(partida.getPedidoEmPreparo() == null, "deixa de estar em preparo");
        verificar(partida.getCozinha().estaVazia(), "a cozinha é limpa");
        verificar(partida.getVidas() < Partida.VIDAS_INICIAIS, "o jogador perde vida");

        // Com pontos na conta, o cancelamento tira pontos.
        Partida p2 = partidaComUmCliente();
        p2.atenderCliente();
        montarXBurger(p2);
        p2.entregar();
        int antes = p2.getPontos();
        p2.getFila().enfileirar(new Cliente("Zeca", 20f));
        Pedido segundo = p2.atenderCliente();
        verificar(antes > 0 && segundo != null, "preparação: pontos acumulados e segundo pedido feito");
        for (int i = 0; i < 25 && segundo.getStatus() != StatusPedido.CANCELADO; i++) {
            p2.atualizar(1f);
        }
        verificar(segundo.getStatus() == StatusPedido.CANCELADO, "cliente que já pediu continua perdendo paciência");
        verificar(p2.getPontos() == antes - Partida.PENALIDADE_CANCELAMENTO, "cancelamento tira pontos");
    }

    private static void testarFimDeJogo() {
        System.out.println("== Fim de jogo ==");
        Partida partida = new Partida(cardapioUnico(), 7L);
        int passos = 0;
        while (!partida.estaEmFimDeJogo() && passos < 1000) {
            partida.atualizar(1f);
            passos++;
        }
        verificar(partida.estaEmFimDeJogo(), "sem atender ninguém o jogo acaba");
        verificar(partida.getVidas() == 0, "termina sem vidas");

        float tempo = partida.getTempo();
        partida.atualizar(10f);
        verificar(partida.getTempo() == tempo, "o tempo para depois do fim");
        verificar(partida.atenderCliente() == null, "não atende depois do fim");
        verificar(!partida.adicionarIngrediente(Ingrediente.PAO), "não monta depois do fim");

        Cliente exemplo = new Cliente("Teste", 10f);
        verificar(exemplo.getPedido() == null, "cliente sem pedido não tem referência");
    }

    private static void verificar(boolean condicao, String descricao) {
        if (condicao) {
            System.out.println("  OK     " + descricao);
        } else {
            System.out.println("  FALHOU " + descricao);
            falhas++;
        }
    }
}
