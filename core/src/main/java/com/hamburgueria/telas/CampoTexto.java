package com.hamburgueria.telas;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Caixa de texto desenhada dentro do próprio jogo (não depende de diálogo do sistema, que não funciona
 * em todos os backends). Enquanto está ativa, captura o teclado: letras digitam, BACKSPACE apaga,
 * ENTER confirma e ESC cancela.
 *
 * Uso: abrir(...) ao apertar uma tecla; a cada frame, terminou() avisa (uma única vez) que o jogador
 * confirmou ou cancelou, e getResultado() devolve o texto (ou null se cancelou).
 */
public class CampoTexto extends InputAdapter {

    private static final float CAIXA_X = 230f;
    private static final float CAIXA_Y = 250f;
    private static final float CAIXA_LARGURA = 500f;
    private static final float CAIXA_ALTURA = 120f;

    private boolean ativo;
    private boolean pronto;
    private String rotulo = "";
    private String resultado;
    private int tamanhoMaximo;
    private boolean soDigitos;
    private final StringBuilder texto = new StringBuilder();
    private float tempo;

    /** Abre a caixa. O texto inicial já vem preenchido e pode ser apagado. */
    public void abrir(String rotulo, String inicial, int tamanhoMaximo, boolean soDigitos) {
        this.rotulo = rotulo;
        this.tamanhoMaximo = tamanhoMaximo;
        this.soDigitos = soDigitos;
        this.texto.setLength(0);
        if (inicial != null) {
            this.texto.append(inicial);
        }
        this.resultado = null;
        this.pronto = false;
        this.ativo = true;
    }

    public boolean isAtivo() {
        return ativo;
    }

    /** Verdadeiro uma única vez depois que o jogador confirmou (ENTER) ou cancelou (ESC). */
    public boolean terminou() {
        if (pronto) {
            pronto = false;
            return true;
        }
        return false;
    }

    /** Texto confirmado, ou null se o jogador cancelou. */
    public String getResultado() {
        return resultado;
    }

    @Override
    public boolean keyDown(int tecla) {
        if (!ativo) {
            return false;
        }
        if (tecla == Input.Keys.ENTER) {
            resultado = texto.toString();
            encerrar();
        } else if (tecla == Input.Keys.ESCAPE) {
            resultado = null;
            encerrar();
        } else if (tecla == Input.Keys.BACKSPACE && texto.length() > 0) {
            texto.setLength(texto.length() - 1);
        }
        return true;
    }

    @Override
    public boolean keyTyped(char caractere) {
        if (!ativo) {
            return false;
        }
        boolean imprimivel = caractere >= 32 && caractere != 127;
        boolean permitido = !soDigitos || (caractere >= '0' && caractere <= '9');
        if (imprimivel && permitido && caractere != '|' && texto.length() < tamanhoMaximo) {
            texto.append(caractere);
        }
        return true;
    }

    private void encerrar() {
        ativo = false;
        pronto = true;
    }

    /** Desenha a caixa por cima da tela. Só faz algo enquanto estiver ativa. */
    public void desenhar(com.badlogic.gdx.math.Matrix4 projecao, ShapeRenderer formas, SpriteBatch batch,
                         BitmapFont fonte, float delta) {
        if (!ativo) {
            return;
        }
        tempo += delta;
        formas.setProjectionMatrix(projecao);
        formas.begin(ShapeRenderer.ShapeType.Filled);
        formas.setColor(0f, 0f, 0f, 1f);
        formas.rect(CAIXA_X - 6f, CAIXA_Y - 6f, CAIXA_LARGURA + 12f, CAIXA_ALTURA + 12f);
        formas.setColor(0.22f, 0.15f, 0.10f, 1f);
        formas.rect(CAIXA_X, CAIXA_Y, CAIXA_LARGURA, CAIXA_ALTURA);
        formas.setColor(0.10f, 0.08f, 0.07f, 1f);
        formas.rect(CAIXA_X + 20f, CAIXA_Y + 38f, CAIXA_LARGURA - 40f, 32f);
        formas.end();

        batch.setProjectionMatrix(projecao);
        batch.begin();
        fonte.getData().setScale(1.3f);
        fonte.setColor(new Color(1f, 0.82f, 0.43f, 1f));
        fonte.draw(batch, rotulo, CAIXA_X + 20f, CAIXA_Y + CAIXA_ALTURA - 15f);
        fonte.setColor(Color.WHITE);
        boolean cursor = ((int) (tempo * 2f)) % 2 == 0;
        fonte.draw(batch, texto.toString() + (cursor ? "_" : ""), CAIXA_X + 30f, CAIXA_Y + 62f);
        fonte.getData().setScale(1f);
        fonte.setColor(Color.LIGHT_GRAY);
        fonte.draw(batch, "ENTER confirma   |   ESC cancela", CAIXA_X + 20f, CAIXA_Y + 28f);
        batch.end();
    }
}
