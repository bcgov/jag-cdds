describe('Health Controller Tests', () => {
  it('Test Actuator Health Api', () => {
    cy.request({
      url: Cypress.env('cdds_host') + 'actuator/health',
      headers: {
        authorization: Cypress.env('cdds_token')
      }
    }).then((response) => {
      expect(response.status).to.eq(200)
      expect(response.body.status).to.eq('UP')
    })
  })

  it('Test Health Api', () => {
    const payload = `<soapenv:Envelope xmlns:soapenv="http://www.w3.org/2003/05/soap-envelope" xmlns:cdds="http://courts.gov.bc.ca/xml/ns/cdds/v1">
    <soapenv:Header/>
    <soapenv:Body>
        <cdds:getHealth/>
    </soapenv:Body>
</soapenv:Envelope>`

    cy.request({
      url: Cypress.env('cdds_host') + 'ws/',
      body: payload,
      method: 'POST',
      headers: {
        authorization: Cypress.env('cdds_token')
      }
    }).then((response) => {
      expect(response.status).to.eq(200)
      cy.readFile('./cypress/ExampleRequests/getHealthV1.xml').should('eq', response.body)
    })
  })

  it('Test Ping Api', () => {
    const payload = `<soapenv:Envelope xmlns:soapenv="http://www.w3.org/2003/05/soap-envelope" xmlns:cdds="http://courts.gov.bc.ca/xml/ns/cdds/v1">
    <soapenv:Header/>
    <soapenv:Body>
        <cdds:getPing/>
    </soapenv:Body>
</soapenv:Envelope>`

    cy.request({
      url: Cypress.env('cdds_host') + 'ws/',
      body: payload,
      method: 'POST',
      headers: {
        authorization: Cypress.env('cdds_token')
      }
    }).then((response) => {
      expect(response.status).to.eq(200)
      cy.readFile('./cypress/ExampleRequests/getPingV1.xml').should('eq', response.body)
    })
  })


  it('Test get wsdl', () => {

    cy.request({
      url: Cypress.env('cdds_host') + 'ws/JusticeCDDS.wsProvider:cdds?WSDL',
      method: 'GET',
      headers: {
        authorization: Cypress.env('cdds_token')
      }
    }).then((response) => {
      expect(response.status).to.eq(200)
      cy.readFile('./cypress/ExampleRequests/wsdl.xml').should('eq', response.body)
    })
  })

  
  it('Test security', () => {

    cy.request({
      url: Cypress.env('cdds_host') + 'ws/',
      method: 'GET',
      failOnStatusCode: false
    }).then((response) => {
      expect(response.status).to.eq(401)
    
    })
  })

})
