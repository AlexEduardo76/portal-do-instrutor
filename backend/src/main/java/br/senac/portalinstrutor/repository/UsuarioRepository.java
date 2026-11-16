package br.senac.portalinstrutor.repository;

import br.senac.portalinstrutor.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByLogin(String login);

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByCpf(String cpf);

    // Login/e-mail são persistidos em minúsculas; a igualdade direta usa os
    // índices únicos de usuarios em vez de varrer a tabela com LOWER().
    @Query("SELECT u FROM Usuario u WHERE u.login = :identificador OR u.email = :identificador")
    Optional<Usuario> findByLoginOuEmail(@Param("identificador") String identificador);

    @Query("SELECT u FROM Usuario u WHERE u.cpf = :cpf")
    Optional<Usuario> findByCpfExato(@Param("cpf") String cpf);
}
