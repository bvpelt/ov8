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
  'https://service.omgevingswet.overheid.nl/publiek/omgevingsdocumenten/api/presenteren/v8/regelingen?page=1&size=1&embedded=true' \
  -H 'accept: application/hal+json' \
  -H 'X-API-KEY: 8b9b4c2f-81a8-490a-acf4-82d82c77beee'


curl -k -X 'GET' \
  'https://service.omgevingswet.overheid.nl/publiek/omgevingsdocumenten/api/presenteren/v8/regelingen?geldigOp=2025-06-09&inWerkingOp=2025-06-09&beschikbaarOp=2025-06-09T19:30:52.716757698+02:00&page=1&size=2'
  -H 'accept: application/hal+json' \
  -H 'X-API-KEY: 8b9b4c2f-81a8-490a-acf4-82d82c77beee'


curl -k -X 'GET' \
  'https://service.omgevingswet.overheid.nl/publiek/omgevingsdocumenten/api/presenteren/v8/regelingen/_akn_nl_act_gm0363_2020_omgevingsplan' \
  -H 'accept: application/hal+json' \
  -H 'X-API-KEY: 8b9b4c2f-81a8-490a-acf4-82d82c77beee'


curl -k -X 'GET' \
  'https://service.omgevingswet.overheid.nl/publiek/omgevingsdocumenten/api/presenteren/v8/ontwerpregelingen?beschikbaarOp=2025-07-18T18%3A55%3A45.29612883Z&synchroniseerMetTileset=actueel&_expand=false&page=1&size=20&_sort=registratietijdstip' \
  -H 'accept: application/hal+json' \
  -H 'x-api-key: 8b9b4c2f-81a8-490a-acf4-82d82c77beee'