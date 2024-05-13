# lra-test

This is a sample to show Camel Saga and LRA services.

The purpose is to show that when using the InMemorySagaService all completion and compensation routes are executed, but
when switched to using the LRAService they are not.

The main route starting the Saga runs a timer ten times and the Reserve Credit route randoms an error so if that error
is not triggered, run again.

For this test you need Docker and the following container running:

```bash
docker run -p 8180:8080 quay.io/jbosstm/lra-coordinator:latest
```

In the ```CamelRouter.java``` swap between the ```InMemorySagaService``` and the ```LRAService``` to see the difference.

Logs are printed to show compensation and completion.

NOTE: For the simplicity of the test I have set the LRA Service properties in the wrong way on purpose, otherwise you
can't just swap the services in the code. 