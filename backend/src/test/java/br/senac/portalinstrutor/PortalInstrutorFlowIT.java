package br.senac.portalinstrutor;

import br.senac.portalinstrutor.model.Usuario;
import br.senac.portalinstrutor.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PortalInstrutorFlowIT {

    private static final String LOGIN = "instrutor";
    private static final String SENHA = "senha12345";

    @Autowired
    MockMvc mockMvc;
    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void seedUsuario() {
        if (usuarioRepository.findByLogin(LOGIN).isEmpty()) {
            usuarioRepository.save(new Usuario("Instrutor Teste", LOGIN, "instrutor@teste.com",
                "52998224725", passwordEncoder.encode(SENHA)));
        }
    }

    @Test
    void exigeAutenticacaoParaAcessarDashboard() throws Exception {
        mockMvc.perform(get("/api/dashboard")).andExpect(status().isUnauthorized());
    }

    @Test
    void logoutInvalidaSessao() throws Exception {
        MockHttpSession session = login();

        mockMvc.perform(post("/api/auth/logout").session(session))
            .andExpect(status().isNoContent())
            .andExpect(cookie().maxAge("JSESSIONID", 0));

        mockMvc.perform(get("/api/dashboard")).andExpect(status().isUnauthorized());
    }

    @Test
    void fluxoCompletoDoInstrutor() throws Exception {
        MockHttpSession session = login();

        String turmaResp = mockMvc.perform(post("/api/turmas").session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"Turma Teste","codigo":"T1","curso":"Curso","turno":"Manha","sala":"Sala 1",
                     "qtdAlunos":10,"qtdUcs":2,"horas":40}
                    """))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        Number turmaId = JsonPath.read(turmaResp, "$.id");

        String alunoResp = mockMvc.perform(post("/api/turmas/" + turmaId + "/alunos").session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"Aluno Teste","matricula":"A1","cpf":"529.982.247-25"}
                    """))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        Number alunoId = JsonPath.read(alunoResp, "$.id");

        String freqBody = """
            {"data":"2026-06-10","registros":[{"alunoId":%d,"presente":true}]}
            """.formatted(alunoId.longValue());
        mockMvc.perform(put("/api/turmas/" + turmaId + "/frequencias").session(session)
                .contentType(MediaType.APPLICATION_JSON).content(freqBody))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/turmas/" + turmaId + "/frequencias")
                .param("data", "2026-06-10").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.jaRegistrada").value(true))
            .andExpect(jsonPath("$.alunos[0].id").value(alunoId.longValue()));

        String avaliacaoResp = mockMvc.perform(post("/api/turmas/" + turmaId + "/avaliacoes").session(session)
                .contentType(MediaType.APPLICATION_JSON).content("{\"titulo\":\"Prova 1\"}"))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        Number avaliacaoId = JsonPath.read(avaliacaoResp, "$.id");

        String notaBody = """
            {"registros":[{"alunoId":%d,"valor":80.0}]}
            """.formatted(alunoId.longValue());
        mockMvc.perform(put("/api/avaliacoes/" + avaliacaoId + "/notas").session(session)
                .contentType(MediaType.APPLICATION_JSON).content(notaBody))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/avaliacoes/" + avaliacaoId + "/notas").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.notas[0].alunoId").value(alunoId.longValue()))
            .andExpect(jsonPath("$.notas[0].valor").value(80.0));

        mockMvc.perform(get("/api/dashboard").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totais.turmas").value(1))
            .andExpect(jsonPath("$.totais.alunos").value(1));
    }

    @Test
    void todosOsEndpointsRespondem() throws Exception {
        MockHttpSession session = login();

        String turmaResp = mockMvc.perform(post("/api/turmas").session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"Turma Smoke","codigo":"SMK","curso":"Curso","turno":"Noite","sala":"S9",
                     "qtdAlunos":5,"qtdUcs":1,"horas":20}
                    """))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        long turmaId = ((Number) JsonPath.read(turmaResp, "$.id")).longValue();

        String alunoResp = mockMvc.perform(post("/api/turmas/" + turmaId + "/alunos").session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"Aluno Smoke","matricula":"SM1","cpf":"111.444.777-35"}
                    """))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        long alunoId = ((Number) JsonPath.read(alunoResp, "$.id")).longValue();

        mockMvc.perform(get("/api/auth/me").session(session)).andExpect(status().isOk());
        mockMvc.perform(get("/api/turmas").session(session)).andExpect(status().isOk());
        mockMvc.perform(get("/api/turmas/resumo").session(session)).andExpect(status().isOk());
        mockMvc.perform(get("/api/turmas/alunos/contagem").param("ids", String.valueOf(turmaId)).session(session))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/turmas/ucs/contagem").param("ids", String.valueOf(turmaId)).session(session))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/turmas/" + turmaId + "/alunos").session(session)).andExpect(status().isOk());
        mockMvc.perform(get("/api/turmas/" + turmaId + "/frequencias").param("data", "2026-06-10").session(session))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/turmas/" + turmaId + "/avaliacoes").session(session)).andExpect(status().isOk());
        mockMvc.perform(get("/api/turmas/" + turmaId + "/ocorrencias").session(session)).andExpect(status().isOk());
        mockMvc.perform(post("/api/turmas/" + turmaId + "/ocorrencias").session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"alunoId\":" + alunoId + ",\"data\":\"2026-06-11\",\"descricao\":\"Ocorrencia smoke\"}"))
            .andExpect(status().isCreated());

        String ucResp = mockMvc.perform(post("/api/turmas/" + turmaId + "/ucs").session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"UC Smoke\",\"cargaHoraria\":40}"))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        long ucId = ((Number) JsonPath.read(ucResp, "$.id")).longValue();

        mockMvc.perform(get("/api/turmas/" + turmaId + "/ucs").session(session)).andExpect(status().isOk());
        mockMvc.perform(put("/api/ucs/" + ucId).session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"UC Smoke Editada\",\"cargaHoraria\":60}"))
            .andExpect(status().isOk());
        mockMvc.perform(delete("/api/ucs/" + ucId).session(session)).andExpect(status().isNoContent());

        String eventoResp = mockMvc.perform(post("/api/eventos").session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\":\"Evento Smoke\",\"tipo\":\"PROVA\",\"data\":\"2026-06-15\","
                    + "\"hora\":\"08:00:00\",\"turmaNome\":\"Turma Smoke\",\"descricao\":\"desc\"}"))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        long eventoId = ((Number) JsonPath.read(eventoResp, "$.id")).longValue();

        mockMvc.perform(get("/api/eventos").param("inicio", "2026-06-01").param("fim", "2026-06-30").session(session))
            .andExpect(status().isOk());
        mockMvc.perform(put("/api/eventos/" + eventoId).session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\":\"Evento Smoke Editado\",\"tipo\":\"PROVA\",\"data\":\"2026-06-16\"}"))
            .andExpect(status().isOk());
        mockMvc.perform(delete("/api/eventos/" + eventoId).session(session)).andExpect(status().isNoContent());

        String aulaResp = mockMvc.perform(post("/api/aulas").session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\":\"Aula Smoke\",\"dataHora\":\"2026-06-10T19:00:00-03:00\","
                    + "\"sala\":\"Sala 9\",\"turmaId\":" + turmaId + "}"))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        long aulaId = ((Number) JsonPath.read(aulaResp, "$.id")).longValue();

        mockMvc.perform(get("/api/aulas")
                .param("inicio", "2026-06-01T00:00:00-03:00")
                .param("fim", "2026-06-30T23:59:59-03:00").session(session))
            .andExpect(status().isOk());
        mockMvc.perform(put("/api/aulas/" + aulaId).session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\":\"Aula Smoke Editada\",\"dataHora\":\"2026-06-11T19:00:00-03:00\","
                    + "\"sala\":\"Sala 10\",\"turmaId\":" + turmaId + "}"))
            .andExpect(status().isOk());
        mockMvc.perform(delete("/api/aulas/" + aulaId).session(session)).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/atividades").session(session)).andExpect(status().isOk());
        mockMvc.perform(delete("/api/turmas/" + turmaId).session(session)).andExpect(status().isNoContent());
    }

    private MockHttpSession login() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"identificador\":\"instrutor\",\"senha\":\"senha12345\"}"))
            .andExpect(status().isOk())
            .andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(session).isNotNull();
        return session;
    }
}
