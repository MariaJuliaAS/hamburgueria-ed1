package com.hamburgueria.logica;

/** Resultado de tentar entregar o lanche que está na cozinha. */
public enum ResultadoEntrega {
    SEM_PEDIDO,    // não há pedido sendo preparado
    LANCHE_VAZIO,  // nada foi montado ainda
    ERRADO,        // o lanche não confere com a receita (perde pontos, lanche é descartado)
    ENTREGUE       // acertou: ganha pontos e o pedido sai da lista
}
