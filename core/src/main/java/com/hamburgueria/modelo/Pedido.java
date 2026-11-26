package com.hamburgueria.modelo;

/**
 * Pedido de um cliente: qual receita ele quer e em que etapa o pedido está.
 * Enquanto o pedido está ativo, o tempo passa e a paciência do cliente diminui.
 */
public class Pedido {

    private static int proximoId = 1;

    private final int id;
    private final Receita receita;
    private final Cliente cliente;
    private StatusPedido status;
    private float tempoEspera;

    public Pedido(Cliente cliente, Receita receita) {
        if (cliente == null || receita == null) {
            throw new IllegalArgumentException("Cliente e receita são obrigatórios");
        }
        this.id = proximoId++;
        this.cliente = cliente;
        this.receita = receita;
        this.status = StatusPedido.AGUARDANDO;
        this.tempoEspera = 0f;
        cliente.setPedido(this);
    }

    /**
     * Faz o tempo passar. Só conta enquanto o pedido está ativo.
     * Se a paciência do cliente acabar, o pedido é cancelado.
     */
    public void atualizar(float delta) {
        atualizar(delta, 1f);
    }

    /** Atualiza o pedido; o multiplicador acelera a perda de paciência conforme os dias avançam. */
    public void atualizar(float delta, float multiplicadorPaciência) {
        if (!estaAtivo()) {
            return;
        }
        tempoEspera += delta;
        cliente.atualizar(delta * multiplicadorPaciência);
        if (cliente.acabouPaciencia()) {
            status = StatusPedido.CANCELADO;
        }
    }

    public void iniciarPreparo() {
        exigirStatus(StatusPedido.AGUARDANDO);
        status = StatusPedido.EM_PREPARO;
    }

    public void marcarPronto() {
        exigirStatus(StatusPedido.EM_PREPARO);
        status = StatusPedido.PRONTO;
    }

    public void entregar() {
        exigirStatus(StatusPedido.PRONTO);
        status = StatusPedido.ENTREGUE;
    }

    public void cancelar() {
        if (!estaAtivo()) {
            throw new IllegalStateException("O pedido " + id + " já foi encerrado (" + status + ")");
        }
        status = StatusPedido.CANCELADO;
    }

    /** Ativo é qualquer pedido que ainda não foi entregue nem cancelado. */
    public boolean estaAtivo() {
        return status == StatusPedido.AGUARDANDO
                || status == StatusPedido.EM_PREPARO
                || status == StatusPedido.PRONTO;
    }

    /**
     * Pontos ganhos: o valor da receita mais um bônus de até 50% por entregar rápido
     * (quanto mais paciência sobrou, maior o bônus). Pedido não entregue vale zero.
     */
    public int calcularPontos() {
        if (status != StatusPedido.ENTREGUE) {
            return 0;
        }
        int bonus = Math.round(receita.getPontos() * cliente.getPercentualPaciencia() * 0.5f);
        return receita.getPontos() + bonus;
    }

    /** Gorjeta em moedas: até 10% do valor da receita, proporcional à paciência que sobrou. */
    public int calcularGorjeta() {
        if (status != StatusPedido.ENTREGUE) {
            return 0;
        }
        return Math.round(receita.getPontos() * cliente.getPercentualPaciencia() * 0.1f);
    }

    /** Moedas ganhas na entrega: 10% do valor da receita mais a gorjeta. */
    public int calcularMoedas() {
        if (status != StatusPedido.ENTREGUE) {
            return 0;
        }
        return receita.getPontos() / 10 + calcularGorjeta();
    }

    public int getId() {
        return id;
    }

    public Receita getReceita() {
        return receita;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public float getTempoEspera() {
        return tempoEspera;
    }

    private void exigirStatus(StatusPedido esperado) {
        if (status != esperado) {
            throw new IllegalStateException(
                    "Pedido " + id + " está " + status + " e deveria estar " + esperado);
        }
    }
}
