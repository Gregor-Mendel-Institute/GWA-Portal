package com.gmi.nordborglab.jpaontology.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmi.nordborglab.jpaontology.model.Term;

import java.util.Set;

public interface TermRepository extends JpaRepository<Term, Integer> {
	
	public Term findByAcc(String acc);

	Set<Term> findAllByAccIn(Set<String> accs);
}
