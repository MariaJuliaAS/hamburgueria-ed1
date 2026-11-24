package com.hamburgueria.modelo;

import com.hamburgueria.estruturas.Pilha;

/**
 * Teste simples (sem JUnit) das classes do modelo.
 * Rode a main: cada verificação imprime OK ou FALHOU.
 */
public class TesteModelo {

    private static int falhas = 0;

    public static void main(String[] args) {
        testarIngrediente();
        testarReceita();
        testarCliente();
        testarPedido();

        System.out.println();
        if (falhas == 0) {
            System.out.println("Todos os testes passaram.");
        } else {
            System.out.println(falhas + " teste(s) falharam.");
            System.exit(1);
        }
    }

    private static Receita xBurger() {
        return new Receita("X-Burger",
                new Ingrediente[]{Ingrediente.PAO, Ingrediente.CARNE, Ingrediente.QUEIJO, Ingrediente.PAO},
                100);
    }

    private static void testarIngrediente() {
        System.out.println("== Ingrediente ==");
        verificar(Ingrediente.CARNE.getNome().equals("Carne"), "nome da carne");
        verificar(Ingrediente.CARNE.getTempoPreparo() > Ingrediente.PAO.getTempoPreparo(),
                "carne demora mais que o pão");
    }

    private static void testarReceita() {
        System.out.println("== Receita ==");
        Receita receita = xBurger();
        verificar(receita.getQuantidade() == 4, "X-Burger tem 4 camadas");
        verificar(receita.getIngrediente(0) == Ingrediente.PAO, "base é pão");
        verificar(receita.getIngrediente(1) == Ingrediente.CARNE, "segunda camada é carne");
        verificar(receita.getTempoPreparoTotal() == 6.0f, "tempo total 1 + 3 + 1 + 1 = 6");
        verificar(receita.toString().equals("X-Burger: Pão, Carne, Queijo, Pão"), "toString da receita");

        Ingrediente[] original = {Ingrediente.PAO, Ingrediente.CARNE};
        Receita copia = new Receita("Simples", original, 50);
        original[1] = Ingrediente.TOMATE;
        verificar(copia.getIngrediente(1) == Ingrediente.CARNE, "alterar o vetor original não muda a receita");

        Pilha<Ingrediente> certa = new Pilha<>();
        certa.empilhar(Ingrediente.PAO);
        certa.empilhar(Ingrediente.CARNE);
        certa.empilhar(Ingrediente.QUEIJO);
        certa.empilhar(Ingrediente.PAO);
        verificar(receita.confere(certa), "pilha na ordem certa confere");

        Pilha<Ingrediente> ordemErrada = new Pilha<>();
        ordemErrada.empilhar(Ingrediente.PAO);
        ordemErrada.empilhar(Ingrediente.QUEIJO);
        ordemErrada.empilhar(Ingrediente.CARNE);
        ordemErrada.empilhar(Ingrediente.PAO);
        verificar(!receita.confere(ordemErrada), "ordem trocada não confere");

        Pilha<Ingrediente> incompleta = new Pilha<>();
        incompleta.empilhar(Ingrediente.PAO);
        incompleta.empilhar(Ingrediente.CARNE);
        verificar(!receita.confere(incompleta), "pilha incompleta não confere");

        Pilha<Ingrediente> comSobra = new Pilha<>();
        comSobra.empilhar(Ingrediente.PAO);
        comSobra.empilhar(Ingrediente.CARNE);
        comSobra.empilhar(Ingrediente.QUEIJO);
        comSobra.empilhar(Ingrediente.PAO);
        comSobra.empilhar(Ingrediente.ALFACE);
        verificar(!receita.confere(comSobra), "pilha com camada extra não confere");

        verificar(certa.tamanho() == 4, "conferir não altera a pilha");

        boolean lancou = false;
        try {
            new Receita("Vazia", new Ingrediente[0], 10);
        } catch (IllegalArgumentException e) {
            lancou = true;
        }
        verificar(lancou, "receita sem ingredientes lança exceção");

        lancou = false;
        try {
            receita.getIngrediente(9);
        } catch (IndexOutOfBoundsException e) {
            lancou = true;
        }
        verificar(lancou, "posição inválida lança exceção");
    }

    private static void testarCliente() {
        System.out.println("== Cliente ==");
        Cliente cliente = new Cliente("Ana", 20f);

        verificar(cliente.getPercentualPaciencia() == 1.0f, "começa com paciência cheia");
        verificar(!cliente.estaImpaciente(), "começa calmo");

        cliente.atualizar(10f);
        verificar(cliente.getPacienciaAtual() == 10f, "paciência cai com o tempo");
        verificar(cliente.getPercentualPaciencia() == 0.5f, "metade da paciência");
        verificar(cliente.estaImpaciente(), "impaciente na metade");
        verificar(!cliente.acabouPaciencia(), "ainda não acabou");

        cliente.atualizar(15f);
        verificar(cliente.getPacienciaAtual() == 0f, "paciência não fica negativa");
        verificar(cliente.acabouPaciencia(), "paciência acabou");
        verificar(!cliente.estaImpaciente(), "quem foi embora não conta como impaciente");

        boolean lancou = false;
        try {
            new Cliente("Zero", 0f);
        } catch (IllegalArgumentException e) {
            lancou = true;
        }
        verificar(lancou, "paciência máxima zero lança exceção");
    }

    private static void testarPedido() {
        System.out.println("== Pedido ==");
        Cliente cliente = new Cliente("Bia", 20f);
        Pedido pedido = new Pedido(cliente, xBurger());

        verificar(pedido.getStatus() == StatusPedido.AGUARDANDO, "pedido novo está aguardando");
        verificar(cliente.getPedido() == pedido, "cliente aponta para o pedido");
        verificar(pedido.estaAtivo(), "pedido novo está ativo");
        verificar(pedido.calcularPontos() == 0, "pedido não entregue vale 0 pontos");

        pedido.atualizar(5f);
        verificar(pedido.getTempoEspera() == 5f, "tempo de espera acumula");
        verificar(cliente.getPacienciaAtual() == 15f, "paciência do cliente cai junto");

        pedido.iniciarPreparo();
        verificar(pedido.getStatus() == StatusPedido.EM_PREPARO, "status em preparo");
        pedido.atualizar(5f);
        pedido.marcarPronto();
        verificar(pedido.getStatus() == StatusPedido.PRONTO, "status pronto");
        verificar(pedido.estaAtivo(), "pronto ainda é ativo (falta entregar)");
        pedido.entregar();
        verificar(pedido.getStatus() == StatusPedido.ENTREGUE, "status entregue");
        verificar(!pedido.estaAtivo(), "entregue não é mais ativo");

        float espera = pedido.getTempoEspera();
        pedido.atualizar(100f);
        verificar(pedido.getTempoEspera() == espera, "tempo não passa depois de entregue");
        verificar(cliente.getPacienciaAtual() == 10f, "paciência congela depois de entregue");
        verificar(pedido.calcularPontos() == 125, "100 pontos + bônus de 25 (metade da paciência)");

        Cliente apressado = new Cliente("Caio", 20f);
        Pedido rapido = new Pedido(apressado, xBurger());
        rapido.iniciarPreparo();
        rapido.marcarPronto();
        rapido.entregar();
        verificar(rapido.calcularPontos() == 150, "entrega imediata dá 100 + 50 de bônus");

        Cliente demorado = new Cliente("Davi", 20f);
        Pedido perdido = new Pedido(demorado, xBurger());
        perdido.atualizar(25f);
        verificar(perdido.getStatus() == StatusPedido.CANCELADO, "paciência zerada cancela o pedido");
        verificar(!perdido.estaAtivo(), "cancelado não é ativo");
        verificar(perdido.calcularPontos() == 0, "cancelado vale 0 pontos");

        boolean lancou = false;
        try {
            perdido.iniciarPreparo();
        } catch (IllegalStateException e) {
            lancou = true;
        }
        verificar(lancou, "não dá para preparar pedido cancelado");

        Pedido fora = new Pedido(new Cliente("Eva", 20f), xBurger());
        lancou = false;
        try {
            fora.entregar();
        } catch (IllegalStateException e) {
            lancou = true;
        }
        verificar(lancou, "não dá para entregar sem estar pronto");

        lancou = false;
        try {
            perdido.cancelar();
        } catch (IllegalStateException e) {
            lancou = true;
        }
        verificar(lancou, "cancelar pedido já encerrado lança exceção");

        Pedido cancelavel = new Pedido(new Cliente("Fabi", 20f), xBurger());
        cancelavel.cancelar();
        verificar(cancelavel.getStatus() == StatusPedido.CANCELADO, "cancelar manualmente funciona");

        verificar(pedido.getId() != rapido.getId(), "cada pedido tem um id próprio");
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
