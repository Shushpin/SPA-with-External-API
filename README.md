# Keycloak Security Patterns: SPA with External API (Resource Server) (not recommended)

This repository is part of a series demonstrating security architecture patterns using Keycloak and Spring Boot.  
It focuses on implementing the **SPA with External API** pattern, where the frontend acts as a **Public Client** and the backend acts as a stateless **Resource Server**.

---

## Architecture Overview

Unlike the [Backend-for-Frontend (BFF) pattern](https://github.com/Shushpin/SPA-with-intermediary-API-BFF-.git), this approach completely decouples the frontend from the backend's authentication process.

The Single Page Application (SPA) directly interacts with Keycloak using the **Authorization Code Flow with PKCE (Proof Key for Code Exchange)**. Once authenticated, the SPA stores the Access Token locally and attaches it as a `Bearer` token to the `Authorization` header of every HTTP request sent to the backend API.

The Spring Boot backend does not maintain user sessions or handle login redirects. It operates strictly as a **Resource Server**, intercepting incoming requests, extracting the JWT (JSON Web Token), and mathematically validating its cryptographic signature using Keycloak's public keys.

---

## Authentication Flow

1. User clicks "Login" on the frontend (SPA).
2. The SPA dynamically generates a PKCE Code Verifier and Code Challenge.
3. The SPA redirects the user to Keycloak along with the Code Challenge.
4. User authenticates on the Keycloak login page.
5. Keycloak redirects the user back to the SPA with a temporary Authorization Code.
6. The SPA directly exchanges the Authorization Code + PKCE Code Verifier for an Access Token and Refresh Token.
7. The SPA stores the tokens (e.g., in memory or `localStorage`).
8. The SPA requests data from the Spring Boot API, attaching the Access Token in the header.
9. The Spring Boot API validates the token signature offline and returns the protected data.

---

## Advantages & Trade-offs

### ✅ Advantages
- **Stateless Backend:** The API does not store HTTP sessions. This makes the backend incredibly lightweight, easy to scale horizontally, and perfectly suited for microservices.
- **Decoupled Architecture:** The backend only cares about valid tokens, not how they were obtained. You can reuse the exact same API for different clients (e.g., a React web app, an iOS app, and an Android app).
- **Reduced Backend Load:** The backend delegates the entire OAuth2 flow to the frontend and Keycloak.

### ⚠️ Trade-offs (Security Risks)
- **High XSS Vulnerability:** Because the Access Token lives in the browser (accessible via JavaScript), it is highly susceptible to Cross-Site Scripting (XSS) attacks. If malicious scripts run on the page, they can easily steal the tokens.
- **CORS Complexity:** The frontend and backend usually run on different domains/ports. You must carefully configure Cross-Origin Resource Sharing (CORS) on the backend to accept preflight (`OPTIONS`) requests.
- **Client-Side Complexity:** The frontend application must implement logic to securely store tokens, handle expiration, and silently request new tokens using the Refresh Token.

---

## Keycloak Configuration

To run this project, configure your local Keycloak instance as follows:

### Client Setup (Public Client)
Since this is a browser-based application, it cannot securely store a Client Secret. We must configure it as a Public Client.

- **Realm:** `spa-realm`
- **Client ID:** `spa-client`
- **Client Authentication:** `OFF` *(This is critical: it removes the need for a client secret)*
- **Authorization Flow:** Standard Flow (Authorization Code)
- **Valid Redirect URIs:** `http://localhost:3000/*` *(Replace with your frontend's actual port, e.g., `http://localhost:63342/*` for IntelliJ built-in server)*
- **Web origins:** `+` *(This tells Keycloak to allow CORS requests from the domains listed in Valid Redirect URIs)*

---

## Project Structure

This repository contains two main components:
1. **Frontend (`index.html`):** A vanilla HTML/JS application utilizing the official `keycloak.js` adapter. It demonstrates the PKCE flow and token extraction.
2. **Backend (Spring Boot):** A Java application utilizing `spring-boot-starter-oauth2-resource-server`. It demonstrates CORS configuration and JWT validation.

---

## Getting Started

1. **Start Keycloak:** Run Keycloak locally (e.g., via Docker on port `8081`) and apply the configuration mentioned above. Ensure you have created at least one test user.
2. **Run the Backend:** Start the Spring Boot application. It will launch on port `8080` and automatically fetch the public keys from Keycloak (`issuer-uri`).
3. **Run the Frontend:** Open the `index.html` file in your browser (preferably served via a local web server to avoid `file://` protocol issues).
4. **Test the Flow:** - Click **Login** and authenticate.
    - Observe the Access Token generated on the screen.
    - Click **Get Secret Data** to perform an authenticated CORS request to the Spring Boot backend.