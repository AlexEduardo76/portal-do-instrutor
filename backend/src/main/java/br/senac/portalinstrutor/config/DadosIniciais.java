package br.senac.portalinstrutor.config;

import br.senac.portalinstrutor.model.Aluno;
import br.senac.portalinstrutor.model.Atividade;
import br.senac.portalinstrutor.model.Aula;
import br.senac.portalinstrutor.model.Evento;
import br.senac.portalinstrutor.model.Frequencia;
import br.senac.portalinstrutor.model.Ocorrencia;
import br.senac.portalinstrutor.model.Turma;
import br.senac.portalinstrutor.model.UnidadeCurricular;
import br.senac.portalinstrutor.model.Usuario;
import br.senac.portalinstrutor.model.enums.TipoEvento;
import br.senac.portalinstrutor.repository.AlunoRepository;
import br.senac.portalinstrutor.repository.AtividadeRepository;
import br.senac.portalinstrutor.repository.AulaRepository;
import br.senac.portalinstrutor.repository.EventoRepository;
import br.senac.portalinstrutor.repository.FrequenciaRepository;
import br.senac.portalinstrutor.repository.OcorrenciaRepository;
import br.senac.portalinstrutor.repository.TurmaRepository;
import br.senac.portalinstrutor.repository.UnidadeCurricularRepository;
import br.senac.portalinstrutor.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("!test")
public class DadosIniciais implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosIniciais.class);
    private static final int TAMANHO_MINIMO_SENHA = 8;
    private static final String LOGIN_DEMO = "instrutor";
    private static final String NOME_DEMO = "Marina Duarte Campos";
    private static final String EMAIL_DEMO = "marina.campos@senac.br";
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private final UsuarioRepository usuarioRepository;
    private final TurmaRepository turmaRepository;
    private final AlunoRepository alunoRepository;
    private final UnidadeCurricularRepository unidadeCurricularRepository;
    private final AulaRepository aulaRepository;
    private final EventoRepository eventoRepository;
    private final FrequenciaRepository frequenciaRepository;
    private final OcorrenciaRepository ocorrenciaRepository;
    private final AtividadeRepository atividadeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    public DadosIniciais(UsuarioRepository usuarioRepository, TurmaRepository turmaRepository,
                         AlunoRepository alunoRepository, UnidadeCurricularRepository unidadeCurricularRepository,
                         AulaRepository aulaRepository, EventoRepository eventoRepository,
                         FrequenciaRepository frequenciaRepository, OcorrenciaRepository ocorrenciaRepository,
                         AtividadeRepository atividadeRepository, PasswordEncoder passwordEncoder,
                         AppProperties appProperties) {
        this.usuarioRepository = usuarioRepository;
        this.turmaRepository = turmaRepository;
        this.alunoRepository = alunoRepository;
        this.unidadeCurricularRepository = unidadeCurricularRepository;
        this.aulaRepository = aulaRepository;
        this.eventoRepository = eventoRepository;
        this.frequenciaRepository = frequenciaRepository;
        this.ocorrenciaRepository = ocorrenciaRepository;
        this.atividadeRepository = atividadeRepository;
        this.passwordEncoder = passwordEncoder;
        this.appProperties = appProperties;
    }

    @Override
    public void run(String... args) {
        String seedSenha = appProperties.seed().senha();
        if (seedSenha == null || seedSenha.isBlank()) {
            return;
        }

        if (seedSenha.length() < TAMANHO_MINIMO_SENHA) {
            log.warn("Não foi criado o usuário demo: a senha configurada em app.seed.senha é muito curta (mínimo {} caracteres).", TAMANHO_MINIMO_SENHA);
            return;
        }

        try {
            Usuario usuario = usuarioRepository.findByLogin(LOGIN_DEMO).orElseGet(() -> criarUsuarioDemo(seedSenha));

            if (!turmaRepository.findByInstrutorLoginAndAtivaTrueOrderByNome(LOGIN_DEMO).isEmpty()) {
                return;
            }

            semearDadosDemo(usuario);
            log.warn("Dados fictícios SENAC criados para o usuário demo \"{}\". NÃO use este usuário em produção.", LOGIN_DEMO);
        } catch (Exception ex) {
            // Um conflito de dados não pode impedir a aplicação de subir.
            log.error("Não foi possível criar os dados de demonstração. A aplicação continuará sem seed.", ex);
        }
    }

    private Usuario criarUsuarioDemo(String seedSenha) {
        Usuario usuario = new Usuario(
            NOME_DEMO,
            LOGIN_DEMO,
            EMAIL_DEMO,
            "52998224725",
            passwordEncoder.encode(seedSenha)
        );
        return usuarioRepository.save(usuario);
    }

    private void semearDadosDemo(Usuario instrutor) {
        String login = instrutor.getLogin();
        int ano = LocalDate.now(FUSO).getYear();

        Turma tds = criarTurma(instrutor,
            "TDS " + ano + ".2 · Noturno", "TDS-" + ano + "-2N",
            "Técnico em Desenvolvimento de Sistemas", "Noturno", "Lab. 04", 1200);

        Turma redes = criarTurma(instrutor,
            "Redes " + ano + ".2 · Noturno", "RED-" + ano + "-2N",
            "Técnico em Redes de Computadores", "Noturno", "Lab. 02", 1000);

        Turma seguranca = criarTurma(instrutor,
            "Segurança " + ano + ".2 · Vespertino", "SEG-" + ano + "-2V",
            "Técnico em Segurança da Informação", "Vespertino", "Lab. 06", 1000);

        Turma web = criarTurma(instrutor,
            "Programação Web · Sábados", "WEB-" + ano + "-1S",
            "Programação Web com JavaScript", "Sábados", "Lab. 01", 60);

        criarUcs(tds, "Lógica de Programação", 120);
        criarUcs(tds, "Programação Orientada a Objetos", 160);
        criarUcs(tds, "Banco de Dados Relacional", 120);
        criarUcs(tds, "Desenvolvimento Web Front-end", 160);
        criarUcs(tds, "Projeto Integrador", 100);

        criarUcs(redes, "Fundamentos de Redes", 120);
        criarUcs(redes, "Infraestrutura e Servidores", 160);
        criarUcs(redes, "Segurança de Redes", 100);
        criarUcs(redes, "Computação em Nuvem", 80);

        criarUcs(seguranca, "Gestão de Riscos e LGPD", 100);
        criarUcs(seguranca, "Testes de Invasão", 120);
        criarUcs(seguranca, "Resposta a Incidentes", 100);
        criarUcs(seguranca, "Criptografia Aplicada", 80);

        criarUcs(web, "HTML e CSS Modernos", 20);
        criarUcs(web, "JavaScript Essencial", 24);
        criarUcs(web, "Projeto Final Web", 16);

        List<Aluno> alunosTds = criarAlunos(tds, 1,
            "Ana Beatriz Ferreira Lima", "Bruno Henrique Almeida Souza", "Camila Rodrigues Santos",
            "Daniel Oliveira Martins", "Eduarda Nunes Carvalho", "Felipe Augusto Ribeiro",
            "Gabriela Mendes Costa", "Henrique Barbosa Pereira");

        List<Aluno> alunosRedes = criarAlunos(redes, 2,
            "Igor Siqueira Alves", "Jéssica Moreira Lopes", "Kaique Fernandes Rocha",
            "Larissa Gomes Azevedo", "Matheus Teixeira Dias", "Natália Campos Freitas");

        List<Aluno> alunosSeguranca = criarAlunos(seguranca, 3,
            "Otávio Cardoso Nogueira", "Patrícia Almeida Ramos", "Rafael Monteiro Pires",
            "Sabrina Duarte Melo", "Thiago Batista Correia");

        List<Aluno> alunosWeb = criarAlunos(web, 4,
            "Ursula Pinto Macedo", "Vinícius Araújo Reis", "Wesley Prado Farias",
            "Yasmin Lopes Andrade", "Zeca Ramos Vieira", "Amanda Castro Brito", "Caio Mendes Furtado");

        atualizarTurma(tds, alunosTds.size(), 5);
        atualizarTurma(redes, alunosRedes.size(), 4);
        atualizarTurma(seguranca, alunosSeguranca.size(), 4);
        atualizarTurma(web, alunosWeb.size(), 3);

        registrarFrequencias(tds, alunosTds);
        registrarFrequencias(redes, alunosRedes);
        registrarFrequencias(seguranca, alunosSeguranca);
        registrarFrequencias(web, alunosWeb);

        ZonedDateTime agora = ZonedDateTime.now(FUSO);
        criarAula("Lógica de Programação e Algoritmos", agora.plusDays(1).withHour(19).withMinute(0), tds);
        criarAula("Programação Orientada a Objetos", agora.plusDays(3).withHour(19).withMinute(0), tds);
        criarAula("Banco de Dados Relacional", agora.plusDays(5).withHour(19).withMinute(0), tds);
        criarAula("Infraestrutura e Servidores", agora.plusDays(2).withHour(19).withMinute(30), redes);
        criarAula("Segurança de Redes", agora.plusDays(6).withHour(19).withMinute(30), redes);
        criarAula("Testes de Invasão", agora.plusDays(4).withHour(14).withMinute(0), seguranca);
        criarAula("Resposta a Incidentes", agora.plusDays(7).withHour(14).withMinute(0), seguranca);
        criarAula("JavaScript Essencial", agora.plusDays(4).withHour(9).withMinute(0), web);
        criarAula("Projeto Final Web", agora.plusDays(11).withHour(9).withMinute(0), web);

        criarEventos(login, tds, redes, seguranca, web);

        criarOcorrencias(login, tds, alunosTds);

        criarAtividades(login);

        log.info("Seed SENAC: {} turmas, {} alunos, {} UCs, {} aulas, {} eventos.",
            turmaRepository.count(), alunoRepository.count(), unidadeCurricularRepository.count(),
            aulaRepository.count(), eventoRepository.count());
    }

    private Turma criarTurma(Usuario instrutor, String nome, String codigo, String curso,
                             String turno, String sala, int horas) {
        return turmaRepository.save(new Turma(nome, codigo, curso, turno, sala, 0, 0, horas, instrutor));
    }

    private void atualizarTurma(Turma turma, int alunos, int ucs) {
        turma.setQtdAlunos(alunos);
        turma.setQtdUcs(ucs);
        turmaRepository.save(turma);
    }

    private void criarUcs(Turma turma, String nome, int cargaHoraria) {
        unidadeCurricularRepository.save(new UnidadeCurricular(nome, cargaHoraria, turma));
    }

    private List<Aluno> criarAlunos(Turma turma, int base, String... nomes) {
        List<Aluno> alunos = new ArrayList<>();
        int sequencia = 1;
        for (String nome : nomes) {
            String matricula = String.valueOf(20260000 + base * 100 + sequencia);
            alunos.add(alunoRepository.save(new Aluno(nome, matricula, null, turma)));
            sequencia++;
        }
        return alunos;
    }

    private void registrarFrequencias(Turma turma, List<Aluno> alunos) {
        LocalDate hoje = LocalDate.now(FUSO);
        List<LocalDate> datas = List.of(hoje.minusWeeks(3), hoje.minusWeeks(2), hoje.minusWeeks(1));

        for (int i = 0; i < alunos.size(); i++) {
            Aluno aluno = alunos.get(i);
            int faltas = i % 6 == 0 ? 2 : (i % 4 == 0 ? 1 : 0);

            for (int d = 0; d < datas.size(); d++) {
                boolean presente = d >= faltas;
                frequenciaRepository.save(new Frequencia(aluno, turma, datas.get(d), presente, presente ? 4 : 0));
            }
        }
    }

    private void criarAula(String titulo, ZonedDateTime dataHora, Turma turma) {
        aulaRepository.save(new Aula(titulo, dataHora.withSecond(0).withNano(0), turma.getSala(), turma));
    }

    private void criarEventos(String login, Turma tds, Turma redes, Turma seguranca, Turma web) {
        LocalDate hoje = LocalDate.now(FUSO);

        criarEvento(login, "Avaliação de Lógica de Programação", TipoEvento.PROVA,
            hoje.plusDays(8), LocalTime.of(19, 0), "Lab. 04", tds);

        criarEvento(login, "Entrega do Projeto Integrador", TipoEvento.ENTREGA,
            hoje.plusDays(15), LocalTime.of(23, 59), "Ambiente virtual", tds);

        criarEvento(login, "Reunião pedagógica do semestre", TipoEvento.REUNIAO,
            hoje.plusDays(5), LocalTime.of(10, 0), "Sala dos professores", null);

        criarEvento(login, "Semana SENAC de Tecnologia", TipoEvento.OUTRO,
            hoje.plusDays(21), LocalTime.of(8, 0), "Auditório", null);

        criarEvento(login, "Manutenção dos laboratórios de redes", TipoEvento.ANOTACAO,
            hoje.plusDays(3), LocalTime.of(18, 0), "Lab. 02", redes);

        criarEvento(login, "Simulado de resposta a incidentes", TipoEvento.PROVA,
            hoje.plusDays(12), LocalTime.of(14, 0), "Lab. 06", seguranca);

        criarEvento(login, "Workshop de JavaScript moderno", TipoEvento.CONTEUDO,
            hoje.plusDays(10), LocalTime.of(9, 0), "Lab. 01", web);

        criarEvento(login, "Conselho de classe", TipoEvento.REUNIAO,
            hoje.plusDays(28), LocalTime.of(16, 0), "Sala 12", null);
    }

    private void criarEvento(String login, String titulo, TipoEvento tipo, LocalDate data,
                             LocalTime hora, String turmaNome, Turma turma) {
        Evento evento = new Evento(login, titulo, tipo, data, hora, turmaNome, null);
        evento.setTurma(turma);
        eventoRepository.save(evento);
    }

    private void criarOcorrencias(String login, Turma turma, List<Aluno> alunos) {
        if (alunos.size() < 3) return;

        LocalDate hoje = LocalDate.now(FUSO);
        LocalDateTime agora = LocalDateTime.now(FUSO);

        ocorrenciaRepository.save(new Ocorrencia(
            alunos.get(0), turma, login, hoje.minusDays(2),
            "Aluno chegou 20 minutos atrasado. Conversado sobre a importância da pontualidade e registrado o atraso na frequência.",
            agora.minusDays(2)));

        ocorrenciaRepository.save(new Ocorrencia(
            alunos.get(1), turma, login, hoje.minusDays(5),
            "Dificuldade recorrente em lógica de programação. Encaminhado para monitoria no contraturno.",
            agora.minusDays(5)));

        ocorrenciaRepository.save(new Ocorrencia(
            alunos.get(2), turma, login, hoje.minusDays(1),
            "Apresentou o protótipo do projeto integrador dentro do prazo. Bom domínio de banco de dados.",
            agora.minusDays(1)));
    }

    private void criarAtividades(String login) {
        LocalDateTime agora = LocalDateTime.now(FUSO);

        atividadeRepository.save(new Atividade(login,
            "Frequência da turma TDS " + LocalDate.now(FUSO).getYear() + ".2 lançada", agora.minusHours(2)));
        atividadeRepository.save(new Atividade(login,
            "Ocorrência registrada para Ana Beatriz Ferreira Lima", agora.minusHours(7)));
        atividadeRepository.save(new Atividade(login,
            "Unidade curricular Projeto Integrador adicionada à turma TDS", agora.minusDays(1)));
        atividadeRepository.save(new Atividade(login,
            "Evento \"Entrega do Projeto Integrador\" criado no calendário", agora.minusDays(2)));
        atividadeRepository.save(new Atividade(login,
            "Turma Programação Web · Sábados atualizada", agora.minusDays(3)));
    }
}
