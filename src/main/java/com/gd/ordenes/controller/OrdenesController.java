package com.gd.ordenes.controller;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.gd.ordenes.entities.request.ActualizarFactura;
import com.gd.ordenes.entities.request.PaymentIntenRequest;
import com.gd.ordenes.entities.response.GenericResponse;
import com.gd.ordenes.exceptions.NoFoundException;
import com.gd.ordenes.service.OrdenesService;
import com.stripe.exception.StripeException;

@RestController
@RequestMapping("/ordenes")
@CrossOrigin(origins = "*")
@EnableWebMvc
public class OrdenesController {


   private OrdenesService ordenesService;
	OrdenesController( OrdenesService ordenesService){
		this.ordenesService=ordenesService;
	}
	  
    @PostMapping("/paymentintent")
    public ResponseEntity<GenericResponse> payment(@RequestBody PaymentIntenRequest paymentIntenRequest,@RequestHeader("Stripe-Token") String stripeToken) throws StripeException, NoFoundException {
    	
        return new ResponseEntity<>(ordenesService.crearPago(paymentIntenRequest, stripeToken), HttpStatus.OK);
    }

    @PostMapping("/confirm/{idPago}/{idReferencia}")
    public ResponseEntity<GenericResponse> confirm(@PathVariable("idPago") long idPago,@PathVariable("idReferencia") String idReferencia,@RequestHeader("Stripe-Token") String stripeToken) throws StripeException, NoFoundException {
        return new ResponseEntity<>(ordenesService.comfirmarPago(idPago, idReferencia, stripeToken), HttpStatus.CREATED);
    }

    @PostMapping("/cancel/{idPago}/{idReferencia}")
    public ResponseEntity<GenericResponse> cancel(@PathVariable("idPago") long idPago,@PathVariable("idReferencia") String idReferencia) throws StripeException, NoFoundException {
    	
        return new ResponseEntity<>(ordenesService.cancelarPago(idPago, idReferencia), HttpStatus.OK);
    }
    
    @GetMapping("/lista")
    public ResponseEntity<GenericResponse> lista(){
        return new ResponseEntity<>(ordenesService.lista(), HttpStatus.OK);
    }
    
    @GetMapping("/getPago/{idPago}")
    public ResponseEntity<GenericResponse> obtienePago(@PathVariable("idPago") long idPago) throws NoFoundException{
        return new ResponseEntity<>(ordenesService.getPago(idPago), HttpStatus.OK);
    }
    
    @PutMapping("/actualizar")
    public ResponseEntity<GenericResponse> actualizar(@RequestBody ActualizarFactura actPago) throws NoFoundException{
        return new ResponseEntity<>(ordenesService.updateFactura(actPago), HttpStatus.OK);
    }
}