package ms_pagos.service;

import ms_pagos.config.IzipayClient;
import ms_pagos.model.PagoRequest;
import ms_pagos.model.PagoResponse;
import ms_pagos.repository.PagoRequestRepository;
import ms_pagos.repository.PagoResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class PagoService {

    @Autowired
    private PagoRequestRepository pagoRequestRepository;

    @Autowired
    private PagoResponseRepository pagoResponseRepository;

    @Autowired
    private IzipayClient izipayClient;

    @Value("${izipay.merchant-code}")
    private String merchantCode;

    public PagoResponse procesarPago(PagoRequest pagoRequest) {

        // 1. Guardar el request en MySQL
        PagoRequest pagoGuardado = pagoRequestRepository.save(pagoRequest);

        // 2. Armar el JSON según formato real de Izipay API REST
        double monto = pagoGuardado.getTotalamount() != null ? pagoGuardado.getTotalamount() : 0.0;
        int valorCentimos = (int) Math.round(monto * 100);

        Map<String, Object> customer = new HashMap<>();
        customer.put("email", pagoGuardado.getEmail());

        Map<String, Object> requestIzipay = new HashMap<>();
        requestIzipay.put("amount", valorCentimos);
        requestIzipay.put("currency", pagoGuardado.getCurrency());
        requestIzipay.put("orderId", pagoGuardado.getReference());
        requestIzipay.put("customer", customer);

        // 3. Llamar a Izipay
        System.out.println("MONTO CENTIMOS: " + valorCentimos);
        System.out.println("REQUEST A IZIPAY: " + requestIzipay);
        Map<String, Object> respuestaIzipay = izipayClient.procesarPago(requestIzipay);
        System.out.println("RESPUESTA IZIPAY: " + respuestaIzipay);

        // 4. Guardar la respuesta en MySQL
        Map<String, Object> answer = new HashMap<>();
        if (respuestaIzipay.get("answer") instanceof Map) {
            answer = (Map<String, Object>) respuestaIzipay.get("answer");
        }

        PagoResponse pagoResponse = new PagoResponse();
        pagoResponse.setPagoRequest(pagoGuardado);
        pagoResponse.setCodigoRespuesta(String.valueOf(answer.getOrDefault("errorCode", "00")));
        pagoResponse.setEstado(String.valueOf(respuestaIzipay.getOrDefault("status", "UNKNOWN")));
        pagoResponse.setMensaje(String.valueOf(answer.getOrDefault("errorMessage", "Sin mensaje")));
        pagoResponse.setFechaRespuesta(String.valueOf(respuestaIzipay.getOrDefault("serverDate", "")));

        return pagoResponseRepository.save(pagoResponse);
    }
}