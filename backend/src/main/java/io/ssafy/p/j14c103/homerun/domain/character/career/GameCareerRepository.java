package io.ssafy.p.j14c103.homerun.domain.character.career;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameCareerRepository extends JpaRepository<GameCareer, Integer> {
}
