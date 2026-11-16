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
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
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
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");

    private final UsuarioRepository usuarioRepository;
    private final TurmaRepository turmaRepository;
    private final AlunoRepository alunoRepository;
    private final UnidadeCurricularRepository unidadeCurricularRepository;
    private final AulaRepository aulaRepository;
    private final FrequenciaRepository frequenciaRepository;
    private final EventoRepository eventoRepository;
    private final OcorrenciaRepository ocorrenciaRepository;
    private final AtividadeRepository atividadeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    public DadosIniciais(UsuarioRepository usuarioRepository, TurmaRepository turmaRepository,
                         AlunoRepository alunoRepository, UnidadeCurricularRepository unidadeCurricularRepository,
                         AulaRepository aulaRepository, FrequenciaRepository frequenciaRepository,
                         EventoRepository eventoRepository, OcorrenciaRepository ocorrenciaRepository,
                         AtividadeRepository atividadeRepository, PasswordEncoder passwordEncoder,
                         AppProperties appProperties) {
        this.usuarioRepository = usuarioRepository;
        this.turmaRepository = turmaRepository;
        this.alunoRepository = alunoRepository;
        this.unidadeCurricularRepository = unidadeCurricularRepository;
        this.aulaRepository = aulaRepository;
        this.frequenciaRepository = frequenciaRepository;
        this.eventoRepository = eventoRepository;
        this.ocorrenciaRepository = ocorrenciaRepository;
        this.atividadeRepository = atividadeRepository;
        this.passwordEncoder = passwordEncoder;
        this.appProperties = appProperties;
    }

    @Override
    @Transactional
    public void run(String... args) {
        String seedSenha = appProperties.seed().senha();
        if (seedSenha == null || seedSenha.isBlank()) {
            return; // Não cria usuário demo se seedSenha não estiver configurado
        }

        if (seedSenha.length() < TAMANHO_MINIMO_SENHA) {
            log.warn("Não foi criado o usuário demo: a senha configurada em app.seed.senha é muito curta (mínimo {} caracteres).", TAMANHO_MINIMO_SENHA);
            return;
        }

        Usuario instrutor = usuarioRepository.findByLogin(LOGIN_DEMO).orElseGet(() -> {
            // CPF fictício e válido, apenas para fins de demonstração.
            Usuario usuario = new Usuario(
                "Marina Duarte Campos",
                LOGIN_DEMO,
                "marina.campos@senac.br",
                "52998224725",
                passwordEncoder.encode(seedSenha)
            );
            usuarioRepository.save(usuario);
            log.warn("Usuário demo \"instrutor\" criado a partir de app.seed.senha. NÃO use este usuário em produção.");
            return usuario;
        });

        // Atualiza o antigo usuário genérico para o perfil fictício de instrutora.
        if ("Instrutor Demo".equals(instrutor.getNome())) {
            instrutor.setNome("Marina Duarte Campos");
            instrutor.setEmail("marina.campos@senac.br");
            usuarioRepository.save(instrutor);
        }

        if (turmaRepository.findByInstrutorLoginAndAtivaTrueOrderByNome(LOGIN_DEMO).isEmpty()) {
            semearDadosDeExemplo(instrutor);
            log.info("Dados fictícios do curso Técnico em Desenvolvimento de Sistemas criados para o usuário demo.");
        }
    }

    private void semearDadosDeExemplo(Usuario instrutor) {
        LocalDate hoje = LocalDate.now(ZONA);

        Turma noturno = turmaRepository.save(new Turma(
            "TDS 2026.1 — Noturno",
            "TDS-2026-1N",
            "Técnico em Desenvolvimento de Sistemas",
            "Noturno",
            "Laboratório 03",
            8,
            4,
            1200,
            instrutor
        ));

        Turma vespertino = turmaRepository.save(new Turma(
            "TDS 2026.1 — Vespertino",
            "TDS-2026-1V",
            "Técnico em Desenvolvimento de Sistemas",
            "Vespertino",
            "Laboratório 05",
            6,
            4,
            1200,
            instrutor
        ));

        Turma webLivre = turmaRepository.save(new Turma(
            "Programação Web — Turma Livre",
            "PW-2026-01",
            "Desenvolvimento Web (Formação Inicial e Continuada)",
            "Sábado",
            "Laboratório 02",
            5,
            2,
            160,
            instrutor
        ));

        List<Aluno> alunosNoturno = alunoRepository.saveAll(List.of(
            new Aluno("Ana Beatriz Ferreira Lima", "2026001", "12345678909", noturno),
            new Aluno("Bruno Henrique Souza Alves", "2026002", "23456789092", noturno),
            new Aluno("Carla Menezes Oliveira", "2026003", "34567890175", noturno),
            new Aluno("Diego Ramos Cardoso", "2026004", "45678901249", noturno),
            new Aluno("Eduarda Nascimento Pires", "2026005", "56789012303", noturno),
            new Aluno("Felipe Andrade Moreira", "2026006", "67890123469", noturno),
            new Aluno("Gabriela Teixeira Costa", "2026007", "78901234505", noturno),
            new Aluno("Henrique Barbosa Dias", "2026008", "89012345642", noturno)
        ));

        List<Aluno> alunosVespertino = alunoRepository.saveAll(List.of(
            new Aluno("Isabela Rocha Martins", "2026101", "90123456770", vespertino),
            new Aluno("João Pedro Almeida Reis", "2026102", "10293847541", vespertino),
            new Aluno("Larissa Fernandes Pinto", "2026103", "48271530607", vespertino),
            new Aluno("Matheus Correia Nunes", "2026104", "73519084279", vespertino),
            new Aluno("Natália Ribeiro Campos", "2026105", "61827394528", vespertino),
            new Aluno("Otávio Silveira Mendes", "2026106", "90481526749", vespertino)
        ));

        List<Aluno> alunosWebLivre = alunoRepository.saveAll(List.of(
            new Aluno("Patrícia Gomes Araújo", "PW2601", "27164958319", webLivre),
            new Aluno("Rafael Monteiro Freitas", "PW2602", "85302641771", webLivre),
            new Aluno("Sofia Carvalho Batista", "PW2603", "39674125809", webLivre),
            new Aluno("Thiago Lopes Vieira", "PW2604", "14725836982", webLivre),
            new Aluno("Vitória Azevedo Cunha", "PW2605", "25836914737", webLivre)
        ));

        unidadeCurricularRepository.saveAll(List.of(
            new UnidadeCurricular("Banco de Dados", 120, noturno),
            new UnidadeCurricular("Programação de Aplicações Web", 160, noturno),
            new UnidadeCurricular("Desenvolvimento de Sistemas", 180, noturno),
            new UnidadeCurricular("Projeto Integrador", 140, noturno),
            new UnidadeCurricular("Banco de Dados", 120, vespertino),
            new UnidadeCurricular("Programação de Aplicações Web", 160, vespertino),
            new UnidadeCurricular("Desenvolvimento de Sistemas", 180, vespertino),
            new UnidadeCurricular("Projeto Integrador", 140, vespertino),
            new UnidadeCurricular("HTML, CSS e JavaScript", 80, webLivre),
            new UnidadeCurricular("APIs REST com Java", 80, webLivre)
        ));

        aulaRepository.saveAll(List.of(
            novaAula("Aula 18 — APIs REST com Spring Boot", hoje.plusDays(1), LocalTime.of(19, 0), "Laboratório 03", noturno),
            novaAula("Aula 19 — Testes automatizados com JUnit", hoje.plusDays(3), LocalTime.of(19, 0), "Laboratório 03", noturno),
            novaAula("Aula 20 — Deploy e boas práticas", hoje.plusDays(7), LocalTime.of(19, 0), "Laboratório 03", noturno),
            novaAula("Aula 14 — Modelagem de banco de dados", hoje.plusDays(2), LocalTime.of(13, 30), "Laboratório 05", vespertino),
            novaAula("Aula 15 — Consultas SQL e índices", hoje.plusDays(4), LocalTime.of(13, 30), "Laboratório 05", vespertino),
            novaAula("Encontro 6 — Consumo de APIs com fetch", hoje.plusDays(5), LocalTime.of(9, 0), "Laboratório 02", webLivre)
        ));

        semearFrequencias(noturno, alunosNoturno, 8, 6, 7);
        semearFrequencias(vespertino, alunosVespertino, 7, 4, 5);
        semearFrequencias(webLivre, alunosWebLivre, 5, 3, 4);

        eventoRepository.saveAll(List.of(
            novoEvento(instrutor, "Reunião pedagógica do curso TDS", TipoEvento.REUNIAO, hoje.plusDays(1),
                LocalTime.of(18, 0), null, "Alinhamento do planejamento das unidades curriculares do próximo semestre."),
            novoEvento(instrutor, "Entrega da Sprint 2 — Projeto Integrador", TipoEvento.ENTREGA, hoje.plusDays(2),
                LocalTime.of(13, 30), vespertino, "Apresentação do protótipo navegável e do repositório no GitHub."),
            novoEvento(instrutor, "Workshop: Git e GitHub na prática", TipoEvento.AULA, hoje.plusDays(4),
                LocalTime.of(9, 0), webLivre, "Boas práticas de versionamento, branches e pull requests."),
            novoEvento(instrutor, "Apresentação dos projetos finais", TipoEvento.PROVA, hoje.plusDays(5),
                LocalTime.of(19, 0), noturno, "Banca de avaliação dos sistemas desenvolvidos pelas equipes."),
            novoEvento(instrutor, "Feriado institucional", TipoEvento.FERIADO, hoje.plusDays(9),
                null, null, "Sem atividades acadêmicas."),
            novoEvento(instrutor, "Hackathon interno de TDS", TipoEvento.OUTRO, hoje.minusDays(3),
                LocalTime.of(19, 0), noturno, "Maratona de desenvolvimento com foco em soluções para o Senac.")
        ));

        ocorrenciaRepository.saveAll(List.of(
            novaOcorrencia(alunosNoturno.get(0), noturno, instrutor, hoje.minusDays(5),
                "Participou ativamente do hackathon interno e apoiou os colegas na revisão do código."),
            novaOcorrencia(alunosNoturno.get(7), noturno, instrutor, hoje.minusDays(4),
                "Ausências recorrentes nas aulas de banco de dados. Contato com o responsável realizado."),
            novaOcorrencia(alunosVespertino.get(3), vespertino, instrutor, hoje.minusDays(2),
                "Atraso na entrega da atividade de APIs REST; novo prazo acordado com a equipe."),
            novaOcorrencia(alunosNoturno.get(6), noturno, instrutor, hoje.minusDays(1),
                "Destaque na monitoria de lógica de programação para os colegas com mais dificuldade.")
        ));

        atividadeRepository.saveAll(List.of(
            novaAtividade(instrutor, "Turma TDS 2026.1 — Noturno criada", 30),
            novaAtividade(instrutor, "Aluno Henrique Barbosa Dias cadastrado — TDS 2026.1 — Noturno", 22),
            novaAtividade(instrutor, "Frequência registrada — TDS 2026.1 — Noturno", 1),
            novaAtividade(instrutor, "Evento \"Entrega da Sprint 2\" criado", 2),
            novaAtividade(instrutor, "Ocorrência registrada — Henrique Barbosa Dias", 4)
        ));
    }

    private Aula novaAula(String titulo, LocalDate data, LocalTime hora, String sala, Turma turma) {
        return new Aula(titulo, ZonedDateTime.of(data, hora, ZONA), sala, turma);
    }

    private void semearFrequencias(Turma turma, List<Aluno> alunos, int dias, int... alunosComFaltas) {
        List<LocalDate> datas = ultimosDiasUteis(dias);
        List<Frequencia> registros = new ArrayList<>();

        for (int d = 0; d < datas.size(); d++) {
            for (int a = 0; a < alunos.size(); a++) {
                boolean faltoso = contem(alunosComFaltas, a);
                boolean falta = faltoso ? d % 2 == 0 : (a + d) % 11 == 0;
                boolean atraso = !falta && (a + d) % 9 == 0;
                int marcadas = falta ? 0 : (atraso ? 3 : 4);
                registros.add(new Frequencia(alunos.get(a), turma, datas.get(d), !falta, marcadas));
            }
        }

        frequenciaRepository.saveAll(registros);
    }

    private List<LocalDate> ultimosDiasUteis(int quantidade) {
        List<LocalDate> datas = new ArrayList<>();
        LocalDate dia = LocalDate.now(ZONA).minusDays(1);
        while (datas.size() < quantidade) {
            DayOfWeek dow = dia.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                datas.add(dia);
            }
            dia = dia.minusDays(1);
        }
        return datas;
    }

    private boolean contem(int[] valores, int procurado) {
        for (int valor : valores) {
            if (valor == procurado) return true;
        }
        return false;
    }

    private Evento novoEvento(Usuario instrutor, String titulo, TipoEvento tipo, LocalDate data, LocalTime hora,
                              Turma turma, String descricao) {
        String turmaNome = turma != null ? turma.getNome() : null;
        Evento evento = new Evento(instrutor.getLogin(), titulo, tipo, data, hora, turmaNome, descricao);
        evento.setTurma(turma);
        return evento;
    }

    private Ocorrencia novaOcorrencia(Aluno aluno, Turma turma, Usuario instrutor, LocalDate data, String descricao) {
        return new Ocorrencia(aluno, turma, instrutor.getLogin(), data, descricao,
            LocalDateTime.of(data, LocalTime.of(20, 15)));
    }

    private Atividade novaAtividade(Usuario instrutor, String descricao, int diasAtras) {
        return new Atividade(instrutor.getLogin(), descricao,
            LocalDateTime.now(ZONA).minusDays(diasAtras).withHour(19).withMinute(30));
    }
}
