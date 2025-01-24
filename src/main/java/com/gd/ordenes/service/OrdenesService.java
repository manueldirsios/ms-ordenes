package com.gd.ordenes.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gd.ordenes.entities.LineaPago;
import com.gd.ordenes.entities.Pago;
import com.gd.ordenes.entities.Producto;
import com.gd.ordenes.entities.request.ActualizarFactura;
import com.gd.ordenes.entities.request.PaymentIntenRequest;
import com.gd.ordenes.entities.request.ProductoPagoRequest;
import com.gd.ordenes.entities.response.GenericResponse;
import com.gd.ordenes.entities.response.PagoResponse;
import com.gd.ordenes.exceptions.NoFoundException;
import com.gd.ordenes.repository.LineaPagoRepository;
import com.gd.ordenes.repository.PagoRepository;
import com.gd.ordenes.repository.ProductoRepository;
import com.gd.ordenes.util.Util;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Token;
import com.stripe.param.PaymentIntentConfirmParams;
import com.stripe.param.PaymentIntentCreateParams;

@Service
public class OrdenesService {

	private final PagoRepository pagosRepository;
	private final LineaPagoRepository lineaPagoRepository;
	private final ProductoRepository productoRepository;
	private final CounterService counterService;

	// Constructor con inyección de dependencias
	public OrdenesService(PagoRepository pagosRepository, LineaPagoRepository lineaPagoRepository,
			ProductoRepository productoRepository, CounterService counterService) {
		this.pagosRepository = pagosRepository;
		this.lineaPagoRepository = lineaPagoRepository;
		this.productoRepository = productoRepository;
		this.counterService = counterService;
	}

	@Value("${stripe.key.secret}")
	String secretKey;
	
	public static final String PAGO_NOENCONTRADO="Pago no Encontrado";
	
	public GenericResponse crearPago(PaymentIntenRequest paymentIntenRequest, String stripeToken) throws StripeException, NoFoundException {
		GenericResponse response=new GenericResponse();
	  	Pago pago=save(paymentIntenRequest);
    	PagoResponse pagoResponse=new PagoResponse();
    	
    	PaymentIntent paymentIntent = paymentIntent(paymentIntenRequest,stripeToken);
    	
    	update(pago, paymentIntent.getId());
    	pagoResponse.setIdPagoStripe(paymentIntent.getId());
    	pagoResponse.setIdPago(pago.getId());
    	pagoResponse.setDescripcion(paymentIntent.getDescription());
    	pagoResponse.setMonto(paymentIntent.getAmount()/100);
    	response.setTransaccion(pagoResponse);
    	return response;
	}
	
	public GenericResponse comfirmarPago( long idPago,String idReferencia,String stripeToken) throws StripeException, NoFoundException {
		GenericResponse response=new GenericResponse();
		PagoResponse pagoResponse=new PagoResponse();

        PaymentIntent paymentIntent = confirm(idReferencia,stripeToken);
        updateByReference(idPago,"CONFIRMADO");
        
        pagoResponse.setIdPagoStripe(paymentIntent.getId());
    	pagoResponse.setIdPago(idPago);
    	pagoResponse.setDescripcion(paymentIntent.getDescription());
    	pagoResponse.setMonto(paymentIntent.getAmount());
    	response.setTransaccion(pagoResponse);
    	return response;
	}
	
	public GenericResponse cancelarPago( long idPago, String idReferencia) throws StripeException, NoFoundException {
		GenericResponse response=new GenericResponse();
		PagoResponse pagoResponse=new PagoResponse();

        PaymentIntent paymentIntent = cancel(idReferencia);
        updateByReference(idPago,"CANCELADO");
        pagoResponse.setIdPagoStripe(paymentIntent.getId());
    	pagoResponse.setIdPago(idPago);
    	pagoResponse.setDescripcion(paymentIntent.getDescription());
    	pagoResponse.setMonto(paymentIntent.getAmount());
    	response.setTransaccion(pagoResponse);
    	return response;
	}

	public GenericResponse lista() {
		GenericResponse response=new GenericResponse();
		List<Pago> lista = pagosRepository.findAll();

		// Convertir a lista mutable
		List<Pago> listaMutable = new ArrayList<>(lista);
		// Ordenar la lista por la propiedad `fecha` en orden ascendente
		listaMutable.sort(Comparator.comparing(Pago::getFechaPago).reversed());
		response.setTransaccion(listaMutable);
		return response;
	}

	public GenericResponse getPago(long id) throws NoFoundException {
		GenericResponse response=new GenericResponse();
		Pago pago=pagosRepository.findById(id).orElseThrow(()-> new NoFoundException(404,PAGO_NOENCONTRADO));
		response.setTransaccion(pago);
		return response;
	}

	public Pago save(PaymentIntenRequest paymentIntentDto) throws NoFoundException {
		long idPago = counterService.getNextId("PagoCounter");

		Pago pago = new Pago();
		List<LineaPago> lineasPago = new ArrayList<>();
		pago.setDescripcionPago(paymentIntentDto.getDescription());
		pago.setId(idPago);
		pago.setEstatusPago("ENVIADO");
		pago.setImporteTotal(paymentIntentDto.getAmount());
		pago.setFechaPago(Util.obtenerFecha());
		pago.setFechaModificacion(Util.obtenerFecha());
		String refTemporal = "TEMP_" + LocalDateTime.now().getNano();
		pago.setReferenciaPago(refTemporal);

		for (ProductoPagoRequest art : paymentIntentDto.getListArticulos()) {
			long idLinea = counterService.getNextId("LineaPagoCounter");
			LineaPago linea = new LineaPago();
			Producto producto = productoRepository.findById(art.getId())
					.orElseThrow(() -> new NoFoundException(404,"Artículo no encontrado"));

			linea.setProductoId(producto.getId());
			linea.setId(idLinea);
			linea.setPagoId(idPago);
			linea.setSubtotal(producto.getPrecioFinal() * art.getCantidad());
			linea.setCantidad(art.getCantidad());
			linea.setProducto(producto);
			lineaPagoRepository.save(linea);
			lineasPago.add(linea);

		}
		pago.setLineasPago(lineasPago);

		pagosRepository.save(pago);
		return pago;
	}

	public void update(Pago pago, String referenciaPago) {
		pago.setFechaModificacion(Util.obtenerFecha());
		pago.setEstatusPago("PENDIENTE");
		pago.setReferenciaPago(referenciaPago);
		pagosRepository.save(pago);
	}

	public void updateByReference(long idPago, String estatus) throws NoFoundException {
		Pago pago=pagosRepository.findById(idPago).orElseThrow(()-> new NoFoundException(404,PAGO_NOENCONTRADO));
		pago.setFechaModificacion(Util.obtenerFecha());
		pago.setEstatusPago(estatus);
		pagosRepository.save(pago);
	}

	public GenericResponse updateFactura(ActualizarFactura actPago) throws NoFoundException {
		GenericResponse response=new GenericResponse();

		Pago pago=pagosRepository.findById(actPago.getIdPago()).orElseThrow(()-> new NoFoundException(404,PAGO_NOENCONTRADO));
		pago.setIdFactura(actPago.getIdFactura());
		pago.setFechaModificacion(Util.obtenerFecha());
		pagosRepository.save(pago);
		response.setTransaccion(true);
		return response;
	}

	public PaymentIntent paymentIntent(PaymentIntenRequest paymentIntentRequest, String token) throws StripeException {
		Stripe.apiKey = secretKey;
		// Validar el token
		validateToken(token);

		// Configurar parámetros para crear el PaymentIntent
		PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
				.setAmount((long) paymentIntentRequest.getAmount() * 100) // Cantidad en centavos
				.setCurrency(paymentIntentRequest.getCurrency().toString())
				.setDescription(paymentIntentRequest.getDescription()).addAllPaymentMethodType(List.of("card")).build();
		return PaymentIntent.create(params);
	}

	public PaymentIntent confirm(String id, String token) throws StripeException {
		Stripe.apiKey = secretKey;
		// Validar el token
		validateToken(token);

		// Recuperar el PaymentIntent desde Stripe
		PaymentIntent paymentIntent = PaymentIntent.retrieve(id);
		// Configurar los parámetros de confirmación
		PaymentIntentConfirmParams confirmParams = PaymentIntentConfirmParams.builder().setPaymentMethod("pm_card_visa")// ID
																														// del
																														// método
																														// de
																														// pago
				.build();

		// Confirmar el PaymentIntent
		return paymentIntent.confirm(confirmParams);
	}

	public PaymentIntent cancel(String id) throws StripeException {
		Stripe.apiKey = secretKey;
		// Recuperar el PaymentIntent desde Stripe
		PaymentIntent paymentIntent = PaymentIntent.retrieve(id);

		// Cancelar el PaymentIntent
		return paymentIntent.cancel();
	}

	/**
	 * Valida un token generado desde el cliente de Stripe.
	 *
	 * @param token Token generado desde el cliente.
	 * @throws StripeException Si el token es inválido o no existe.
	 */
	private void validateToken(String token) throws StripeException {
		if (token == null || token.isEmpty()) {
			throw new IllegalArgumentException("El token es requerido y no puede estar vacío.");
		}

		// Usa Stripe para validar el token llamando a su API
		// Si el token es inválido, Stripe lanzará un StripeException automáticamente.
		Token stripeToken = Token.retrieve(token);

		if (stripeToken == null || stripeToken.getUsed()) {
			throw new IllegalArgumentException("El token es inválido o ya ha sido utilizado.");
		}
	}
}