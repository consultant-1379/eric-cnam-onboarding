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

package contracts.api.postCreateWorkload.positive

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("""
Represents a successful scenario of Creating a Workload Instance

```
given:
  client requests to create a Workload instance with HelmSource url
when:
  a valid request is submitted
then:
  the Workflow instance is created
```

""")
    request {
        method 'POST'
        url "/cnonb/v1/workload_instances"
        multipart(
                workloadInstancePostRequestDto: named(
                        name: value('request.json'),
                        content: $(consumer(nonBlank()), producer(file('multipart-json-part.json'))),
                        contentType: value("application/json")
                ),
                clusterConnectionInfo: named(
                        name: value('clusterConnectionInfo.config'),
                        content: $(consumer(nonEmpty()), producer(file('clusterConnectionInfo.config')))
                )
        )
    }
    response {
        status CREATED()
        body(
                """
                       {
                        "url": "testUrl"
                       }
                """
        )
    }
}



