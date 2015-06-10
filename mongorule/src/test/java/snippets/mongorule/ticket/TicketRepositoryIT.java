package snippets.mongorule.ticket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import snippets.mongorule.Application;
import snippets.mongorule.MongoCleanupRule;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class TicketRepositoryIT {

	@Autowired
	private TicketRepository repository;
	@Autowired
	private MongoTemplate mongoTemplate;

	@Rule
	public final MongoCleanupRule cleanupRule = new MongoCleanupRule(this, Ticket.class);

	@Test
	public void testSaveAndFindTicket() throws Exception {
		Ticket ticket1 = new Ticket("1", "blabla");
		repository.save(ticket1);
		Ticket ticket2 = new Ticket("2", "hihi");
		repository.save(ticket2);

		assertEquals(ticket1, repository.findByTicketId("1"));
		assertEquals(ticket2, repository.findByTicketId("2"));
		assertNull(repository.findByTicketId("3"));
	}

	@Test(expected = DuplicateKeyException.class)
	public void testSaveNewTicketWithExistingTicketId() throws Exception {
		repository.save(new Ticket("1", "blabla"));
		repository.save(new Ticket("1", "hihi"));
	}

}
