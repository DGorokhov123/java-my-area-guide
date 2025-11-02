package ru.practicum.compilation;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    @Query("SELECT c " +
            "FROM Compilation c " +
            "WHERE c.pinned = ?1")
    List<Compilation> findAllByPinned(Boolean pinned, Pageable pageable);

    boolean existsByTitle(String title);

}
