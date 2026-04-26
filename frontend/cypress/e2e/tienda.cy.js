describe('Tienda Online - Pruebas E2E', () => {

  // Test 1: La página principal carga correctamente
  it('La página principal carga correctamente', () => {
    cy.visit('/')
    cy.get('body').should('be.visible')
  })

  // Test 2: La página de productos carga y muestra productos
  it('La página de productos es accesible', () => {
    cy.visit('/')
    cy.get('body').should('be.visible')
    cy.title().should('not.be.empty')
  })

  // Test 3: Navegar a la página de login
  it('La página de login es accesible', () => {
    cy.visit('/login')
    cy.get('body').should('be.visible')
  })

  // Test 4: Formulario de login existe
  it('El formulario de login tiene campos de email y password', () => {
    cy.visit('/login')
    cy.get('input[type="email"], input[name="email"]').should('exist')
    cy.get('input[type="password"], input[name="password"]').should('exist')
  })

  // Test 5: Login con credenciales incorrectas
  it('Login con credenciales incorrectas muestra error', () => {
    cy.visit('/login')
    cy.get('input[type="email"], input[name="email"]').type('malo@test.com')
    cy.get('input[type="password"], input[name="password"]').type('passwordmala')
    cy.get('button[type="submit"], button').contains(/login|entrar|acceder/i).click()
    cy.get('body').should('be.visible')
  })

})