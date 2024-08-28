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

package contracts.api.postOnboarding.negative

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("""
Represents an error scenario of onboarding CSAR.

```
given:
  client requests to onboard a CSAR archive
when:
  invalid request without archive is submitted
then:
  the request was rejected with 400 Bad Request
```

""")
    request {
        method 'POST'
        url "/cnonb/v1/onboarding"
        headers {
            contentType(multipartFormData())
        }
    }
    response {
        status BAD_REQUEST()
    }
}