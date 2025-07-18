import { Injectable } from '@angular/core';
import Keycloak, { KeycloakInstance } from 'keycloak-js';

@Injectable({
  providedIn: 'root',
})
export class KeycloakService {
  private keycloakInstance: KeycloakInstance;
  private initialized = false;   // track if initialized

  constructor() {
    this.keycloakInstance = new Keycloak({
      url: 'http://localhost:8188',
      realm: 'my-quarkus-app',
      clientId: 'frontend',
    });
  }

  // Initialize Keycloak and check login status
  init(): Promise<boolean> {
    if (this.initialized) {
      // Already initialized, just return the current status
      return Promise.resolve(this.keycloakInstance.authenticated ?? false);
    }

    this.initialized = true;

    return this.keycloakInstance
      .init({
        onLoad: 'check-sso',
        pkceMethod: 'S256',
        checkLoginIframe: false,
      })
      .then((authenticated) => authenticated)
      .catch((err) => {
        this.initialized = false; // reset flag on failure
        return Promise.reject(err);
      });
  }

  login(): Promise<void> {
    return this.keycloakInstance.login({
      redirectUri: window.location.origin, // Redirects back to your app after login
    });
  }

  register(): Promise<void> {
    return this.keycloakInstance.register({
      redirectUri: window.location.origin, // Redirects back to your app after register
    });
  }


  logout(): Promise<void> {
    return this.keycloakInstance.logout({ redirectUri: window.location.origin });
  }

  isLoggedIn(): boolean {
    return this.keycloakInstance.authenticated ?? false;
  }

  getToken(): Promise<string> {
    return this.keycloakInstance.updateToken(30).then(() => {
      return this.keycloakInstance.token ?? '';
    });
  }

  getUsername(): string | undefined {
    return this.keycloakInstance.tokenParsed?.['preferred_username'];
  }

}
