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
        verificar(partida.getNivel() == 0, "começa no nível 0");
        partida.atualizar(35f);
        verificar(partida.getNivel() == 1, "sobe para o nível 1 depois de 30 segundos");
        verificar(partida.getTempo() == 35f, "tempo acumulado");
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
