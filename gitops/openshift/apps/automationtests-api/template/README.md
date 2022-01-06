## Templates to create openshift components related to jag-cdds-tests api deployment

### Command to execute template
1) Login to OC using login command
2) Run below command in each env. namespace dev/test/prod
   ``oc process -f jag-cdds-tests.yaml --param-file=jag-cdds-tests.env | oc apply -f -``


