import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { importProvidersFrom } from '@angular/core';
import { KeycloakService } from './app/keycloak.service';
import { HttpClientModule } from '@angular/common/http';

const keycloakService = new KeycloakService();

keycloakService.init()
  .catch(err => {
    console.warn('Keycloak initialization failed but continuing', err);
  })
  .finally(() => {
    bootstrapApplication(AppComponent, {
      providers: [
        { provide: KeycloakService, useValue: keycloakService },
        importProvidersFrom(HttpClientModule)  // <-- recommended way now
      ]
    });
  });

