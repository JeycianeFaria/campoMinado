package com.example.campominado.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Tabuleiro implements CampoObservador{

    private int linhas;
    private int colunas;
    private int minas;

    private final List<Campo> campos = new ArrayList<>();
    private final List<Consumer<Boolean>> observadores = new ArrayList<>();


    public Tabuleiro(int linhas, int colunas, int minas) {
        this.linhas = linhas;
        this.colunas = colunas;
        this.minas = minas;

        gerarCampos();
        associarVizinhos();
        sortearAsMinas();

    }

    public void registrarObservadores(Consumer<Boolean> observador){
        observadores.add(observador);
    }

    private void notificarObservadores(boolean resultado) {
        observadores.stream()
                .forEach(o -> o.accept(resultado));
    }

    public Campo getCampo(int linha, int coluna) {

        var campoOptional = campos.stream()
                .filter(campo -> campo.getLinha() == linha && campo.getColuna() == coluna)
                .findFirst();

        if (campoOptional.isEmpty()) {
            //TODO Implementar nova versao
        }

        return campoOptional.get();
    }


    public void abrirCampo(int linha, int coluna) {

        try {

            campos.parallelStream()
                    .filter(c -> c.getLinha() == linha && c.getColuna() == coluna)
                    .findFirst()
                    .ifPresent(c -> c.abrir());

        } catch (Exception e) {
            //FIXME Ajustar implementação do método abrir
            campos.forEach(c -> c.setAberto(true));
            throw e;

        }

    }

    public void mostrarMinas(){
        campos.stream()
                .filter(c -> c.isMinado())
                .forEach(c -> c.setAberto(true));
    }

    public void marcarCampo(int linha, int coluna) {
        campos.parallelStream()
                .filter(c -> c.getLinha() == linha && c.getColuna() == coluna)
                .findFirst()
                .ifPresent(c -> c.alternarMarcacao());
    }

    private void gerarCampos() {

        for (int linha = 0; linha < linhas; linha++) {
            for (int coluna = 0; coluna < colunas; coluna++) {

                Campo campo = new Campo(linha,coluna );
                campo.registrarObservadores(this);
                campos.add(campo);

            }
        }

    }

    private void associarVizinhos() {
        for (Campo c1 : campos) {
            for (Campo c2 : campos) {
                c1.adicionarVizinho(c2);
            }
        }
    }

    private void sortearAsMinas() {
        long minasArmadas = 0;
        Predicate<Campo> minado = c -> c.isMinado();

        do {

            int aleatorio = (int) (Math.random() * campos.size());
            campos.get(aleatorio).minar();
            minasArmadas = campos.stream().filter(minado).count();

        } while (minasArmadas < minas);


    }

    public boolean objetivoAlcancado() {
        return campos.stream().allMatch(c -> c.objetivoAlcancado());
    }

    public void reiniciar() {
        campos.stream().forEach(c -> c.reiniciar());
        sortearAsMinas();
    }

    @Override
    public void eventoOcorreu(Campo campo, CampoEvento evento) {

        if (evento == CampoEvento.EXPLODIR){
            mostrarMinas();
            notificarObservadores(false);
        } else if (objetivoAlcancado()){
            System.out.println("Ganhou...");
            notificarObservadores(true);
        }

    }

}
