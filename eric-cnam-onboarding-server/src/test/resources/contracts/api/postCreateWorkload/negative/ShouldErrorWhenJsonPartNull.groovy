/*******************************************************************************
 * COPYRIGHT Ericsson 2023
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

package contracts.api.postCreateWorkload.negative

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("""
Represents an error scenario of creating a Workload Instance

```
given:
  client requests to create a Workload instance with invalid Json part
when:
  an invalid request is submitted
then:
  the request is rejected with 400 Bad Request
```

""")
    request {
        method 'POST'
        url "/cnonb/v1/workload_instances"
        multipart(
                workloadInstancePostRequestDto: named(
                        name: value('request.json'),
                        content: value(null),
                        contentType: value("application/json")
                )
        )
    }
    response {
        status BAD_REQUEST()
        body(
                """
                       {
                          "type":"Bad Request",
                          "title":"MalformedRequest",
                          "status":400,
                          "detail":"Required request part 'workloadInstancePostRequestDto' is not present",
                          "instance":""
                       }
                """
        )
    }
}

