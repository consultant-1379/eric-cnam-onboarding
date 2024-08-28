/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/

package contracts.api.putUpdateWorkload.negative

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("""
Represents an error scenario of updating a Workload Instance

```
given:
  client requests to update a Workload instance with invalid Json part
when:
  an invalid request is submitted
then:
  the request is rejected with 400 Bad Request
```

""")
    request {
        method 'PUT'
        url "/cnonb/v1/workload_instances"
        multipart(
                workloadInstancePostRequestDto: named(
                        name: value('request.json'),
                        content: $(consumer(nonBlank()), producer(file('invalid-multipart-data.json'))),
                        contentType: value("application/json")
                )
        )
    }
    response {
        status BAD_REQUEST()
    }
}

