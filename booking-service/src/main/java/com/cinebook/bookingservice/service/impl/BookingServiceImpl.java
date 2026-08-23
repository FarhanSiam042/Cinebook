package com.cinebook.bookingservice.service.impl;

import com.cinebook.bookingservice.config.RabbitConfig;
import com.cinebook.bookingservice.dto.BookingResponse;
import com.cinebook.bookingservice.dto.ConfirmHoldRequest;
import com.cinebook.bookingservice.dto.CreateBookingRequest;
import com.cinebook.bookingservice.dto.MovieInfoResponse;
import com.cinebook.bookingservice.dto.ShowtimeInfoResponse;
import com.cinebook.bookingservice.dto.TheaterInfoResponse;
import com.cinebook.bookingservice.event.BookingConfirmedEvent;
import com.cinebook.bookingservice.exception.BookingNotFoundException;
import com.cinebook.bookingservice.exception.ExternalServiceException;
import com.cinebook.bookingservice.feign.MovieClient;
import com.cinebook.bookingservice.feign.SeatLockClient;
import com.cinebook.bookingservice.feign.TheaterClient;
import com.cinebook.bookingservice.model.Booking;
import com.cinebook.bookingservice.model.BookingStatus;
import com.cinebook.bookingservice.repository.BookingRepository;
import com.cinebook.bookingservice.service.BookingService;
import feign.FeignException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

	private final BookingRepository bookingRepository;
	private final MovieClient movieClient;
	private final TheaterClient theaterClient;
	private final SeatLockClient seatLockClient;
	private final RabbitTemplate rabbitTemplate;

	@Override
	@Transactional
	public BookingResponse createBooking(CreateBookingRequest request) {
		ShowtimeInfoResponse showtime = fetchShowtime(request.showtimeId());
		MovieInfoResponse movie = fetchMovie(showtime.movieId());
		TheaterInfoResponse theater = fetchTheater(showtime.theaterId());

		String bookingReference = UUID.randomUUID().toString();
		confirmSeatHold(request.holdToken(), bookingReference);

		int seatCount = request.seatIds().size();
		BigDecimal amount = showtime.price()
				.multiply(BigDecimal.valueOf(seatCount))
				.setScale(2, RoundingMode.HALF_UP);

		Booking booking = Booking.builder()
				.bookingReference(bookingReference)
				.customerName(request.customerName())
				.customerEmail(request.customerEmail())
				.movieId(movie.id())
				.movieTitle(movie.title())
				.theaterId(theater.id())
				.theaterName(theater.name())
				.showTime(showtime.startTime())
				.seatCount(seatCount)
				.seatIds(request.seatIds())
				.seatLabels(request.seatLabels() != null ? request.seatLabels() : List.of())
				.amount(amount)
				.status(BookingStatus.CONFIRMED)
				.build();

		Booking savedBooking = bookingRepository.save(booking);
		publishBookingConfirmed(savedBooking);
		return toResponse(savedBooking);
	}

	@Override
	@Transactional(readOnly = true)
	public List<BookingResponse> getAllBookings() {
		return bookingRepository.findAll().stream().map(this::toResponse).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public BookingResponse getBookingById(Long id) {
		return toResponse(findBookingById(id));
	}

	@Override
	@Transactional(readOnly = true)
	public BookingResponse getBookingByReference(String bookingReference) {
		return bookingRepository.findByBookingReference(bookingReference)
				.map(this::toResponse)
				.orElseThrow(() -> new BookingNotFoundException("Booking not found for reference: " + bookingReference));
	}

	private ShowtimeInfoResponse fetchShowtime(Long showtimeId) {
		try {
			return movieClient.getShowtimeById(showtimeId);
		}
		catch (FeignException exception) {
			throw new ExternalServiceException("Unable to load showtime " + showtimeId + ": " + exception.getMessage());
		}
	}

	private MovieInfoResponse fetchMovie(Long movieId) {
		try {
			return movieClient.getMovieById(movieId);
		}
		catch (FeignException exception) {
			throw new ExternalServiceException("Unable to load movie " + movieId + ": " + exception.getMessage());
		}
	}

	private TheaterInfoResponse fetchTheater(Long theaterId) {
		try {
			return theaterClient.getTheaterById(theaterId);
		}
		catch (FeignException exception) {
			throw new ExternalServiceException("Unable to load theater " + theaterId + ": " + exception.getMessage());
		}
	}

	// Converts the caller's seat hold into a permanent reservation in seat-lock-service.
	// The hold token is the only proof of ownership required here -- see seat-lock-service's
	// SecurityConfig for why that confirm endpoint is intentionally unauthenticated.
	private void confirmSeatHold(String holdToken, String bookingReference) {
		try {
			seatLockClient.confirm(holdToken, new ConfirmHoldRequest(bookingReference));
		}
		catch (FeignException.NotFound | FeignException.Gone exception) {
			throw new ExternalServiceException("Your seat hold has expired. Please reselect your seats and try again.");
		}
		catch (FeignException exception) {
			throw new ExternalServiceException("Unable to confirm seat hold: " + exception.getMessage());
		}
	}

	private Booking findBookingById(Long id) {
		return bookingRepository.findById(id)
				.orElseThrow(() -> new BookingNotFoundException("Booking not found for id: " + id));
	}

	private void publishBookingConfirmed(Booking booking) {
		BookingConfirmedEvent event = new BookingConfirmedEvent(
				booking.getId(),
				booking.getBookingReference(),
				booking.getCustomerName(),
				booking.getCustomerEmail(),
				booking.getMovieId(),
				booking.getMovieTitle(),
				booking.getTheaterId(),
				booking.getTheaterName(),
				booking.getShowTime(),
				booking.getSeatCount(),
				booking.getAmount()
		);
		rabbitTemplate.convertAndSend(RabbitConfig.EVENTS_EXCHANGE, RabbitConfig.BOOKING_CONFIRMED_QUEUE, event);
	}

	private BookingResponse toResponse(Booking booking) {
		return new BookingResponse(
				booking.getId(),
				booking.getBookingReference(),
				booking.getCustomerName(),
				booking.getCustomerEmail(),
				booking.getMovieId(),
				booking.getMovieTitle(),
				booking.getTheaterId(),
				booking.getTheaterName(),
				booking.getShowTime(),
				booking.getSeatCount(),
				booking.getSeatIds(),
				booking.getSeatLabels(),
				booking.getAmount(),
				booking.getStatus(),
				booking.getCreatedAt(),
				booking.getUpdatedAt()
		);
	}
}
