package kz.aitu.sdp.trip;

import java.util.List;
import kz.aitu.sdp.trip.app.Quote;
import kz.aitu.sdp.trip.app.TripPlan;
import kz.aitu.sdp.trip.app.TripPlanner;
import kz.aitu.sdp.trip.app.TripRequest;
import kz.aitu.sdp.trip.booking.AgencyBookingChannel;
import kz.aitu.sdp.trip.booking.Booking;
import kz.aitu.sdp.trip.booking.BookingChannel;
import kz.aitu.sdp.trip.booking.BookingChannels;
import kz.aitu.sdp.trip.booking.BookingRejectedException;
import kz.aitu.sdp.trip.booking.CorporateBookingChannel;
import kz.aitu.sdp.trip.booking.Customer;
import kz.aitu.sdp.trip.booking.OnlineBookingChannel;
import kz.aitu.sdp.trip.factory.TripFactoryProvider;
import kz.aitu.sdp.trip.product.TripTier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Part B: the Creator hierarchy and the documents its factory method produces. */
class BookingChannelTest {

    private static final TripRequest REQUEST = new TripRequest("Aliya", "Antalya", 3, 2);

    private static TripPlanner planner(TripTier tier) {
        return new TripPlanner(TripFactoryProvider.forTier(tier));
    }

    private static TripPlan plan(TripTier tier) {
        return planner(tier).planTrip(REQUEST);
    }

    private static Quote quote(TripTier tier) {
        return planner(tier).quote(plan(tier));
    }

    private static Customer person() {
        return Customer.person("Aliya", 2, "aliya@example.com");
    }

    @Test
    void onlineChannelIssuesAnEmailVoucherWithADiscount() {
        Booking booking = new OnlineBookingChannel().book(plan(TripTier.STANDARD), quote(TripTier.STANDARD), person());

        assertAll(
                () -> assertEquals("email", booking.document().format()),
                () -> assertFalse(booking.document().requiresSignature()),
                () -> assertEquals(219_000 * 97 / 100, booking.pricePaid()),
                () -> assertEquals(0, booking.paymentDeadlineDays()));
    }

    @Test
    void agencyChannelIssuesASignedPaperVoucherWithAServiceFee() {
        Booking booking = new AgencyBookingChannel().book(plan(TripTier.STANDARD), quote(TripTier.STANDARD), person());

        assertAll(
                () -> assertEquals("paper", booking.document().format()),
                () -> assertTrue(booking.document().requiresSignature()),
                () -> assertEquals(219_000 * 105 / 100, booking.pricePaid()),
                () -> assertEquals(3, booking.paymentDeadlineDays()));
    }

    @Test
    void corporateChannelInvoicesTheCompanyAndGivesThirtyDays() {
        Customer company = new Customer("Aliya", 2, "aliya@company.kz", "AITU LLP");

        Booking booking = new CorporateBookingChannel().book(plan(TripTier.STANDARD), quote(TripTier.STANDARD), company);

        assertAll(
                () -> assertEquals("pdf-invoice", booking.document().format()),
                () -> assertEquals(30, booking.paymentDeadlineDays()),
                () -> assertTrue(booking.document().render().contains("AITU LLP")),
                () -> assertTrue(booking.document().render().contains("VAT")));
    }

    @Test
    void corporateGroupOfFiveGetsTheGroupDiscount() {
        TripRequest groupRequest = new TripRequest("Aliya", "Antalya", 3, 5);
        TripPlanner planner = planner(TripTier.STANDARD);
        TripPlan groupPlan = planner.planTrip(groupRequest);
        Customer company = new Customer("Aliya", 5, "aliya@company.kz", "AITU LLP");

        Booking booking = new CorporateBookingChannel().book(groupPlan, planner.quote(groupPlan), company);

        assertEquals(planner.quote(groupPlan).total() * 93 / 100, booking.pricePaid());
    }

    /** The same inherited book() runs for every channel: only the document differs. */
    @Test
    void everyChannelRunsTheSameBookingLogic() {
        List<BookingChannel> channels = List.of(
                new OnlineBookingChannel(), new AgencyBookingChannel(), new CorporateBookingChannel());
        Customer company = new Customer("Aliya", 2, "aliya@company.kz", "AITU LLP");

        for (BookingChannel channel : channels) {
            Booking booking = channel.book(plan(TripTier.STANDARD), quote(TripTier.STANDARD), company);

            assertAll(
                    () -> assertNotNull(booking.document(), channel.name()),
                    () -> assertTrue(booking.reference().endsWith("-0001"), booking.reference()),
                    () -> assertTrue(booking.pricePaid() > 0),
                    () -> assertTrue(channel.confirmationMessage(booking).contains(booking.reference())));
        }
    }

    @Test
    void documentsCarryTheProductsOfThePlan() {
        Booking booking = new OnlineBookingChannel().book(plan(TripTier.BUDGET), quote(TripTier.BUDGET), person());
        String text = booking.document().render();

        assertAll(
                () -> assertTrue(text.contains("Intercity bus"), text),
                () -> assertTrue(text.contains("Hostel"), text),
                () -> assertTrue(text.contains("Self-guided"), text));
    }

    @Test
    void referenceNumbersAreSequentialInsideOneChannel() {
        BookingChannel channel = new OnlineBookingChannel();

        Booking first = channel.book(plan(TripTier.BUDGET), quote(TripTier.BUDGET), person());
        Booking second = channel.book(plan(TripTier.BUDGET), quote(TripTier.BUDGET), person());

        assertAll(
                () -> assertEquals("ONL-0001", first.reference()),
                () -> assertEquals("ONL-0002", second.reference()));
    }

    /** Negative scenario: the excursion cannot host the group. */
    @Test
    void bookingIsRejectedWhenTheGroupIsLargerThanTheExcursion() {
        TripRequest bigGroup = new TripRequest("Aliya", "Antalya", 3, 5);
        TripPlanner planner = planner(TripTier.PREMIUM);
        TripPlan bigPlan = planner.planTrip(bigGroup);
        Customer customer = Customer.person("Aliya", 5, "aliya@example.com");

        BookingRejectedException error = assertThrows(BookingRejectedException.class,
                () -> new OnlineBookingChannel().book(bigPlan, planner.quote(bigPlan), customer));

        assertTrue(error.getMessage().contains("Private guide"), error.getMessage());
    }

    /** Negative scenario: a corporate booking without a company. */
    @Test
    void corporateBookingWithoutACompanyIsRejected() {
        assertThrows(BookingRejectedException.class,
                () -> new CorporateBookingChannel().book(plan(TripTier.STANDARD), quote(TripTier.STANDARD), person()));
    }

    @Test
    void channelsAreCreatedByName() {
        assertAll(
                () -> assertEquals("Online", BookingChannels.byName("online").name()),
                () -> assertEquals("Agency", BookingChannels.byName("AGENCY").name()),
                () -> assertEquals("Corporate", BookingChannels.byName("corporate").name()),
                () -> assertEquals(3, BookingChannels.supportedChannels().size()));
    }
}
