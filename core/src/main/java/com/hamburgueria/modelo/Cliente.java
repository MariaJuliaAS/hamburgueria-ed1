package com.hamburgueria.modelo;

/**
 * Cliente da hamburgueria. A paciência diminui com o tempo (em segundos)
 * e, quando chega a zero, o cliente vai embora.
 */
public class Cliente {

    /** Abaixo dessa fração da paciência máxima o cliente é considerado impaciente. */
    private static final float LIMITE_IMPACIENTE = 0.5f;

    private final String nome;
    private final float pacienciaMaxima;
    private float pacienciaAtual;
    private Pedido pedido;
    // Posição desenhada na tela; a tela a atualiza aos poucos para o cliente "andar" até o destino.
    private float telaX = Float.NaN;
    private float telaY = Float.NaN;

    public Cliente(String nome, float pacienciaMaxima) {
        if (pacienciaMaxima <= 0f) {
            throw new IllegalArgumentException("A paciência máxima precisa ser maior que zero");
        }
        this.nome = nome;
        this.pacienciaMaxima = pacienciaMaxima;
        this.pacienciaAtual = pacienciaMaxima;
    }

    /** Diminui a paciência. O delta é o tempo do frame, em segundos. */
    public void atualizar(float delta) {
        pacienciaAtual -= delta;
        if (pacienciaAtual < 0f) {
            pacienciaAtual = 0f;
        }
    }

    public boolean acabouPaciencia() {
        return pacienciaAtual <= 0f;
    }

    /** Verdadeiro quando a paciência está na metade ou menos, mas ainda não acabou. */
    public boolean estaImpaciente() {
        return !acabouPaciencia() && getPercentualPaciencia() <= LIMITE_IMPACIENTE;
    }

    /** Valor de 1.0 (calmo) até 0.0 (foi embora). Útil para a barra de paciência na tela. */
    public float getPercentualPaciencia() {
        return pacienciaAtual / pacienciaMaxima;
    }

    public String getNome() {
        return nome;
    }

    public float getPacienciaMaxima() {
        return pacienciaMaxima;
    }

    public float getPacienciaAtual() {
        return pacienciaAtual;
    }

    public boolean temPosicaoNaTela() {
        return !Float.isNaN(telaX);
    }

    public float getTelaX() {
        return telaX;
    }

    public float getTelaY() {
        return telaY;
    }

    public void setPosicaoNaTela(float x, float y) {
        this.telaX = x;
        this.telaY = y;
    }

    public Pedido getPedido() {
        return pedido;
    }

    /** Chamado pelo próprio Pedido ao ser criado. */
    void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }
}
