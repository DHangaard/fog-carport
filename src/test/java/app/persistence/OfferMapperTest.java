package app.persistence;

import app.entities.Offer;
import app.entities.OfferDate;
import app.enums.OfferStatus;
import app.exceptions.DatabaseException;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OfferMapperTest
{
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";
    private static final String URL = "jdbc:postgresql://localhost:5432/%s?currentSchema=test";
    private static final String DB = "carport";

    private static final ConnectionPool connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);
    private static OfferMapper offerMapper;

    @BeforeAll
    static void setupClass()
    {
        try (Connection connection = connectionPool.getConnection())
        {
            try (Statement stmt = connection.createStatement())
            {
                stmt.execute("DROP TABLE IF EXISTS test.bill_of_materials CASCADE");
                stmt.execute("DROP TABLE IF EXISTS test.offer CASCADE");
                stmt.execute("DROP TABLE IF EXISTS test.carport CASCADE");
                stmt.execute("DROP TABLE IF EXISTS test.shed CASCADE");
                stmt.execute("DROP TABLE IF EXISTS test.users CASCADE");
                stmt.execute("DROP TABLE IF EXISTS test.zip_code CASCADE");

                stmt.execute("DROP SEQUENCE IF EXISTS test.offer_offer_id_seq CASCADE");
                stmt.execute("DROP SEQUENCE IF EXISTS test.carport_carport_id_seq CASCADE");
                stmt.execute("DROP SEQUENCE IF EXISTS test.shed_shed_id_seq CASCADE");
                stmt.execute("DROP SEQUENCE IF EXISTS test.users_user_id_seq CASCADE");

                stmt.execute("CREATE TABLE test.zip_code AS (SELECT * FROM public.zip_code) WITH NO DATA");
                stmt.execute("ALTER TABLE test.zip_code ADD PRIMARY KEY (zip_code)");

                stmt.execute("CREATE TABLE test.users AS (SELECT * FROM public.users) WITH NO DATA");
                stmt.execute("CREATE SEQUENCE test.users_user_id_seq");
                stmt.execute("ALTER TABLE test.users ALTER COLUMN user_id SET DEFAULT nextval('test.users_user_id_seq')");
                stmt.execute("ALTER TABLE test.users ADD PRIMARY KEY (user_id)");
                stmt.execute("ALTER TABLE test.users ADD CONSTRAINT users_email_key UNIQUE (email)");
                stmt.execute("ALTER TABLE test.users ADD CONSTRAINT users_zip_code_fk " +
                        "FOREIGN KEY (zip_code) REFERENCES test.zip_code (zip_code)");

                stmt.execute("CREATE TABLE test.shed AS (SELECT * FROM public.shed) WITH NO DATA");
                stmt.execute("CREATE SEQUENCE test.shed_shed_id_seq");
                stmt.execute("ALTER TABLE test.shed ALTER COLUMN shed_id SET DEFAULT nextval('test.shed_shed_id_seq')");
                stmt.execute("ALTER TABLE test.shed ADD PRIMARY KEY (shed_id)");

                stmt.execute("CREATE TABLE test.carport AS (SELECT * FROM public.carport) WITH NO DATA");
                stmt.execute("CREATE SEQUENCE test.carport_carport_id_seq");
                stmt.execute("ALTER TABLE test.carport ALTER COLUMN carport_id SET DEFAULT nextval('test.carport_carport_id_seq')");
                stmt.execute("ALTER TABLE test.carport ADD PRIMARY KEY (carport_id)");
                stmt.execute("ALTER TABLE test.carport ADD CONSTRAINT carport_shed_fk " +
                        "FOREIGN KEY (shed_id) REFERENCES test.shed (shed_id)");

                stmt.execute("CREATE TABLE test.offer AS (SELECT * FROM public.offer) WITH NO DATA");
                stmt.execute("CREATE SEQUENCE test.offer_offer_id_seq");
                stmt.execute("ALTER TABLE test.offer ALTER COLUMN offer_id SET DEFAULT nextval('test.offer_offer_id_seq')");
                stmt.execute("ALTER TABLE test.offer ADD PRIMARY KEY (offer_id)");
                stmt.execute("ALTER TABLE test.offer ALTER COLUMN seller_id DROP NOT NULL");

                stmt.execute("ALTER TABLE test.offer ADD CONSTRAINT offer_customer_fk " +
                        "FOREIGN KEY (customer_id) REFERENCES test.users (user_id)");
                stmt.execute("ALTER TABLE test.offer ADD CONSTRAINT offer_seller_fk " +
                        "FOREIGN KEY (seller_id) REFERENCES test.users (user_id)");
                stmt.execute("ALTER TABLE test.offer ADD CONSTRAINT offer_carport_fk " +
                        "FOREIGN KEY (carport_id) REFERENCES test.carport (carport_id)");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            fail("Database setup failed: " + e.getMessage());
        }

        offerMapper = new OfferMapper(connectionPool);
    }

    @BeforeEach
    void setUp()
    {
        try (Connection connection = connectionPool.getConnection())
        {
            try (Statement stmt = connection.createStatement())
            {
                stmt.execute("DELETE FROM test.offer");
                stmt.execute("DELETE FROM test.carport");
                stmt.execute("DELETE FROM test.shed");
                stmt.execute("DELETE FROM test.users");
                stmt.execute("DELETE FROM test.zip_code");

                stmt.execute("INSERT INTO test.zip_code (zip_code, city) VALUES " +
                        "(1000, 'København K'), " +
                        "(2100, 'København Ø'), " +
                        "(2200, 'København N')");

                stmt.execute("INSERT INTO test.users (user_id, first_name, last_name, email, hashed_password, " +
                        "zip_code, street, role, phone_number) VALUES " +
                        "(1, 'Mads', 'Nielsen', 'mads.nielsen@gmail.com', '$2a$10$hash1', 1000, 'Bredgade 25', 'CUSTOMER', '20345678'), " +
                        "(2, 'Sofie', 'Jensen', 'sofie.jensen@fog.dk', '$2a$10$hash2', 2100, 'Østerbrogade 112', 'SALESREP', '30456789'), " +
                        "(3, 'Lars', 'Andersen', 'lars.andersen@gmail.com', '$2a$10$hash3', 2200, 'Nørrebrogade 45', 'CUSTOMER', '40567890')");

                stmt.execute("INSERT INTO test.carport (carport_id, length, width, roof_type, shed_id) VALUES " +
                        "(1, 600, 500, 'FLAT', NULL), " +
                        "(2, 780, 600, 'FLAT', NULL), " +
                        "(3, 500, 400, 'FLAT', NULL)");

                stmt.execute("INSERT INTO test.offer (offer_id, customer_id, seller_id, carport_id, " +
                        "request_created_at, created_date, expiration_date, customer_comment, offer_status) VALUES " +
                        "(1, 1, NULL, 1, CURRENT_TIMESTAMP - INTERVAL '1 day', NULL, NULL, " +
                        "'Jeg ønsker et tilbud på en carport 600x500 cm', 'PENDING')");

                stmt.execute("INSERT INTO test.offer (offer_id, customer_id, seller_id, carport_id, " +
                        "request_created_at, created_date, expiration_date, customer_comment, offer_status) VALUES " +
                        "(2, 3, 2, 2, CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '2 days', " +
                        "CURRENT_TIMESTAMP + INTERVAL '12 days', 'Carport med rejsning til 20 grader', 'READY')");

                stmt.execute("INSERT INTO test.offer (offer_id, customer_id, seller_id, carport_id, " +
                        "request_created_at, created_date, expiration_date, customer_comment, offer_status) VALUES " +
                        "(3, 1, 2, 3, CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP - INTERVAL '9 days', " +
                        "CURRENT_TIMESTAMP + INTERVAL '5 days', 'Lille carport til motorcykel', 'ACCEPTED')");

                stmt.execute("SELECT setval('test.users_user_id_seq', COALESCE((SELECT MAX(user_id) + 1 FROM test.users), 1), false)");
                stmt.execute("SELECT setval('test.carport_carport_id_seq', COALESCE((SELECT MAX(carport_id) + 1 FROM test.carport), 1), false)");
                stmt.execute("SELECT setval('test.offer_offer_id_seq', COALESCE((SELECT MAX(offer_id) + 1 FROM test.offer), 1), false)");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            fail("Database setup failed: " + e.getMessage());
        }
    }

    @Test
    void testConnection() throws SQLException
    {
        assertNotNull(connectionPool.getConnection());
    }

    @Test
    void testCreateOffer() throws DatabaseException, SQLException
    {
        Connection connection = connectionPool.getConnection();
        connection.setAutoCommit(false);

        Offer offer = offerMapper.createOffer(
                connection,
                1,
                1,
                "Jeg vil gerne have en carport til min nye bil"
        );

        connection.commit();
        connection.close();

        assertNotNull(offer);
        assertEquals(4, offer.getOfferId());
        assertEquals(1, offer.getCustomerId());
        assertNull(offer.getSellerId());
        assertEquals(1, offer.getCarportId());
        assertEquals("Jeg vil gerne have en carport til min nye bil", offer.getCustomerComment());
        assertEquals(OfferStatus.PENDING, offer.getOfferStatus());
        assertNotNull(offer.getOfferDate().getCustomerRequestCreatedAt());
        assertNull(offer.getOfferDate().getCreatedAt());
        assertNull(offer.getOfferDate().getExpirationDate());
    }

    @Test
    void testGetOfferById() throws DatabaseException
    {
        Offer offer = offerMapper.getOfferById(1);

        assertNotNull(offer);
        assertEquals(1, offer.getOfferId());
        assertEquals(1, offer.getCustomerId());
        assertNull(offer.getSellerId());
        assertEquals(OfferStatus.PENDING, offer.getOfferStatus());
    }

    @Test
    void testGetOfferByIdNotFound()
    {
        assertThrows(DatabaseException.class, () -> {
            offerMapper.getOfferById(999);
        });
    }

    @Test
    void testGetAllOffers() throws DatabaseException
    {
        List<Offer> offers = offerMapper.getAllOffers();

        assertNotNull(offers);
        assertEquals(3, offers.size());
    }

    @Test
    void testGetAllOffersByCustomerId() throws DatabaseException
    {
        List<Offer> customerOffers = offerMapper.getAllOffersByCustomerId(1);

        assertNotNull(customerOffers);
        assertEquals(2, customerOffers.size());
    }

    @Test
    void testGetAllOffersByStatus() throws DatabaseException
    {
        List<Offer> pending = offerMapper.getAllOffersByStatus(OfferStatus.PENDING);
        assertEquals(1, pending.size());

        List<Offer> ready = offerMapper.getAllOffersByStatus(OfferStatus.READY);
        assertEquals(1, ready.size());

        List<Offer> accepted = offerMapper.getAllOffersByStatus(OfferStatus.ACCEPTED);
        assertEquals(1, accepted.size());

        List<Offer> rejected = offerMapper.getAllOffersByStatus(OfferStatus.REJECTED);
        assertEquals(0, rejected.size());
    }

    @Test
    void testUpdateOfferFromPendingToReady() throws DatabaseException
    {
        Offer offer = offerMapper.getOfferById(1);
        assertEquals(OfferStatus.PENDING, offer.getOfferStatus());
        assertNull(offer.getSellerId());

        offer.setSellerId(2);
        offer.setOfferStatus(OfferStatus.READY);

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp expiration = Timestamp.valueOf(LocalDateTime.now().plusDays(14));
        offer.getOfferDate().setCreatedAt(now);
        offer.getOfferDate().setExpirationDate(expiration);

        boolean updated = offerMapper.updateOffer(offer);

        assertTrue(updated);

        Offer updatedOffer = offerMapper.getOfferById(1);
        assertEquals(2, updatedOffer.getSellerId());
        assertEquals(OfferStatus.READY, updatedOffer.getOfferStatus());
        assertNotNull(updatedOffer.getOfferDate().getCreatedAt());
        assertNotNull(updatedOffer.getOfferDate().getExpirationDate());
    }

    @Test
    void testUpdateOfferFromReadyToAccepted() throws DatabaseException
    {
        Offer offer = offerMapper.getOfferById(2);
        assertEquals(OfferStatus.READY, offer.getOfferStatus());

        offer.setOfferStatus(OfferStatus.ACCEPTED);
        boolean updated = offerMapper.updateOffer(offer);

        assertTrue(updated);

        Offer acceptedOffer = offerMapper.getOfferById(2);
        assertEquals(OfferStatus.ACCEPTED, acceptedOffer.getOfferStatus());
    }

    @Test
    void testUpdateOfferFromReadyToRejected() throws DatabaseException
    {
        Offer offer = offerMapper.getOfferById(2);
        assertEquals(OfferStatus.READY, offer.getOfferStatus());

        offer.setOfferStatus(OfferStatus.REJECTED);
        offer.setCustomerComment("Prisen er desværre for høj");
        boolean updated = offerMapper.updateOffer(offer);

        assertTrue(updated);

        Offer rejectedOffer = offerMapper.getOfferById(2);
        assertEquals(OfferStatus.REJECTED, rejectedOffer.getOfferStatus());
        assertEquals("Prisen er desværre for høj", rejectedOffer.getCustomerComment());
    }

    @Test
    void testUpdateOfferToExpired() throws DatabaseException
    {
        Offer offer = offerMapper.getOfferById(2);
        assertEquals(OfferStatus.READY, offer.getOfferStatus());

        offer.setOfferStatus(OfferStatus.EXPIRED);
        boolean updated = offerMapper.updateOffer(offer);

        assertTrue(updated);

        Offer expiredOffer = offerMapper.getOfferById(2);
        assertEquals(OfferStatus.EXPIRED, expiredOffer.getOfferStatus());
    }

    @Test
    void testUpdateOfferNotFound() throws DatabaseException
    {
        OfferDate offerDate = new OfferDate(
                Timestamp.valueOf(LocalDateTime.now()),
                null,
                null
        );

        Offer fakeOffer = new Offer(
                999,
                1,
                null,
                1,
                offerDate,
                "Comment",
                OfferStatus.PENDING
        );

        boolean updated = offerMapper.updateOffer(fakeOffer);

        assertFalse(updated);
    }

    @Test
    void testDeleteOffer() throws DatabaseException
    {
        boolean deleted = offerMapper.deleteOffer(1);

        assertTrue(deleted);

        assertThrows(DatabaseException.class, () -> {
            offerMapper.getOfferById(1);
        });
    }

    @Test
    void testDeleteOfferNotFound() throws DatabaseException
    {
        boolean deleted = offerMapper.deleteOffer(999);

        assertFalse(deleted);
    }

    @Test
    void testCompleteOfferWorkflow() throws DatabaseException, SQLException
    {
        Connection connection = connectionPool.getConnection();
        connection.setAutoCommit(false);

        Offer request = offerMapper.createOffer(connection, 1, 1, "Ny carport ønskes");
        connection.commit();
        connection.close();

        assertEquals(OfferStatus.PENDING, request.getOfferStatus());
        assertNull(request.getSellerId());

        request.setSellerId(2);
        request.setOfferStatus(OfferStatus.READY);
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        request.getOfferDate().setCreatedAt(now);
        request.getOfferDate().setExpirationDate(Timestamp.valueOf(LocalDateTime.now().plusDays(14)));

        boolean updated = offerMapper.updateOffer(request);
        assertTrue(updated);

        Offer readyOffer = offerMapper.getOfferById(request.getOfferId());
        assertEquals(OfferStatus.READY, readyOffer.getOfferStatus());
        assertEquals(2, readyOffer.getSellerId());

        readyOffer.setOfferStatus(OfferStatus.ACCEPTED);
        updated = offerMapper.updateOffer(readyOffer);
        assertTrue(updated);

        Offer acceptedOffer = offerMapper.getOfferById(request.getOfferId());
        assertEquals(OfferStatus.ACCEPTED, acceptedOffer.getOfferStatus());
    }

    @Test
    void testOfferExpirationLogic() throws DatabaseException
    {
        Offer offer = offerMapper.getOfferById(2);
        assertEquals(OfferStatus.READY, offer.getOfferStatus());

        Timestamp expirationDate = offer.getOfferDate().getExpirationDate();
        assertNotNull(expirationDate);

        assertTrue(expirationDate.after(new Timestamp(System.currentTimeMillis())));

        offer.getOfferDate().setExpirationDate(Timestamp.valueOf(LocalDateTime.now().minusDays(1)));
        offer.setOfferStatus(OfferStatus.EXPIRED);

        boolean updated = offerMapper.updateOffer(offer);
        assertTrue(updated);

        Offer expiredOffer = offerMapper.getOfferById(2);
        assertEquals(OfferStatus.EXPIRED, expiredOffer.getOfferStatus());
    }
}