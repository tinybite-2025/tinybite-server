package ita.tinybite.domain.auth.repository;

import ita.tinybite.domain.user.entity.Term;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TermRepository extends JpaRepository<Term, Long> {

    @Query("SELECT t " +
            "FROM Term t " +
            "WHERE t.title IN :titles")
    List<Term> findAllByTitle(List<String> titles);

    @Query("SELECT t " +
            "FROM Term t " +
            "WHERE t.required = true")
    List<Term> findRequiredTerm();

}
