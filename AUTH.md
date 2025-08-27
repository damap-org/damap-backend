# Managing AUTH in Damap

Start in local prod:
(Local Prod is used to simulate the production environment on a local machine for testing purposes.)

> mvn quarkus:dev -Dquarkus.pro file=prod-local

Start in prod:

> mvn quarkus:run

⚠️ Make sure to update the key file paths (`privateKey.pem`, `publicKey.pem`) in your configuration to match your actual file locations. Only update the information in the prod block and not in the dev block.  
For example:

```yaml
smallrye:
  jwt:
    sign:
      key:
        location: privateKey.pem
mp:
  jwt:
    verify:
      publickey:
        location: publicKey.pem
```

NOTE:
> For development, we use a dummy key pair located in the `src/main/resources` folder.

To generate new keys, use:
> openssl genpkey -algorithm RSA -out privateKey.pem -pkeyopt rsa_keygen_bits:2048
> openssl rsa -pubout -in privateKey.pem -out publicKey.pem

Please correctly set the "damap.env" variable in application.yaml to ensure correct AUTH configuration.