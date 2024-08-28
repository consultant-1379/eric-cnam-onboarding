# This is CNAM Onboarding dependencies preparation for local development purposes and tests execution. It was created only for internal usage!
Please refer to Confluence page for more details:
https://eteamspace.internal.ericsson.com/display/AA/How+to+work+with+Onboarding+service+dependencies+local+environment
## Make sure [docker] and [docker-compose] installed to LVDI

## Configuration
Go to docker-compose.yaml file location and fill in required parameters to .env file. Possible variables with their default parameters in the table:
| Parameter | Type | Default | Details
| ------ | ------ | ------ | ------ |
| GITREPO_SECURED | boolean |false | Activates/deactivates git repository base authentification |
| GITREPO_USER | string | testuser | Repo username |
| GITREPO_PASS | string | testpass | Repo password |
| GITREPO_PORT | string | 8084 | Local port to interact with repository |
| OCI_REGISTRY_USER | string | testuser | Registry username |
| OCI_REGISTRY_PASS | string | testpass | Registry password |
| OCI_REGISTRY_PORT | string | 8085 | Local port to interact with registry |

## Basic operations
Start/restart all services in detached mode (daemonize). Whenever _.env_ file is updated.: ```docker-compose up --detach```
Start/restart with Docker images rebuild. Whenever _Dockerfile_ is updated: ```docker-compose up --detach --build```
Start only "git-repo" service: ```docker-compose up --detach git-repo```
Start only "oci-registry" service: ```docker-compose up --detach oci-registry```
Stop services by removing containers, images remain: ```docker-compose down```
Clean workspace after work is finished, remove containers and delete their images: ```docker-compose down --rmi all```
Remove all dangling images (cache) then work is finished: ```docker rmi $(docker images -q -a -f dangling=true)```
## Git Repository
Supported options to connect:
- **HTTP/HTTPS**
    It is default behavior, git repository would be available by addresses:
http://localhost:8084/local-git-repo.git - HTTP
https://localhost:8084/local-git-repo.git - HTTPS
- **HTTP/HTTPS + base authentification**
    If **GITREPO_SECURED** set to **_true_** base authentication would be activated with credentials stored in **GITREPO_USER** and **GITREPO_PASS**
- **HTTPS enable/disable certificates verification**
    Self-signed keys are configured on the time of environment creation, you can choose secure/insecure mode to interact with help of a script or by yourself. To make git trust server certificates:
    ```sh
    ./gitconfig_manager.sh secure
    ```
    To disable git SSL keys verification:
    ```sh
    ./gitconfig_manager.sh insecure
    ```
Other script options:
Show current git configuration:```./gitconfig_manager.sh show ```
Remove sslcainfo and sslverify configuration:```/gitconfig_manager.sh clear```
Check local git repo connection: ```./gitconfig_manager.sh```, other way is to send GET request to http://localhost:8084/ping, if it is up and running 'pong' would be a response

## Registry
Registry would be available by address: http://localhost:8085. Make sure you can login with a coresponding tool:
```docker login -u testuser -p testpass http://localhost:8085``` or ```oras login -u testuser -p testpass localhost:8085```. If connection was succeded it stores creadentials to ~/.docker/config.json, so make sure to wipe out local registry credentials configuration whenever you want to change username/password values.

[docker]: https://github.com/moby/moby/releases
[docker-compose]: https://github.com/docker/compose/releases
