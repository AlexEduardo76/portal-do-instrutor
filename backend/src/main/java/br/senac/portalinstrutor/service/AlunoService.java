package br.senac.portalinstrutor.service;

import br.senac.portalinstrutor.dto.request.AlunoRequest;
import br.senac.portalinstrutor.dto.response.AlunoResponse;
import br.senac.portalinstrutor.exception.ConflitoException;
import br.senac.portalinstrutor.exception.RecursoNaoEncontradoException;
import br.senac.portalinstrutor.exception.RegraNegocioException;
import br.senac.portalinstrutor.mapper.AlunoMapper;
import br.senac.portalinstrutor.model.Aluno;
import br.senac.portalinstrutor.model.Turma;
import br.senac.portalinstrutor.repository.AlunoRepository;
import br.senac.portalinstrutor.util.CpfUtil;
import br.senac.portalinstrutor.util.TextoUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final TurmaService turmaService;
    private final AtividadeService atividadeService;
    private final UsuarioLogadoService usuarioLogadoService;

    public AlunoService(AlunoRepository alunoRepository, TurmaService turmaService, AtividadeService atividadeService, UsuarioLogadoService usuarioLogadoService) {
        this.alunoRepository = alunoRepository;
        this.turmaService = turmaService;
        this.atividadeService = atividadeService;
        this.usuarioLogadoService = usuarioLogadoService;
    }

    @Transactional(readOnly = true)
    public List<AlunoResponse> listarPorTurma(Long turmaId) {
        turmaService.buscarPorId(turmaId); // valida se a turma pertence ao instrutor
        return alunoRepository.findByTurmaIdAndAtivoTrueOrderByNome(turmaId).stream()
            .map(AlunoMapper::toResponse)
            .toList();
    }

    @Transactional
    public AlunoResponse criar(Long turmaId, AlunoRequest request) {
        Turma turma = turmaService.buscarPorId(turmaId);

        String nome = TextoUtil.trim(request.nome());
        String matricula = TextoUtil.trim(request.matricula());
        String cpf = normalizarCpf(request.cpf());

        if (alunoRepository.existsByTurmaIdAndMatriculaIgnoreCaseAndAtivoTrue(turmaId, matricula)) {
            throw new ConflitoException("Já existe um aluno com essa matrícula nesta turma.");
        }
        if (cpf != null && alunoRepository.existsByTurmaIdAndCpfAndAtivoTrue(turmaId, cpf)) {
            throw new ConflitoException("Já existe um aluno com esse CPF nesta turma.");
        }

        // B6: reativa aluno removido (soft delete) com mesma matrícula/CPF em vez de inserir novo.
        Optional<Aluno> inativo = alunoRepository.findFirstByTurmaIdAndMatriculaIgnoreCaseAndAtivoFalse(turmaId, matricula);
        if (inativo.isEmpty() && cpf != null) {
            inativo = alunoRepository.findFirstByTurmaIdAndCpfAndAtivoFalse(turmaId, cpf);
        }
        if (inativo.isPresent()) {
            Aluno aluno = inativo.get();
            aluno.setNome(nome);
            aluno.setMatricula(matricula);
            aluno.setCpf(cpf);
            aluno.setAtivo(true);
            aluno = alunoRepository.save(aluno);
            atividadeService.registrar("Aluno " + aluno.getNome() + " readicionado à turma");
            return AlunoMapper.toResponse(aluno);
        }

        Aluno aluno = AlunoMapper.toEntity(nome, matricula, cpf, turma);
        aluno = alunoRepository.save(aluno);

        atividadeService.registrar("Aluno " + aluno.getNome() + " cadastrado na turma");

        return AlunoMapper.toResponse(aluno);
    }

    @Transactional
    public AlunoResponse atualizar(Long id, AlunoRequest request) {
        String login = usuarioLogadoService.getLogin();
        Aluno aluno = alunoRepository.findByIdAndInstrutorLogin(id, login)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno não encontrado."));

        String nome = TextoUtil.trim(request.nome());
        String matricula = TextoUtil.trim(request.matricula());
        String cpf = normalizarCpf(request.cpf());

        var duplicado = alunoRepository.findDuplicadoNaTurma(aluno.getTurma().getId(), id, matricula, cpf != null ? cpf : "");
        if (duplicado.isPresent()) {
            throw new ConflitoException("Já existe um aluno com essa matrícula ou CPF nesta turma (" + duplicado.get().getNome() + ").");
        }

        AlunoMapper.updateEntity(aluno, nome, matricula);
        aluno.setCpf(cpf);
        aluno = alunoRepository.save(aluno);

        return AlunoMapper.toResponse(aluno);
    }

    @Transactional
    public void remover(Long id) {
        String login = usuarioLogadoService.getLogin();
        Aluno aluno = alunoRepository.findByIdAndInstrutorLogin(id, login)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno não encontrado."));

        String nome = aluno.getNome();
        aluno.setAtivo(false);
        alunoRepository.save(aluno);

        atividadeService.registrar("Aluno " + nome + " removido da turma");
    }

    private String normalizarCpf(String cpfBruto) {
        if (cpfBruto == null) {
            return null;
        }
        String digitos = CpfUtil.apenasDigitos(cpfBruto);
        if (digitos.isBlank()) {
            return null;
        }
        if (!CpfUtil.isValido(digitos)) {
            throw new RegraNegocioException("CPF inválido. Confira os 11 números.");
        }
        return digitos;
    }
}
