package br.com.zup.edu.universidade.controller;

import br.com.zup.edu.universidade.controller.request.AvaliacaoAlunoRequest;
import br.com.zup.edu.universidade.controller.request.RespostaQuestaoRequest;
import br.com.zup.edu.universidade.model.*;
import br.com.zup.edu.universidade.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("test")
class FazerAvaliacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

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

    private Aluno aluno;

    private Avaliacao avaliacao;

    private Questao questaoA;

    private Questao questaoB;

    private Questao questaoC;

    @BeforeEach
    void setUp() {
        this.respostaAvaliacaoRepository.deleteAll();
        this.avaliacaoRepository.deleteAll();
        this.questaoRepository.deleteAll();
        this.alunoRepository.deleteAll();

        this.aluno = new Aluno("Antonio", "AE13", LocalDate.now());
        this.alunoRepository.save(aluno);

        this.questaoA = new Questao("Quest??o A", "Resposta A", BigDecimal.ZERO);
        this.questaoB = new Questao("Quest??o B", "Resposta B", BigDecimal.ONE);
        this.questaoC = new Questao("Quest??o C", "Resposta C", BigDecimal.TEN);
        this.avaliacao = new Avaliacao(Set.of(this.questaoA, this.questaoB, this.questaoC));
        this.avaliacaoRepository.save(avaliacao);
    }

    @Test
    @DisplayName("Aluno n??o cadastrado n??o pode responder uma avalia????o")
    void alunoNaoCadastradoNaoPodeResponderUmaAvaliacao() throws Exception {

        // Cen??rio
        List<RespostaQuestaoRequest> respostas = List.of(
                new RespostaQuestaoRequest(this.questaoA.getId(), "Resposta da quest??o 1"),
                new RespostaQuestaoRequest(this.questaoB.getId(), "Resposta da quest??o 2"),
                new RespostaQuestaoRequest(this.questaoC.getId(), "Resposta da quest??o 3")
        );

        AvaliacaoAlunoRequest avaliacaoAlunoRequest = new AvaliacaoAlunoRequest(respostas);

        String payloadRequest = mapper.writeValueAsString(avaliacaoAlunoRequest);

        MockHttpServletRequestBuilder request = post(
                        "/alunos/{id}/avaliacoes/{idAvaliacao}/respostas",
                        Long.MAX_VALUE,
                        Long.MAX_VALUE
                )
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadRequest);

        // A????o e Corretude
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
    @DisplayName("Aluno n??o pode responder uma avalia????o n??o cadastrada")
    void alunoNaoPodeResponderUmaAvaliacaoNaoCadastrada() throws Exception {

        // Cen??rio
        List<RespostaQuestaoRequest> respostas = List.of(
                new RespostaQuestaoRequest(this.questaoA.getId(), "Resposta da quest??o 1"),
                new RespostaQuestaoRequest(this.questaoB.getId(), "Resposta da quest??o 2"),
                new RespostaQuestaoRequest(this.questaoC.getId(), "Resposta da quest??o 3")
        );

        AvaliacaoAlunoRequest avaliacaoAlunoRequest = new AvaliacaoAlunoRequest(respostas);

        String payloadRequest = mapper.writeValueAsString(avaliacaoAlunoRequest);

        MockHttpServletRequestBuilder request = post(
                "/alunos/{id}/avaliacoes/{idAvaliacao}/respostas",
                this.aluno.getId(),
                Long.MAX_VALUE
        )
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadRequest);

        // A????o e Corretude
        Exception resolvedException = mockMvc.perform(request)
                .andExpect(
                        status().isNotFound()
                )
                .andReturn()
                .getResolvedException();

        // Asserts
        assertNotNull(resolvedException);
        assertEquals(ResponseStatusException.class, resolvedException.getClass());
        assertEquals("Avaliacao n??o cadastrada", ((ResponseStatusException) resolvedException).getReason());

    }

    @Test
    @DisplayName("Aluno n??o pode responder uma avalia????o sem respostas")
    void alunoNaoPodeResponderUmaAvaliacaoSemRespostas() throws Exception {

        // Cen??rio
        AvaliacaoAlunoRequest avaliacaoAlunoRequest = new AvaliacaoAlunoRequest(null);

        String payloadRequest = mapper.writeValueAsString(avaliacaoAlunoRequest);

        MockHttpServletRequestBuilder request = post(
                "/alunos/{id}/avaliacoes/{idAvaliacao}/respostas",
                this.aluno.getId(),
                this.avaliacao.getId()
        )
                .header("Accept-Language", "pt-br")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadRequest);

        // A????o e Corretude
        String payloadResponse = mockMvc.perform(request)
                .andExpect(
                        status().isBadRequest()
                )
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        TypeFactory typeFactory = mapper.getTypeFactory();

        List<String> response = mapper.readValue(payloadResponse, typeFactory.constructCollectionType(
                List.class,
                String.class
        ));

        // Asserts
        assertThat(response)
                .hasSize(1)
                .contains("O campo respostas n??o deve ser nulo");

    }

    @Test
    @DisplayName("Aluno n??o pode responder uma avalia????o com resposta com dados nulos para alguma quest??o")
    void alunoNaoPodeResponderUmaAvaliacaoComRespostaComDadosNulosParaAlgumaQuestao() throws Exception {

        // Cen??rio
        List<RespostaQuestaoRequest> respostas = List.of(
                new RespostaQuestaoRequest(this.questaoA.getId(), "Resposta da quest??o 1"),
                new RespostaQuestaoRequest(this.questaoB.getId(), "Resposta da quest??o 2"),
                new RespostaQuestaoRequest(null, null)
        );

        AvaliacaoAlunoRequest avaliacaoAlunoRequest = new AvaliacaoAlunoRequest(respostas);

        String payloadRequest = mapper.writeValueAsString(avaliacaoAlunoRequest);

        MockHttpServletRequestBuilder request = post(
                "/alunos/{id}/avaliacoes/{idAvaliacao}/respostas",
                this.aluno.getId(),
                this.avaliacao.getId()
        )
                .header("Accept-Language", "pt-br")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadRequest);

        // A????o e Corretude
        String payloadResponse = mockMvc.perform(request)
                .andExpect(
                        status().isBadRequest()
                )
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        TypeFactory typeFactory = mapper.getTypeFactory();

        List<String> response = mapper.readValue(payloadResponse, typeFactory.constructCollectionType(
                List.class,
                String.class
        ));

        // Asserts
        assertThat(response)
                .hasSize(2)
                .contains("O campo respostas[2].resposta n??o deve estar em branco",
                        "O campo respostas[2].idQuestao n??o deve ser nulo");

    }

    @Test
    @DisplayName("Aluno n??o pode responder uma avalia????o com resposta para uma quest??o inv??lida")
    void alunoNaoPodeResponderUmaAvaliacaoComRespostaParaUmaQuestaoInvalida() throws Exception {

        // Cen??rio
        List<RespostaQuestaoRequest> respostas = List.of(
                new RespostaQuestaoRequest(this.questaoA.getId(), "Resposta da quest??o 1"),
                new RespostaQuestaoRequest(this.questaoB.getId(), "Resposta da quest??o 2"),
                new RespostaQuestaoRequest(-1L, "Resposta para a quest??o")
        );

        AvaliacaoAlunoRequest avaliacaoAlunoRequest = new AvaliacaoAlunoRequest(respostas);

        String payloadRequest = mapper.writeValueAsString(avaliacaoAlunoRequest);

        MockHttpServletRequestBuilder request = post(
                "/alunos/{id}/avaliacoes/{idAvaliacao}/respostas",
                this.aluno.getId(),
                this.avaliacao.getId()
        )
                .header("Accept-Language", "pt-br")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadRequest);

        // A????o e Corretude
        String payloadResponse = mockMvc.perform(request)
                .andExpect(
                        status().isBadRequest()
                )
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        TypeFactory typeFactory = mapper.getTypeFactory();

        List<String> response = mapper.readValue(payloadResponse, typeFactory.constructCollectionType(
                List.class,
                String.class
        ));

        // Asserts
        assertThat(response)
                .hasSize(1)
                .contains("O campo respostas[2].idQuestao deve ser maior que 0");

    }

    @Test
    @DisplayName("Aluno n??o pode responder uma avalia????o com resposta para uma quest??o que n??o faz parte da avalia????o")
    void alunoNaoPodeResponderUmaAvaliacaoComRespostaParaUmaQuestaoQueNaoFazParteDaAvaliacao() throws Exception {

        // Cen??rio
        List<RespostaQuestaoRequest> respostas = List.of(
                new RespostaQuestaoRequest(this.questaoA.getId(), "Resposta da quest??o 1"),
                new RespostaQuestaoRequest(this.questaoB.getId(), "Resposta da quest??o 2"),
                new RespostaQuestaoRequest(Long.MAX_VALUE, "Resposta para a quest??o")
        );

        AvaliacaoAlunoRequest avaliacaoAlunoRequest = new AvaliacaoAlunoRequest(respostas);

        String payloadRequest = mapper.writeValueAsString(avaliacaoAlunoRequest);

        MockHttpServletRequestBuilder request = post(
                "/alunos/{id}/avaliacoes/{idAvaliacao}/respostas",
                this.aluno.getId(),
                this.avaliacao.getId()
        )
                .header("Accept-Language", "pt-br")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadRequest);

        // A????o e Corretude
        Exception resolvedException = mockMvc.perform(request)
                .andExpect(
                        status().isUnprocessableEntity()
                )
                .andReturn()
                .getResolvedException();

        // Asserts
        assertNotNull(resolvedException);
        assertEquals(ResponseStatusException.class, resolvedException.getClass());
        assertEquals("Nao existe cadastro para questao com id 9223372036854775807", ((ResponseStatusException) resolvedException).getReason());

    }

    @Test
    @DisplayName("Aluno pode responder avalia????o")
    void alunoPodeResponderAvaliacao() throws Exception {

        // Cen??rio
        List<RespostaQuestaoRequest> respostas = List.of(
                new RespostaQuestaoRequest(this.questaoA.getId(), "Resposta da quest??o 1"),
                new RespostaQuestaoRequest(this.questaoB.getId(), "Resposta da quest??o 2"),
                new RespostaQuestaoRequest(this.questaoC.getId(), "Resposta da quest??o 3")
        );

        AvaliacaoAlunoRequest avaliacaoAlunoRequest = new AvaliacaoAlunoRequest(respostas);

        String payloadRequest = mapper.writeValueAsString(avaliacaoAlunoRequest);

        MockHttpServletRequestBuilder request = post(
                "/alunos/{id}/avaliacoes/{idAvaliacao}/respostas",
                this.aluno.getId(),
                this.avaliacao.getId()
        )
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadRequest);

        // A????o e Corretude
        String location = mockMvc.perform(request)
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        redirectedUrlPattern("http://localhost/alunos/*/avaliacoes/*/respostas/*")
                )
                .andReturn()
                .getResponse()
                .getHeader("location");

        int posicaoAposUltimaBarra = location.lastIndexOf("/") + 1;

        Long idRespostaAvaliacao = Long.valueOf(location.substring(posicaoAposUltimaBarra));

        Optional<RespostaAvaliacao> possivelRespostaAvaliacao = this.respostaAvaliacaoRepository.findById(idRespostaAvaliacao);

        // Asserts
        assertTrue(possivelRespostaAvaliacao.isPresent());

        RespostaAvaliacao respostaAvaliacao = possivelRespostaAvaliacao.get();
        Set<RespostaQuestao> conjuntoRespostas = respostaAvaliacao.getRespostas();

        assertThat(conjuntoRespostas)
                .hasSize(3)
                .allMatch(rq -> this.respostaQuestaoRepository.existsById(rq.getId()));

    }

}