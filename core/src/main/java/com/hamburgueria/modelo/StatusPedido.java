package com.hamburgueria.modelo;

/** Etapas da vida de um pedido. */
public enum StatusPedido {
    AGUARDANDO,   // cliente fez o pedido, a cozinha ainda não começou
    EM_PREPARO,   // o lanche está sendo montado
    PRONTO,       // montado, falta entregar
    ENTREGUE,     // cliente recebeu
    CANCELADO     // cliente perdeu a paciência e foi embora
}
