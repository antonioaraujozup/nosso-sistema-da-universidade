package br.com.zup.edu.universidade.controller;

import br.com.zup.edu.universidade.model.*;
import br.com.zup.edu.universidade.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("test")
class RemoverAlunoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private QuestaoRepository questaoRepository;

    @Autowired
    private RespostaAvaliacaoRepository respostaAvaliacaoRepository;

    @Autowired
    private RespostaQuestaoRepository respostaQuestaoRepository;

    @BeforeEach
    void setUp() {
        this.respostaQuestaoRepository.deleteAll();
        this.respostaAvaliacaoRepository.deleteAll();
        this.avaliacaoRepository.deleteAll();
        this.questaoRepository.deleteAll();
        this.alunoRepository.deleteAll();
    }

    @Test
    @DisplayName("Não deve remover um aluno não cadastrado")
    void naoDeveRemoverUmAlunoNaoCadastrado() throws Exception {

        // Cenário
        MockHttpServletRequestBuilder request = delete("/alunos/{id}", Long.MAX_VALUE);

        // Ação e Corretude
        Exception resolvedException = mockMvc.perform(request)
                .andExpect(
                        status().isNotFound()
                )
                .andReturn()
                .getResolvedException();

        // Asserts
        assertNotNull(resolvedException);
        assertEquals(ResponseStatusException.class, resolvedException.getClass());
        assertEquals("aluno nao cadastrado", ((ResponseStatusException) resolvedException).getReason());

    }

    @Test
    @DisplayName("Deve remover um aluno")
    void deveRemoverUmAluno() throws Exception {

        // Cenário
        Aluno aluno = new Aluno("Antonio", "AE13", LocalDate.now());
        this.alunoRepository.save(aluno);

        Questao questaoA = new Questao("Questão A", "Resposta A", BigDecimal.ZERO);
        Questao questaoB = new Questao("Questão B", "Resposta B", BigDecimal.ONE);
        Questao questaoC = new Questao("Questão C", "Resposta C", BigDecimal.TEN);
        Avaliacao avaliacao = new Avaliacao(Set.of(questaoA, questaoB, questaoC));
        this.avaliacaoRepository.save(avaliacao);

        RespostaQuestao respostaQuestaoA = new RespostaQuestao(aluno, questaoA, "Resposta da questão A");
        RespostaQuestao respostaQuestaoB = new RespostaQuestao(aluno, questaoB, "Resposta da questão B");
        RespostaQuestao respostaQuestaoC = new RespostaQuestao(aluno, questaoC, "Resposta da questão C");
        RespostaAvaliacao respostaAvaliacao = new RespostaAvaliacao(aluno, avaliacao, Set.of(respostaQuestaoA, respostaQuestaoB, respostaQuestaoC));
        this.respostaAvaliacaoRepository.save(respostaAvaliacao);

        MockHttpServletRequestBuilder request = delete("/alunos/{id}", aluno.getId());

        // Ação e Corretude
        mockMvc.perform(request)
                .andExpect(
                        status().isNoContent()
                );

        // Asserts
        assertFalse(this.alunoRepository.existsById(aluno.getId()));
        assertFalse(this.respostaAvaliacaoRepository.existsById(respostaAvaliacao.getId()));
        assertThat(respostaAvaliacao.getRespostas())
                .allMatch(rq -> !this.respostaQuestaoRepository.existsById(rq.getId()));

    }

}