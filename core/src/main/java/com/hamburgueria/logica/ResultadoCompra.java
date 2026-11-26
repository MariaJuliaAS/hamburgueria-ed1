package com.hamburgueria.logica;

/** Resultado de tentar comprar um ingrediente na loja. */
public enum ResultadoCompra {
    COMPRADO,        // moedas descontadas e ingrediente liberado
    SEM_MOEDAS,      // saldo insuficiente
    JA_DESBLOQUEADO  // o jogador já tem esse ingrediente
}
