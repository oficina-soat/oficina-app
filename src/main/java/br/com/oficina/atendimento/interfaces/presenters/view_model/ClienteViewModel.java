package br.com.oficina.atendimento.interfaces.presenters.view_model;

public record ClienteViewModel(long id, long pessoaId, String documento, String nome, String email) {
}
