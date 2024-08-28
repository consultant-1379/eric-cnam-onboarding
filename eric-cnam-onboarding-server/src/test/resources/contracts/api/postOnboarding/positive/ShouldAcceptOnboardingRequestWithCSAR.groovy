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

package contracts.api.postOnboarding.positive

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("""
Represents a successful scenario of onboarding CSAR archive

```
given:
  client requests to onboard CSAR archive
when:
  a valid request is submitted
then:
  the onboarding is created
```

""")
    request {
        method 'POST'
        url "/cnonb/v1/onboarding"
        multipart(
                csarArchive: named(
                        name: value('testArchive.csar'),
                        content: $(consumer(nonBlank()), producer('some archive content')))
                )
    }
    response {
        status CREATED()
        body(
                """
                       {
                        "helmfileUrl": "testLink",
                        "helmChartUrls": [
                            "testLink"
                        ]
                       }
                """
        )
    }
}

