package br.com.zup.edu.universidade.controller.request;

import br.com.zup.edu.universidade.model.Aluno;
import br.com.zup.edu.universidade.model.Avaliacao;
import br.com.zup.edu.universidade.model.RespostaAvaliacao;
import br.com.zup.edu.universidade.model.RespostaQuestao;
import br.com.zup.edu.universidade.repository.AvaliacaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

public class AvaliacaoAlunoRequest {

    @NotNull
    private List<@Valid RespostaQuestaoRequest> respostas;

    public AvaliacaoAlunoRequest(List<RespostaQuestaoRequest> respostas) {

        this.respostas = respostas;
    }

    public AvaliacaoAlunoRequest() {
    }

    public RespostaAvaliacao paraRespostaAvaliacao(Aluno aluno, Avaliacao avaliacao) {


        Set<RespostaQuestao> respostasQuestoes = respostas.stream()
                .map(request -> request.paraRespostaQuestao(aluno, avaliacao.getQuestoes()))
                .collect(Collectors.toSet());

        return new RespostaAvaliacao(aluno, avaliacao, respostasQuestoes);
    }


    public List<RespostaQuestaoRequest> getRespostas() {
        return respostas;
    }
}
