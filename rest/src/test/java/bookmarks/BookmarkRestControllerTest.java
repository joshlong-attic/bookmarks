package bookmarks;

import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Josh Long
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@Transactional
public class BookmarkRestControllerTest {


/*
    @Before
    public void setUp() {

        HttpServletRequest request = new MockHttpServletRequest();
        ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(requestAttributes);

        paymentLink = new Link("payment", PaymentLinks.PAYMENT_REL);
        receiptLink = new Link("receipt", PaymentLinks.RECEIPT_REL);

        processor = new PaymentOrderResourceProcessor(paymentLinks);
        when(paymentLinks.getPaymentLink(Mockito.any(Order.class))).thenReturn(paymentLink);
        when(paymentLinks.getReceiptLink(Mockito.any(Order.class))).thenReturn(receiptLink);
    }*/


}
