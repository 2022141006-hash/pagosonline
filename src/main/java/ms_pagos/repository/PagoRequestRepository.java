package ms_pagos.repository;

import ms_pagos.model.PagoRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PagoRequestRepository extends JpaRepository<PagoRequest, Long> {

}