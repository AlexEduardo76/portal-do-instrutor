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

    @Query("SELECT u FROM Usuario u WHERE LOWER(u.login) = LOWER(:identificador) OR LOWER(u.email) = LOWER(:identificador)")
    Optional<Usuario> findByLoginOuEmail(@Param("identificador") String identificador);

    @Query("SELECT u FROM Usuario u WHERE u.cpf = :cpf")
    Optional<Usuario> findByCpfExato(@Param("cpf") String cpf);
}
