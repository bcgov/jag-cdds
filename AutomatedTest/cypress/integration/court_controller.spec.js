describe('Court Controller Tests', () => {
    it('Test Get Court List Api', () => {
        const payload = `<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:v1="http://courts.gov.bc.ca/xml/ns/cdds/v1" xmlns:ns="http://courts.gov.bc.ca/XMLSchema/PCSS/1.0.0">
        <soap:Header/>
        <soap:Body>
            <v1:getDigitalDisplayCourtList>
                <getDigitalDisplayCourtListRequest>
                    <ns:getDigitalDisplayCourtListRequest>
                        <ns:RequestAgencyIdentifierId>83.0001</ns:RequestAgencyIdentifierId>
                        <ns:RequestPartId>19014.0001</ns:RequestPartId>
                        <ns:RequestDtm>2021-09-14 09:26:56.6</ns:RequestDtm>
                        <ns:AgencyIdentifierId>83.0001</ns:AgencyIdentifierId>
                        <ns:AppearanceDt>2019-04-22 00:00:00.0</ns:AppearanceDt>
                        <!--Optional:-->
                        <ns:CtrmRoomCd>001</ns:CtrmRoomCd>
                    </ns:getDigitalDisplayCourtListRequest>
                </getDigitalDisplayCourtListRequest>
            </v1:getDigitalDisplayCourtList>
        </soap:Body>
    </soap:Envelope>`
    
        cy.request({
          url: Cypress.env('cdds_host') + 'ws/',
          body: payload,
          method: 'POST',
          headers: {
            authorization: Cypress.env('cdds_token')
          }
        }).then((response) => {
          expect(response.status).to.eq(200)
          cy.readFile('./cypress/ExampleRequests/getCourtListV1.xml').should('eq', response.body)
        })
      })
})