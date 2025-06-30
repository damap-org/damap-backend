#!/bin/bash
openssl req -x509 -out tls/bundle.crt -keyout tls/private.pem \
  -newkey rsa:2048 -nodes -sha256 \
  -subj '/CN=damap.localhost' -extensions EXT -config <( \
   printf "[dn]\nCN=damap.localhost\n[req]\ndistinguished_name = dn\n[EXT]\nsubjectAltName=DNS:damap.localhost\nkeyUsage=digitalSignature\nextendedKeyUsage=serverAuth")
