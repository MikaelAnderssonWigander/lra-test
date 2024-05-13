package org.acme;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.Exchange;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.model.SagaPropagation;
import org.apache.camel.saga.InMemorySagaService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class CamelRoute extends EndpointRouteBuilder {

    @Override
    public void configure() throws Exception {
//        getCamelContext().addService(new InMemorySagaService());

//        final LRASagaService lraSagaService = new LRASagaService();
//        lraSagaService.setLocalParticipantUrl(localParticipantUrl);
//        lraSagaService.setCoordinatorUrl(coordinatorUrl);
//        getCamelContext().addService(lraSagaService);

        from(timer("myTimer").repeatCount(10))
                .saga()
                .to(direct("newOrder"))
                .to(direct("reserveCredit"));

        from(direct("newOrder"))
                .saga()
                .propagation(SagaPropagation.MANDATORY)
                .compensation("direct:cancelOrder")
                .completion("direct:completeOrder") // completion endpoint
                .transform().header(Exchange.SAGA_LONG_RUNNING_ACTION)
                .log("Order ${body} created");

        from(direct("cancelOrder"))
                .transform().header(Exchange.SAGA_LONG_RUNNING_ACTION)
                .log("COMPENSATED: Order ${body}");

        from(direct("reserveCredit"))
                .saga()
                .propagation(SagaPropagation.MANDATORY)
                .compensation("direct:refundCredit")
                .transform().header(Exchange.SAGA_LONG_RUNNING_ACTION)
                .log("Credit ${header.amount} reserved in action ${body}")
                .choice()
                .when(x -> Math.random() >= 0.85)
                .throwException(new RuntimeException("Random failure during reserve Credit"))
                .end();

        from(direct("refundCredit"))
                .transform().header(Exchange.SAGA_LONG_RUNNING_ACTION)
                .log("COMPENSATED: Credit for action ${body}");

        from(direct("completeOrder"))
                .transform().header(Exchange.SAGA_LONG_RUNNING_ACTION)
                .log("COMPLETED: Order ${body}");
    }
}
