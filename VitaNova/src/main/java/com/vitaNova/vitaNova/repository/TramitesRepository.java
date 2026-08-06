package com.vitaNova.vitaNova.repository;

import com.vitaNova.vitaNova.model.Tramites;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TramitesRepository
        extends JpaRepository<Tramites, Long> {

}