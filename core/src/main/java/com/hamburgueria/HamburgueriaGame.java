package com.hamburgueria;

import com.badlogic.gdx.Game;
import com.hamburgueria.logica.Cardapio;
import com.hamburgueria.logica.Partida;
import com.hamburgueria.telas.GameOverScreen;
import com.hamburgueria.telas.JogoScreen;
import com.hamburgueria.telas.MenuScreen;

/** Classe principal: controla a troca entre as telas do jogo. */
public class HamburgueriaGame extends Game {

    @Override
    public void create() {
        mostrarMenu();
    }

    public void mostrarMenu() {
        setScreen(new MenuScreen(this));
    }

    public void iniciarPartida() {
        setScreen(new JogoScreen(this, new Partida(Cardapio.carregar())));
    }

    public void mostrarGameOver(Partida partida) {
        setScreen(new GameOverScreen(this, partida.getPontos(), partida.getEntregues(), partida.getTempo()));
    }
}
