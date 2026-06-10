package ms_pagos.repository;

import ms_pagos.model.PagoResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PagoResponseRepository extends JpaRepository<PagoResponse, Long> {

}