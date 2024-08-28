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

package contracts.api.putUpdateWorkload.positive

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("""
Represents a successful scenario of Updating a Workload Instance

```
given:
  client requests to update a Workload instance with HelmSource url
when:
  a valid request is submitted
then:
  the Workload instance is updated
```

""")
    request {
        method 'PUT'
        url "/cnonb/v1/workload_instances"
        multipart(
                workloadInstancePutRequestDto: named(
                        name: value('request.json'),
                        content: $(consumer(nonBlank()), producer(file('multipart-json-part.json'))),
                        contentType: value("application/json")
                )

        )
    }
    response {
        status ACCEPTED()
        body(
                """
                       {
                        "url": "testUrl"
                       }
                """
        )
    }

}
