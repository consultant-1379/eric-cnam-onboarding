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
package contracts.api.health

import org.springframework.cloud.contract.spec.Contract


Contract.make {
    description("""
Represents a successful scenario of health request

```
given:
  client make health request
when:
  a valid request is made
then:
  service is healthy.
```

""")
    request {
        method GET()
        url "/cnonb/v1/health"
    }
    response {
        status OK()
    }
}
