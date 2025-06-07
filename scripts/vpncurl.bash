#!/bin/bash

curl -k -X 'GET' \
  'https://ozon-pro.dsolv.cloud.kadaster.nl/publiek/omgevingsdocumenten/api/presenteren/v8/regelingen?page=1&size=20&_sort=registratietijdstip' \
  -H 'accept: application/hal+json' \
  -H 'x-api-key: 8b9b4c2f-81a8-490a-acf4-82d82c77beee'


curl -k -X 'GET' \
  'https://service.omgevingswet.overheid.nl/publiek/omgevingsdocumenten/api/presenteren/v8/regelingen?page=1&size=20&_sort=registratietijdstip' \
  -H 'accept: application/hal+json' \
  -H 'x-api-key: 8b9b4c2f-81a8-490a-acf4-82d82c77beee'


curl -k -X 'GET' \
  'https://service.omgevingswet.overheid.nl/publiek/omgevingsdocumenten/api/presenteren/v8/regelingen' \
  -H 'accept: application/hal+json' \
  -H 'x-api-key: 8b9b4c2f-81a8-490a-acf4-82d82c77beee'

curl -k -X 'GET' \
  'https://service.omgevingswet.overheid.nl/publiek/omgevingsdocumenten/api/presenteren/v8/regelingen?page=1&size=1' \
  -H 'accept: application/hal+json' \
  -H 'X-API-KEY: 8b9b4c2f-81a8-490a-acf4-82d82c77beee'